package org.eclipse.scout.rt.client.extension.ui.form.fields.radiobuttongroup;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractValueFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.radiobuttongroup.RadioButtonGroupChains.RadioButtonGroupFilterLookupResultChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.radiobuttongroup.RadioButtonGroupChains.RadioButtonGroupPrepareLookupChain;
import org.eclipse.scout.rt.client.ui.form.fields.radiobuttongroup.AbstractRadioButtonGroup;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;

public abstract class AbstractRadioButtonGroupExtension<T, OWNER extends AbstractRadioButtonGroup<T>> extends AbstractValueFieldExtension<T, OWNER> implements IRadioButtonGroupExtension<T, OWNER> {

  public AbstractRadioButtonGroupExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execPrepareLookup(RadioButtonGroupPrepareLookupChain<T> chain, ILookupCall<T> call) {
    chain.execPrepareLookup(call);
  }

  @Override
  public void execFilterLookupResult(RadioButtonGroupFilterLookupResultChain<T> chain, ILookupCall<T> call, List<ILookupRow<T>> result) throws ProcessingException {
    chain.execFilterLookupResult(call, result);
  }
}
