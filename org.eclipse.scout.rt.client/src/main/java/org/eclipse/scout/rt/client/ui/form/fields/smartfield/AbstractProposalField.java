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

import org.eclipse.scout.rt.client.ModelContextProxy;
import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;

@ClassId("1c8c645d-9e75-4bb1-9f79-c0532d2cdb72")
public abstract class AbstractProposalField<VALUE> extends AbstractSmartField<VALUE> implements IProposalField<VALUE> {

  public AbstractProposalField() {
    this(true);
  }

  public AbstractProposalField(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected ISmartFieldUIFacade<VALUE> createUIFacade() {
    return BEANS.get(ModelContextProxy.class).newProxy(new ProposalFieldUIFacade(this), ModelContext.copyCurrent());
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setMaxLength(getConfiguredMaxLength());
    setTrimText(getConfiguredTrimText());
  }

  /**
   * Configures the initial value of {@link AbstractProposalField#getMaxLength()
   * <p>
   * Subclasses can override this method
   * <p>
   * Default is 4000
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  protected int getConfiguredMaxLength() {
    return 4000;
  }

  /**
   * @return true if leading and trailing whitespace should be stripped from the entered text while validating the
   *         value. default is true.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  protected boolean getConfiguredTrimText() {
    return true;
  }

  @Override
  public String getValueAsString() {
    return (String) getValue();
  }

  @Override
  @SuppressWarnings("unchecked")
  public void setValueAsString(String value) {
    setValue((VALUE) value);
  }

  @Override
  protected String formatValueInternal(VALUE value) {
    return value != null ? value.toString() : "";
  }

  @Override
  public void setMaxLength(int maxLength) {
    boolean changed = propertySupport.setPropertyInt(PROP_MAX_LENGTH, Math.max(0, maxLength));
    if (changed && isInitialized()) {
      setValue(getValue());
    }
  }

  @Override
  public int getMaxLength() {
    return propertySupport.getPropertyInt(PROP_MAX_LENGTH);
  }

  @Override
  public void setTrimText(boolean trimText) {
    boolean changed = propertySupport.setPropertyBool(PROP_TRIM_TEXT_ON_VALIDATE, trimText);
    if (changed && isInitialized()) {
      setValue(getValue());
    }
  }

  @Override
  public boolean isTrimText() {
    return propertySupport.getPropertyBool(PROP_TRIM_TEXT_ON_VALIDATE);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected VALUE getValueFromLookupRow(ILookupRow<VALUE> row) {
    return (VALUE) row.getText();
  }

  @Override
  public IProposalFieldUIFacade<VALUE> getUIFacade() {
    return (IProposalFieldUIFacade<VALUE>) super.getUIFacade();
  }

  @Override
  @SuppressWarnings("unchecked")
  protected VALUE validateValueInternal(VALUE rawValue) {
    VALUE validValue = super.validateValueInternal(rawValue);
    if (validValue != null) {
      String stringValue = (String) validValue;
      if (isTrimText()) {
        stringValue = stringValue.trim();
      }
      if (stringValue.length() > getMaxLength()) {
        stringValue = stringValue.substring(0, getMaxLength());
      }
      validValue = (VALUE) stringValue;
    }
    return validValue;
  }

  @Override
  protected void valueChangedInternal() {
    // When a current lookup-row is available, we don't need to perform a lookup
    // Usually this happens after the user has selected a row from the proposal-chooser (table or tree).
    final ILookupRow<VALUE> currentLookupRow = getLookupRow();
    if (currentLookupRow != null) {
      installLookupRowContext(currentLookupRow);
      return;
    }

    // Other than the smart-field the proposal field does not do a lookup by key to find and set
    // a lookup row. A lookup row is never set by value.
  }
}
