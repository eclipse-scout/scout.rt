package org.eclipse.scout.rt.client.ui.form.fields.smartfield2;

import org.eclipse.scout.rt.client.ui.form.fields.smartfield.IContentAssistField;

/**
 * Generic type V: value of the SmartField2, which is also the key used in lookup-rows.
 */
public interface ISmartField2<VALUE> extends IContentAssistField<VALUE, VALUE> {

  String PROP_RESULT = "result";

  void query(String queryText, Object filterKey);

  SmartField2Result getResult();

}
