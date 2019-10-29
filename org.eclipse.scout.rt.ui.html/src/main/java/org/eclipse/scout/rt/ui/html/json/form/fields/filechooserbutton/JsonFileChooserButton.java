/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.ui.html.json.form.fields.filechooserbutton;

import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.client.ui.form.fields.filechooserbutton.IFileChooserButton;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.basic.filechooser.JsonFileChooserAcceptAttributeBuilder;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonValueField;
import org.eclipse.scout.rt.ui.html.res.IBinaryResourceConsumer;
import org.json.JSONArray;

/**
 * @since 8.0
 */
public class JsonFileChooserButton<M extends IFileChooserButton> extends JsonValueField<M> implements IBinaryResourceConsumer {

  protected static final String PROP_ACCEPT_TYPES = "acceptTypes";

  public JsonFileChooserButton(M model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "FileChooserButton";
  }

  @Override
  protected void initJsonProperties(M model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<M>(IFileChooserButton.PROP_MAXIMUM_UPLOAD_SIZE, model) {
      @Override
      protected Long modelValue() {
        return getModel().getMaximumUploadSize();
      }
    });
    putJsonProperty(new JsonProperty<M>(IFileChooserButton.PROP_ICON_ID, model) {
      @Override
      protected String modelValue() {
        return getModel().getIconId();
      }
    });
    putJsonProperty(new JsonProperty<M>(IFileChooserButton.PROP_HTML_ENABLED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isHtmlEnabled();
      }
    });
    putJsonProperty(new JsonProperty<M>(IFileChooserButton.PROP_FILE_EXTENSIONS, model) {
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
  public void consumeBinaryResource(List<BinaryResource> binaryResources, Map<String, String> uploadProperties) {
    if (!CollectionUtility.isEmpty(binaryResources)) {
      getModel().setValue(CollectionUtility.firstElement(binaryResources));
    }
  }

  @Override
  public long getMaximumUploadSize() {
    return getModel().getMaximumUploadSize();
  }
}
