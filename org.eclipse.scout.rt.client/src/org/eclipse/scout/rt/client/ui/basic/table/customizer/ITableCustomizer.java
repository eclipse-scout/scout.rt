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
package org.eclipse.scout.rt.client.ui.basic.table.customizer;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;

/**
 * Perform table customization, such as adding custom columns
 */
public interface ITableCustomizer {

  /**
   * Append custom columns
   * 
   * @param columnList
   *          live and mutable list of configured columns, not yet initialized
   */
  void injectCustomColumns(List<IColumn<?>> columnList);

  /**
   * Add a new custom column to the table by for example showing a form with potential candidates
   */
  void addColumn() throws ProcessingException;

  /**
   * Modify an existing custom column
   */
  void modifyColumn(ICustomColumn<?> col) throws ProcessingException;

  /**
   * Remove an existing custom column, asks with a message box before deleting
   */
  void removeColumn(ICustomColumn<?> col) throws ProcessingException;

  /**
   * Remove all existing custom columns, asks with a message box before deleting
   */
  void removeAllColumns() throws ProcessingException;
}
