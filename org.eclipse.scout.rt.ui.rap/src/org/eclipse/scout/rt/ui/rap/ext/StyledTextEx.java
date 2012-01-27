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
package org.eclipse.scout.rt.ui.rap.ext;

import org.eclipse.scout.rt.ui.rap.ext.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;

/**
 * <h3>StyledTextEx</h3> ...
 * 
 * @since 3.7.0 June 2011
 */
/*
 * XXX hstaudacher I don't thing that we need a styled text for all the fields.
 * Your solution with the composite + text + button does work fine. Maybe we
 * can avoid using the StyledText and avoid this inheritence line
 */
public class StyledTextEx extends StyledText {
  private static final long serialVersionUID = 1L;

  private Menu m_copyPasteMenu;

  public StyledTextEx(Composite parent, int style) {
    super(parent, style);
    // Make sure that the menus are initially enabled
    setEnabled(true);
  }

  @Override
  protected void checkSubclass() {
  }

  @Override
  public void setEnabled(boolean enabled) {
//    super.setEnabled(enabled);
    super.setEditable(enabled);
    if (!isDisposed()) {
      if (enabled) {
        setMenu(m_copyPasteMenu);
      }
      else {
        setMenu(null);
      }
    }

    Composite parent = getParent();
    if (parent != null && !parent.isDisposed()) {
      if (enabled) {
        parent.setMenu(null);
      }
      else {
        parent.setMenu(m_copyPasteMenu);
      }
    }
  }

  @Override
  /** {@inheritDoc} */
  public void setBounds(int x, int y, int width, int height) {
    super.setBounds(x, y, width, height);
    updateVerticalScrollbarVisibility();
  }

  protected void updateVerticalScrollbarVisibility() {
    /*XXX
    Rectangle clientArea = getClientArea();
    Point size = computeSize(clientArea.width, SWT.DEFAULT, false);
    ScrollBar vBar = getVerticalBar();
    if (vBar != null && !vBar.isDisposed()) {
      vBar.setVisible(size.y > clientArea.height);
    }
    */
  }
}
