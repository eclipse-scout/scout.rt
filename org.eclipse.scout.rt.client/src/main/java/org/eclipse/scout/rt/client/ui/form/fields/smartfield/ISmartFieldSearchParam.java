package org.eclipse.scout.rt.client.ui.form.fields.smartfield;

public interface ISmartFieldSearchParam<LOOKUP_KEY> {

  /**
   * @return The original search text as typed in the UI
   */
  String getSearchText();

  /**
   * @return The search query used to perform a lookup (not necessary identical to the original search text)
   */
  String getSearchQuery();

  LOOKUP_KEY getParentKey();

  boolean isSelectCurrentValue();

  boolean isByParentSearch();

  String getWildcard();

}
