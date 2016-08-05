package org.eclipse.scout.rt.client.extension.ui.outline.pages.fixture;

import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractLongColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithTable;
import org.eclipse.scout.rt.platform.Order;

/**
 * @since 6.0
 */
public class AbstractPersonTablePage<T extends AbstractPersonTablePage<T>.Table> extends AbstractPageWithTable<T> {

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
