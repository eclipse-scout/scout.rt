package org.eclipse.scout.rt.ui.html.json.form.fields.smartfield;

import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield2.IProposalField2;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield2.ISmartField2;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.json.JSONObject;

public class JsonProposalField2<VALUE, MODEL extends IProposalField2<VALUE>> extends JsonSmartField2<VALUE, MODEL> {

  public JsonProposalField2(MODEL model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "ProposalField2";
  }

  @Override
  protected void initJsonProperties(MODEL model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<IProposalField2<VALUE>>(IProposalField2.PROP_MAX_LENGTH, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getMaxLength();
      }
    });
    putJsonProperty(new JsonProperty<IProposalField2<VALUE>>(IProposalField2.PROP_TRIM_TEXT_ON_VALIDATE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isTrimText();
      }
    });
  }

  @Override
  protected Object jsonToValue(String jsonValue) {
    return jsonValue; // simply return the string
  }

  @Override
  protected Object valueToJson(VALUE value) {
    assert value instanceof String;
    return value;
  }

  @Override
  protected void setValueFromUI(Object value) {
    getModel().getUIFacade().setValueAsStringFromUI((String) value);
  }

  @Override
  protected void handleUiAcceptInput(JsonEvent event) {
    JSONObject data = event.getData();
    if (data.has(IValueField.PROP_DISPLAY_TEXT)) {
      this.handleUiDisplayTextChange(data);
    }
    if (data.has(IValueField.PROP_ERROR_STATUS)) {
      this.handleUiErrorStatusChange(data);
    }
    // The difference to the smart-field is that the proposal-field
    // can receive lookup-row and value in a single event. For instance:
    // lookupRow=null, value=Foo (in case a custom text has been set)
    if (data.has(ISmartField2.PROP_LOOKUP_ROW)) {
      this.handleUiLookupRowChange(data);
    }
    if (data.has(IValueField.PROP_VALUE)) {
      handleUiValueChange(data);
    }
  }

}
