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
