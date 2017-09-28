/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield;

import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractValueFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.SmartFieldChains.SmartFieldFilterBrowseLookupResultChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.SmartFieldChains.SmartFieldFilterKeyLookupResultChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.SmartFieldChains.SmartFieldFilterLookupResultChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.SmartFieldChains.SmartFieldFilterRecLookupResultChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.SmartFieldChains.SmartFieldFilterTextLookupResultChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.SmartFieldChains.SmartFieldPrepareBrowseLookupChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.SmartFieldChains.SmartFieldPrepareKeyLookupChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.SmartFieldChains.SmartFieldPrepareLookupChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.SmartFieldChains.SmartFieldPrepareRecLookupChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.SmartFieldChains.SmartFieldPrepareTextLookupChain;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractSmartField;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;

public abstract class AbstractSmartFieldExtension<VALUE, OWNER extends AbstractSmartField<VALUE>> extends AbstractValueFieldExtension<VALUE, OWNER> implements ISmartFieldExtension<VALUE, OWNER> {

  public AbstractSmartFieldExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execFilterBrowseLookupResult(SmartFieldFilterBrowseLookupResultChain<VALUE> chain, ILookupCall<VALUE> call, List<ILookupRow<VALUE>> result) {
    chain.execFilterBrowseLookupResult(call, result);
  }

  @Override
  public void execFilterKeyLookupResult(SmartFieldFilterKeyLookupResultChain<VALUE> chain, ILookupCall<VALUE> call, List<ILookupRow<VALUE>> result) {
    chain.execFilterKeyLookupResult(call, result);
  }

  @Override
  public void execPrepareLookup(SmartFieldPrepareLookupChain<VALUE> chain, ILookupCall<VALUE> call) {
    chain.execPrepareLookup(call);
  }

  @Override
  public void execPrepareTextLookup(SmartFieldPrepareTextLookupChain<VALUE> chain, ILookupCall<VALUE> call, String text) {
    chain.execPrepareTextLookup(call, text);
  }

  @Override
  public void execPrepareBrowseLookup(SmartFieldPrepareBrowseLookupChain<VALUE> chain, ILookupCall<VALUE> call) {
    chain.execPrepareBrowseLookup(call);
  }

  @Override
  public void execFilterTextLookupResult(SmartFieldFilterTextLookupResultChain<VALUE> chain, ILookupCall<VALUE> call, List<ILookupRow<VALUE>> result) {
    chain.execFilterTextLookupResult(call, result);
  }

  @Override
  public void execPrepareRecLookup(SmartFieldPrepareRecLookupChain<VALUE> chain, ILookupCall<VALUE> call, VALUE parentKey) {
    chain.execPrepareRecLookup(call, parentKey);
  }

  @Override
  public void execFilterLookupResult(SmartFieldFilterLookupResultChain<VALUE> chain, ILookupCall<VALUE> call, List<ILookupRow<VALUE>> result) {
    chain.execFilterLookupResult(call, result);
  }

  @Override
  public void execFilterRecLookupResult(SmartFieldFilterRecLookupResultChain<VALUE> chain, ILookupCall<VALUE> call, List<ILookupRow<VALUE>> result) {
    chain.execFilterRecLookupResult(call, result);
  }

  @Override
  public void execPrepareKeyLookup(SmartFieldPrepareKeyLookupChain<VALUE> chain, ILookupCall<VALUE> call, VALUE key) {
    chain.execPrepareKeyLookup(call, key);
  }
}
