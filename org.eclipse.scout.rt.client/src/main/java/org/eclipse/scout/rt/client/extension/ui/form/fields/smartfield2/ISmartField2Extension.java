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
package org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield2;

import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.form.fields.IValueFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield2.SmartField2Chains.SmartField2BrowseNewChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield2.SmartField2Chains.SmartField2FilterBrowseLookupResultChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield2.SmartField2Chains.SmartField2FilterKeyLookupResultChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield2.SmartField2Chains.SmartField2FilterLookupResultChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield2.SmartField2Chains.SmartField2FilterRecLookupResultChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield2.SmartField2Chains.SmartField2FilterTextLookupResultChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield2.SmartField2Chains.SmartField2PrepareBrowseLookupChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield2.SmartField2Chains.SmartField2PrepareKeyLookupChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield2.SmartField2Chains.SmartField2PrepareLookupChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield2.SmartField2Chains.SmartField2PrepareRecLookupChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield2.SmartField2Chains.SmartField2PrepareTextLookupChain;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield2.AbstractSmartField2;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;

public interface ISmartField2Extension<VALUE, OWNER extends AbstractSmartField2<VALUE>> extends IValueFieldExtension<VALUE, OWNER> {

  void execFilterBrowseLookupResult(SmartField2FilterBrowseLookupResultChain<VALUE> chain, ILookupCall<VALUE> call, List<ILookupRow<VALUE>> result);

  ILookupRow<VALUE> execBrowseNew(SmartField2BrowseNewChain<VALUE> chain, String searchText);

  void execFilterKeyLookupResult(SmartField2FilterKeyLookupResultChain<VALUE> chain, ILookupCall<VALUE> call, List<ILookupRow<VALUE>> result);

  void execPrepareLookup(SmartField2PrepareLookupChain<VALUE> chain, ILookupCall<VALUE> call);

  void execPrepareTextLookup(SmartField2PrepareTextLookupChain<VALUE> chain, ILookupCall<VALUE> call, String text);

  void execPrepareBrowseLookup(SmartField2PrepareBrowseLookupChain<VALUE> chain, ILookupCall<VALUE> call, String browseHint);

  void execFilterTextLookupResult(SmartField2FilterTextLookupResultChain<VALUE> chain, ILookupCall<VALUE> call, List<ILookupRow<VALUE>> result);

  void execPrepareRecLookup(SmartField2PrepareRecLookupChain<VALUE> chain, ILookupCall<VALUE> call, VALUE parentKey);

  void execFilterLookupResult(SmartField2FilterLookupResultChain<VALUE> chain, ILookupCall<VALUE> call, List<ILookupRow<VALUE>> result);

  void execFilterRecLookupResult(SmartField2FilterRecLookupResultChain<VALUE> chain, ILookupCall<VALUE> call, List<ILookupRow<VALUE>> result);

  void execPrepareKeyLookup(SmartField2PrepareKeyLookupChain<VALUE> chain, ILookupCall<VALUE> call, VALUE key);

}
