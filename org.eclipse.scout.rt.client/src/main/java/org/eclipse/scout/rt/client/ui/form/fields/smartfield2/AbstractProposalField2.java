package org.eclipse.scout.rt.client.ui.form.fields.smartfield2;

import org.eclipse.scout.rt.platform.classid.ClassId;

@ClassId("1c8c645d-9e75-4bb1-9f79-c0532d2cdb72")
public abstract class AbstractProposalField2<VALUE> extends AbstractSmartField2<VALUE> implements IProposalField2<VALUE> {

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

}
