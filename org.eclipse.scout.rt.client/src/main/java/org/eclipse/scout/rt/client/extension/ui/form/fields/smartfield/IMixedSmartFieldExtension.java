package org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield;

import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.MixedSmartFieldChains.MixedSmartFieldConvertKeyToValueChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.MixedSmartFieldChains.MixedSmartFieldConvertValueToKeyChain;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractMixedSmartField;

public interface IMixedSmartFieldExtension<VALUE, LOOKUP_KEY, OWNER extends AbstractMixedSmartField<VALUE, LOOKUP_KEY>> extends IContentAssistFieldExtension<VALUE, LOOKUP_KEY, OWNER> {

  LOOKUP_KEY execConvertValueToKey(MixedSmartFieldConvertValueToKeyChain<VALUE, LOOKUP_KEY> chain, VALUE value);

  VALUE execConvertKeyToValue(MixedSmartFieldConvertKeyToValueChain<VALUE, LOOKUP_KEY> chain, LOOKUP_KEY key);
}
