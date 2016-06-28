package org.eclipse.scout.rt.shared.extension.dto.fixture;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.dto.FormData.SdkCommand;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.client.ui.form.fields.wrappedform.AbstractWrappedFormField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.extension.dto.fixture.WrappedFormFieldOuterForm.MainBox.StringField;
import org.eclipse.scout.rt.shared.extension.dto.fixture.WrappedFormFieldOuterForm.MainBox.WrappedFormField;

/**
 * @since 5.2
 */
@FormData(value = WrappedFormFieldOuterFormData.class, sdkCommand = SdkCommand.CREATE)
public class WrappedFormFieldOuterForm extends AbstractForm {

  public MainBox getMainBox() {
    return (MainBox) getRootGroupBox();
  }

  public WrappedFormField getWrappedFormField() {
    return getFieldByClass(WrappedFormField.class);
  }

  public StringField getStringField() {
    return getFieldByClass(StringField.class);
  }

  public class MainBox extends AbstractGroupBox {

    @Order(10)
    public class WrappedFormField extends AbstractWrappedFormField<OrigForm> {
    }

    @Order(20)
    public class StringField extends AbstractStringField {
    }
  }
}
