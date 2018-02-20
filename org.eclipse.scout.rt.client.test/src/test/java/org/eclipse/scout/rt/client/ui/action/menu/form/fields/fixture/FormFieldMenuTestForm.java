package org.eclipse.scout.rt.client.ui.action.menu.form.fields.fixture;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.dto.FormData.SdkCommand;
import org.eclipse.scout.rt.client.ui.action.menu.form.fields.AbstractFormFieldMenu;
import org.eclipse.scout.rt.client.ui.action.menu.form.fields.fixture.FormFieldMenuTestForm.InnerForm.MainBox.TestStringField;
import org.eclipse.scout.rt.client.ui.action.menu.form.fields.fixture.FormFieldMenuTestForm.MainBox.FormFieldMenu.BigDecimalInMenuField;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.bigdecimalfield.AbstractBigDecimalField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.client.ui.form.fields.wrappedform.AbstractWrappedFormField;
import org.eclipse.scout.rt.platform.Order;

@FormData(value = FormFieldMenuTestFormData.class, sdkCommand = SdkCommand.CREATE)
public class FormFieldMenuTestForm extends AbstractForm {

  public TestStringField getTestStringField() {
    return getFieldByClass(TestStringField.class);
  }

  public BigDecimalInMenuField getBigDecimalInMenuField() {
    return getFieldByClass(BigDecimalInMenuField.class);
  }

  public class MainBox extends AbstractGroupBox {
    @Order(1000)
    public class FormFieldMenu extends AbstractFormFieldMenu {
      @Order(1000)
      public class BigDecimalInMenuField extends AbstractBigDecimalField {
      }
    }

    @Order(2000)
    public class InnerFormField extends AbstractWrappedFormField<InnerForm> {
      @Override
      protected Class<? extends IForm> getConfiguredInnerForm() {
        return InnerForm.class;
      }
    }
  }

  public static class InnerForm extends AbstractForm {
    public class MainBox extends AbstractGroupBox {
      public class TestStringField extends AbstractStringField {
      }
    }
  }
}
