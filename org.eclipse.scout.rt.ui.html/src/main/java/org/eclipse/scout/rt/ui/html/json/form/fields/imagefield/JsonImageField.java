/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.form.fields.imagefield;

import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.client.ui.IIconIdPrefix;
import org.eclipse.scout.rt.client.ui.dnd.IDNDSupport;
import org.eclipse.scout.rt.client.ui.dnd.ResourceListTransferObject;
import org.eclipse.scout.rt.client.ui.form.fields.filechooserfield.IFileChooserField;
import org.eclipse.scout.rt.client.ui.form.fields.imagefield.IImageField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.basic.filechooser.JsonFileChooserAcceptAttributeBuilder;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonFormField;
import org.eclipse.scout.rt.ui.html.res.BinaryResourceHolder;
import org.eclipse.scout.rt.ui.html.res.BinaryResourceUrlUtility;
import org.eclipse.scout.rt.ui.html.res.IBinaryResourceConsumer;
import org.eclipse.scout.rt.ui.html.res.IBinaryResourceProvider;
import org.json.JSONArray;
import org.json.JSONObject;

public class JsonImageField<IMAGE_FIELD extends IImageField> extends JsonFormField<IMAGE_FIELD> implements IBinaryResourceProvider, IBinaryResourceConsumer {

  public static final String PROP_IMAGE_URL = "imageUrl";
  public static final String PROP_ACCEPT_TYPES = "acceptTypes";

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
    putJsonProperty(new JsonProperty<IMAGE_FIELD>(IImageField.PROP_UPLOAD_ENABLED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isUploadEnabled();
      }
    });
    putJsonProperty(new JsonProperty<IMAGE_FIELD>(IFileChooserField.PROP_FILE_EXTENSIONS, model) {
      @Override
      protected List<String> modelValue() {
        return getModel().getFileExtensions();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        @SuppressWarnings("unchecked")
        List<String> fileExtensions = (List<String>) value;
        return new JSONArray(BEANS.get(JsonFileChooserAcceptAttributeBuilder.class)
            .withTypes(fileExtensions)
            .build());
      }

      @Override
      public String jsonPropertyName() {
        return PROP_ACCEPT_TYPES;
      }
    });
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    json.put(PROP_IMAGE_URL, getImageUrl());
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

  /**
   * Returns an URL for the image or imageId, respectively (first one that is not <code>null</code>). If no image is
   * set, <code>null</code> is returned.
   */
  protected String getImageUrl() {
    if (getModel().getImage() != null) {
      // We don't send the image via JSON to the client, we only set a flag that this adapter has an image
      // The client will request the image in a separate http request. See: ResourceRequestHandler
      BinaryResource imageResource = BinaryResourceUrlUtility.extractBinaryResource(getModel().getImage(), "image", "jpg");
      if (imageResource != null && imageResource.getContent() != null) {
        return BinaryResourceUrlUtility.createDynamicAdapterResourceUrl(this, imageResource);
      }
    }
    if (getModel().getImageUrl() != null) {
      return getModel().getImageUrl();
    }
    String imageId = getModel().getImageId();
    if (imageId != null) {
      if (isFontIcon(imageId)) {
        return imageId;
      }
      else {
        return BinaryResourceUrlUtility.createIconUrl(imageId);
      }
    }
    return null;
  }

  protected boolean isFontIcon(String imageId) {
    if (StringUtility.isNullOrEmpty(imageId)) {
      return false;
    }
    return imageId.startsWith(IIconIdPrefix.FONT);
  }

  // When an adapter has multiple images, it must deal itself with that case. For instance it could
  // add a sequence-number to the contentId to distinct between different images.
  @Override
  public BinaryResourceHolder provideBinaryResource(String requestFilename) {
    BinaryResource image = BinaryResourceUrlUtility.extractBinaryResource(getModel().getImage(), "image", "jpg");
    if (image == null || image.getContent() == null) {
      return null;
    }

    String imageFilenameWithFingerprint = BinaryResourceUrlUtility.getFilenameWithFingerprint(image);
    if (imageFilenameWithFingerprint.equals(requestFilename)) {
      return new BinaryResourceHolder(image);
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
  public long getMaximumUploadSize() {
    return getModel().getDropMaximumSize();
  }
}
