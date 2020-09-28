/**
 * This file is part of logisim-evolution.
 *
 * Logisim-evolution is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Logisim-evolution is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along 
 * with logisim-evolution. If not, see <http://www.gnu.org/licenses/>.
 *
 * Original code by Carl Burch (http://www.cburch.com), 2011.
 * Subsequent modifications by:
 *   + College of the Holy Cross
 *     http://www.holycross.edu
 *   + Haute École Spécialisée Bernoise/Berner Fachhochschule
 *     http://www.bfh.ch
 *   + Haute École du paysage, d'ingénierie et d'architecture de Genève
 *     http://hepia.hesge.ch/
 *   + Haute École d'Ingénierie et de Gestion du Canton de Vaud
 *     http://www.heig-vd.ch/
 */

package com.cburch.logisim.proj;

import static com.cburch.logisim.proj.Strings.S;

import com.cburch.logisim.LogisimRuntimeSettings;
import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.file.LoadFailedException;
import com.cburch.logisim.file.LoadedLibrary;
import com.cburch.logisim.file.Loader;
import com.cburch.logisim.file.LogisimFile;
import com.cburch.logisim.file.LogisimFileActions;
import com.cburch.logisim.gui.main.Frame;
import com.cburch.logisim.gui.start.SplashScreen;
import com.cburch.logisim.prefs.AppPreferences;
import com.cburch.logisim.tools.Library;
import com.cburch.logisim.tools.LibraryTools;
import com.cburch.logisim.tools.Tool;
import com.cburch.logisim.util.JFileChoosers;
import com.cburch.logisim.util.StringUtil;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

public class ProjectActions {
  private static String FILE_NAME_FORMAT_ERROR = "FileNameError";
  private static String FILE_NAME_KEYWORD_ERROR = "ExistingToolName";

  private static class CreateFrame implements Runnable {
    private Loader loader;
    private Project proj;
    private boolean isStartupScreen;

    public CreateFrame(Loader loader, Project proj, boolean isStartup) {
      this.loader = loader;
      this.proj = proj;
      this.isStartupScreen = isStartup;
    }

    @Override
    public void run() {
      try {
        Frame frame = createFrame(null, proj);
        frame.setVisible(true);
        frame.toFront();
        frame.getCanvas().requestFocus();
        loader.setParent(frame);
        if (isStartupScreen) {
          proj.setStartupScreen(true);
        }
      } catch (Exception e) {
        Writer result = new StringWriter();
        PrintWriter printWriter = new PrintWriter(result);
        e.printStackTrace(printWriter);
        JOptionPane.showMessageDialog(null, result.toString());
        System.exit(-1);
      }
    }
  }

  /**
   * Returns true if the filename contains valid characters only, that is, alphanumeric characters
   * and underscores.
   */
  private static boolean checkValidFilename(
      String filename, Project proj, HashMap<String, String> Errors) {
    boolean IsOk = true;
    HashMap<String, Library> TempSet = new HashMap<String, Library>();
    HashSet<String> ForbiddenNames = new HashSet<String>();
    LibraryTools.BuildLibraryList(proj.getLogisimFile(), TempSet);
    LibraryTools.BuildToolList(proj.getLogisimFile(), ForbiddenNames);
    ForbiddenNames.addAll(TempSet.keySet());
    Pattern p = Pattern.compile("[^a-z0-9_.]", Pattern.CASE_INSENSITIVE);
    Matcher m = p.matcher(filename);
    if (m.find()) {
      IsOk = false;
      Errors.put(FILE_NAME_FORMAT_ERROR, S.get("InvalidFileFormatError"));
    }
    if (ForbiddenNames.contains(filename.toUpperCase())) {
      IsOk = false;
      Errors.put(FILE_NAME_KEYWORD_ERROR, S.get("UsedLibraryToolnameError"));
    }
    return IsOk;
  }

  private static Project completeProject(
      SplashScreen monitor, Loader loader, LogisimFile file, boolean isStartup) {
    if (monitor != null) monitor.setProgress(SplashScreen.PROJECT_CREATE);
    Project ret = new Project(file);
    if (monitor != null) monitor.setProgress(SplashScreen.FRAME_CREATE);
    SwingUtilities.invokeLater(new CreateFrame(loader, ret, isStartup));
    updatecircs(file, ret);
    return ret;
  }

  private static LogisimFile createEmptyFile(Loader loader, Project proj) {
    InputStream templReader = AppPreferences.getEmptyTemplate().createStream();
    LogisimFile file;
    try {
      file = loader.openLogisimFile(templReader);
    } catch (Exception t) {
      file = LogisimFile.createNew(loader, proj);
      file.addCircuit(new Circuit("main", file, proj));
    } finally {
      try {
        templReader.close();
      } catch (IOException e) {
      }
    }
    return file;
  }

  private static Frame createFrame(Project sourceProject, Project newProject) {
    if (sourceProject != null) {
      Frame frame = sourceProject.getFrame();
      if (frame != null) {
        frame.savePreferences();
      }
    }
    Frame newFrame = new Frame(newProject);
    newProject.setFrame(newFrame);
    return newFrame;
  }

  public static LogisimFile createNewFile(Project baseProject) {
    Loader loader = new Loader(baseProject == null ? null : baseProject.getFrame());
    InputStream templReader = AppPreferences.getTemplate().createStream();
    LogisimFile file;
    try {
      file = loader.openLogisimFile(templReader);
    } catch (IOException ex) {
      displayException(baseProject.getFrame(), ex);
      file = createEmptyFile(loader, baseProject);
    } catch (LoadFailedException ex) {
      if (!ex.isShown()) {
        displayException(baseProject.getFrame(), ex);
      }
      file = createEmptyFile(loader, baseProject);
    } finally {
      try {
        templReader.close();
      } catch (IOException e) {
      }
    }
    return file;
  }

  private static void displayException(Component parent, Exception ex) {
    String msg = StringUtil.format(S.get("templateOpenError"), ex.toString());
    String ttl = S.get("templateOpenErrorTitle");
    JOptionPane.showMessageDialog(parent, msg, ttl, JOptionPane.ERROR_MESSAGE);
  }

  public static Project doNew(Project baseProject) {
    LogisimFile file = createNewFile(baseProject);
    Project newProj = new Project(file);
    Frame frame = createFrame(baseProject, newProj);
    frame.setVisible(true);
    frame.getCanvas().requestFocus();
    newProj.getLogisimFile().getLoader().setParent(frame);
    updatecircs(file, newProj);
    return newProj;
  }

  public static Project doNew(SplashScreen monitor) {
    return doNew(monitor, false);
  }

  public static Project doNew(SplashScreen monitor, boolean isStartupScreen) {
    if (monitor != null) monitor.setProgress(SplashScreen.FILE_CREATE);
    Loader loader = new Loader(monitor);
    InputStream templReader = AppPreferences.getTemplate().createStream();
    LogisimFile file = null;
    try {
      file = loader.openLogisimFile(templReader);
    } catch (IOException ex) {
      displayException(monitor, ex);
    } catch (LoadFailedException ex) {
      displayException(monitor, ex);
    } finally {
      try {
        templReader.close();
      } catch (IOException e) {
      }
    }
    if (file == null) file = createEmptyFile(loader, null);
    return completeProject(monitor, loader, file, isStartupScreen);
  }

  public static void doMerge(Component parent, Project baseProject) {
    JFileChooser chooser;
    LogisimFile mergelib;
    Loader loader = null;
    if (baseProject != null) {
      Loader oldLoader = baseProject.getLogisimFile().getLoader();
      chooser = oldLoader.createChooser();
      if (oldLoader.getMainFile() != null) {
        chooser.setSelectedFile(oldLoader.getMainFile());
      }
    } else {
      chooser = JFileChoosers.create();
    }
    chooser.setFileFilter(Loader.LOGISIM_FILTER);
    chooser.setDialogTitle(S.get("FileMergeItem"));

    int returnVal = chooser.showOpenDialog(parent);
    if (returnVal != JFileChooser.APPROVE_OPTION) return;
    File selected = chooser.getSelectedFile();
    loader = new Loader(baseProject == null ? parent : baseProject.getFrame());
    try {
      mergelib = loader.openLogisimFile(selected);
      if (mergelib == null) return;
    } catch (LoadFailedException ex) {
      if (!ex.isShown()) {
        JOptionPane.showMessageDialog(
            parent,
            StringUtil.format(S.get("fileMergeError"), ex.toString()),
            S.get("FileMergeErrorItem"),
            JOptionPane.ERROR_MESSAGE);
      }
      return;
    }
    baseProject.doAction(LogisimFileActions.MergeFile(mergelib, baseProject.getLogisimFile()));
  }

  public static void updatecircs(LogisimFile lib, Project proj) {
    for (Circuit circ : lib.getCircuits()) {
      circ.SetProject(proj);
    }
    for (Library libs : lib.getLibraries()) {
      if (libs instanceof LoadedLibrary) {
        LoadedLibrary test = (LoadedLibrary) libs;
        if (test.getBase() instanceof LogisimFile) {
          updatecircs((LogisimFile) test.getBase(), proj);
        }
      }
    }
  }

  public static boolean doOpen(Component parent, Project baseProject) {
    JFileChooser chooser;
    if (baseProject != null) {
      Loader oldLoader = baseProject.getLogisimFile().getLoader();
      chooser = oldLoader.createChooser();
      if (oldLoader.getMainFile() != null) {
        chooser.setSelectedFile(oldLoader.getMainFile());
      }
    } else {
      chooser = JFileChoosers.create();
    }
    chooser.setFileFilter(Loader.LOGISIM_FILTER);
    chooser.setDialogTitle(S.get("FileOpenItem"));

    int returnVal = chooser.showOpenDialog(parent);
    if (returnVal != JFileChooser.APPROVE_OPTION) return false;
    File selected = chooser.getSelectedFile();
    if (selected != null) {
      doOpen(parent, baseProject, selected);
    }

    LogisimRuntimeSettings.setIsGui(LogisimRuntimeSettings.GUI);
    return true;
  }

  public static Project doOpen(Component parent, Project baseProject, File f) {
    Project proj = Projects.findProjectFor(f);
    Loader loader = null;
    LogisimRuntimeSettings.setIsGui(LogisimRuntimeSettings.GUI);
    if (proj != null) {
      proj.getFrame().toFront();
      loader = proj.getLogisimFile().getLoader();
      if (proj.isFileDirty()) {
        String message =
            StringUtil.format(S.get("openAlreadyMessage"), proj.getLogisimFile().getName());
        String[] options = {
          S.get("openAlreadyLoseChangesOption"),
          S.get("openAlreadyNewWindowOption"),
          S.get("openAlreadyCancelOption"),
        };
        int result =
            JOptionPane.showOptionDialog(
                proj.getFrame(),
                message,
                S.get("openAlreadyTitle"),
                0,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[2]);
        if (result == 0) {; // keep proj as is, so that load happens into the window
        } else if (result == 1) {
          proj = null; // we'll create a new project
        } else {
          return proj;
        }
      }
    }

    if (proj == null && baseProject != null && baseProject.isStartupScreen()) {
      proj = baseProject;
      proj.setStartupScreen(false);
      loader = baseProject.getLogisimFile().getLoader();
    } else {
      loader = new Loader(baseProject == null ? parent : baseProject.getFrame());
    }

    try {
      LogisimFile lib = loader.openLogisimFile(f);
      if (lib == null) return null;
      AppPreferences.updateRecentFile(f);
      LibraryTools.RemovePresentLibraries(lib, new HashMap<String, Library>(), true);
      if (proj == null) {
        proj = new Project(lib);
        updatecircs(lib, proj);
      } else {
        updatecircs(lib, proj);
        proj.setLogisimFile(lib);
      }
    } catch (LoadFailedException ex) {
      if (!ex.isShown()) {
        JOptionPane.showMessageDialog(
            parent,
            StringUtil.format(S.get("fileOpenError"), ex.toString()),
            S.get("fileOpenErrorTitle"),
            JOptionPane.ERROR_MESSAGE);
      }
      return null;
    }

    Frame frame = proj.getFrame();
    if (frame == null) {
      frame = createFrame(baseProject, proj);
    }
    frame.setVisible(true);
    frame.toFront();
    frame.getCanvas().requestFocus();
    proj.getLogisimFile().getLoader().setParent(frame);
    return proj;
  }

  public static Project doOpen(SplashScreen monitor, File source, Map<File, File> substitutions)
      throws LoadFailedException {
    if (monitor != null) monitor.setProgress(SplashScreen.FILE_LOAD);
    Loader loader = new Loader(monitor);
    LogisimFile file = loader.openLogisimFile(source, substitutions);
    if (file == null)
      return null;
    AppPreferences.updateRecentFile(source);

    LogisimRuntimeSettings.setIsGui(LogisimRuntimeSettings.GUI);
    return completeProject(monitor, loader, file, false);
  }

  public static Project doOpenNoWindow(SplashScreen monitor, File source)
      throws LoadFailedException {
    Loader loader = new Loader(monitor);
    LogisimFile file = loader.openLogisimFile(source);
    if (file == null)
      return null;
    Project ret = new Project(file);
    updatecircs(file, ret);
    LogisimRuntimeSettings.setIsGui(LogisimRuntimeSettings.CLI);
    return ret;
  }

  public static boolean doQuit() {
    Frame top = Projects.getTopFrame();
    top.savePreferences();

    for (Project proj : new ArrayList<Project>(Projects.getOpenProjects())) {
      if (!proj.confirmClose(S.get("confirmQuitTitle"))) {
        return false;
      }
    }
    System.exit(0);
    return true;
  }

  public static boolean doSave(Project proj) {
    Loader loader = proj.getLogisimFile().getLoader();
    File f = loader.getMainFile();
    if (f == null) return doSaveAs(proj);
    else return doSave(proj, f);
  }

  public static boolean doSave(Project proj, File f) {
    Loader loader = proj.getLogisimFile().getLoader();
    Tool oldTool = proj.getTool();
    proj.setTool(null);
    boolean ret = loader.save(proj.getLogisimFile(), f);
    if (ret) {
      AppPreferences.updateRecentFile(f);
      proj.setFileAsClean();
    }
    proj.setTool(oldTool);
    return ret;
  }

  /**
   * Saves a Logisim project in a .circ file.
   *
   * <p>It is the action listener for the File->Save as... menu option.
   *
   * @param proj project to be saved
   * @return true if success, false otherwise
   */
  public static boolean doSaveAs(Project proj) {
    Loader loader = proj.getLogisimFile().getLoader();
    JFileChooser chooser = loader.createChooser();
    chooser.setAcceptAllFileFilterUsed​(false);
    chooser.addChoosableFileFilter(Loader.LOGISIM_CAD_FILTER);
    chooser.addChoosableFileFilter(Loader.LOGISIM_EVOLUTION_FILTER);
    File mf = loader.getMainFile();
    if (mf != null) {
      if (mf.getName().endsWith(Loader.LOGISIM_EVOLUTION_EXTENSION))
        chooser.setFileFilter(Loader.LOGISIM_EVOLUTION_FILTER);
      else
        chooser.setFileFilter(Loader.LOGISIM_CAD_FILTER);
      chooser.setSelectedFile(Loader.fileWithoutExtension(mf));
    }
    int returnVal;
    boolean validFilename = false;
    File f;
    HashMap<String, String> Error = new HashMap<String, String>();
    do {
      Error.clear();
      returnVal = chooser.showSaveDialog(proj.getFrame());
      if (returnVal != JFileChooser.APPROVE_OPTION) {
        return false;
      }
      f = chooser.getSelectedFile();
      FileFilter filt = chooser.getFileFilter();
      String circExt;
      if (filt == Loader.LOGISIM_CAD_FILTER)
        circExt = Loader.LOGISIM_CAD_EXTENSION;
      else if (filt == Loader.LOGISIM_EVOLUTION_FILTER)
        circExt = Loader.LOGISIM_EVOLUTION_EXTENSION;
      else
        throw new RuntimeException("ProjectActions.doSaveAs: File filter not recognised");
      f = Loader.fileWithExtension(f, circExt);
      validFilename = checkValidFilename(f.getName(), proj, Error);
      if (!validFilename) {
        String Message = "\"" + chooser.getSelectedFile() + "\":\n";
        for (String key : Error.keySet()) {
          String error = S.get(Error.get(key));
          Message = Message.concat("=> " + error + "\n");
        }
        JOptionPane.showMessageDialog(
            chooser, Message, S.get("FileSaveAsItem"), JOptionPane.ERROR_MESSAGE);
      }
    } while (!validFilename);
    if (f.exists()) {
      int confirm =
          JOptionPane.showConfirmDialog(
              proj.getFrame(),
              S.get("confirmOverwriteMessage"),
              S.get("confirmOverwriteTitle"),
              JOptionPane.YES_NO_OPTION);
      if (confirm != JOptionPane.YES_OPTION) return false;
    }
    return doSave(proj, f);
  }

  private ProjectActions() {}
}
