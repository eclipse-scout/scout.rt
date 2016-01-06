package org.eclipse.scout.rt.client.ui.basic.table.organizer;

import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.TableOrganizer;

/**
 * Provides an instance of {@link TableOrganizer}.
 *
 * @since 5.2
 */
public class TableOrganizerProvider implements ITableOrganizerProvider {

  @Override
  public ITableOrganizer createTableOrganizer(ITable table) {
    return new TableOrganizer(table);
  }

}
