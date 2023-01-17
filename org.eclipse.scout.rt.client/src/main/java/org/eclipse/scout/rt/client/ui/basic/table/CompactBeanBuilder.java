/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.table;

import java.util.List;
import java.util.function.BiFunction;

import org.eclipse.scout.rt.client.ui.basic.table.columns.CompactBean;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;

/**
 * Marker interface that extends from BiFunction. The purpose is to convert a {@link ITableRow} to a
 * {@link CompactBean}.<br>
 * In the {@link #apply(Object, Object)} method cell values can either be retrieved by iterating over the given columns
 * or by accessing the desired columns directly using the named column getters of the table.
 */
public interface CompactBeanBuilder extends BiFunction<List<IColumn<?>>, ITableRow, CompactBean> {
  // DO NOT add methods without default implementation so that it can be used as lambda

  /**
   * Called before any row is processed. May be used to prepare general data.
   */
  default void prepare() {
    // NOP
  }
}
