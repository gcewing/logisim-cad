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

package com.cburch.logisim.circuit.appear;

import static com.cburch.logisim.circuit.Strings.S;

import com.cburch.draw.model.CanvasObject;
import com.cburch.draw.model.Handle;
import com.cburch.draw.model.HandleGesture;
import com.cburch.draw.shapes.SvgCreator;
import com.cburch.draw.util.TextMetrics;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeOption;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.std.wiring.Pin;
import com.cburch.logisim.std.wiring.PinAttributes;
import com.cburch.logisim.util.UnmodifiableList;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class AppearancePort extends AppearanceElement {
  private static final int INPUT_RADIUS = 4;
  private static final int OUTPUT_RADIUS = 5;
  private static final int MINOR_RADIUS = 2;
  public static final Color COLOR = Color.BLUE;

  private Instance pin;
  
  private AttributeOption pinNumberLocation;

  public AppearancePort(Location location, Instance pin) {
    super(location);
    this.pin = pin;
    pinNumberLocation = PinAttributes.PINNO_NE;
  }

  @Override
  public boolean contains(Location loc, boolean assumeFilled) {
    if (isInput()) {
      return getBounds().contains(loc);
    } else {
      return super.isInCircle(loc, OUTPUT_RADIUS);
    }
  }
  
  @Override
  public List<Attribute<?>> getAttributes() {
    return PinAttributes.APPEARANCE_ATTRIBUTES;
  }

  @Override
  public Bounds getBounds() {
    int r = isInput() ? INPUT_RADIUS : OUTPUT_RADIUS;
    return super.getBounds(r);
  }

  @Override
  public String getDisplayName() {
    return S.get("circuitPort");
  }

  @Override
  public String getDisplayNameAndLabel() {
    String label = pin.getAttributeValue(StdAttr.LABEL);
    if (label != null && label.length() > 0) return getDisplayName() + " \"" + label + "\"";
    else return getDisplayName();
  }

  @Override
  public List<Handle> getHandles(HandleGesture gesture) {
    Location loc = getLocation();

    int r = isInput() ? INPUT_RADIUS : OUTPUT_RADIUS;
    return UnmodifiableList.create(
        new Handle[] {
          new Handle(this, loc.translate(-r, -r)),
          new Handle(this, loc.translate(r, -r)),
          new Handle(this, loc.translate(r, r)),
          new Handle(this, loc.translate(-r, r))
        });
  }

  public Instance getPin() {
    return pin;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <V> V getValue(Attribute<V> attr) {
    if (attr == PinAttributes.PIN_NUMBER_LOCATION)
      return (V) pinNumberLocation;
    else
      return null;
  }

  private boolean isInput() {
    Instance p = pin;
    return p == null || Pin.FACTORY.isInputPin(p);
  }

  @Override
  public boolean matches(CanvasObject other) {
    if (other instanceof AppearancePort) {
      AppearancePort that = (AppearancePort) other;
      return this.matches(that) && this.pin == that.pin;
    } else {
      return false;
    }
  }

  @Override
  public int matchesHashCode() {
    return super.matchesHashCode() + pin.hashCode();
  }

  @Override
  public void paint(Graphics g, HandleGesture gesture) {
    Location location = getLocation();
    int x = location.getX();
    int y = location.getY();
    g.setColor(COLOR);
    if (isInput()) {
      int r = INPUT_RADIUS;
      g.drawRect(x - r, y - r, 2 * r, 2 * r);
    } else {
      int r = OUTPUT_RADIUS;
      g.drawOval(x - r, y - r, 2 * r, 2 * r);
    }
    g.fillOval(x - MINOR_RADIUS, y - MINOR_RADIUS, 2 * MINOR_RADIUS, 2 * MINOR_RADIUS);
    paintPinNumber(g);
  }
  
  private static final Font pinNumberFont = new Font("SansSerif", Font.PLAIN, 9);
  private static final Color pinNumberColor = Color.black;
  private static final int pinNumberMargin = 3;
  private static final int pinNumberLeading = 0;
  
  public void paintPinNumber(Graphics g) {
    Location loc = getLocation();
    int x0 = loc.getX();
    int y0 = loc.getY();
    String pinNo = pin.getAttributeValue(PinAttributes.PIN_NUMBER);
    AttributeOption opt = pinNumberLocation;
    g.setFont(pinNumberFont);
    g.setColor(pinNumberColor);
    TextMetrics tm = new TextMetrics(g, pinNo);
    int width = tm.width;
    int ascent = tm.ascent;
    int descent = tm.descent;
    int x, y;
    if (opt == PinAttributes.PINNO_NE) {
      x = x0 + pinNumberMargin;
      y = y0 - descent - pinNumberLeading;
    }
    else if (opt == PinAttributes.PINNO_NW) {
      x = x0 - pinNumberMargin - width + 1;
      y = y0 - descent - pinNumberLeading;
    }
    else if (opt == PinAttributes.PINNO_SE) {
      x = x0 + pinNumberMargin;
      y = y0 + ascent + pinNumberLeading;
    }
    else {
      x = x0 - pinNumberMargin - width + 1;
      y = y0 + ascent + pinNumberLeading;
    }
    g.drawString(pinNo, x, y);
  }    

  void setPin(Instance value) {
    pin = value;
  }

  @Override
  protected void updateValue(Attribute<?> attr, Object value) {
    if (attr == PinAttributes.PIN_NUMBER_LOCATION)
      pinNumberLocation = (AttributeOption) value;
  }

  @Override
  public Element toSvgElement(Document doc) {
    Location loc = getLocation();
    Location pinLoc = pin.getLocation();
    Element ret = SvgCreator.createShapeElement(doc, "circ-port", this);
    int r = isInput() ? INPUT_RADIUS : OUTPUT_RADIUS;
    ret.setAttribute("x", "" + (loc.getX() - r));
    ret.setAttribute("y", "" + (loc.getY() - r));
    ret.setAttribute("width", "" + 2 * r);
    ret.setAttribute("height", "" + 2 * r);
    ret.setAttribute("pin", "" + pinLoc.getX() + "," + pinLoc.getY());
    return ret;
  }
}
