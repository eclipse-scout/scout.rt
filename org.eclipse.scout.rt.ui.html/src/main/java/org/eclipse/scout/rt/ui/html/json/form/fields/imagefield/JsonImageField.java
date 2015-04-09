package org.eclipse.scout.rt.ui.html.json.form.fields.imagefield;

import java.util.List;
import java.util.zip.Adler32;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenu;
import org.eclipse.scout.rt.client.ui.form.fields.imagebox.IImageField;
import org.eclipse.scout.rt.shared.data.basic.BinaryResource;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonObjectUtility;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.action.DisplayableActionFilter;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonFormField;
import org.eclipse.scout.rt.ui.html.json.menu.IContextMenuOwner;
import org.eclipse.scout.rt.ui.html.json.menu.JsonContextMenu;
import org.eclipse.scout.rt.ui.html.res.BinaryResourceUrlUtility;
import org.eclipse.scout.rt.ui.html.res.IBinaryResourceProvider;
import org.json.JSONObject;

public class JsonImageField<T extends IImageField> extends JsonFormField<T> implements IBinaryResourceProvider, IContextMenuOwner {

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
        return getModel().getImageId();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return BinaryResourceUrlUtility.createIconUrl((String) value);
      }
    });
    putJsonProperty(new JsonProperty<T>(IImageField.PROP_IMAGE, model) {
      @Override
      protected Object modelValue() {
        return getModel().getImage();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        // We don't send the image via JSON to the client, we only set a flag that this adapter has an image
        // The client will request the image in a separate http request. See: StaticResourceRequestInterceptor
        BinaryResource image = extractBinaryResource(value);
        return image != null ? BinaryResourceUrlUtility.createDynamicAdapterResourceUrl(JsonImageField.this, image.getFilename()) : null;
      }
    });
    putJsonProperty(new JsonProperty<T>(IImageField.PROP_SCROLL_BAR_ENABLED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isScrollBarEnabled();
      }
    });
    putJsonProperty(new JsonProperty<T>(IImageField.PROP_AUTO_FIT, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isAutoFit();
      }
    });
  }

  protected BinaryResource extractBinaryResource(Object raw) {
    if (raw instanceof BinaryResource) {
      return (BinaryResource) raw;
    }
    if (raw instanceof byte[]) {
      Adler32 crc = new Adler32();
      crc.update((byte[]) raw);
      return new BinaryResource("image-" + (crc.getValue()) + "-" + (((byte[]) raw).length) + ".jpg", (byte[]) raw);
    }
    return null;
  }

  // When an adapter has multiple images, it must deal itself with that case. For instance it could
  // add a sequence-number to the contentId to distinct between different images.
  @Override
  public BinaryResource loadDynamicResource(String filename) {
    BinaryResource res = extractBinaryResource(getModel().getImage());
    if (res != null && filename.equals(res.getFilename())) {
      return res;
    }
    return null;
  }

  @Override
  protected void attachChildAdapters() {
    super.attachChildAdapters();
    attachAdapter(getModel().getContextMenu(), new DisplayableActionFilter<IMenu>());
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    JsonContextMenu<IContextMenu> jsonContextMenu = getAdapter(getModel().getContextMenu());
    if (jsonContextMenu != null) {
      JsonObjectUtility.putProperty(json, PROP_MENUS, jsonContextMenu.childActionsToJson());
    }
    return json;
  }

  @Override
  public void handleModelContextMenuChanged(List<IJsonAdapter<?>> menuAdapters) {
    addPropertyChangeEvent(PROP_MENUS, JsonObjectUtility.adapterIdsToJson(menuAdapters));
  }
}
