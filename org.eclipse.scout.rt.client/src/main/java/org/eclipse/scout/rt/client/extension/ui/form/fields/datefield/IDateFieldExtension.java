package org.eclipse.scout.rt.client.extension.ui.form.fields.datefield;

import java.util.Date;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IBasicFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.datefield.DateFieldChains.DateFieldShiftDateChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.datefield.DateFieldChains.DateFieldShiftTimeChain;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.AbstractDateField;

public interface IDateFieldExtension<OWNER extends AbstractDateField> extends IBasicFieldExtension<Date, OWNER> {

  void execShiftTime(DateFieldShiftTimeChain chain, int level, int value) throws ProcessingException;

  void execShiftDate(DateFieldShiftDateChain chain, int level, int value) throws ProcessingException;
}
