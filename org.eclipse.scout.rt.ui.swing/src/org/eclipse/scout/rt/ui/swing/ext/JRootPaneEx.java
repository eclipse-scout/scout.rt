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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

import javax.swing.JRootPane;
import javax.swing.SwingUtilities;

import org.eclipse.scout.rt.ui.swing.SwingLayoutUtility;

/**
 * Root pane with lazy size validation (minSize, maxSize)
 * <p>
 * When minSize or maxSize or preferred height is not respected then {@link #reflow()} is called.
 * <p>
 * Only the dimension that needs resize is changed (width, height).
 * <p>
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=363148
 */
public class JRootPaneEx extends JRootPane {
  private static final long serialVersionUID = 1L;

  private Runnable m_reflowJob;

  @Override
  protected LayoutManager createRootLayout() {
    return new RootLayoutEx();
  }

  /**
   * calling this method causes the root pane to post {@link SwingUtilities#invokeLater(Runnable)} a job that later
   * calls {@link #reflow()} on this root pane
   * 
   * @param c
   *          the component that changed visibility
   */
  public void notifyVisibleChanged(Component c) {
    if (m_reflowJob == null) {
      m_reflowJob = new Runnable() {
        @Override
        public void run() {
          try {
            evaluateReflow();
          }
          finally {
            m_reflowJob = null;
          }
        }
      };
      SwingUtilities.invokeLater(m_reflowJob);
    }
  }

  protected void evaluateReflow() {
    if (getParent() == null || !isVisible() || !isShowing()) {
      return;
    }
    // check minSize and maxSize requirements
    Dimension d = getSize();
    if (d.width > 0 && d.height > 0) {
      Dimension[] sizes = SwingLayoutUtility.getValidatedSizes(JRootPaneEx.this);
      Dimension minMaxSize = new Dimension(
          Math.min(Math.max(d.width, sizes[0].width), sizes[2].width),
          Math.min(Math.max(d.height, sizes[0].height), sizes[2].height)
          );
      int dw = minMaxSize.width - d.width;
      int dh = minMaxSize.height - d.height;
      int dwPref = sizes[1].width - d.width;
      int dhPref = sizes[1].height - d.height;
      //OLD: if (dw != 0 || dh != 0 || dwPref != 0 || dhPref != 0) {
      //NEW: only force re-pack when min/max or at least prefHeight is not met, forcing preferred size breaks the bugs https://bugs.eclipse.org/bugs/show_bug.cgi?id=363148 and tickets 107768, 106554
      if (dw != 0 || dh != 0 || dhPref > 0) {
        reflow();
      }
    }
  }

  @Override
  public void validate() {
    SwingLayoutUtility.invalidateSubtree(this);
    super.validate();
  }

  protected void reflow() {
  }

  protected class RootLayoutEx extends JRootPane.RootLayout {

    private static final long serialVersionUID = 1L;

    @Override
    public Dimension maximumLayoutSize(Container target) {
      Dimension rd, mbd;
      Insets i = getInsets();
      if (menuBar != null && menuBar.isVisible()) {
        mbd = menuBar.getMaximumSize();
      }
      else {
        mbd = new Dimension(0, 0);
      }
      if (contentPane != null) {
        rd = contentPane.getMaximumSize();
      }
      else {
        rd = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE - i.top - i.bottom - mbd.height - 1);
      }
      // fixed bug here, use max (and not min)
      return new Dimension(Math.max(rd.width, mbd.width) + i.left + i.right, rd.height + mbd.height + i.top + i.bottom);
    }
  }

}
