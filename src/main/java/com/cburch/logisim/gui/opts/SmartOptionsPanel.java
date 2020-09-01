/**
 * This file is part of logisim-cad, a fork of logisim-evolution.
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

package com.cburch.logisim.gui.opts;

import static com.cburch.logisim.gui.Strings.S;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeEvent;
import com.cburch.logisim.data.AttributeListener;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.file.Options;
import com.cburch.logisim.file.Options;
import com.cburch.logisim.gui.opts.OptionsPanel;
import com.cburch.logisim.util.TableLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.LayoutManager;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public abstract class SmartOptionsPanel extends OptionsPanel implements AttributeListener {

  public SmartOptionsPanel(OptionsFrame frame) {
    super(frame);
    setLayout(new TableLayout(2));
    attributeSet = frame.getOptions().getAttributeSet();
    addOptions();
    attributeSet.addAttributeListener(this);
  }
  
  public SmartOptionsPanel(OptionsFrame frame, LayoutManager manager) {
    super(frame, manager);
    addOptions();
  }
  
  protected List<Option> options = new ArrayList<>();
  protected AttributeSet attributeSet;
  
  protected void addOptions() {
    // Override this to make addOption calls
  }
  
  protected void addOption(Option opt) {
    opt.panel = this;
    options.add(opt);
//     add(opt.gui);
    Box lbox = new Box(BoxLayout.X_AXIS);
    lbox.add(opt.label);
    lbox.add(Box.createRigidArea(new Dimension(10, 0)));
    add(lbox);
    add(opt.getField());
    opt.setFieldValue(attributeSet.getValue(opt.attr));
  }
  
  public void setAttribute(Attribute attr, Object value) {
    AttributeSet attrs = getOptions().getAttributeSet();
    getProject().doAction(OptionsActions.setAttribute(attrs, attr, value));
  }

  @Override  
  public void attributeListChanged(AttributeEvent e) {
  }

  @Override
  public void attributeValueChanged(AttributeEvent e) {
    Attribute<?> attr = e.getAttribute();
    Object val = e.getValue();
    for (Option opt : options)
      if (opt.attr == attr) {
        opt.setFieldValue(val);
        return;
      }
  }

  @Override
  public void localeChanged() {
    for (Option opt : options)
      opt.localChanged();
  }
  
  //------------------------------------------------------------------------
  
  protected static abstract class Option<V> {
  
    String labelKey;
    JLabel label;
    Attribute attr;
//     JComponent gui;
    SmartOptionsPanel panel;
    OptionListener listener;
    
    Option(String labelKey, Attribute attr) {
      this.labelKey = labelKey;
      this.attr = attr;
      label = new JLabel();
      listener = new OptionListener();
      updateLabel();
    }
    
    abstract Component getField();
    
    private class OptionListener implements ActionListener {
      public void actionPerformed(ActionEvent event) {
        V value = getFieldValue();
        if (value != null)
          panel.setAttribute(attr, value);
      }
    } // OptionListener
    
    void localChanged() {
      updateLabel();
    }
    
    private void updateLabel() {
      label.setText(S.get(labelKey));
    }
    
    abstract V getFieldValue();
    abstract void setFieldValue(V value);
  
  } // Option
  
  //------------------------------------------------------------------------
  
  protected static class IntegerOption extends Option<Integer> {
  
    JTextField field;

    IntegerOption(String labelKey, int size, Attribute attr) {
      super(labelKey, attr);
      field = new JTextField(size);
      field.addActionListener(listener);
//       gui = new JPanel();
//       gui.add(label);
//       gui.add(field);
    }

    @Override
    Component getField() {
      return field;
    }

    @Override
    Integer getFieldValue() {
      return new Integer(field.getText());
    }
    
    @Override
    void setFieldValue(Integer value) {
      field.setText(value.toString());
    }
    
  } // IntegerOption

  //------------------------------------------------------------------------
  
  protected static class BooleanOption extends Option<Boolean> {
  
    JCheckBox field;
    
    BooleanOption(String labelKey, Attribute attr) {
      super(labelKey, attr);
      field = new JCheckBox();
      field.addActionListener(listener);
//       gui = new JPanel();
//       gui.add(field);
//       gui.add(label);
    }
    
    @Override
    Component getField() {
      return field;
    }

    @Override
    Boolean getFieldValue() {
      return field.isSelected();
    }
    
    @Override
    void setFieldValue(Boolean value) {
      field.setSelected(value);
    }

  } // BooleanOption

  //------------------------------------------------------------------------

} // SmartOptionsPanel
