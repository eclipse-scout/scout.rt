package org.eclipse.scout.rt.client.ui.basic.table.controls;

import org.eclipse.scout.rt.client.ui.form.IForm;

public interface IFormTableControl extends ITableControl {

  String PROP_FORM = "form";

  void setForm(IForm form);

  IForm getForm();
}
