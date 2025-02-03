/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.booleanfield;

import org.eclipse.scout.rt.client.ui.form.fields.IValueField;

public interface IBooleanField extends IValueField<Boolean> {

  String PROP_WRAP_TEXT = "wrapText";
  String PROP_TRI_STATE_ENABLED = "triStateEnabled";
  String PROP_KEY_STROKE = "keyStroke";

  void setWrapText(boolean wrapText);

  boolean isWrapText();

  void setChecked(boolean checked);

  boolean isChecked();

  /**
   * see {@link #isTriStateEnabled()}
   *
   * @since 6.1
   */
  void setTriStateEnabled(boolean triStateEnabled);

  /**
   * <ul>
   * <li><b>true:</b> the check box can have a {@link #getValue()} of <code>true</code>, <code>false</code> and
   * <code>null</code>. <code>null</code> is the third state that represents "undefined" and is typically displayed
   * using a filled rectangular area.
   * <li><b>false:</b> the check box can have a {@link #getValue()} of <code>true</code> and <code>false</code>. The
   * value is never <code>null</code> (setting the value to <code>null</code> will automatically convert it to
   * <code>false</code>).
   * </ul>
   * The default is <code>false</code>.
   *
   * @return <code>true</code> if this check box supports the so-called "tri-state" and allows setting the value to
   * <code>null</code> to represent the "undefined" value.
   * @since 6.1
   */
  boolean isTriStateEnabled();

  String getKeyStroke();

  void setKeyStroke(String keyStroke);

  /**
   * Toggle the value.
   * <p>
   * If the checkbox is not {@link #isTriStateEnabled()} then toggles between: true, false
   * <p>
   * If the checkbox is {@link #isTriStateEnabled()} then toggles between: true, false, null
   *
   * @since 6.1
   */
  void toggleValue();

  IBooleanFieldUIFacade getUIFacade();
}
