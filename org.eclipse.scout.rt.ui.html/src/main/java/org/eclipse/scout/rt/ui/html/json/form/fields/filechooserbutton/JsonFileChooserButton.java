/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.form.fields.filechooserbutton;

import java.beans.PropertyChangeEvent;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
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
  public Collection<String> getAcceptedUploadFileExtensions() {
    return getModel().getFileExtensions();
  }

  @Override
  public void consumeBinaryResource(List<BinaryResource> binaryResources, Map<String, String> uploadProperties) {
    if (!CollectionUtility.isEmpty(binaryResources)) {
      getModel().setValue(CollectionUtility.firstElement(binaryResources));
    }
  }

  @Override
  protected void handleModelPropertyChange(PropertyChangeEvent event) {
    if (IValueField.PROP_VALUE.equals(event.getPropertyName()) && getModel().getValue() == null) {
      // If model value is cleared, remove it in the corresponding UI widget as well. Otherwise,
      // the same document could not be re-uploaded again, even when the model value is null again.
      // Note that we only synchronize the case "value -> no value", but not "no value -> value"
      // or "value -> another value", because BinaryResources cannot be serialized.
      addPropertyChangeEvent(IValueField.PROP_VALUE, null);
    }
    else {
      super.handleModelPropertyChange(event);
    }
  }

  @Override
  public long getMaximumUploadSize() {
    return getModel().getMaximumUploadSize();
  }
}
