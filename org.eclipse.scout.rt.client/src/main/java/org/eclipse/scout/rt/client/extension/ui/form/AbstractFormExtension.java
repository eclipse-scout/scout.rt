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
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import org.eclipse.scout.rt.shared.extension.AbstractExtension;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;

public abstract class AbstractFormExtension<FORM extends AbstractForm> extends AbstractExtension<FORM> implements IFormExtension<FORM> {

  public AbstractFormExtension(FORM ownerForm) {
    super(ownerForm);
  }

  @Override
  public void execCloseTimer(FormCloseTimerChain chain) {
    chain.execCloseTimer();
  }

  @Override
  public void execInactivityTimer(FormInactivityTimerChain chain) {
    chain.execInactivityTimer();
  }

  @Override
  public void execStored(FormStoredChain chain) {
    chain.execStored();
  }

  @Override
  public boolean execCheckFields(FormCheckFieldsChain chain) {
    return chain.execCheckFields();
  }

  @Override
  public void execResetSearchFilter(FormResetSearchFilterChain chain, SearchFilter searchFilter) {
    chain.execResetSearchFilter(searchFilter);
  }

  @Override
  public void execAddSearchTerms(FormAddSearchTermsChain chain, SearchFilter search) {
    chain.execAddSearchTerms(search);
  }

  @Override
  public void execOnVetoException(FormOnVetoExceptionChain chain, VetoException e, int code) {
    chain.execOnVetoException(e, code);
  }

  @Override
  public void execFormActivated(FormFormActivatedChain chain) {
    chain.execFormActivated();
  }

  @Override
  public void execDisposeForm(FormDisposeFormChain chain) {
    chain.execDisposeForm();
  }

  @Override
  public void execTimer(FormTimerChain chain, String timerId) {
    chain.execTimer(timerId);
  }

  @Override
  public AbstractFormData execCreateFormData(FormCreateFormDataChain chain) {
    return chain.execCreateFormData();
  }

  @Override
  public void execInitForm(FormInitFormChain chain) {
    chain.execInitForm();
  }

  @Override
  public boolean execValidate(FormValidateChain chain) {
    return chain.execValidate();
  }

  @Override
  public void execOnCloseRequest(FormOnCloseRequestChain chain, boolean kill, Set<Integer> enabledButtonSystemTypes) {
    chain.execOnCloseRequest(kill, enabledButtonSystemTypes);
  }

  @Override
  public void execDataChanged(FormDataChangedChain chain, Object... dataTypes) {
    chain.execDataChanged(dataTypes);
  }

}
