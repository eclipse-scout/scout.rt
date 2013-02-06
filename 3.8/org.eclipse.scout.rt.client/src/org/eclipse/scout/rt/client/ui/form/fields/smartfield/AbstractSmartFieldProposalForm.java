/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields.smartfield;

import org.eclipse.scout.commons.annotations.ConfigPropertyValue;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;

public abstract class AbstractSmartFieldProposalForm extends AbstractForm implements ISmartFieldProposalForm {

  private final ISmartField<?> m_smartField;

  public AbstractSmartFieldProposalForm(ISmartField<?> smartField) throws ProcessingException {
    super(false);
    m_smartField = smartField;
    callInitializer();
  }

  @ConfigPropertyValue("false")
  @Override
  protected boolean getConfiguredAskIfNeedSave() {
    return false;
  }

  @Override
  protected boolean getConfiguredModal() {
    return false;
  }

  @Override
  protected int getConfiguredDisplayHint() {
    return DISPLAY_HINT_VIEW;
  }

  @Override
  public ISmartField<? extends Object> getSmartField() {
    return m_smartField;
  }

  @Override
  public String getSearchText() {
    return propertySupport.getPropertyString(PROP_SEARCH_TEXT);
  }

  @Override
  public void setSearchText(String text) {
    propertySupport.setPropertyString(PROP_SEARCH_TEXT, text);
  }

}
