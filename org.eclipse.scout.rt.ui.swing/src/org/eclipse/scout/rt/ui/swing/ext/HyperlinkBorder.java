/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swing.ext;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Stroke;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;
import javax.swing.plaf.ColorUIResource;

import org.eclipse.scout.commons.NumberUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.TypeCastUtility;

public class HyperlinkBorder extends AbstractBorder {
  private static final long serialVersionUID = 1L;
  private static Insets borderInsets = new Insets(1, 3, 1, 3);
  private static final Color DEFAULT_LINE_COLOR = new Color(68, 85, 153);

  @Override
  public Insets getBorderInsets(Component c) {
    return getBorderInsets(c, new Insets(0, 0, 0, 0));
  }

  @Override
  public Insets getBorderInsets(Component c, Insets insets) {
    insets.top = borderInsets.top;
    insets.left = borderInsets.left;
    insets.bottom = borderInsets.bottom;
    insets.right = borderInsets.right;
    return insets;
  }

  @Override
  public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
    JLabel label = (JLabel) c;
    Rectangle viewR = new Rectangle(borderInsets.left, borderInsets.top, c.getWidth() - borderInsets.left - borderInsets.right, c.getHeight() - borderInsets.top - borderInsets.bottom);
    Rectangle iconR = new Rectangle();
    Rectangle textR = new Rectangle();
    FontMetrics fm = label.getFontMetrics(label.getFont());
    SwingUtilities.layoutCompoundLabel(
        label,
        fm,
        label.getText(),
        label.getIcon(),
        label.getVerticalAlignment(),
        label.getHorizontalAlignment(),
        label.getVerticalTextPosition(),
        label.getHorizontalTextPosition(),
        viewR,
        iconR,
        textR,
        label.getIconTextGap()
        );
    x = textR.x;
    y = textR.y;
    w = textR.width;
    h = textR.height;
    g.setColor(getForegroundColor(c));
    if (!StringUtility.isNullOrEmpty(label.getText())) {
      if (c.isEnabled() && c.isFocusOwner()) {
        drawFocus(g, x - 1, y, w + 2, h);
      }
      if (c.isEnabled() && c.getMousePosition() != null) {
        g.drawLine(x, y + h - 1, x + w - 1, y + h - 1);
      }
    }
  }

  /**
   * We cannot rely on the foreground color, because when the foreground color is instance of ColorUIResource,
   * the foreground color is chosen by the look & feel. Which means getForeground() may return "black"
   * (and is istanceof ColorUIResource) but on the screen the widget is red (because this color was chosen
   * by the look & feel. Best thing we can do here is to lookup a color in the UIDefaults when the
   * foreground is not defined for the widget.
   * 
   * @param c
   * @return
   */
  private Color getForegroundColor(Component c) {
    if (c.getForeground() instanceof ColorUIResource) {
      Color defaultLineColor = UIManager.getDefaults().getColor("Hyperlink.foreground");
      if (defaultLineColor == null) {
        defaultLineColor = DEFAULT_LINE_COLOR;
      }
      return defaultLineColor;
    }
    else {
      return c.getForeground();
    }
  }

  private static void drawFocus(Graphics g, int x, int y, int w, int h) {
    Graphics2D g2d = (Graphics2D) g;
    Stroke old = g2d.getStroke();
    float pattern0 = NumberUtility.nvl(TypeCastUtility.castValue(UIManager.getLookAndFeelDefaults().get("Hyperlink.borderPattern0"), Float.class), Float.valueOf(1)).floatValue();
    float pattern1 = NumberUtility.nvl(TypeCastUtility.castValue(UIManager.getLookAndFeelDefaults().get("Hyperlink.borderPattern1"), Float.class), Float.valueOf(2)).floatValue();
    g2d.setStroke(new BasicStroke(1f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1f, new float[]{pattern0, pattern1}, 1));
    g2d.drawLine(x, y, x + w - 1, y);
    g2d.drawLine(x, y + h, x + w - 1, y + h);
    g2d.drawLine(x, y, x, y + h - 1);
    g2d.drawLine(x + w, y, x + w, y + h - 1);
    g2d.setStroke(old);
  }
}
