/*******************************************************************************
 * Copyright (c) 2014 Schweizerische Bundesbahnen SBB, BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Schweizerische Bundesbahnen SBB - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields.colorpickerfield;

import java.util.List;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.form.fields.IBasicField;

public interface IColorField extends IBasicField<String> {
  String PROP_ICON_ID = "iconId";

  List<IMenu> getMenus();

  @Override
  IColorFieldUiFacade getUIFacade();

  /**
   * @return
   */
  String getIconId();

  /**
   * @param s
   */
  void setIconId(String s);
}
