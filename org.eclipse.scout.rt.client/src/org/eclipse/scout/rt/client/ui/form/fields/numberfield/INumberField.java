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

import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.decimalfield.IDecimalField;

/**
 * Field type representing a non-fractional number such as Integer, Long,
 * BigInteger
 * 
 * @see IDecimalField for floating point numbers
 */
public interface INumberField<T extends Number> extends IValueField<T> {

  void setFormat(String s);

  String getFormat();

  void setGroupingUsed(boolean b);

  boolean isGroupingUsed();

  void setMinValue(T value);

  T getMinValue();

  void setMaxValue(T value);

  T getMaxValue();

  INumberFieldUIFacade getUIFacade();

}
