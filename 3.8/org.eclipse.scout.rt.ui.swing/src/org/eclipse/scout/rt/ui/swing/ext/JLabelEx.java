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

import java.awt.Font;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;

import org.eclipse.scout.rt.ui.swing.SwingUtility;

/**
 * Label with additional features
 * <ul>
 * <li>multiline and wrapping capability</li>
 * <li>tooltip when label is not fully displayed</li>
 */
public class JLabelEx extends JLabel {
  private static final long serialVersionUID = 1L;

  private boolean m_bold = false;
  private String m_tooltipTextCached;

  public JLabelEx() {
    addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        setAppropriateTooltipTextEx();
      }

      @Override
      public void componentShown(ComponentEvent e) {
        setAppropriateTooltipTextEx();
      }
    });
  }

  // override for mutiline texts
  @Override
  public void setText(String text) {
    if (SwingUtility.isMultilineLabelText(text)) {
      text = SwingUtility.createHtmlLabelText(text, false);
    }
    super.setText(text);
    setAppropriateTooltipTextEx();
  }

  public boolean isBold() {
    return m_bold;
  }

  public void setBold(boolean b) {
    if (b != m_bold) {
      m_bold = b;
      // change label font to bold
      Font f = getFont();
      if (f != null && (f.getStyle() == Font.BOLD) != b) {
        f = new Font(f.getName(), b ? Font.BOLD : Font.PLAIN, f.getSize());
      }
      setFont(f);
    }
  }

  @Override
  public void setToolTipText(String text) {
    text = SwingUtility.createHtmlLabelText(text, true);
    m_tooltipTextCached = text;
    super.setToolTipText(text);
  }

  private void setAppropriateTooltipTextEx() {
    int prefWidth = getPreferredSize().width;
    if (getWidth() < prefWidth) {
      super.setToolTipText(getText());
    }
    else {
      super.setToolTipText(m_tooltipTextCached);
    }
  }

  @Override
  public Point getToolTipLocation(MouseEvent e) {
    return SwingUtility.getAdjustedToolTipLocation(e, this, getTopLevelAncestor());
  }
}
