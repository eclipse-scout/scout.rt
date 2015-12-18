package org.eclipse.scout.rt.client.ui.basic.table;

import java.util.Map;
import java.util.Set;

/**
 *
 */
public interface ITableRowCustomValueContributor {

  void enrichCustomValues(ITableRow row, Map<String, Object> customValues);

  Set<String> getConfiguredContributedValueIds();

}
