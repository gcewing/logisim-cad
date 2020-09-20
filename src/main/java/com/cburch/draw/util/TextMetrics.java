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

import java.awt.Canvas;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.*;

public class TextMetrics {

  public int ascent;
  public int descent;
  public int leading;
  public int height; // = ascent + height + leading
  public int width; // valid only if constructor was given a string

  public double ascentF;
  public double descentF;
  public double leadingF;
  public double heightF; // = ascent + height + leading
  public double widthF; // valid only if constructor was given a string
  public double capHeightF;
  
  protected static FontRenderContext genericFontRenderContext =
    new FontRenderContext(new AffineTransform(), true, true);

  public TextMetrics(Graphics g) {
    this(g, null, null);
  }

  public TextMetrics(Graphics g, String text) {
    this(g, null, text);
  }

  public TextMetrics(Graphics g, Font font) {
    this(g, font, null);
  }

  public TextMetrics(Graphics g, Font font, String text) {
    if (font == null) {
      if (g == null)
        throw new IllegalStateException("need g");
      font = g.getFont();
    }
    FontRenderContext fr = (g != null)
      ? ((Graphics2D) g).getFontRenderContext()
      : genericFontRenderContext;

    if (text == null) {
      text = "ÄAy";
      widthF = 0;
    } else {
      widthF = font.getStringBounds(text, fr).getWidth();
    }

    LineMetrics lm = font.getLineMetrics(text, fr);
    ascentF = lm.getAscent();
    descentF = lm.getDescent();
    leadingF = lm.getLeading();
    heightF = ascentF + descentF + leadingF;
    capHeightF = font.getStringBounds("X", fr).getHeight();
    
    ascent = (int) Math.ceil(ascentF);
    descent = (int) Math.ceil(descentF);
    leading = (int) Math.ceil(leadingF);
    height = ascent + descent + leading;
  }

  private static Canvas canvas = new Canvas();
  
  private static Font defaultFont(Component c, Font font) {
    if (c == null) c = canvas;
    if (font == null) font = c.getFont();
    return font;
  }

  public TextMetrics(Component c, Font font, String text) {
    this((Graphics)null, defaultFont(c, font), text);
//     FontMetrics fm = c.getFontMetrics(font);
//     width = (text != null ? fm.stringWidth(text) : 0);
//     ascent = fm.getAscent();
//     descent = fm.getDescent();
//     leading = 0;
//     height = ascent + descent + leading;
  }
}
