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
package org.eclipse.scout.rt.client.ui.form.fields.smartfield2;

import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractProposalField;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;

@ClassId("1c8c645d-9e75-4bb1-9f79-c0532d2cdb72")
public abstract class AbstractProposalField2<VALUE> extends AbstractSmartField2<VALUE> implements IProposalField2<VALUE> {

  @Override
  protected void initConfig() {
    super.initConfig();
    setMaxLength(getConfiguredMaxLength());
    setTrimText(getConfiguredTrimText());
    setAutoCloseChooser(getConfiguredAutoCloseChooser());
  }

  /**
   * Configures whether the proposal chooser should automatically be closed when there are no proposals available.
   * <p>
   * Subclasses can override this method. Default is true.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  protected boolean getConfiguredAutoCloseChooser() { // FIXME [awe] 7.0 - SF2: remove this property!?
    return true;
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
  public void setAutoCloseChooser(boolean autoCloseChooser) {
    propertySupport.setPropertyBool(PROP_AUTO_CLOSE_CHOOSER, autoCloseChooser);
  }

  @Override
  public boolean isAutoCloseChooser() {
    return propertySupport.getPropertyBool(PROP_AUTO_CLOSE_CHOOSER);
  }

  @Override
  public void setMaxLength(int maxLength) {
    propertySupport.setPropertyInt(PROP_MAX_LENGTH, maxLength);
  }

  @Override
  public int getMaxLength() {
    return propertySupport.getPropertyInt(PROP_MAX_LENGTH);
  }

  @Override
  public void setTrimText(boolean trimText) {
    propertySupport.setPropertyBool(PROP_TRIM_TEXT_ON_VALIDATE, trimText);
  }

  @Override
  public boolean isTrimText() {
    return propertySupport.getPropertyBool(PROP_TRIM_TEXT_ON_VALIDATE);
  }

  @Override
  protected boolean lookupRowMatchesValue(ILookupRow<VALUE> lookupRow, VALUE value) {
    return ObjectUtility.equals(lookupRow.getText(), value);
  }

}
