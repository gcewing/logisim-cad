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

package com.cburch.draw.util;

import com.cburch.logisim.data.Bounds;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import javax.swing.JTextField;

public class EditableLabel implements Cloneable {
  public static final int LEFT = JTextField.LEFT;
  public static final int RIGHT = JTextField.RIGHT;
  public static final int CENTER = JTextField.CENTER;

  public static final int TOP = 8;
  public static final int MIDDLE = 9;
  public static final int BASELINE = 10;
  public static final int BOTTOM = 11;

  private int x;
  private int y;
  private String text;
  private Font font;
  private Color color;
  private int horzAlign;
  private int vertAlign;
  private boolean dimsKnown;
  private int width;
  private int ascent;
  private int descent;
  private int margin;

  public EditableLabel(int x, int y, String text, Font font) {
    this.x = x;
    this.y = y;
    this.text = text;
    this.font = font;
    this.color = Color.BLACK;
    this.horzAlign = LEFT;
    this.vertAlign = BASELINE;
    this.dimsKnown = false;
  }

  @Override
  public EditableLabel clone() {
    try {
      return (EditableLabel) super.clone();
    } catch (CloneNotSupportedException e) {
      return new EditableLabel(x, y, text, font);
    }
  }

  private void computeDimensions(Graphics g) {
    TextMetrics tm = new TextMetrics(g, text);
    width = tm.width;
    ascent = tm.ascent;
    descent = tm.descent;
    dimsKnown = true;
  }

  public void configureTextField(EditableLabelField field) {
    configureTextField(field, 1.0);
  }

  public void configureTextField(EditableLabelField field, double zoom) {
    Font f = font;
    if (zoom != 1.0) {
      f = f.deriveFont(AffineTransform.getScaleInstance(zoom, zoom));
    }
    field.setFont(f);

    Dimension dim = field.getPreferredSize();
    int w;
    int border = EditableLabelField.FIELD_BORDER;
    if (dimsKnown) {
      w = width + 1 + 2 * border;
    } else {
      TextMetrics tm = new TextMetrics(field, font, text);
      ascent = tm.ascent;
      descent = tm.descent;
      w = tm.width;
    }

    float x0 = x;
    float y0 = getBaseY() - ascent;
    if (zoom != 1.0) {
      x0 = (int) Math.round(x0 * zoom);
      y0 = (int) Math.round(y0 * zoom);
      w = (int) Math.round(w * zoom);
    }

    w = Math.max(w, dim.width);
    int h = dim.height;
    switch (horzAlign) {
      case LEFT:
        x0 = x0 - border;
        break;
      case CENTER:
        x0 = x0 - (w / 2.0f) + 1;
        break;
      case RIGHT:
        x0 = x0 - w + border + 1;
        break;
      default:
        x0 = x0 - border;
    }
    y0 = y0 - border;

    field.setHorizontalAlignment(horzAlign);
    field.setForeground(color);
    field.setBounds((int) x0, (int) y0, w, h);
  }

  public boolean contains(int qx, int qy) {
    float x0 = getLeftX();
    float y0 = getBaseY();
    return (qx >= x0 && qx < x0 + width + 2 * margin && qy >= y0 - ascent && qy < y0 + descent);
  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof EditableLabel) {
      EditableLabel that = (EditableLabel) other;
      return this.x == that.x
          && this.y == that.y
          && this.text.equals(that.text)
          && this.font.equals(that.font)
          && this.color.equals(that.color)
          && this.horzAlign == that.horzAlign
          && this.vertAlign == that.vertAlign;
    } else {
      return false;
    }
  }

  private float getBaseY() {
    switch (vertAlign) {
      case TOP:
        return y + ascent;
      case MIDDLE:
        return y + (ascent - descent) / 2.0f;
      case BASELINE:
        return y;
      case BOTTOM:
        return y - descent;
      default:
        return y;
    }
  }

  //
  // more complex methods
  //
  public Bounds getBounds() {
    int x0 = (int) getLeftX();
    int y0 = (int) getBaseY() - ascent;
    int w = width + 2 * margin;
    int h = ascent + descent;
    return Bounds.create(x0, y0, w, h);
  }

  public Color getColor() {
    return color;
  }

  public Font getFont() {
    return font;
  }

  public int getHorizontalAlignment() {
    return horzAlign;
  }

  private float getLeftX() {
    switch (horzAlign) {
      case LEFT:
        return x - margin;
      case CENTER:
        return x - width / 2.0f - margin;
      case RIGHT:
        return x - width - margin;
      default:
        return x;
    }
  }
  
  public int getMargin() {
    return margin;
  }
  
  public int getSnapX() {
    int sx;
    switch (horzAlign) {
      case LEFT:
        sx = x - margin;
        break;
      case CENTER:
        sx = x;
        break;
      case RIGHT:
        sx = x + margin;
        break;
      default:
        sx = x;
    }
    return sx;  
  }
  
  public String getText() {
    return text;
  }

  public int getVerticalAlignment() {
    return vertAlign;
  }

  //
  // accessor methods
  //
  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  @Override
  public int hashCode() {
    int ret = x * 31 + y;
    ret = ret * 31 + text.hashCode();
    ret = ret * 31 + font.hashCode();
    ret = ret * 31 + color.hashCode();
    ret = ret * 31 + horzAlign;
    ret = ret * 31 + vertAlign;
    return ret;
  }

  public void paint(Graphics g) {
    g.setFont(font);
    g.setColor(color);
    computeDimensions(g);
    float x0 = getLeftX() + margin;
    float y0 = getBaseY();
    ((Graphics2D) g).drawString(text, x0, y0);
  }

  public void setColor(Color value) {
    color = value;
  }

  public void setFont(Font value) {
    font = value;
    dimsKnown = false;
  }

  public void setHorizontalAlignment(int value) {
    if (value != LEFT && value != CENTER && value != RIGHT) {
      throw new IllegalArgumentException("argument must be LEFT, CENTER, or RIGHT");
    }
    int sx = getSnapX();
    horzAlign = value;
    dimsKnown = false;
    setSnapX(sx);
  }

  public void setLocation(int x, int y) {
    this.x = x;
    this.y = y;
  }
  
  public void setMargin(int m) {
    switch (horzAlign) {
      case LEFT:
        x = x - margin + m;
        break;
      case CENTER:
        break;
      case RIGHT:
        x = x + margin - m;
        break;
    }
    margin = m;
  }

  public void setSnapX(int sx) {
    switch (horzAlign) {
      case LEFT:
        x = sx + margin;
        break;
      case CENTER:
        x = sx;
        break;
      case RIGHT:
        x = sx - margin;
        break;
      default:
        x = sx;
    }
  }

  public void setText(String value) {
    dimsKnown = false;
    text = value;
  }

  public void setVerticalAlignment(int value) {
    if (value != TOP && value != MIDDLE && value != BASELINE && value != BOTTOM) {
      throw new IllegalArgumentException("argument must be TOP, MIDDLE, BASELINE, or BOTTOM");
    }
    vertAlign = value;
    dimsKnown = false;
  }
}
