/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.sequencebox;

import org.eclipse.scout.rt.client.ui.form.fields.ICompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.LogicalGridLayoutConfig;

/**
 * A {@link IFormField} that contains an ordered sequence of {@link IFormField}s.<br>
 * E.g. a range with start and end date.
 */
public interface ISequenceBox extends ICompositeField {

  String PROP_LAYOUT_CONFIG = "layoutConfig";

  /**
   * @return true, if the sequence is validated automatically, whenever a value of the contained fields changes.
   * <code>false</code> otherwise.
   */
  boolean isAutoCheckFromTo();

  /**
   * <code>true</code>: The sequence of contained fields is validated automatically, whenever a value of the contained
   * fields changes.<br>
   * <code>false</code>: The sequence of contained fields is not validated automatically.
   */
  void setAutoCheckFromTo(boolean autoCheckFromTo);

  void setLayoutConfig(LogicalGridLayoutConfig config);

  LogicalGridLayoutConfig getLayoutConfig();
}
