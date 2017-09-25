package org.eclipse.scout.rt.client.ui.form.fields.smartfield.result;

import java.util.List;

import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;

public interface ISmartFieldResult<LOOKUP_KEY> {

  IQueryParam getQueryParam();

  Throwable getException();

  List<ILookupRow<LOOKUP_KEY>> getLookupRows();

}
