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

import java.awt.Dimension;
import java.awt.LayoutManager;

import javax.swing.JPanel;

/**
 * JPanel with support for independent preferredWidth and preferredHeight
 */
public class JPanelEx extends JPanel {
  private static final long serialVersionUID = 1L;
  private int m_preferredWidth;
  private int m_preferredHeight;

  public JPanelEx() {
    this(new BorderLayoutEx(0, 0));
  }

  public JPanelEx(LayoutManager layout) {
    super(layout);
    setOpaque(false);
  }

  public int getPreferredWidth() {
    return m_preferredWidth;
  }

  public void setPreferredWidth(int i) {
    if (m_preferredWidth != i) {
      m_preferredWidth = i;
      revalidate();
      repaint();
    }
  }

  public int getPreferredHeight() {
    return m_preferredHeight;
  }

  public void setPreferredHeight(int i) {
    if (m_preferredHeight != i) {
      m_preferredHeight = i;
      revalidate();
      repaint();
    }
  }

  @Override
  public Dimension getMinimumSize() {
    Dimension d = super.getMinimumSize();
    if (m_preferredWidth > 0) {
      d = new Dimension(d);
      d.width = Math.min(d.width, m_preferredWidth);
    }
    if (m_preferredHeight > 0) {
      d = new Dimension(d);
      d.height = Math.min(d.height, m_preferredHeight);
    }
    return d;
  }

  @Override
  public Dimension getPreferredSize() {
    Dimension d = super.getPreferredSize();
    if (m_preferredWidth > 0) {
      d = new Dimension(d);
      d.width = m_preferredWidth;
    }
    if (m_preferredHeight > 0) {
      d = new Dimension(d);
      d.height = m_preferredHeight;
    }
    return d;
  }

  @Override
  public Dimension getMaximumSize() {
    Dimension d = super.getMaximumSize();
    if (m_preferredWidth > 0) {
      d = new Dimension(d);
      d.width = Math.max(d.width, m_preferredWidth);
    }
    if (m_preferredHeight > 0) {
      d = new Dimension(d);
      d.height = Math.max(d.height, m_preferredHeight);
    }
    return d;
  }

}
