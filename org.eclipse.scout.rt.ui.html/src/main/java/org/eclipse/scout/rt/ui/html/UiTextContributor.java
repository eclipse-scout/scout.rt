package org.eclipse.scout.rt.ui.html;

import java.util.Arrays;
import java.util.Set;

public class UiTextContributor implements IUiTextContributor {

  @Override
  public void contributeUiTextKeys(Set<String> textKeys) {
    textKeys.addAll(Arrays.asList(
        // From org.eclipse.scout.rt.client
        "ResetTableColumns",
        "ColumnSorting",
        "Column",
        // From org.eclipse.scout.rt.ui.html
        "LoadOptions_",
        "NoOptions",
        "OneOption",
        "NumOptions",
        "InvalidDateFormat",
        "FilterBy_",
        "SearchFor_",
        "TableRowCount0",
        "TableRowCount1",
        "TableRowCount",
        "NumRowsSelected",
        "SelectAll",
        "SelectNone",
        "NumRowsFiltered",
        "NumRowsFilteredBy",
        "RemoveFilter",
        "NumRowsLoaded",
        "ReloadData",
        "Reload",
        "showEveryDate",
        "groupedByWeekday",
        "groupedByMonth",
        "groupedByYear",
        "Count",
        "ConnectionInterrupted",
        "ConnectionReestablished",
        "Reconnecting_",
        "ServerError",
        "SessionTimeout",
        "SessionExpiredMsg",
        "Move",
        "toBegin",
        "forward",
        "backward",
        "toEnd",
        "ascending",
        "descending",
        "ascendingAdditionally",
        "descendingAdditionally",
        "Sum",
        "overEverything",
        "grouped",
        "ColorCells",
        "fromRedToGreen",
        "fromGreenToRed",
        "withBarGraph",
        "remove",
        "add",
        "FilterBy",
        "Reconnecting",
        "Show",
        "Up",
        "Back",
        "Continue",
        "Ignore",
        "UiProcessingErrorTitle",
        "UiProcessingErrorText",
        "UiProcessingErrorAction"
        ));
  }
}
