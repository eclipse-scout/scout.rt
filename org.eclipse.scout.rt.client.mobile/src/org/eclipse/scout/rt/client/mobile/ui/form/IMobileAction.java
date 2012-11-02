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
package org.eclipse.scout.rt.client.mobile.ui.form;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;

/**
 * @since 3.9.0
 */
public interface IMobileAction extends IMenu {

  String PROP_HORIZONTAL_ALIGNMENT = "horizontalAlignment";

  int HORIZONTAL_ALIGNMENT_LEFT = -1;
  int HORIZONTAL_ALIGNMENT_RIGHT = 1;

  int getHorizontalAlignment();

  /**
   * @param alignment
   *          negative for left and positive for right alignment
   */
  void setHorizontalAlignment(int alignment);
}
