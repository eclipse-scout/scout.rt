/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.smartfield;

import org.eclipse.scout.rt.client.ModelContextProxy;
import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.util.StringUtility;
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
    setTrimText(getConfiguredTrimText());
  }

  @Override
  protected int getConfiguredMaxLength() {
    return 4000;
  }

  /**
   * @return true if leading and trailing whitespace should be stripped from the entered text while validating the
   * value. default is true.
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
  public void setTrimText(boolean trimText) {
    boolean changed = propertySupport.setPropertyBool(PROP_TRIM_TEXT_ON_VALIDATE, trimText);
    if (trimText && changed && isInitConfigDone()) {
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
        if (isTrimText()) { // trim again
          stringValue = stringValue.trim();
        }
      }
      if (!isMultilineText()) {
        // omit leading and trailing newlines
        stringValue = StringUtility.trimNewLines(stringValue);
        // replace newlines by spaces
        stringValue = stringValue.replaceAll("\r\n", " ").replaceAll("[\r\n]", " ");
      }
      validValue = (VALUE) StringUtility.nullIfEmpty(stringValue);
    }
    return validValue;
  }

  @Override
  protected void valueChangedInternal() {
    // NOP
  }
}
