/*
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui.form;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;

/**
 * @since 6.0
 */
public interface IFormMenu<FORM extends IForm> extends IMenu {

  String PROP_FORM = "form";
  String PROP_POPUP_CLOSABLE = "popupClosable";
  String PROP_POPUP_MOVABLE = "popupMovable";
  String PROP_POPUP_RESIZABLE = "popupResizable";

  FORM getForm();

  /**
   * Set a new <b>started</b> form to the menu.
   * <p>
   * The form is shown whenever the menu is selected.
   */
  void setForm(FORM f);

  boolean isPopupClosable();

  void setPopupClosable(boolean popupClosable);

  boolean isPopupMovable();

  void setPopupMovable(boolean popupMovable);

  boolean isPopupResizable();

  void setPopupResizable(boolean popupResizable);
}
