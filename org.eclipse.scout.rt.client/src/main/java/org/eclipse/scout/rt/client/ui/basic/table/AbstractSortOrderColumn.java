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

import org.eclipse.scout.rt.client.extension.ui.basic.table.ISortOrderColumnExtension;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.platform.classid.ClassId;

@ClassId("a92f1e53-7443-4e2a-b8d2-43826c959c84")
public abstract class AbstractSortOrderColumn extends AbstractColumn<IColumn> implements ISortOrderColumn {

  protected static class LocalSortOrderColumnExtension<OWNER extends AbstractSortOrderColumn> extends LocalColumnExtension<IColumn, OWNER> implements ISortOrderColumnExtension<OWNER> {

    public LocalSortOrderColumnExtension(OWNER owner) {
      super(owner);
    }
  }

  @Override
  protected ISortOrderColumnExtension<? extends AbstractSortOrderColumn> createLocalExtension() {
    return new LocalSortOrderColumnExtension<>(this);
  }
}
