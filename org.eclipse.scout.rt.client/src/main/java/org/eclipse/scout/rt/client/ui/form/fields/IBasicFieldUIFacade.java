/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
   *          new value of the display text in the ui.
   */
  void parseAndSetValueFromUI(String value);

  /**
   * Iff {@link IBasicField#PROP_UPDATE_DISPLAY_TEXT_ON_MODIFY} is true the UI calls this method on any changes in the
   * field. If {@link IBasicField#PROP_UPDATE_DISPLAY_TEXT_ON_MODIFY} is false the model's display-text is only updated
   * when a new value is set.
   *
   * @param text
   */
  void setDisplayTextFromUI(String text);

}
