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
package org.eclipse.scout.rt.ui.swt.basic.table.celleditor;

import org.eclipse.swt.widgets.Shell;

/**
 * Listener to be notified about form-field and traversal events of {@link SwtScoutFormFieldPopup}.
 */
public interface IFormFieldPopupListener {

  /**
   * Event constant to indicate the content to be stored. (e.g. because of an <code>OK</code> keystroke, traversal event
   * or due to popup deactivation)
   */
  int TYPE_OK = 1 << 0;

  /**
   * Event constant to indicate the content to be discarded (e.g. because of an <code>ESC</code> keystroke).
   */
  int TYPE_CANCEL = 1 << 1;

  /**
   * Event constant to indicate that a traversal event occured.
   */
  int TYPE_FOCUS_NEXT = 1 << 2;

  /**
   * Event constant to indicate that a traversal event occured.
   */
  int TYPE_FOCUS_BACK = 1 << 3;

  /**
   * Callback to indicate a popup event occured. Thereby, the {@link Shell} is not closed.
   *
   * @param event
   *          <em>bitwise OR</em> encoded style constant to describe the event.
   */
  void handleEvent(int event);
}
