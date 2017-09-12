package org.eclipse.scout.rt.client.ui.basic.table.organizer;

import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * Provides an instance of {@link IOrganizeColumnsForm}.
 *
 * @since 5.2
 */
@FunctionalInterface
@ApplicationScoped
public interface IOrganizeColumnsFormProvider {

  IOrganizeColumnsForm createOrganizeColumnsForm(ITable table);

}
