package org.eclipse.scout.rt.client.extension.ui.form.fields.datefield;

import java.util.Date;

import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractBasicFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.ValueFieldChains.ValueFieldParseValueChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.datefield.DateFieldChains.DateFieldShiftDateChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.datefield.DateFieldChains.DateFieldShiftTimeChain;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.AbstractDateField;

public abstract class AbstractDateFieldExtension<OWNER extends AbstractDateField> extends AbstractBasicFieldExtension<Date, OWNER> implements IDateFieldExtension<OWNER> {

  public AbstractDateFieldExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execShiftTime(DateFieldShiftTimeChain chain, int level, int value) {
    chain.execShiftTime(level, value);
  }

  @Override
  public void execShiftDate(DateFieldShiftDateChain chain, int level, int value) {
    chain.execShiftDate(level, value);
  }

  /**
   * parsing is not longer supported on model. Client parses date and sets value;
   */
  @Override
  public final Date execParseValue(ValueFieldParseValueChain<Date> chain, String text) {
    return null;
  }
}
