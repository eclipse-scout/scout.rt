package org.eclipse.scout.rt.client.extension.ui.form.fields.datefield;

import org.eclipse.scout.rt.client.ui.form.fields.datefield.AbstractUTCDateField;

public abstract class AbstractUTCDateFieldExtension<OWNER extends AbstractUTCDateField> extends AbstractDateFieldExtension<OWNER> implements IUTCDateFieldExtension<OWNER> {

  public AbstractUTCDateFieldExtension(OWNER owner) {
    super(owner);
  }
}
