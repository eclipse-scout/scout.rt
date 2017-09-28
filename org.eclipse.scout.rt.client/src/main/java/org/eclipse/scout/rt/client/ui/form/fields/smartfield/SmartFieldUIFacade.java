/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields.smartfield;

import org.eclipse.scout.rt.client.ui.form.fields.ParsingFailedStatus;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.util.TriState;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;

public class SmartFieldUIFacade<VALUE> implements ISmartFieldUIFacade<VALUE> {

  private final AbstractSmartField<VALUE> m_smartField;

  public SmartFieldUIFacade(AbstractSmartField<VALUE> smartField) {
    this.m_smartField = smartField;
  }

  @Override
  public void setDisplayTextFromUI(String text) {
    m_smartField.setDisplayText(text);
  }

  @Override
  public void setErrorStatusFromUI(IStatus errorStatus) {
    m_smartField.removeErrorStatus(ParsingFailedStatus.class);
    if (errorStatus != null) {
      m_smartField.addErrorStatus(errorStatus);
    }
  }

  @Override
  public void setActiveFilterFromUI(TriState activeFilter) {
    m_smartField.setActiveFilter(activeFilter);
  }

  @Override
  public void setLookupRowFromUI(ILookupRow<VALUE> lookupRow) {
    m_smartField.setValueByLookupRow(lookupRow);
  }

  @Override
  public void setValueFromUI(VALUE value) {
    m_smartField.setValue(value);
  }

  public AbstractSmartField<VALUE> getSmartField() {
    return m_smartField;
  }

}
