/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
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
   * @param newText
   *          new value of the display text in the ui.
   * @param whileTyping
   *          true to indicate if the user is typing (e.g. {@link IBasicField#isValidateOnAnyKey()} is true) or
   *          false if the method is called on focus lost.
   * @return
   */
  boolean setTextFromUI(String newText, boolean whileTyping);

}
