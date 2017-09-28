/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
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
