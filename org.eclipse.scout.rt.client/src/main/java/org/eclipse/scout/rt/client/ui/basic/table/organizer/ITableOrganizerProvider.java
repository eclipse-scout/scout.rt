package org.eclipse.scout.rt.client.ui.basic.table.organizer;

import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * Provider for {@link ITableOrganizer}.
 *
 * @since 5.2
 */
@FunctionalInterface
@ApplicationScoped
public interface ITableOrganizerProvider {

  ITableOrganizer createTableOrganizer(ITable table);

}
