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
package org.eclipse.scout.rt.client.ui.basic.table.customizer;

import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.platform.util.collection.OrderedCollection;

/**
 * Perform table customization, such as adding custom columns
 */
public interface ITableCustomizer {

  /**
   * Append custom columns
   *
   * @param columns
   *          live and mutable collection of configured columns, not yet initialized
   */
  void injectCustomColumns(OrderedCollection<IColumn<?>> columns);

  /**
   * Add a new custom column to the table by for example showing a form with potential candidates
   */
  void addColumn();

  /**
   * Modify an existing custom column
   */
  void modifyColumn(ICustomColumn<?> col);

  /**
   * Remove an existing custom column
   */
  void removeColumn(ICustomColumn<?> col);

  /**
   * Remove all existing custom columns
   */
  void removeAllColumns();

  /**
   * Get the serialized data of the TableCustomizer for further processing (e.g. storing a bookmark)
   */
  byte[] getSerializedData();

  /**
   * Import the serialized data, e.g. after restoring from a bookmark
   */
  void setSerializedData(byte[] data);

  /**
   * Get a key which identifies both: the table this instance refers to and the concrete implementation of the
   * customizer.
   * <p>
   * This key will be used when persisting the customizer's serialized state in the application's preferences.
   *
   * @return either the key or null if this customizer is volatile and should therefore not be persisted in the
   *         preferences
   */
  String getPreferencesKey();

}
