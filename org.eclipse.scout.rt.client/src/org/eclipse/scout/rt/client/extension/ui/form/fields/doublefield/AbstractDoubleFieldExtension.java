package org.eclipse.scout.rt.client.extension.ui.form.fields.doublefield;

import org.eclipse.scout.rt.client.extension.ui.form.fields.decimalfield.AbstractDecimalFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.doublefield.AbstractDoubleField;

@SuppressWarnings("deprecation")
public abstract class AbstractDoubleFieldExtension<OWNER extends AbstractDoubleField> extends AbstractDecimalFieldExtension<Double, OWNER> implements IDoubleFieldExtension<OWNER> {

  public AbstractDoubleFieldExtension(OWNER owner) {
    super(owner);
  }
}
