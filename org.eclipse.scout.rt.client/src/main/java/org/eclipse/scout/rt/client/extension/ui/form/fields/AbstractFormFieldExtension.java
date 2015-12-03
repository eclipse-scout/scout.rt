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
package org.eclipse.scout.rt.client.extension.ui.form.fields;

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
import org.eclipse.scout.rt.shared.extension.AbstractExtension;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;

public abstract class AbstractFormFieldExtension<OWNER_FIELD extends AbstractFormField> extends AbstractExtension<OWNER_FIELD>
    implements IFormFieldExtension<OWNER_FIELD> {

  public AbstractFormFieldExtension(OWNER_FIELD owner) {
    super(owner);
  }

  @Override
  public void execDataChanged(FormFieldDataChangedChain chain, Object... dataTypes) {
    chain.execDataChanged(dataTypes);
  }

  @Override
  public void execAddSearchTerms(FormFieldAddSearchTermsChain chain, SearchFilter search) {
    chain.execAddSearchTerms(search);
  }

  @Override
  public void execChangedMasterValue(FormFieldChangedMasterValueChain chain, Object newMasterValue) {
    chain.execChangedMasterValue(newMasterValue);
  }

  @Override
  public void execDisposeField(FormFieldDisposeFieldChain chain) {
    chain.execDisposeField();
  }

  @Override
  public void execInitField(FormFieldInitFieldChain chain) {
    chain.execInitField();
  }

  @Override
  public boolean execCalculateVisible(FormFieldCalculateVisibleChain chain) {
    return chain.execCalculateVisible();
  }

  @Override
  public void execMarkSaved(FormFieldMarkSavedChain chain) {
    chain.execMarkSaved();
  }

  @Override
  public boolean execIsEmpty(FormFieldIsEmptyChain chain) {
    return chain.execIsEmpty();
  }

  @Override
  public boolean execIsSaveNeeded(FormFieldIsSaveNeededChain chain) {
    return chain.execIsSaveNeeded();
  }
}
