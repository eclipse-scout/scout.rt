#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.client.helloworld;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.rt.shared.TEXTS;

import ${package}.client.helloworld.HelloWorldForm.MainBox.TopBox;
import ${package}.client.helloworld.HelloWorldForm.MainBox.TopBox.MessageField;
import ${package}.shared.helloworld.HelloWorldFormData;
import ${package}.shared.helloworld.IHelloWorldFormService;

/**
 * <h3>{@link HelloWorldForm}</h3>
 *
 * @author ${userName}
 */
@FormData(value = HelloWorldFormData.class, sdkCommand = FormData.SdkCommand.CREATE)
public class HelloWorldForm extends AbstractForm {

  public HelloWorldForm() {
    setHandler(new ViewHandler());
  }

  @Override
  protected boolean getConfiguredAskIfNeedSave() {
    return false;
  }

  @Override
  protected int getConfiguredModalityHint() {
    return MODALITY_HINT_MODELESS;
  }

  @Override
  protected String getConfiguredIconId() {
    return AbstractIcons.World;
  }

  public MainBox getMainBox() {
    return getFieldByClass(MainBox.class);
  }

  public TopBox getTopBox() {
    return getFieldByClass(TopBox.class);
  }

  public MessageField getMessageField() {
    return getFieldByClass(MessageField.class);
  }

  @Order(1000)
  public class MainBox extends AbstractGroupBox {

    @Order(1000)
    public class TopBox extends AbstractGroupBox {

      @Override
      protected String getConfiguredLabel() {
        return TEXTS.get("MessageFromServer");
      }

      @Order(1000)
      public class MessageField extends AbstractStringField {
        @Override
        protected int getConfiguredGridW() {
          return 2;
        }

        @Override
        protected String getConfiguredLabel() {
          return TEXTS.get("Message");
        }

        @Override
        protected boolean getConfiguredEnabled() {
          return false;
        }
      }
    }
  }

  public class ViewHandler extends AbstractFormHandler {

    @Override
    protected void execLoad() {
      IHelloWorldFormService service = BEANS.get(IHelloWorldFormService.class);
      HelloWorldFormData formData = new HelloWorldFormData();
      exportFormData(formData);
      formData = service.load(formData);
      importFormData(formData);
    }
  }
}
