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
package org.eclipse.scout.rt.client.ui.form.fields.button;

/**
 * Interface for a RadioButton
 *
 * @since 4.0.0-M7
 */
public interface IRadioButton<T> extends IButton {
  String PROP_RADIOVALUE = "radioValue";

  /**
   * @return radio button value
   * @since moved to {@link IRadioButton} in 4.0.0-M7
   */
  T getRadioValue();

  /**
   * @param o
   *          radio button value
   * @since moved to {@link IRadioButton} in 4.0.0-M7
   */
  void setRadioValue(T o);
}
