package org.eclipse.scout.rt.ui.html.json.form.fields.imagefield;

import org.eclipse.scout.rt.client.ui.form.fields.imagebox.IImageField;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonFormField;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonProperty;

public class JsonImageField<T extends IImageField> extends JsonFormField<T> {

  public JsonImageField(T model, IJsonSession session, String id) {
    super(model, session, id);
  }

  @Override
  public String getObjectType() {
    return "ImageField";
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<T>(IImageField.PROP_IMAGE_ID, model) {
      @Override
      protected String modelValue() {
        return getModel().getImageId();
      }
    });

  }
}
