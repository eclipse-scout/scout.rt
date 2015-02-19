package org.eclipse.scout.rt.client.extension.ui.form.fields.datefield;

import java.util.Date;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractBasicFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.datefield.DateFieldChains.DateFieldShiftDateChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.datefield.DateFieldChains.DateFieldShiftTimeChain;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.AbstractDateField;

public abstract class AbstractDateFieldExtension<OWNER extends AbstractDateField> extends AbstractBasicFieldExtension<Date, OWNER> implements IDateFieldExtension<OWNER> {

  public AbstractDateFieldExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execShiftTime(DateFieldShiftTimeChain chain, int level, int value) throws ProcessingException {
    chain.execShiftTime(level, value);
  }

  @Override
  public void execShiftDate(DateFieldShiftDateChain chain, int level, int value) throws ProcessingException {
    chain.execShiftDate(level, value);
  }
}
