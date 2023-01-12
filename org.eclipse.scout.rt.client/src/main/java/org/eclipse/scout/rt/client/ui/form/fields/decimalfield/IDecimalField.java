/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.decimalfield;

import org.eclipse.scout.rt.client.ui.form.fields.numberfield.INumberField;
import org.eclipse.scout.rt.client.ui.valuecontainer.IDecimalValueContainer;

/**
 * Field type representing a fractional, decimal number such as Float, Double, BigDecimal
 */
public interface IDecimalField<T extends Number> extends INumberField<T>, IDecimalValueContainer<T> {

}
