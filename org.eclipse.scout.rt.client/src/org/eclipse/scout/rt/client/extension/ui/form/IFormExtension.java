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
import org.eclipse.scout.rt.shared.extension.IExtension;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;

/**
 *
 */
public interface IFormExtension<OWNER_FORM extends AbstractForm> extends IExtension<OWNER_FORM> {

  void execCloseTimer(FormCloseTimerChain chain) throws ProcessingException;

  void execInactivityTimer(FormInactivityTimerChain chain) throws ProcessingException;

  void execStored(FormStoredChain chain) throws ProcessingException;

  boolean execCheckFields(FormCheckFieldsChain chain) throws ProcessingException;

  void execResetSearchFilter(FormResetSearchFilterChain chain, SearchFilter searchFilter) throws ProcessingException;

  void execAddSearchTerms(FormAddSearchTermsChain chain, SearchFilter search);

  void execOnVetoException(FormOnVetoExceptionChain chain, VetoException e, int code) throws ProcessingException;

  void execFormActivated(FormFormActivatedChain chain) throws ProcessingException;

  void execDisposeForm(FormDisposeFormChain chain) throws ProcessingException;

  void execTimer(FormTimerChain chain, String timerId) throws ProcessingException;

  AbstractFormData execCreateFormData(FormCreateFormDataChain chain) throws ProcessingException;

  void execInitForm(FormInitFormChain chain) throws ProcessingException;

  boolean execValidate(FormValidateChain chain) throws ProcessingException;

  void execOnCloseRequest(FormOnCloseRequestChain chain, boolean kill, HashSet<Integer> enabledButtonSystemTypes) throws ProcessingException;

  void execDataChanged(FormDataChangedChain chain, Object... dataTypes) throws ProcessingException;

}
