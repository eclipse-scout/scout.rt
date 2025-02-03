/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield;

import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.form.fields.IValueFieldExtension;
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

public interface ISmartFieldExtension<VALUE, OWNER extends AbstractSmartField<VALUE>> extends IValueFieldExtension<VALUE, OWNER> {

  void execFilterBrowseLookupResult(SmartFieldFilterBrowseLookupResultChain<VALUE> chain, ILookupCall<VALUE> call, List<ILookupRow<VALUE>> result);

  void execFilterKeyLookupResult(SmartFieldFilterKeyLookupResultChain<VALUE> chain, ILookupCall<VALUE> call, List<ILookupRow<VALUE>> result);

  void execPrepareLookup(SmartFieldPrepareLookupChain<VALUE> chain, ILookupCall<VALUE> call);

  void execPrepareTextLookup(SmartFieldPrepareTextLookupChain<VALUE> chain, ILookupCall<VALUE> call, String text);

  void execPrepareBrowseLookup(SmartFieldPrepareBrowseLookupChain<VALUE> chain, ILookupCall<VALUE> call);

  void execFilterTextLookupResult(SmartFieldFilterTextLookupResultChain<VALUE> chain, ILookupCall<VALUE> call, List<ILookupRow<VALUE>> result);

  void execPrepareRecLookup(SmartFieldPrepareRecLookupChain<VALUE> chain, ILookupCall<VALUE> call, VALUE parentKey);

  void execFilterLookupResult(SmartFieldFilterLookupResultChain<VALUE> chain, ILookupCall<VALUE> call, List<ILookupRow<VALUE>> result);

  void execFilterRecLookupResult(SmartFieldFilterRecLookupResultChain<VALUE> chain, ILookupCall<VALUE> call, List<ILookupRow<VALUE>> result);

  void execPrepareKeyLookup(SmartFieldPrepareKeyLookupChain<VALUE> chain, ILookupCall<VALUE> call, VALUE key);
}
