package org.eclipse.scout.rt.ui.html.json.form.fields.imagefield;

import org.eclipse.scout.rt.client.ui.form.fields.imagebox.BinaryContent;
import org.eclipse.scout.rt.client.ui.form.fields.imagebox.IBinaryContentProvider;
import org.eclipse.scout.rt.client.ui.form.fields.imagebox.IImageField;
import org.eclipse.scout.rt.ui.html.ImageUrlUtility;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonFormField;

public class JsonImageField<T extends IImageField> extends JsonFormField<T> implements IBinaryContentProvider {

  public JsonImageField(T model, IJsonSession jsonSession, String id, IJsonAdapter<?> parent) {
    super(model, jsonSession, id, parent);
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
        return ImageUrlUtility.createIconUrl(JsonImageField.this, getModel().getImageId());
      }
    });
    // We don't send the image via JSON to the client, we only set a flag that this adapter has an image
    // The client will request the image in a separate http request. See: StaticResourceRequestInterceptor
    putJsonProperty(new JsonProperty<T>(IImageField.PROP_IMAGE, model) {
      @Override
      protected String modelValue() {
        BinaryContent image = (BinaryContent) getModel().getImage();
        return ImageUrlUtility.createImageUrl(JsonImageField.this, image);
      }
    });
    putJsonProperty(new JsonProperty<T>(IImageField.PROP_SCROLL_BAR_ENABLED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isScrollBarEnabled();
      }
    });

  }

  // When an adapter has multiple images, it must deal itself with that case. For instance it could
  // add a sequence-number to the contentId to distinct between different images.
  @Override
  public BinaryContent getBinaryContent(String contentId) {
    if (!contentId.startsWith(getId())) {
      throw new IllegalArgumentException("content ID does not match adapater ID");
    }
    return (BinaryContent) getModel().getImage();
  }
}
