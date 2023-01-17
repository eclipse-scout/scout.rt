/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.outline.pages.fixture;

import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractLongColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithTable;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.ISearchForm;
import org.eclipse.scout.rt.platform.Order;

/**
 * @since 6.0
 */
public abstract class AbstractPersonTablePage<T extends AbstractPersonTablePage<T>.Table> extends AbstractPageWithTable<T> {

  @Override
  protected Class<? extends ISearchForm> getConfiguredSearchForm() {
    return PersonSearchForm.class;
  }

  @Override
  protected void execInitPage() {
    getSearchFormInternal().setSearchFilter(null);
  }

  public class Table extends AbstractTable {
    public AbstractPersonTablePage<?>.Table.NameColumn getNameColumn() {
      return getColumnSet().getColumnByClass(NameColumn.class);
    }

    public AbstractPersonTablePage<?>.Table.AgeColumn getAgeColumn() {
      return getColumnSet().getColumnByClass(AgeColumn.class);
    }

    @Order(10)
    public class NameColumn extends AbstractStringColumn {
    }

    @Order(20)
    public class AgeColumn extends AbstractLongColumn {
    }

    @Order(10)
    public class EditMenu extends AbstractMenu {
    }
  }
}
