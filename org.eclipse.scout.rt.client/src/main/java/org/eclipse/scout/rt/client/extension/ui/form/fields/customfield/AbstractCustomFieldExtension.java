package org.eclipse.scout.rt.client.extension.ui.form.fields.customfield;

import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractFormFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.customfield.AbstractCustomField;

public abstract class AbstractCustomFieldExtension<OWNER extends AbstractCustomField> extends AbstractFormFieldExtension<OWNER> implements ICustomFieldExtension<OWNER> {

  public AbstractCustomFieldExtension(OWNER owner) {
    super(owner);
  }
}
