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
package org.eclipse.scout.rt.ui.swt.action.menu.text;

import org.eclipse.scout.rt.ui.swt.internal.StyledTextFieldUndoRedoSupport;
import org.eclipse.scout.rt.ui.swt.util.SwtUtility;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;

/**
 *
 */
public class SwtRedoMenuItem extends AbstractStyledTextSystemMenuItem {

  private StyledTextFieldUndoRedoSupport m_undoRedoSupport;

  /**
   * @param menu
   * @param label
   * @param textControl
   */
  public SwtRedoMenuItem(Menu menu, StyledTextFieldUndoRedoSupport undoRedoSupport) {
    super(menu, SwtUtility.getNlsText(Display.getCurrent(), "Redo"), undoRedoSupport.getStyledText());
    m_undoRedoSupport = undoRedoSupport;
  }

  @Override
  protected void initMenuItem(Menu parentMenu) {
    super.initMenuItem(parentMenu);

  }

  @Override
  protected void updateEnability() {
    setEnabled(getTextControl().isEnabled() && getTextControl().getEditable() && m_undoRedoSupport.hasRedoChanges());
  }

  @Override
  protected void doAction() {
    m_undoRedoSupport.redo();
  }
}
