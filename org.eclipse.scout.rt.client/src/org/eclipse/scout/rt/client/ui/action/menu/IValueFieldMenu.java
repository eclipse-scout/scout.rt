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
package org.eclipse.scout.rt.client.ui.action.menu;

/**
 *
 */
public interface IValueFieldMenu extends IMenu {

  /**
   * {@link Boolean}
   */
  static String PROP_NULL_VALUE_MENU = "nullValueMenu";

  static String PROP_NOT_NULL_VALUE_MENU = "notNullValueMenu";

  boolean isNullValueMenu();

  void setNullValueMenu(boolean nullValueMenu);

  boolean isNotNullValueMenu();

  void setNotNullValueMenu(boolean notNullValueMenu);
}
