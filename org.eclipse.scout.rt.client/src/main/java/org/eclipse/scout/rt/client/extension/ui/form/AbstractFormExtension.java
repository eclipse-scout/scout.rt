/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.extension.ui.form;

import java.util.HashSet;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.exception.VetoException;
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
import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import org.eclipse.scout.rt.shared.extension.AbstractExtension;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;

public abstract class AbstractFormExtension<FORM extends AbstractForm> extends AbstractExtension<FORM> implements IFormExtension<FORM> {

  public AbstractFormExtension(FORM ownerForm) {
    super(ownerForm);
  }

  @Override
  public void execCloseTimer(FormCloseTimerChain chain) throws ProcessingException {
    chain.execCloseTimer();
  }

  @Override
  public void execInactivityTimer(FormInactivityTimerChain chain) throws ProcessingException {
    chain.execInactivityTimer();
  }

  @Override
  public void execStored(FormStoredChain chain) throws ProcessingException {
    chain.execStored();
  }

  @Override
  public boolean execCheckFields(FormCheckFieldsChain chain) throws ProcessingException {
    return chain.execCheckFields();
  }

  @Override
  public void execResetSearchFilter(FormResetSearchFilterChain chain, SearchFilter searchFilter) throws ProcessingException {
    chain.execResetSearchFilter(searchFilter);
  }

  @Override
  public void execAddSearchTerms(FormAddSearchTermsChain chain, SearchFilter search) {
    chain.execAddSearchTerms(search);
  }

  @Override
  public void execOnVetoException(FormOnVetoExceptionChain chain, VetoException e, int code) throws ProcessingException {
    chain.execOnVetoException(e, code);
  }

  @Override
  public void execFormActivated(FormFormActivatedChain chain) throws ProcessingException {
    chain.execFormActivated();
  }

  @Override
  public void execDisposeForm(FormDisposeFormChain chain) throws ProcessingException {
    chain.execDisposeForm();
  }

  @Override
  public void execTimer(FormTimerChain chain, String timerId) throws ProcessingException {
    chain.execTimer(timerId);
  }

  @Override
  public AbstractFormData execCreateFormData(FormCreateFormDataChain chain) throws ProcessingException {
    return chain.execCreateFormData();
  }

  @Override
  public void execInitForm(FormInitFormChain chain) throws ProcessingException {
    chain.execInitForm();
  }

  @Override
  public boolean execValidate(FormValidateChain chain) throws ProcessingException {
    return chain.execValidate();
  }

  @Override
  public void execOnCloseRequest(FormOnCloseRequestChain chain, boolean kill, HashSet<Integer> enabledButtonSystemTypes) throws ProcessingException {
    chain.execOnCloseRequest(kill, enabledButtonSystemTypes);
  }

  @Override
  public void execDataChanged(FormDataChangedChain chain, Object... dataTypes) throws ProcessingException {
    chain.execDataChanged(dataTypes);
  }

}
