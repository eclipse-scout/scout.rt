/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
  // DO NOT add methods so that it can be used as lambda
}
