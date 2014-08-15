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
package org.eclipse.scout.rt.shared.ui.menu;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;

public interface IMenu5 extends IMenu {

  /*
   * System Types
   */
  int SYSTEM_TYPE_NONE = IButton.SYSTEM_TYPE_NONE;
  int SYSTEM_TYPE_CANCEL = IButton.SYSTEM_TYPE_CANCEL;
  int SYSTEM_TYPE_CLOSE = IButton.SYSTEM_TYPE_CLOSE;
  int SYSTEM_TYPE_OK = IButton.SYSTEM_TYPE_OK;
  int SYSTEM_TYPE_RESET = IButton.SYSTEM_TYPE_RESET;
  int SYSTEM_TYPE_SAVE = IButton.SYSTEM_TYPE_SAVE;
  int SYSTEM_TYPE_SAVE_WITHOUT_MARKER_CHANGE = IButton.SYSTEM_TYPE_SAVE_WITHOUT_MARKER_CHANGE;

  int getSystemType();

  void setSystemType(int systemType);

  void addActionListener(ActionListener listener);

  void removeActionListener(ActionListener listener);
}
