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

package com.cburch.logisim.circuit;

import static com.cburch.logisim.circuit.Strings.S;

import java.util.*;
import javax.swing.*;
import com.cburch.logisim.circuit.*;
import com.cburch.logisim.comp.*;
import com.cburch.logisim.data.*;
import com.cburch.logisim.instance.*;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.tools.*;

public class ComponentNumbering {

  private static String ident(Instance inst) {
    return String.format("'%s' at %s", getType(inst), inst.getLocation());
  }
  
  private static Integer parseSerialNo(String text) {
    if (text.equals(""))
      return 0;
    try {
      return Integer.parseInt(text);
    }
    catch (NumberFormatException e) {
      return null;
    }
  }
  
  private static Integer getSerialNo(Instance inst) {
    String text = inst.getAttributeValue(CircuitAttributes.SERIAL_NO_ATTR);
    return parseSerialNo(text);
  }
  
  private static <T> void setAttributeValue(Instance inst, Attribute<T> attr, T value,
      SetAttributeAction act)
  {
//     inst.getAttributeSet().setValue(attr, value);
    act.set(inst.getComponent(), attr, value);
  }
  
  private static void setSerialNo(Instance inst, int serialNo, SetAttributeAction act) {
    String text = Integer.toString(serialNo);
    setAttributeValue(inst, CircuitAttributes.SERIAL_NO_ATTR, text, act);
  }
  
  private static void setVariant(Instance inst, String variant, SetAttributeAction act) {
    setAttributeValue(inst, CircuitAttributes.VARIANT_ATTR, variant, act);
  }
  
  private static String getType(Instance inst) {
    return inst.getFactory().getName();
  }

  //----------------------------------------------------------------------------------------

  private static class VariantSet {
    public int serialNo;
    public String type;
    public String[] variantList;
    public Set<String> variantsUsed;
    
    public VariantSet(Instance inst, int serialNo) {
      this.serialNo = serialNo;
      type = getType(inst);
      variantList = inst.getAttributeValue(CircuitAttributes.VARIANT_LIST_ATTR);
      variantsUsed = new HashSet<String>();
      add(inst);
    }
    
    public boolean add(Instance inst) {
      if (!type.equals(getType(inst)))
        return false;
      String variant = inst.getAttributeValue(CircuitAttributes.VARIANT_ATTR);
      if (variantsUsed.contains(variant))
        return false;
      variantsUsed.add(variant);
      return true;
    }
    
    public boolean allocateVariant(Instance inst, SetAttributeAction act) {
      for (String variant : variantList)
        if (!variantsUsed.contains(variant)) {
          setVariant(inst, variant, act);
          variantsUsed.add(variant);
          return true;
        }
      return false;
    }
  
  } // VariantSet

  //----------------------------------------------------------------------------------------

  private static class ListMap<K extends Comparable, V> extends TreeMap<K, List<V>> {

    public List<V> get(K key) {
      List<V> result = super.get(key);
      if (result == null) {
        result = new ArrayList<>();
        put(key, result);
      }
      return result;
    }
  
  } // ListMap

  //----------------------------------------------------------------------------------------

  private static class VariantSetMap {
    private Map<Integer, VariantSet> serialMap = new TreeMap<Integer, VariantSet>();
    private ListMap<String, VariantSet> typeMap = new ListMap<>();
    
    public VariantSet get(int serialNo) {
      return serialMap.get(serialNo);
    }
    
    public Set<Integer> keySet() {
      return serialMap.keySet();
    }
    
    public Collection<VariantSet> values() {
      return serialMap.values();
    }
    
    public boolean contains(Integer serialNo) {
      return serialMap.containsKey(serialNo);
    }
    
    public VariantSet addEntry(Instance inst, int serialNo) {
      VariantSet vset = new VariantSet(inst, serialNo);
      serialMap.put(serialNo, vset);
      typeMap.get(vset.type).add(vset);
      return vset;
    }
    
    public boolean add(Instance inst, int serialNo) {
      VariantSet vset = serialMap.get(serialNo);
      if (vset == null) {
        vset = addEntry(inst, serialNo);
        return true;
      }
      else
        return vset.add(inst);
    }
    
    public List<VariantSet> getType(String type) {
      return typeMap.get(type);
    }
    
    public Set<String> typeSet() {
      return typeMap.keySet();
    }
  
  } // VariantSetMap
  
  //----------------------------------------------------------------------------------------

  private static class Numberer {
    private Circuit circ;
    private SetAttributeAction act;
    private String prefix;
    private List<Instance> unnumbered = new ArrayList<Instance>();
    private VariantSetMap variantSets = new VariantSetMap();
    
    Numberer(Circuit circ, SetAttributeAction act, String prefix) {
      this.circ = circ;
      this.act = act;
      this.prefix = prefix;
    }
    
    public void add(Instance inst) {
      Integer serialInt = getSerialNo(inst);
      if (serialInt != null) {
        int serialNo = serialInt;
        if (serialNo == 0 || !variantSets.add(inst, serialNo))
          unnumbered.add(inst);
      }
    }
    
    public void dump() {
      System.out.printf("Unnumbered:\n");
      for (Instance inst : unnumbered)
        System.out.printf("   %s\n", ident(inst));
      for (int i : variantSets.keySet()) {
        VariantSet vset = variantSets.get(i);
        System.out.printf("Serial No. %s:\n", i);
        System.out.printf("   Variants: %s\n", Arrays.asList(vset.variantList));
        System.out.printf("   Used: %s\n", vset.variantsUsed);
      }
    }
    
    public void numberAll() {
      int candNo = circ.getStaticAttributes().getValue(CircuitAttributes.STARTING_SERIAL_NO_ATTR);
      for (Instance inst : unnumbered) {
        if (!numberUsingSpare(inst))
          while (!numberUsingSerial(inst, candNo))
            candNo += 1;
      }
    }
    
    private boolean numberUsingSpare(Instance inst) {
      int existingNo = getSerialNo(inst);
      if (existingNo != 0) {
        VariantSet vset = variantSets.get(existingNo);
        if (vset.allocateVariant(inst, act))
          return true;
      }
      String type = getType(inst);
      for (VariantSet vset : variantSets.values())
        if (type.equals(vset.type))
          if (vset.allocateVariant(inst, act)) {
            setSerialNo(inst, vset.serialNo, act);
            return true;
          }
      return false;
    }
    
    private boolean numberUsingSerial(Instance inst, int serialNo) {
      if (variantSets.contains(serialNo))
        return false;
      else {
        setSerialNo(inst, serialNo, act);
        VariantSet vset = variantSets.addEntry(inst, serialNo);
        vset.allocateVariant(inst, act);
        return true;
      }
    }

    public void reportUnused(List<String> report) {
      for (String type : variantSets.typeSet()) {
        List<String> lines = new ArrayList<>();
        for (VariantSet vset : variantSets.getType(type)) {
          List<String> unused = new ArrayList<>();
          for (String variant : vset.variantList)
            if (!vset.variantsUsed.contains(variant))
              unused.add(prefix + vset.serialNo + variant);
          if (unused.size() > 0)
            lines.add("      " + String.join(", ", unused));
        }
        if (lines.size() > 0) {
          report.add("   " + type);
          for (String line : lines)
            report.add(line);
        }
      }
    }

  }  // Numberer
  
  //----------------------------------------------------------------------------------------

  private static class PrefixMap {
    private Circuit circ;
    private Map<String, Numberer> map = new HashMap<>();
    private SetAttributeAction act;
    
    PrefixMap(Circuit circ, SetAttributeAction act) {
      this.circ = circ;
      this.act = act;
    }
    
    public Numberer get(String prefix) {
      Numberer result = map.get(prefix);
      if (result == null) {
        result = new Numberer(circ, act, prefix);
        map.put(prefix, result);
      }
      return result;
    }
    
    public Set<String> keySet() {
      return map.keySet();
    }
  
  } // PrefixMap

  //----------------------------------------------------------------------------------------
  
  public static boolean debug = false;

  public static void doNumberCircuit(Project proj, Circuit circ) {
    if (debug)
      System.out.printf("ProjectCircuitActions.doNumberCircuit(%s, %s)\n", proj, circ);
    SetAttributeAction act = new SetAttributeAction(circ, S.getter("numberComponentsAction"));
    PrefixMap numberers = new PrefixMap(circ, act);
    for (Component comp : circ.getNonWires()) {
      if (comp instanceof InstanceComponent) {
        Instance inst = ((InstanceComponent)comp).getInstance();
        if (inst.getFactory() instanceof SubcircuitFactory) {
          String type = getType(inst);
          String label =  inst.getAttributeValue(StdAttr.LABEL);
          String prefix = inst.getAttributeValue(CircuitAttributes.DESIGNATION_PREFIX_ATTR);
          if (debug)
            System.out.printf("...%s, label '%s', prefix '%s'\n", ident(inst), label, prefix);
          if (label.equals("") && !prefix.equals("")) {
            Numberer numb = numberers.get(prefix);
            numb.add(inst);
          }
        }
      }
    }
    List<String> report = new ArrayList<>();
    report.add("Unused in " + circ.getName());
    for (String prefix : numberers.keySet()) {
      if (debug)
        System.out.printf("Prefix '%s':\n", prefix);
      Numberer numb = numberers.get(prefix);
      if (debug)
        numb.dump();
      numb.numberAll();
      numb.reportUnused(report);
    }
    proj.doAction(act);
    if (debug)
      for (String line : report)
        System.out.printf("%s\n", line);
    JTextArea rta = new JTextArea(String.join("\n", report), 80, 30);
    JScrollPane rsp = new JScrollPane(rta);
    JFrame rfr = new JFrame(circ.getName() + " - Numbering Report");
    rfr.add(rsp);
    rfr.pack();
    rfr.setVisible(true);
  }

}
