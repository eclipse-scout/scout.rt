/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.button;

/**
 * Interface for a RadioButton
 *
 * @since 4.0.0-M7
 */
public interface IRadioButton<T> extends IButton {

  String PROP_WRAP_TEXT = "wrapText";
  String PROP_RADIO_VALUE = "radioValue";

  void setWrapText(boolean wrapText);

  boolean isWrapText();

  /**
   * @return radio button value
   * @since moved to {@link IRadioButton} in 4.0.0-M7
   */
  T getRadioValue();

  /**
   * @param o
   *     radio button value
   * @since moved to {@link IRadioButton} in 4.0.0-M7
   */
  void setRadioValue(T radioValue);
}
