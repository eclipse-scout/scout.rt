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

import java.awt.Color;
import java.awt.Component;

import javax.swing.JScrollPane;

/**
 * Bug fix in layout using {@link ScrollPaneLayoutEx}.
 */
public class JScrollPaneEx extends JScrollPane {
  private static final long serialVersionUID = 1L;

  /**
   * @see JScrollPane#JScrollPane(Component, int, int)
   */
  public JScrollPaneEx(Component view, int vsbPolicy, int hsbPolicy) {
    super(view, vsbPolicy, hsbPolicy);
    //WORKAROUND layout bug fix
    setLayout(new ScrollPaneLayoutEx());
  }

  public JScrollPaneEx(Component view) {
    this(view, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);
  }

  public JScrollPaneEx(int vsbPolicy, int hsbPolicy) {
    this(null, vsbPolicy, hsbPolicy);
  }

  public JScrollPaneEx() {
    this(null, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);
  }

  @Override
  public Color getBackground() {
    // <bsh 2010-10-22>
    // If the scroll pane has no explicit background color, use the background color
    // of the inner control (if present) instead of the parents bg color. This fixes
    // a bug that caused scroll pane background colors to be painted incorrectly after
    // locking and unlocking the screen in Windows. (#90707-100)
    if (!isBackgroundSet() && getViewport() != null && getViewport().getView() != null && getViewport().getView().isBackgroundSet()) {
      return getViewport().getView().getBackground();
    }
    // </bsh>
    return super.getBackground();
  }

  @Override
  public void setBackground(Color bg) {
    super.setBackground(bg);
  }

}
