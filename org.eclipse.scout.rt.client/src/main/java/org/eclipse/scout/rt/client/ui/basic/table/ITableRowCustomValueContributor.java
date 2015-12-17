package org.eclipse.scout.rt.client.ui.basic.table;

import java.util.Map;

/**
 *
 */
public interface ITableRowCustomValueContributor {
  String GEO_LOCATION_CUSTOM_VALUES_ID = "geoLocationCustomValuesId";

  void enrichCustomValues(ITableRow row, Map<String, Object> customValues);

}
