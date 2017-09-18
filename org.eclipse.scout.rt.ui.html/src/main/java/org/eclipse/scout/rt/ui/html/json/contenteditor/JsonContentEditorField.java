package org.eclipse.scout.rt.ui.html.json.contenteditor;

import org.eclipse.scout.rt.client.ui.contenteditor.IContentEditorField;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonFormField;

public class JsonContentEditorField extends JsonFormField<IContentEditorField> {

  public JsonContentEditorField(IContentEditorField model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "ContentEditorField";
  }

  @Override
  protected void initJsonProperties(IContentEditorField model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<IContentEditorField>(IContentEditorField.PROP_CONTENT, model) {
      @Override
      protected String modelValue() {
        return getModel().getContent();
      }
    });
  }
}
