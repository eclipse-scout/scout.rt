/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.labelfield;

import org.eclipse.scout.rt.client.ui.IAppLinkCapable;
import org.eclipse.scout.rt.client.ui.IHtmlCapable;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;

public interface ILabelField extends IValueField<String>, IHtmlCapable, IAppLinkCapable {

  String PROP_WRAP_TEXT = "wrapText";
  String PROP_SELECTABLE = "selectable";

  void setWrapText(boolean wrapText);

  boolean isWrapText();

  /**
   * Specifies whether the label should be selectable or not
   *
   * @since 3.10.0-M6
   */
  void setSelectable(boolean selectable);

  /**
   * returns <code>true</code> if the label is selectable, <code>false</code> otherwise
   */
  boolean isSelectable();

  ILabelFieldUIFacade getUIFacade();
}
