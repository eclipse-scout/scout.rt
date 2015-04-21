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
 * Interface for simple fields (String field, Number field, Decimal field).
 * No picker, no proposal form, no button...
 *
 * @since 3.10.0-M3
 */
public interface IBasicField<T> extends IValueField<T> {
  /**
   * @deprecated use {@link IBasicField#PROP_UPDATE_DISPLAY_TEXT_ON_MODIFY},
   *             {@link IBasicField#setUpdateDisplayTextOnModify(boolean)} and
   *             {@link AbstractBasicField#execChangedDisplayText()} instead; will be removed in 5.1.0;
   */
  @Deprecated
  String PROP_VALIDATE_ON_ANY_KEY = "validateOnAnyKey";
  String PROP_UPDATE_DISPLAY_TEXT_ON_MODIFY = "updateDisplayTextOnModify";

  /**
   * Causes the ui to send a validate event every time the input field content is changed.
   * <p>
   * Be careful when using this property since this can influence performance and the characteristics of text input.
   *
   * @deprecated use {@link IBasicField#setUpdateDisplayTextOnModify(boolean)} and
   *             {@link AbstractBasicField#execChangedDisplayText()} instead; will be removed in 5.1.0;
   */
  @Deprecated
  void setValidateOnAnyKey(boolean b);

  /**
   * @return whether the ui to sends a validate event every time the input field content is changed
   * @deprecated use {@link IBasicField#isUpdateDisplayTextOnModify()} and
   *             {@link AbstractBasicField#execChangedDisplayText()} instead; will be removed in 5.1.0;
   */
  @Deprecated
  boolean isValidateOnAnyKey();

  IBasicFieldUIFacade getUIFacade();

  /**
   * @see AbstractBasicField#getConfiguredUpdateDisplayTextOnModify()
   */
  void setUpdateDisplayTextOnModify(boolean b);

  /**
   * @see AbstractBasicField#getConfiguredUpdateDisplayTextOnModify()
   */
  boolean isUpdateDisplayTextOnModify();

}
