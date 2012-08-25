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
package org.eclipse.scout.rt.ui.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.text.View;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

public final class SwingLayoutUtility {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwingLayoutUtility.class);

  public static final int MIN = 0;
  public static final int PREF = 1;
  public static final int MAX = 2;
  private static int textFieldTopInset = 0;

  static {
    Border textFieldBorder = UIManager.getBorder("TextField.border");
    if (textFieldBorder != null) {
      textFieldTopInset = textFieldBorder.getBorderInsets(new JTextField("X")).top;
    }
  }

  private SwingLayoutUtility() {
  }

  public static int getTextFieldTopInset() {
    return textFieldTopInset;
  }

  public static Dimension getPreferredLabelSize(JLabel c, int widthHint) {
    View v = (View) c.getClientProperty(javax.swing.plaf.basic.BasicHTML.propertyKey);
    if (v == null) {
      return c.getPreferredSize();
    }
    //
    String text = c.getText();
    Icon icon = c.isEnabled() ? c.getIcon() : c.getDisabledIcon();
    int hAlign = c.getHorizontalAlignment();
    int vAlign = c.getVerticalAlignment();
    int hTextPosition = c.getHorizontalTextPosition();
    int vTextPosition = c.getVerticalTextPosition();
    int iconTextGap = c.getIconTextGap();
    // int mnemonicIndex=c.getDisplayedMnemonicIndex();
    Font font = c.getFont();
    Insets insets = c.getInsets(new Insets(0, 0, 0, 0));
    //
    int dx = insets.left + insets.right;
    int dy = insets.top + insets.bottom;
    if (icon == null && (text == null || font == null)) {
      return new Dimension(dx, dy);
    }
    else if ((text == null) || ((icon != null) && (font == null))) {
      return new Dimension((icon != null ? icon.getIconWidth() : 0) + dx, (icon != null ? icon.getIconHeight() : 0) + dy);
    }
    else {
      Rectangle iconR = new Rectangle();
      Rectangle textR = new Rectangle();
      Rectangle viewR = new Rectangle();
      FontMetrics fm = c.getFontMetrics(font);
      iconR.x = iconR.y = iconR.width = iconR.height = 0;
      textR.x = textR.y = textR.width = textR.height = 0;
      viewR.x = dx;
      viewR.y = dy;
      viewR.width = widthHint - dx;
      viewR.height = Short.MAX_VALUE;
      //PATCH BEGIN: set inner size of html view
      int availTextWidth;
      int gap = (icon == null) ? 0 : iconTextGap;

      if (hTextPosition == SwingConstants.CENTER) {
        availTextWidth = viewR.width;
      }
      else {
        availTextWidth = viewR.width - (((icon == null) ? 0 : icon.getIconWidth()) + gap);
      }
      v.setSize(availTextWidth, 0);
      //PATCH END
      SwingUtilities.layoutCompoundLabel(c, fm, text, icon, vAlign, hAlign, vTextPosition, hTextPosition, viewR, iconR, textR, iconTextGap);
      int x1 = Math.min(iconR.x, textR.x);
      int x2 = Math.max(iconR.x + iconR.width, textR.x + textR.width);
      int y1 = Math.min(iconR.y, textR.y);
      int y2 = Math.max(iconR.y + iconR.height, textR.y + textR.height);
      Dimension rv = new Dimension(x2 - x1, y2 - y1);
      rv.width += dx;
      rv.height += dy;
      return rv;
    }
  }

  public static Dimension getSize(Component c, int sizeflag) {
    if (c == null) {
      return new Dimension(0, 0);
    }
    //special case due to swing bug: html labels need to know the current parent layout's size
    if (c instanceof JLabel) {
      View v = (View) ((JLabel) c).getClientProperty(javax.swing.plaf.basic.BasicHTML.propertyKey);
      if (v != null) {
        switch (sizeflag) {
          case MIN: {
            Dimension d = new Dimension(c.getPreferredSize());
            d.width = 1;
            return d;
          }
          case PREF: {
            Dimension d = new Dimension(c.getPreferredSize());
            return d;
          }
          case MAX: {
            Dimension d = new Dimension(10240, 10240);
            d.width = 1;
            return d;
          }
        }
      }
    }
    //
    switch (sizeflag) {
      case MIN: {
        return new Dimension(c.getMinimumSize());
      }
      case PREF: {
        return new Dimension(c.getPreferredSize());
      }
      case MAX: {
        return new Dimension(c.getMaximumSize());
      }
    }
    return new Dimension(c.getPreferredSize());
  }

  public static Dimension getValidatedSize(Component c, int sizeflag) {
    return getValidatedSizes(c)[sizeflag];
  }

  public static Dimension[] getValidatedSizes(Component c) {
    Dimension[] d = new Dimension[]{
        getSize(c, MIN),
        getSize(c, PREF),
        getSize(c, MAX)
    };
    // validation
    if (d[MIN].width > d[PREF].width) {
      d[MIN].width = d[PREF].width;
    }
    if (d[MIN].height > d[PREF].height) {
      d[MIN].height = d[PREF].height;
    }
    if (d[MAX].width < d[PREF].width) {
      d[MAX].width = d[PREF].width;
    }
    if (d[MAX].height < d[PREF].height) {
      d[MAX].height = d[PREF].height;
    }
    return d;
  }

  public static Dimension[] addPadding(Dimension[] dims, Insets insets) {
    Dimension[] d = new Dimension[dims.length];
    if (insets != null) {
      for (int i = 0; i < d.length; i++) {
        d[i] = expandSize(dims[i], insets);
      }
    }
    return d;
  }

  public static Dimension expandSize(Dimension d, Insets insets) {
    if (insets != null) {
      d = new Dimension(
          d.width + insets.left + insets.right,
          d.height + insets.top + insets.bottom
          );
    }
    return d;
  }

  public static Rectangle shrinkSize(Rectangle r, Insets insets) {
    if (insets != null) {
      r = new Rectangle(
          r.x + insets.left,
          r.y + insets.top,
          r.width - insets.left - insets.right,
          r.height - insets.top - insets.bottom
          );
    }
    if (r.width < 0) {
      r.width = 0;
    }
    if (r.height < 0) {
      r.height = 0;
    }
    return r;
  }

  public static void setIconButtonSizes(ISwingEnvironment env, JComponent c) {
    int w = env.getIconButtonSize();
    int h = w;
    c.setMinimumSize(new Dimension(w, h));
    c.setPreferredSize(new Dimension(w, h));
    c.setMaximumSize(new Dimension(w, h));
  }

  public static void setIconButtonWithPopupSizes(ISwingEnvironment env, JComponent c) {
    int w = env.getDropDownButtonWidth();
    int h = env.getIconButtonSize();
    c.setMinimumSize(new Dimension(w, h));
    c.setPreferredSize(new Dimension(w, h));
    c.setMaximumSize(new Dimension(w, h));
  }

  /**
   * Workaround for bug in swing invalidate/validate concept. Swing is caching
   * component sizes and clearing that cache once invalidate is called, which is
   * fine. But in case a validate is in progress and part of the ancestor tree
   * is already layouted based on old sizes then the caches are already set, but
   * the valid flag is still false. Therefore this method can be called which
   * will invalidate all ancestors *including* those that are still invalid in
   * order to clear all size caches. see also invalidateSubTree()
   */
  public static void invalidateAncestors(Component c) {
    if (c != null) {
      while (true) {
        c.invalidate();
        c = c.getParent();
        if (c == null) {
          break;
        }
        if (c instanceof JComponent && ((JComponent) c).isValidateRoot()) {
          break;
        }
      }
    }
  }

  /**
   * Workaround for bug in swing invalidate/validate concept. Swing is caching
   * component sizes and clearing that cache once invalidate is called, which is
   * fine. But in case a validate is in progress and part of the ancestor tree
   * is already layouted based on old sizes then the caches are already set, but
   * the valid flag is still false. Therefore this method can be called which
   * will invalidate all ancestors *including* those that are still invalid in
   * order to clear all size caches. see also invalidateAncestors()
   */
  public static void invalidateSubtree(Component c) {
    invalidateSubtreeRec(c);
  }

  private static void invalidateSubtreeRec(Component c) {
    if (c != null && c.isVisible()) {
      c.invalidate();
      if (c instanceof Container) {
        for (Component child : ((Container) c).getComponents()) {
          invalidateSubtreeRec(child);
        }
      }
    }
  }

  private static boolean dumpSizeTreeRunning;

  public static void dumpSizeTree(Component c) {
    if (!dumpSizeTreeRunning) {
      try {
        dumpSizeTreeRunning = true;
        dumpSizeTreeRec(c, "");
      }
      finally {
        dumpSizeTreeRunning = false;
      }
    }
  }

  private static void dumpSizeTreeRec(Component c, String prefix) {
    if (c.isVisible()) {
      Dimension[] d = getValidatedSizes(c);
      String lay = "null";
      if (c instanceof Container) {
        LayoutManager lm = ((Container) c).getLayout();
        if (lm != null) {
          lay = lm.getClass().getSimpleName();
        }
      }
      Rectangle r = c.getBounds();
      StringBuffer buf = new StringBuffer();
      buf.append("[" + r.x + "," + r.y + "," + r.width + "," + r.height + "]");
      buf.append(" min=(" + d[0].width + "," + d[0].height + ")");
      buf.append(" pref=(" + d[1].width + "," + d[1].height + ")");
      buf.append(" max=(" + d[2].width + "," + d[2].height + ")");
      buf.append(" " + c.getClass().getSimpleName() + " " + c.getName());
      buf.append(" layout=" + lay);
      buf.append(" minimumSet=" + c.isMinimumSizeSet());
      buf.append(" preferredSet=" + c.isPreferredSizeSet());
      buf.append(" maximumSet=" + c.isMaximumSizeSet());
      buf.append(" valid=" + c.isValid());
      buf.append(" visible=" + c.isVisible());
      if (c instanceof JComponent) {
        Object gd = ((JComponent) c).getClientProperty(LogicalGridData.CLIENT_PROPERTY_NAME);
        if (gd != null) {
          buf.append(" logicalGridData=" + gd);
        }
      }
      // details
      if (c instanceof JComponent) {
        LayoutManager layout = ((JComponent) c).getLayout();
        if (layout instanceof LogicalGridLayout) {
          StringWriter w = new StringWriter();
          ((LogicalGridLayout) layout).dumpLayoutInfo((Container) c, new PrintWriter(w, true));
          buf.append("\n  " + w.toString().replace("\n", "\n  "));
        }
      }
      String msg = prefix + buf.toString().replace("\n", "\n" + prefix);
      System.out.println(msg);
      // children
      if (c instanceof Container) {
        for (Component child : ((Container) c).getComponents()) {
          dumpSizeTreeRec(child, prefix + "  ");
        }
      }
    }
  }

}
