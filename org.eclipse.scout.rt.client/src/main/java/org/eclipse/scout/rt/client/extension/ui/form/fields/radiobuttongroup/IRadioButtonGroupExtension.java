package org.eclipse.scout.rt.client.extension.ui.form.fields.radiobuttongroup;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IValueFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.radiobuttongroup.RadioButtonGroupChains.RadioButtonGroupFilterLookupResultChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.radiobuttongroup.RadioButtonGroupChains.RadioButtonGroupPrepareLookupChain;
import org.eclipse.scout.rt.client.ui.form.fields.radiobuttongroup.AbstractRadioButtonGroup;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;

public interface IRadioButtonGroupExtension<T, OWNER extends AbstractRadioButtonGroup<T>> extends IValueFieldExtension<T, OWNER> {

  void execPrepareLookup(RadioButtonGroupPrepareLookupChain<T> chain, ILookupCall<T> call);

  void execFilterLookupResult(RadioButtonGroupFilterLookupResultChain<T> chain, ILookupCall<T> call, List<ILookupRow<T>> result) throws ProcessingException;
}
