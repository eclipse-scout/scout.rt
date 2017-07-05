package org.eclipse.scout.rt.ui.html.json.form.fields.smartfield;

import org.eclipse.scout.rt.client.ui.form.fields.smartfield2.IProposalField2;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
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
  protected void handleUiPropertyChangeValue(JSONObject data) {
    String value = data.optString("value");
    getModel().setValueAsString(value);
  }

}
