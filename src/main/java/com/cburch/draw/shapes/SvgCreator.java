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

package com.cburch.draw.shapes;

import com.cburch.draw.model.AbstractCanvasObject;
import com.cburch.draw.model.Handle;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.Location;
import java.awt.Color;
import java.awt.Font;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class SvgCreator {
  public static boolean colorMatches(Color a, Color b) {
    return a.getRed() == b.getRed() && a.getGreen() == b.getGreen() && a.getBlue() == b.getBlue();
  }
  
  public static Element createShapeElement(Document doc, String shapeName, AbstractCanvasObject shape) {
    Element elt = doc.createElement(shapeName);
    addShapeAttributes(elt, shape);
    return elt;
  }

  protected static void addShapeAttributes(Element elt, AbstractCanvasObject shape) {
    for (Attribute a : shape.getAttributes()) {
      String attrName = a.getName();
      if (attrName.startsWith("a-"))
        elt.setAttribute(attrName, a.toStandardString(shape.getValue(a)));
    }
  }

  public static Element createCurve(Document doc, Curve curve) {
    Element elt = createShapeElement(doc, "path", curve);
    Location e0 = curve.getEnd0();
    Location e1 = curve.getEnd1();
    Location ct = curve.getControl();
    elt.setAttribute(
        "d",
        "M" + e0.getX() + "," + e0.getY() + " Q" + ct.getX() + "," + ct.getY() + " " + e1.getX()
            + "," + e1.getY());
    populateFill(elt, curve);
    return elt;
  }

  public static Element createLine(Document doc, Line line) {
    Element elt = createShapeElement(doc, "line", line);
    Location v1 = line.getEnd0();
    Location v2 = line.getEnd1();
    elt.setAttribute("x1", "" + v1.getX());
    elt.setAttribute("y1", "" + v1.getY());
    elt.setAttribute("x2", "" + v2.getX());
    elt.setAttribute("y2", "" + v2.getY());
    populateStroke(elt, line);
    return elt;
  }

  public static Element createOval(Document doc, Oval oval) {
    double x = oval.getX();
    double y = oval.getY();
    double width = oval.getWidth();
    double height = oval.getHeight();
    Element elt = createOvalElement(doc, x, y, width, height);
    addShapeAttributes(elt, oval);
    populateFill(elt, oval);
    return elt;
  }
  
  public static Element createOvalElement(Document doc, double x, double y, double width, double height) {
    Element elt = doc.createElement("ellipse");
    elt.setAttribute("cx", "" + (x + width / 2));
    elt.setAttribute("cy", "" + (y + height / 2));
    elt.setAttribute("rx", "" + (width / 2));
    elt.setAttribute("ry", "" + (height / 2));
    return elt;
  }

  public static Element createPoly(Document doc, Poly poly) {
    Element elt;
    String name;
    if (poly.isClosed())
      name = "polygon";
    else
      name = "polyline";
    elt = createShapeElement(doc, name, poly);
    
    StringBuilder points = new StringBuilder();
    boolean first = true;
    for (Handle h : poly.getHandles(null)) {
      if (!first) points.append(" ");
      points.append(h.getX() + "," + h.getY());
      first = false;
    }
    elt.setAttribute("points", points.toString());

    populateFill(elt, poly);
    return elt;
  }

  public static Element createRectangle(Document doc, Rectangle rect) {
    return createRectangular(doc, rect);
  }

  private static Element createRectangular(Document doc, Rectangular rect) {
    Element elt = createRectangularElement(doc, rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
    addShapeAttributes(elt, rect);
    populateFill(elt, rect);
    return elt;
  }
  
  public static Element createRectangularElement(Document doc, int x, int y, int width, int height) {
    Element elt = doc.createElement("rect");
    elt.setAttribute("x", "" + x);
    elt.setAttribute("y", "" + y);
    elt.setAttribute("width", "" + width);
    elt.setAttribute("height", "" + height);
    return elt;
  }

  public static Element createRoundRectangle(Document doc, RoundRectangle rrect) {
    Element elt = createRectangular(doc, rrect);
    int r = rrect.getValue(DrawAttr.CORNER_RADIUS).intValue();
    elt.setAttribute("rx", "" + r);
    elt.setAttribute("ry", "" + r);
    return elt;
  }

  public static Element createText(Document doc, Text text) {
    Location loc = text.getLocation();
    Font font = text.getValue(DrawAttr.FONT);
    Color fill = text.getValue(DrawAttr.FILL_COLOR);
    Object halign = text.getValue(DrawAttr.HALIGNMENT);
    Object valign = text.getValue(DrawAttr.VALIGNMENT);
    Element elt = createTextElement(doc, text.getText(), loc.getX(), loc.getY(), font, fill, halign, valign);
    addShapeAttributes(elt, text);
    return elt;
  }

  public static Element createTextElement(Document doc, String text,
    int x, int y, Font font, Color fill, Object halign, Object valign)
  {
    Element elt = doc.createElement("text");
    elt.setAttribute("x", "" + x);
    elt.setAttribute("y", "" + y);
    if (!colorMatches(fill, Color.BLACK)) {
      elt.setAttribute("fill", getColorString(fill));
    }
    if (showOpacity(fill)) {
      elt.setAttribute("fill-opacity", getOpacityString(fill));
    }
    setFontAttribute(elt, font, "");
    if (halign == DrawAttr.HALIGN_LEFT) {
      elt.setAttribute("text-anchor", "start");
    } else if (halign == DrawAttr.HALIGN_RIGHT) {
      elt.setAttribute("text-anchor", "end");
    } else {
      elt.setAttribute("text-anchor", "middle");
    }
    if (valign == DrawAttr.VALIGN_TOP) {
      elt.setAttribute("dominant-baseline", "top");
    } else if (valign == DrawAttr.VALIGN_BOTTOM) {
      elt.setAttribute("dominant-baseline", "bottom");
    } else if (valign == DrawAttr.VALIGN_BASELINE) {
      elt.setAttribute("dominant-baseline", "alphabetic");
    } else {
      elt.setAttribute("dominant-baseline", "central");
    }
    elt.appendChild(doc.createTextNode(text));
    return elt;
  }

  public static void setFontAttribute(Element elt, Font font, String prefix) {
    elt.setAttribute(prefix + "font-family", font.getFamily());
    elt.setAttribute(prefix + "font-size", "" + font.getSize());
    int style = font.getStyle();
    if ((style & Font.ITALIC) != 0) {
      elt.setAttribute(prefix + "font-style", "italic");
    }
    if ((style & Font.BOLD) != 0) {
      elt.setAttribute(prefix + "font-weight", "bold");
    }
  }

  public static String getColorString(Color color) {
    return String.format(
        "#%02x%02x%02x",
        Integer.valueOf(color.getRed()),
        Integer.valueOf(color.getGreen()),
        Integer.valueOf(color.getBlue()));
  }

  private static String getOpacityString(Color color) {
    return String.format("%5.3f", Double.valueOf(color.getAlpha() / 255.0));
  }

  private static void populateFill(Element elt, AbstractCanvasObject shape) {
    Object type = shape.getValue(DrawAttr.PAINT_TYPE);
    if (type == DrawAttr.PAINT_FILL) {
      elt.setAttribute("stroke", "none");
    } else {
      populateStroke(elt, shape);
    }
    if (type == DrawAttr.PAINT_STROKE) {
      elt.setAttribute("fill", "none");
    } else {
      Color fill = shape.getValue(DrawAttr.FILL_COLOR);
      if (colorMatches(fill, Color.BLACK)) {
        elt.removeAttribute("fill");
      } else {
        elt.setAttribute("fill", getColorString(fill));
      }
      if (showOpacity(fill)) {
        elt.setAttribute("fill-opacity", getOpacityString(fill));
      }
    }
  }

  private static void populateStroke(Element elt, AbstractCanvasObject shape) {
    Integer width = shape.getValue(DrawAttr.STROKE_WIDTH);
    if (width != null && width.intValue() != 1) {
      elt.setAttribute("stroke-width", width.toString());
    }
    Color stroke = shape.getValue(DrawAttr.STROKE_COLOR);
    elt.setAttribute("stroke", getColorString(stroke));
    if (showOpacity(stroke)) {
      elt.setAttribute("stroke-opacity", getOpacityString(stroke));
    }
    elt.setAttribute("fill", "none");
  }

  private static boolean showOpacity(Color color) {
    return color.getAlpha() != 255;
  }

  private SvgCreator() {}
}
