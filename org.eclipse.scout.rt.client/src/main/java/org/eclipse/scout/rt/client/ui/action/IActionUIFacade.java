/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.action;

/**
 * Callback to listen for action events triggered by the UI.
 */
public interface IActionUIFacade {

  /**
   * This method is called every time the selection is changed from within the UI. Please note, that
   * {@link #fireActionFromUI()} must be called as well, regardless of whether the selection state changed.
   * 
   * @param selected
   *          <code>true</code> if selected, <code>false</code> otherwise.
   * @see #fireActionFromUI()
   */
  void setSelectedFromUI(boolean selected);

  /**
   * This method is called every time an action is executed from within the UI (e.g. by a click or selection event).
   * This method is also called for selection events no matter if the selection status changed.
   */
  void fireActionFromUI();
}
