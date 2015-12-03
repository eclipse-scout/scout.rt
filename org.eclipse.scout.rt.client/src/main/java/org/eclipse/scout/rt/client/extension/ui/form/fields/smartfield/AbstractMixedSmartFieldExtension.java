/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield;

import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.MixedSmartFieldChains.MixedSmartFieldConvertKeyToValueChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.MixedSmartFieldChains.MixedSmartFieldConvertValueToKeyChain;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractMixedSmartField;

public abstract class AbstractMixedSmartFieldExtension<VALUE, LOOKUP_KEY, OWNER extends AbstractMixedSmartField<VALUE, LOOKUP_KEY>> extends AbstractContentAssistFieldExtension<VALUE, LOOKUP_KEY, OWNER>
    implements IMixedSmartFieldExtension<VALUE, LOOKUP_KEY, OWNER> {

  public AbstractMixedSmartFieldExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public LOOKUP_KEY execConvertValueToKey(MixedSmartFieldConvertValueToKeyChain<VALUE, LOOKUP_KEY> chain, VALUE value) {
    return chain.execConvertValueToKey(value);
  }

  @Override
  public VALUE execConvertKeyToValue(MixedSmartFieldConvertKeyToValueChain<VALUE, LOOKUP_KEY> chain, LOOKUP_KEY key) {
    return chain.execConvertKeyToValue(key);
  }
}
