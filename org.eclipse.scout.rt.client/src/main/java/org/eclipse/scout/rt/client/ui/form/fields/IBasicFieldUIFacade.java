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
 * UI facade for {@link IBasicField}
 *
 * @since 3.10.0 M3
 */
public interface IBasicFieldUIFacade {

  /**
   * Triggers parsing the text and setting a new value.
   *
   * @param value
   *     new value of the display text in the ui.
   */
  void parseAndSetValueFromUI(String value);

  /**
   * Iff {@link IBasicField#PROP_UPDATE_DISPLAY_TEXT_ON_MODIFY} is true the UI calls this method on any changes in the
   * field. If {@link IBasicField#PROP_UPDATE_DISPLAY_TEXT_ON_MODIFY} is false the model's display-text is only updated
   * when a new value is set.
   *
   * @param text
   *     new value of the display text in the ui.
   */
  void setDisplayTextFromUI(String text);
}
