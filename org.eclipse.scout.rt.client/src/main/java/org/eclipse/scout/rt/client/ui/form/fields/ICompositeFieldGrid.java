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
 * Implementations of this interface distribute fields of an {@link ICompositeField} over a grid by setting the GridData
 * of each field.
 *
 * @since 5.2
 */
public interface ICompositeFieldGrid<T extends ICompositeField> {

  /**
   * Validates the grid data of all fields in the given composite field.
   */
  void validate(T compositeField);

  /**
   * @return the column count of the calculated grid
   */
  int getGridColumnCount();

  /**
   * @return the row count of the calculated grid
   */
  int getGridRowCount();
}
