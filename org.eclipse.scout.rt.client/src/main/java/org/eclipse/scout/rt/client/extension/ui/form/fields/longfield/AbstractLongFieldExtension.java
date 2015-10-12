package org.eclipse.scout.rt.client.extension.ui.form.fields.longfield;

import org.eclipse.scout.rt.client.extension.ui.form.fields.numberfield.AbstractNumberFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.longfield.AbstractLongField;

public abstract class AbstractLongFieldExtension<OWNER extends AbstractLongField> extends AbstractNumberFieldExtension<Long, OWNER> implements ILongFieldExtension<OWNER> {

  public AbstractLongFieldExtension(OWNER owner) {
    super(owner);
  }
}
