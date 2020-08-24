/**
 * This file is part of a fork of logisim-evolution.
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
 *   + Greg Ewing
 *     https://www.csse.canterbury.ac.nz/greg.ewing/
 */

package com.cburch.logisim.circuit.appear;

import static com.cburch.logisim.std.Strings.S;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeOption;
import com.cburch.logisim.data.Attributes;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.std.wiring.PinAttributes;
import java.awt.Color;
import java.awt.Font;
import java.util.Arrays;
import java.util.List;

public class PortAttributes {

  public static final Attribute<Direction> PIN_FACING =
      Attributes.forDirection("logisim-port-facing", S.getter("portFacingAttr"));

  public static final Attribute<Boolean> PIN_SHOW_LABEL =
    Attributes.forBoolean("logisim-port-show-label", S.getter("portShowLabelAttr"));

  public static final Attribute<Boolean> PIN_SHOW_NUMBER =
    Attributes.forBoolean("logisim-port-show-pin-number", S.getter("portShowPinNumberAttr"));

  public static final AttributeOption PINNO_ABOVE_LEFT =
    new AttributeOption("above-left", S.getter("pinNumberAboveLeftOption"));
  public static final AttributeOption PINNO_BELOW_RIGHT =
    new AttributeOption("below-right", S.getter("pinNumberBelowRightOption"));

	public static final Attribute<AttributeOption> PIN_NUMBER_POSITION =
    Attributes.forOption(
      "logisim-pin-number-position",
      S.getter("pinNumberPositionAttr"),
      new AttributeOption[] {PINNO_ABOVE_LEFT, PINNO_BELOW_RIGHT});

  public static final Attribute<Font> PIN_LABEL_FONT =
      Attributes.forFont("logisim-port-label-font", S.getter("portLabelFontAttr"));

  public static final Attribute<Color> PIN_LABEL_COLOR =
      Attributes.forColor("logisim-port-label-color", S.getter("portLabelColorAttr"));

  public static final Attribute<Font> PIN_NUMBER_FONT =
      Attributes.forFont("logisim-pin-number-font", S.getter("pinNumberFontAttr"));

  public static final Attribute<Color> PIN_NUMBER_COLOR =
      Attributes.forColor("logisim-pin-number-color", S.getter("pinNumberColorAttr"));

  public static final List<Attribute<?>> PORT_ATTRIBUTES =
      Arrays.asList(
          new Attribute<?>[] {
              PIN_FACING,
              StdAttr.LABEL,
              PIN_SHOW_LABEL,
              PIN_LABEL_FONT,
              PIN_LABEL_COLOR,
              PinAttributes.PIN_NUMBER,
              PIN_SHOW_NUMBER,
              PIN_NUMBER_POSITION,
              PIN_NUMBER_FONT,
              PIN_NUMBER_COLOR
          });    
}
