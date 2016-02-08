package org.eclipse.scout.rt.client.ui.basic.table.organizer;

import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;

/**
 * This interface provides the state for the add/remove and modify buttons in the table-header-menu.
 *
 * @since 5.2
 */
public interface ITableOrganizer {

  boolean isColumnAddable();

  boolean isColumnRemovable(IColumn column);

  boolean isColumnModifiable(IColumn column);

  void addColumn(IColumn<?> column);

  void removeColumn(IColumn column);

  void modifyColumn(IColumn column);

}
