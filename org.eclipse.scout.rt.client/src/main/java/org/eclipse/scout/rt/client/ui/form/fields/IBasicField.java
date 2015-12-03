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
 * Interface for simple fields (String field, Number field, Decimal field). No picker, no proposal form, no button...
 *
 * @since 3.10.0-M3
 */
public interface IBasicField<T> extends IValueField<T> {
  String PROP_UPDATE_DISPLAY_TEXT_ON_MODIFY = "updateDisplayTextOnModify";

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
