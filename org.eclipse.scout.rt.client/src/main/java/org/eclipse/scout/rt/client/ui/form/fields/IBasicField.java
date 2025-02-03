/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields;

/**
 * Interface for simple fields (String field, Number field, Decimal field). No picker, no proposal form, no button...
 *
 * @since 3.10.0-M3
 */
public interface IBasicField<T> extends IValueField<T> {

  String PROP_UPDATE_DISPLAY_TEXT_ON_MODIFY = "updateDisplayTextOnModify";
  String PROP_UPDATE_DISPLAY_TEXT_ON_MODIFY_DELAY = "updateDisplayTextOnModifyDelay";

  int DEFAULT_DELAY = 250; // [ms]

  IBasicFieldUIFacade getUIFacade();

  /**
   * @see AbstractBasicField#getConfiguredUpdateDisplayTextOnModify()
   */
  void setUpdateDisplayTextOnModify(boolean update);

  /**
   * @see AbstractBasicField#getConfiguredUpdateDisplayTextOnModify()
   */
  boolean isUpdateDisplayTextOnModify();

  void setUpdateDisplayTextOnModifyDelay(int delay);

  int getUpdateDisplayTextOnModifyDelay();
}
