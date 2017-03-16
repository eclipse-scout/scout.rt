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
package org.eclipse.scout.rt.client.extension.ui.form;

import java.util.Set;

import org.eclipse.scout.rt.client.extension.ui.form.FormChains.FormAddSearchTermsChain;
import org.eclipse.scout.rt.client.extension.ui.form.FormChains.FormCheckFieldsChain;
import org.eclipse.scout.rt.client.extension.ui.form.FormChains.FormCloseTimerChain;
import org.eclipse.scout.rt.client.extension.ui.form.FormChains.FormCreateFormDataChain;
import org.eclipse.scout.rt.client.extension.ui.form.FormChains.FormDataChangedChain;
import org.eclipse.scout.rt.client.extension.ui.form.FormChains.FormDisposeFormChain;
import org.eclipse.scout.rt.client.extension.ui.form.FormChains.FormFormActivatedChain;
import org.eclipse.scout.rt.client.extension.ui.form.FormChains.FormInactivityTimerChain;
import org.eclipse.scout.rt.client.extension.ui.form.FormChains.FormInitFormChain;
import org.eclipse.scout.rt.client.extension.ui.form.FormChains.FormOnCloseRequestChain;
import org.eclipse.scout.rt.client.extension.ui.form.FormChains.FormOnVetoExceptionChain;
import org.eclipse.scout.rt.client.extension.ui.form.FormChains.FormResetSearchFilterChain;
import org.eclipse.scout.rt.client.extension.ui.form.FormChains.FormStoredChain;
import org.eclipse.scout.rt.client.extension.ui.form.FormChains.FormTimerChain;
import org.eclipse.scout.rt.client.extension.ui.form.FormChains.FormValidateChain;
import org.eclipse.scout.rt.client.extension.ui.form.FormChains.IsSaveNeededFieldsChain;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import org.eclipse.scout.rt.shared.extension.IExtension;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;

public interface IFormExtension<OWNER_FORM extends AbstractForm> extends IExtension<OWNER_FORM> {

  void execCloseTimer(FormCloseTimerChain chain);

  void execInactivityTimer(FormInactivityTimerChain chain);

  void execStored(FormStoredChain chain);

  boolean execIsSaveNeeded(IsSaveNeededFieldsChain chain);

  boolean execCheckFields(FormCheckFieldsChain chain);

  void execResetSearchFilter(FormResetSearchFilterChain chain, SearchFilter searchFilter);

  void execAddSearchTerms(FormAddSearchTermsChain chain, SearchFilter search);

  void execOnVetoException(FormOnVetoExceptionChain chain, VetoException e, int code);

  void execFormActivated(FormFormActivatedChain chain);

  void execDisposeForm(FormDisposeFormChain chain);

  void execTimer(FormTimerChain chain, String timerId);

  AbstractFormData execCreateFormData(FormCreateFormDataChain chain);

  void execInitForm(FormInitFormChain chain);

  boolean execValidate(FormValidateChain chain);

  void execOnCloseRequest(FormOnCloseRequestChain chain, boolean kill, Set<Integer> enabledButtonSystemTypes);

  void execDataChanged(FormDataChangedChain chain, Object... dataTypes);

}
