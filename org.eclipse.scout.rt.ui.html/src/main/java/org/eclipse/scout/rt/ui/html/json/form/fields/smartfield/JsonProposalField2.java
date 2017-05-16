package org.eclipse.scout.rt.ui.html.json.form.fields.smartfield;

import org.eclipse.scout.rt.client.ui.form.fields.smartfield2.IProposalField2;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield2.ISmartField2;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.json.JSONObject;

public class JsonProposalField2<VALUE> extends JsonSmartField2<VALUE, IProposalField2<VALUE>> {

  public JsonProposalField2(IProposalField2<VALUE> model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "ProposalField2";
  }

  @Override
  protected void initJsonProperties(IProposalField2<VALUE> model) {
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
    putJsonProperty(new JsonProperty<IProposalField2<VALUE>>(IProposalField2.PROP_AUTO_CLOSE_CHOOSER, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isAutoCloseChooser();
      }
    });
  }

  @Override
  protected void handleUiPropertyChangeValue(JSONObject data) {
    String value = data.optString("value");
    getModel().setValueAsString(value);
  }

  @Override
  protected void handleUiPropertyChange(String propertyName, JSONObject data) {
    if (ISmartField2.PROP_LOOKUP_ROW.equals(propertyName)) {
      // only required for proposal field, because value is a string there. For regular smart-fields
      // setValue will also set the right lookup row.
      JSONObject lookupRow = data.optJSONObject("lookupRow");
      VALUE key = null;
      if (lookupRow != null) {
        String mappedKey = lookupRow.getString("key");
        key = getLookupRowKeyForId(mappedKey);
      }
      getModel().setLookupRowByKey(key);
    }
    else {
      super.handleUiPropertyChange(propertyName, data);
    }
  }

}
