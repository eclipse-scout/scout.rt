package org.eclipse.scout.rt.ui.html.json.form.fields.imagefield;

import org.eclipse.scout.rt.client.ui.form.fields.imagebox.IImageField;
import org.eclipse.scout.rt.shared.data.basic.BinaryResource;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonFormField;
import org.eclipse.scout.rt.ui.html.res.BinaryResourceUrlUtility;
import org.eclipse.scout.rt.ui.html.res.IBinaryResourceProvider;

public class JsonImageField<T extends IImageField> extends JsonFormField<T> implements IBinaryResourceProvider {

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
        return BinaryResourceUrlUtility.createIconUrl(JsonImageField.this, getModel().getImageId());
      }
    });
    // We don't send the image via JSON to the client, we only set a flag that this adapter has an image
    // The client will request the image in a separate http request. See: StaticResourceRequestInterceptor
    putJsonProperty(new JsonProperty<T>(IImageField.PROP_IMAGE, model) {
      @Override
      protected String modelValue() {
        BinaryResource image = extractBinaryResource();
        return image != null ? BinaryResourceUrlUtility.createCallbackUrl(JsonImageField.this, image.getFilename()) : null;
      }
    });
    putJsonProperty(new JsonProperty<T>(IImageField.PROP_SCROLL_BAR_ENABLED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isScrollBarEnabled();
      }
    });
  }

  protected BinaryResource extractBinaryResource() {
    Object raw = getModel().getImage();
    if (raw instanceof BinaryResource) {
      return (BinaryResource) raw;
    }
    if (raw instanceof byte[]) {
      return new BinaryResource("image.jpg", null, (byte[]) raw, -1);
    }
    return null;
  }

  // When an adapter has multiple images, it must deal itself with that case. For instance it could
  // add a sequence-number to the contentId to distinct between different images.
  @Override
  public BinaryResource loadDynamicResource(String filename) {
    BinaryResource content = extractBinaryResource();
    if (content != null && filename.equals(content.getFilename())) {
      return content;
    }
    return null;
  }
}
