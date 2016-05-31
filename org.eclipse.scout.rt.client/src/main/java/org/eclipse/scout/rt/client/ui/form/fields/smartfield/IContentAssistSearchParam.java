package org.eclipse.scout.rt.client.ui.form.fields.smartfield;

public interface IContentAssistSearchParam<LOOKUP_KEY> {

  String getSearchText();

  LOOKUP_KEY getParentKey();

  boolean isSelectCurrentValue();

  boolean isByParentSearch();

}
