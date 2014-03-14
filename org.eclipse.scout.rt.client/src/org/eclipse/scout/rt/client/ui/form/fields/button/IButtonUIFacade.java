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
package org.eclipse.scout.rt.client.ui.form.fields.button;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;

/**
 * A Button has 2 aspects
 * <ol>
 * <li>System-button / NonSystem-button is marked by getSystemType()<br>
 * System buttons in a dialog have a pre-defined action handling
 * <li>Process-button / NonProcess-button is marked by isProcessButton()<br>
 * Process buttons are normally placed on dialogs button bar on the lower dialog bar
 * </ol>
 */
public interface IButtonUIFacade {

  void fireButtonClickedFromUI();

  IMenu[] fireButtonPopupFromUI();

  /**
   * Called from the UI to check whether there are valid menus defined in the model.
   * 
   * @return <code>true</code> if there is at least one valid menu. Otherwise, <code>false</code>.
   * @since 4.0.0-M6
   */
  boolean hasValidMenusFromUI();

  void setSelectedFromUI(boolean b);

}
