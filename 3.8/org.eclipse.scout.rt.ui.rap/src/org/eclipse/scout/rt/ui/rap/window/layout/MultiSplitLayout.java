/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.window.layout;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Sash;

/**
 * <h3>MultiSplitLayout</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 3.7.0 June 2011
 */
public class MultiSplitLayout extends Layout {
  private static final long serialVersionUID = 1L;

  private Control[][] m_controls;
  private Sash m_verticalSash1;
  private Sash m_verticalSash2;
  private Sash m_horizontalSash00;
  private Sash m_horizontalSash01;
  private Sash m_horizontalSash10;
  private Sash m_horizontalSash11;
  private Sash m_horizontalSash20;
  private Sash m_horizontalSash21;

  public MultiSplitLayout(Composite parent) {
    m_controls = new Control[3][3];
  }

  public void addControl(Control c, int x, int y) {
    m_controls[x][y] = c;
  }

  public Control removeControl(int x, int y) {
    Control c = m_controls[x][y];
    m_controls[x][y] = null;
    return c;
  }

  @Override
  protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected void layout(Composite composite, boolean flushCache) {
    // TODO Auto-generated method stub

  }

  protected void computeVerticalSashes(Composite parent) {
    boolean column1Visible = false;
    for (int i = 0; i < 3; i++) {
      if (m_controls[0][i].getVisible()) {
        column1Visible = true;
      }
    }
    boolean column2Visible = false;
    for (int i = 0; i < 3; i++) {
      if (m_controls[1][i].getVisible()) {
        column2Visible = true;
      }
    }
    if (column1Visible && column2Visible) {
      m_verticalSash1 = new Sash(parent, SWT.VERTICAL);
    }
    boolean column3Visible = false;
    for (int i = 0; i < 3; i++) {
      if (m_controls[2][i].getVisible()) {
        column3Visible = true;
      }
    }
    if (column2Visible && column3Visible) {
      m_verticalSash2 = new Sash(parent, SWT.VERTICAL);
    }

    // check 1 & 3
    if (!m_verticalSash1.getVisible() && !m_verticalSash2.getVisible()) {
      m_verticalSash1.setVisible(column1Visible & column3Visible);
    }
  }

  protected void computeHorizontalSashes(Composite parent) {
    for (int i = 0; i < 3; i++) {
//XXX
    }
    boolean column1Visible = false;
    for (int i = 0; i < 3; i++) {
      if (m_controls[0][i].getVisible()) {
        column1Visible = true;
      }
    }
    boolean column2Visible = false;
    for (int i = 0; i < 3; i++) {
      if (m_controls[1][i].getVisible()) {
        column2Visible = true;
      }
    }
    if (column1Visible && column2Visible) {
      m_verticalSash1 = new Sash(parent, SWT.VERTICAL);
    }
    boolean column3Visible = false;
    for (int i = 0; i < 3; i++) {
      if (m_controls[2][i].getVisible()) {
        column3Visible = true;
      }
    }
    if (column2Visible && column3Visible) {
      m_verticalSash2 = new Sash(parent, SWT.VERTICAL);
    }

    // check 1 & 3
    if (!m_verticalSash1.getVisible() && !m_verticalSash2.getVisible()) {
      m_verticalSash1.setVisible(column1Visible & column3Visible);
    }
  }

}
