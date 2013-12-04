/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields.numberfield;

import java.math.RoundingMode;
import java.text.DecimalFormat;

import org.eclipse.scout.rt.client.ui.form.fields.IBasicField;

/**
 * Field type representing a number
 * 
 * @param <T>
 */
public interface INumberField<T extends Number> extends IBasicField<T> {

  /**
   * Sets the format used for formatting and parsing. (The properties of the provided format are applied to the internal
   * DecimalFormat instance. Therefore changes on the provided formats instance after this method returns have no
   * influence to the field.)
   * 
   * @param format
   * @throws IllegalArgumentException
   *           if format is null
   */
  void setFormat(DecimalFormat format);

  /**
   * @return A copy of the internal DecimalFormat instance. Changes on the returned instance have no effect on the
   *         behavior of the field.
   */
  DecimalFormat getFormat();

  /**
   * Sets whether or not grouping will be used for formatting. Delegates to
   * {@link DecimalFormat#setGroupingUsed(boolean)} of the internal {@link DecimalFormat} instance
   */
  void setGroupingUsed(boolean b);

  boolean isGroupingUsed();

  /**
   * Set the minimum value for this field. Value <code>null</code> means no limitation if supported by generic type else
   * the smallest possible value for the type.
   */
  void setMinValue(T value);

  T getMinValue();

  /**
   * Set the maximum value for this field. Value <code>null</code> means no limitation if supported by generic type else
   * the biggest possible value for the type.
   */
  void setMaxValue(T value);

  T getMaxValue();

  /**
   * set the rounding mode used for formatting and parsing
   * 
   * @param roundingMode
   */
  void setRoundingMode(RoundingMode roundingMode);

  RoundingMode getRoundingMode();

  /**
   * @deprecated use the facade defined by {@link IBasicField#getUIFacade()}.
   *             Will be removed with the M-Release
   */
  @SuppressWarnings("deprecation")
  @Override
  @Deprecated
  INumberFieldUIFacade getUIFacade();

}
