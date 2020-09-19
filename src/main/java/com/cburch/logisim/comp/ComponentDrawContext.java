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

package com.cburch.logisim.comp;

import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.circuit.CircuitState;
import com.cburch.logisim.circuit.Wire;
import com.cburch.logisim.circuit.WireSet;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.file.Options;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.prefs.AppPreferences;
import com.cburch.logisim.util.GraphicsUtil;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;

public class ComponentDrawContext {
  private static final int PIN_OFFS = 2;
  private static final int PIN_RAD = 4;

  private java.awt.Component dest;
  private Circuit circuit;
  private CircuitState circuitState;
  private Graphics base;
  private Graphics g;
  private boolean showState;
  private boolean showColor;
  private boolean printView;
  private WireSet highlightedWires;
  private InstancePainter instancePainter;
  
  private int strokeWidth = Wire.WIDTH;
  private int pinRadius;
  private boolean hideAbuttedPorts;
  
  public int getStrokeWidth() {
    return strokeWidth;
  }
  
  public double getPinRadius() {
    return pinRadius;
  }

  public int getWireWidth() {
    return getStrokeWidth();
  }
  
  public int getBusWidth() {
    return strokeWidth + (Wire.WIDTH_BUS - Wire.WIDTH);
  }

  public int getHighlightedWireWidth() {
    return strokeWidth + (Wire.HIGHLIGHTED_WIDTH - Wire.WIDTH);
  }

  public int getHighlightedBusWidth() {
    return strokeWidth + (Wire.HIGHLIGHTED_WIDTH_BUS - Wire.WIDTH);
  }

  public int getWireWidth(BitWidth w) {
    return getWireWidth(w.getWidth());
  }

  public int getWireWidth(int w) {
    return (w > 1) ? getBusWidth() : getWireWidth();
  }

  public int getHighlightedWireWidth(BitWidth w) {
    return (w.getWidth() > 1) ? getHighlightedBusWidth() : getHighlightedWireWidth();
  }

  public int getWireWidth(BitWidth w, boolean highlighted) {
    return highlighted ? getHighlightedWireWidth(w) : getWireWidth(w);
  }

  public double getJunctionWidth(BitWidth w, boolean highlighted) {
    return getWireWidth(w, highlighted) + (Wire.WIRE_JUNCTION_WIDTH - Wire.WIDTH);
  }
  
  public boolean getHideAbuttedPorts() {
    return hideAbuttedPorts;
  }

  public ComponentDrawContext(
      java.awt.Component dest,
      Circuit circuit,
      CircuitState circuitState,
      Graphics base,
      Graphics g) {
    this(dest, circuit, circuitState, base, g, false);
  }

  public ComponentDrawContext(
      java.awt.Component dest,
      Circuit circuit,
      CircuitState circuitState,
      Graphics base,
      Graphics g,
      boolean printView) {
    this.dest = dest;
    this.circuit = circuit;
    this.circuitState = circuitState;
    this.base = base;
    this.g = g;
    this.showState = true;
    this.showColor = true;
    this.printView = printView;
    this.highlightedWires = WireSet.EMPTY;
    this.instancePainter = new InstancePainter(this, null);
    if (circuit != null) {
      AttributeSet projAttrs = circuit.getProject().getOptions().getAttributeSet();
      this.strokeWidth = projAttrs.getValue(Options.ATTR_STROKE_WIDTH);
      this.hideAbuttedPorts = projAttrs.getValue(Options.ATTR_HIDE_ABUTTED_PORTS);
    }
    this.pinRadius = (strokeWidth < 2) ? 2 : strokeWidth;
  }

  //
  // helper methods
  //
  public void drawBounds(Component comp) {
    GraphicsUtil.switchToWidth(g, getStrokeWidth());
    g.setColor(Color.BLACK);
    Bounds bds = comp.getBounds();
    g.drawRect(bds.getX(), bds.getY(), bds.getWidth(), bds.getHeight());
    GraphicsUtil.switchToWidth(g, 1);
  }

  public void drawClock(Component comp, int i, Direction dir) {
    Color curColor = g.getColor();
    g.setColor(Color.BLACK);
    GraphicsUtil.switchToWidth(g, getStrokeWidth());

    EndData e = comp.getEnd(i);
    Location pt = e.getLocation();
    int x = pt.getX();
    int y = pt.getY();
    final int CLK_SZ = 4;
    final int CLK_SZD = CLK_SZ - 1;
    if (dir == Direction.NORTH) {
      g.drawLine(x - CLK_SZD, y - 1, x, y - CLK_SZ);
      g.drawLine(x + CLK_SZD, y - 1, x, y - CLK_SZ);
    } else if (dir == Direction.SOUTH) {
      g.drawLine(x - CLK_SZD, y + 1, x, y + CLK_SZ);
      g.drawLine(x + CLK_SZD, y + 1, x, y + CLK_SZ);
    } else if (dir == Direction.EAST) {
      g.drawLine(x + 1, y - CLK_SZD, x + CLK_SZ, y);
      g.drawLine(x + 1, y + CLK_SZD, x + CLK_SZ, y);
    } else if (dir == Direction.WEST) {
      g.drawLine(x - 1, y - CLK_SZD, x - CLK_SZ, y);
      g.drawLine(x - 1, y + CLK_SZD, x - CLK_SZ, y);
    }

    g.setColor(curColor);
    GraphicsUtil.switchToWidth(g, 1);
  }

  public void drawClockSymbol(Component comp, int xpos, int ypos) {
    GraphicsUtil.switchToWidth(g, getStrokeWidth());
    g.drawLine(xpos, ypos - 4, xpos + 8, ypos);
    g.drawLine(xpos, ypos + 4, xpos + 8, ypos);
    GraphicsUtil.switchToWidth(g, 1);
  }

  public void drawDongle(int x, int y) {
    GraphicsUtil.switchToWidth(g, getStrokeWidth());
    g.drawOval(x - 5, y - 5, 10, 10);
  }

  public void drawHandle(int x, int y) {
    g.setColor(Color.white);
    g.fillRect(x - 3, y - 3, 7, 7);
    g.setColor(Color.black);
    g.drawRect(x - 3, y - 3, 7, 7);
  }

  public void drawHandle(Location loc) {
    drawHandle(loc.getX(), loc.getY());
  }

  public void drawHandles(Component comp) {
    Bounds b = comp.getBounds(g);
    int left = b.getX();
    int right = left + b.getWidth();
    int top = b.getY();
    int bot = top + b.getHeight();
    drawHandle(right, top);
    drawHandle(left, bot);
    drawHandle(right, bot);
    drawHandle(left, top);
  }
  
  protected boolean shouldDrawPin(Location pt, Component comp) {
    if (hideAbuttedPorts)
      return !circuit.isConnected(pt, comp);
    else
      return !circuit.isConnectedToWire(pt);
  }

  protected void drawPin(Component comp, EndData e, String label, Direction dir) {
    Location pt = e.getLocation();
    int x = pt.getX();
    int y = pt.getY();
    if (shouldDrawPin(pt, comp)) {
      Color curColor = g.getColor();
      if (getShowState()) {
        CircuitState state = getCircuitState();
        g.setColor(state.getValue(pt).getColor());
      } else {
        g.setColor(Color.BLACK);
      }
      GraphicsUtil.fillCenteredCircle(g, x, y, getPinRadius());
      g.setColor(curColor);
    }
    if (label != null) {
      if (dir == Direction.EAST) {
        GraphicsUtil.drawText(g, label, x + 3, y, GraphicsUtil.H_LEFT, GraphicsUtil.V_CENTER);
      } else if (dir == Direction.WEST) {
        GraphicsUtil.drawText(g, label, x - 3, y, GraphicsUtil.H_RIGHT, GraphicsUtil.V_CENTER);
      } else if (dir == Direction.SOUTH) {
        GraphicsUtil.drawText(g, label, x, y - 3, GraphicsUtil.H_CENTER, GraphicsUtil.V_BASELINE);
      } else if (dir == Direction.NORTH) {
        GraphicsUtil.drawText(g, label, x, y + 3, GraphicsUtil.H_CENTER, GraphicsUtil.V_TOP);
      }
    }
  }

  public void drawPin(Component comp, int i) {
    EndData e = comp.getEnd(i);
    drawPin(comp, e, null, null);
  }

  public void drawPin(Component comp, int i, String label, Direction dir) {
    if (i >= 0 && i < comp.getEnds().size()) {
      EndData e = comp.getEnd(i);
      drawPin(comp, e, label, dir);
    }
  }

  public void drawPins(Component comp) {
    for (EndData e : comp.getEnds()) {
      drawPin(comp, e, null, null);
    }
  }

  public void drawRectangle(Component comp) {
    drawRectangle(comp, "");
  }

  public void drawRectangle(Component comp, String label) {
    Bounds bds = comp.getBounds(g);
    drawRectangle(bds.getX(), bds.getY(), bds.getWidth(), bds.getHeight(), label);
  }

  public void drawRectangle(
      ComponentFactory source, int x, int y, AttributeSet attrs, String label) {
    Bounds bds = source.getOffsetBounds(attrs);
    drawRectangle(source, x + bds.getX(), y + bds.getY(), bds.getWidth(), bds.getHeight(), label);
  }

  public void drawRectangle(
      ComponentFactory source, int x, int y, int width, int height, String label) {
    GraphicsUtil.switchToWidth(g, getStrokeWidth());
    g.drawRect(x + 1, y + 1, width - 1, height - 1);
    if (label != null && !label.equals("")) {
      FontMetrics fm = base.getFontMetrics(g.getFont());
      int lwid = fm.stringWidth(label);
      if (height > 20) { // centered at top edge
        g.drawString(label, x + (width - lwid) / 2, y + 2 + fm.getAscent());
      } else { // centered overall
        g.drawString(label, x + (width - lwid) / 2, y + (height + fm.getAscent()) / 2 - 1);
      }
    }
  }

  public void drawRectangle(int x, int y, int width, int height, String label) {
    GraphicsUtil.switchToWidth(g, getStrokeWidth());
    g.drawRect(x, y, width, height);
    if (label != null && !label.equals("")) {
      FontMetrics fm = base.getFontMetrics(g.getFont());
      int lwid = fm.stringWidth(label);
      if (height > 20) { // centered at top edge
        g.drawString(label, x + (width - lwid) / 2, y + 2 + fm.getAscent());
      } else { // centered overall
        g.drawString(label, x + (width - lwid) / 2, y + (height + fm.getAscent()) / 2 - 1);
      }
    }
  }

  public Circuit getCircuit() {
    return circuit;
  }

  public CircuitState getCircuitState() {
    return circuitState;
  }

  public java.awt.Component getDestination() {
    return dest;
  }

  public Object getGateShape() {
    return AppPreferences.GATE_SHAPE.get();
  }

  public Graphics getGraphics() {
    return g;
  }

  public WireSet getHighlightedWires() {
    return highlightedWires;
  }

  public InstancePainter getInstancePainter() {
    return instancePainter;
  }

  public boolean getShowState() {
    return !printView && showState;
  }

  public boolean isPrintView() {
    return printView;
  }

  public void setGraphics(Graphics g) {
    this.g = g;
  }

  public void setHighlightedWires(WireSet value) {
    this.highlightedWires = value == null ? WireSet.EMPTY : value;
  }

  public void setShowColor(boolean value) {
    showColor = value;
  }

  public void setShowState(boolean value) {
    showState = value;
  }

  public boolean shouldDrawColor() {
    return !printView && showColor;
  }

  public void drawRoundBounds(Component comp, Bounds bds, Color color) {
    GraphicsUtil.switchToWidth(g, getStrokeWidth());
    if (color != null && !color.equals(Color.WHITE)) {
      g.setColor(color);
      g.fillRoundRect(bds.getX(), bds.getY(), bds.getWidth(), bds.getHeight(), 10, 10);
    }
    g.setColor(Color.BLACK);
    g.drawRoundRect(bds.getX(), bds.getY(), bds.getWidth(), bds.getHeight(), 10, 10);
    GraphicsUtil.switchToWidth(g, 1);
  }

  public void drawRoundBounds(Component comp, Color color) {
    drawRoundBounds(comp, comp.getBounds(), color);
  }
}
