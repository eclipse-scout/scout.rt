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
package org.eclipse.scout.rt.client.extension.ui.form.fields;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.FormFieldChains.FormFieldAddSearchTermsChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.FormFieldChains.FormFieldCalculateVisibleChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.FormFieldChains.FormFieldChangedMasterValueChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.FormFieldChains.FormFieldDataChangedChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.FormFieldChains.FormFieldDisposeFieldChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.FormFieldChains.FormFieldInitFieldChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.FormFieldChains.FormFieldIsEmptyChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.FormFieldChains.FormFieldIsSaveNeededChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.FormFieldChains.FormFieldMarkSavedChain;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.shared.extension.IExtension;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;

/**
 *
 */
public interface IFormFieldExtension<OWNER extends AbstractFormField> extends IExtension<OWNER> {

  void execDataChanged(FormFieldDataChangedChain chain, Object... dataTypes) throws ProcessingException;

  void execAddSearchTerms(FormFieldAddSearchTermsChain chain, SearchFilter search);

  void execChangedMasterValue(FormFieldChangedMasterValueChain chain, Object newMasterValue) throws ProcessingException;

  void execDisposeField(FormFieldDisposeFieldChain chain) throws ProcessingException;

  void execInitField(FormFieldInitFieldChain chain) throws ProcessingException;

  boolean execCalculateVisible(FormFieldCalculateVisibleChain chain);

  void execMarkSaved(FormFieldMarkSavedChain chain) throws ProcessingException;

  boolean execIsEmpty(FormFieldIsEmptyChain chain) throws ProcessingException;

  boolean execIsSaveNeeded(FormFieldIsSaveNeededChain chain) throws ProcessingException;
}
