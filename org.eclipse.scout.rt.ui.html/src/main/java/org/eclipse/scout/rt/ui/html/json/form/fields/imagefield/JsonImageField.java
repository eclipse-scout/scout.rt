/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.form.fields.imagefield;

import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Map;
import java.util.zip.Adler32;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenu;
import org.eclipse.scout.rt.client.ui.dnd.IDNDSupport;
import org.eclipse.scout.rt.client.ui.dnd.ResourceListTransferObject;
import org.eclipse.scout.rt.client.ui.form.fields.imagefield.IImageField;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.FilteredJsonAdapterIds;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonFormField;
import org.eclipse.scout.rt.ui.html.json.menu.IJsonContextMenuOwner;
import org.eclipse.scout.rt.ui.html.json.menu.JsonContextMenu;
import org.eclipse.scout.rt.ui.html.res.BinaryResourceHolder;
import org.eclipse.scout.rt.ui.html.res.BinaryResourceUrlUtility;
import org.eclipse.scout.rt.ui.html.res.IBinaryResourceConsumer;
import org.eclipse.scout.rt.ui.html.res.IBinaryResourceProvider;
import org.json.JSONObject;

public class JsonImageField<IMAGE_FIELD extends IImageField> extends JsonFormField<IMAGE_FIELD> implements IBinaryResourceProvider, IBinaryResourceConsumer, IJsonContextMenuOwner {

  public static final String PROP_IMAGE_URL = "imageUrl";

  private PropertyChangeListener m_contextMenuListener;
  private JsonContextMenu<IContextMenu> m_jsonContextMenu;

  public JsonImageField(IMAGE_FIELD model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "ImageField";
  }

  @Override
  protected void initJsonProperties(IMAGE_FIELD model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<IMAGE_FIELD>(IImageField.PROP_SCROLL_BAR_ENABLED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isScrollBarEnabled();
      }
    });
    putJsonProperty(new JsonProperty<IMAGE_FIELD>(IImageField.PROP_AUTO_FIT, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isAutoFit();
      }
    });
    putJsonProperty(new JsonProperty<IMAGE_FIELD>(IImageField.PROP_DROP_TYPE, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getDropType();
      }
    });
    putJsonProperty(new JsonProperty<IMAGE_FIELD>(IImageField.PROP_DROP_MAXIMUM_SIZE, model) {
      @Override
      protected Long modelValue() {
        return getModel().getDropMaximumSize();
      }
    });
  }

  @Override
  protected void attachChildAdapters() {
    super.attachChildAdapters();
    m_jsonContextMenu = new JsonContextMenu<>(getModel().getContextMenu(), this);
    m_jsonContextMenu.init();
  }

  @Override
  protected void disposeChildAdapters() {
    m_jsonContextMenu.dispose();
    super.disposeChildAdapters();
  }

  @Override
  protected void attachModel() {
    super.attachModel();
    if (m_contextMenuListener != null) {
      throw new IllegalStateException();
    }
    m_contextMenuListener = evt -> {
      if (IMenu.PROP_VISIBLE.equals(evt.getPropertyName())) {
        handleModelContextMenuVisibleChanged((Boolean) evt.getNewValue());
      }
    };
    getModel().getContextMenu().addPropertyChangeListener(m_contextMenuListener);
  }

  @Override
  protected void detachModel() {
    super.detachModel();
    if (m_contextMenuListener == null) {
      throw new IllegalStateException();
    }
    getModel().getContextMenu().removePropertyChangeListener(m_contextMenuListener);
    m_contextMenuListener = null;
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    json.put(PROP_IMAGE_URL, getImageUrl());
    json.put(PROP_MENUS, m_jsonContextMenu.childActionsToJson());
    json.put(PROP_MENUS_VISIBLE, getModel().getContextMenu().isVisible());
    return json;
  }

  @Override
  protected void handleModelPropertyChange(String propertyName, Object oldValue, Object newValue) {
    if (ObjectUtility.isOneOf(propertyName,
        IImageField.PROP_IMAGE,
        IImageField.PROP_IMAGE_ID,
        IImageField.PROP_IMAGE_URL)) {
      handleModelImageSourceChanged();
    }
    else {
      super.handleModelPropertyChange(propertyName, oldValue, newValue);
    }
  }

  protected void handleModelImageSourceChanged() {
    addPropertyChangeEvent(PROP_IMAGE_URL, getImageUrl());
  }

  @Override
  public void handleModelContextMenuChanged(FilteredJsonAdapterIds<?> filteredAdapters) {
    addPropertyChangeEvent(PROP_MENUS, filteredAdapters);
  }

  protected void handleModelContextMenuVisibleChanged(boolean visible) {
    addPropertyChangeEvent(PROP_MENUS_VISIBLE, visible);
  }

  /**
   * Returns an URL for the image or imageId, respectively (first one that is not <code>null</code>). If no image is
   * set, <code>null</code> is returned.
   */
  protected String getImageUrl() {
    if (getModel().getImage() != null) {
      // We don't send the image via JSON to the client, we only set a flag that this adapter has an image
      // The client will request the image in a separate http request. See: ResourceRequestHandler
      BinaryResource imageResource = extractBinaryResource(getModel().getImage());
      if (imageResource != null) {
        return BinaryResourceUrlUtility.createDynamicAdapterResourceUrl(this, imageResource.getFilename());
      }
    }
    if (getModel().getImageId() != null) {
      return BinaryResourceUrlUtility.createIconUrl(getModel().getImageId());
    }
    if (getModel().getImageUrl() != null) {
      return getModel().getImageUrl();
    }
    return null;
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
  public BinaryResourceHolder provideBinaryResource(String filename) {
    BinaryResource res = extractBinaryResource(getModel().getImage());
    if (res != null && filename.equals(res.getFilename())) {
      return new BinaryResourceHolder(res);
    }
    return null;
  }

  @Override
  public void consumeBinaryResource(List<BinaryResource> binaryResources, Map<String, String> uploadProperties) {
    if ((getModel().getDropType() & IDNDSupport.TYPE_FILE_TRANSFER) == IDNDSupport.TYPE_FILE_TRANSFER) {
      ResourceListTransferObject transferObject = new ResourceListTransferObject(binaryResources);
      getModel().getUIFacade().fireDropActionFromUi(transferObject);
    }
  }

  @Override
  public long getMaximumBinaryResourceUploadSize() {
    return getModel().getDropMaximumSize();
  }
}
