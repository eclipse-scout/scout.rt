/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.form.fields.radiobuttongroup;

import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.form.fields.IValueFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.radiobuttongroup.RadioButtonGroupChains.RadioButtonGroupFilterLookupResultChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.radiobuttongroup.RadioButtonGroupChains.RadioButtonGroupPrepareLookupChain;
import org.eclipse.scout.rt.client.ui.form.fields.radiobuttongroup.AbstractRadioButtonGroup;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;

public interface IRadioButtonGroupExtension<T, OWNER extends AbstractRadioButtonGroup<T>> extends IValueFieldExtension<T, OWNER> {

  void execPrepareLookup(RadioButtonGroupPrepareLookupChain<T> chain, ILookupCall<T> call);

  void execFilterLookupResult(RadioButtonGroupFilterLookupResultChain<T> chain, ILookupCall<T> call, List<ILookupRow<T>> result);
}
