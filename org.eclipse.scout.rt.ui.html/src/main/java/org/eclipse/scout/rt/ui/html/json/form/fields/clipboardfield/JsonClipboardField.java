/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.form.fields.clipboardfield;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.scout.rt.client.ui.form.fields.clipboardfield.IClipboardField;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.resource.BinaryResources;
import org.eclipse.scout.rt.platform.resource.MimeType;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonValueField;
import org.eclipse.scout.rt.ui.html.res.IBinaryResourceConsumer;
import org.json.JSONArray;

public class JsonClipboardField<T extends IClipboardField> extends JsonValueField<T> implements IBinaryResourceConsumer {

  public JsonClipboardField(T model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "ClipboardField";
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<IClipboardField>(IClipboardField.PROP_ALLOWED_MIME_TYPES, model) {
      @Override
      protected List<String> modelValue() {
        return getModel().getAllowedMimeTypes();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return new JSONArray((Collection<?>) value); // Do NOT remove the cast! It is required to use the correct constructor.
      }
    });
    putJsonProperty(new JsonProperty<IClipboardField>(IClipboardField.PROP_MAXIMUM_SIZE, model) {
      @Override
      protected Long modelValue() {
        return getModel().getMaximumSize();
      }
    });
    putJsonProperty(new JsonProperty<IClipboardField>(IClipboardField.PROP_DROP_TYPE, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getDropType();
      }
    });
    putJsonProperty(new JsonProperty<IClipboardField>(IClipboardField.PROP_READ_ONLY, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isReadOnly();
      }
    });
  }

  @Override
  public void consumeBinaryResource(List<BinaryResource> binaryResources, Map<String, String> uploadProperties) {
    binaryResources = new ArrayList<>(binaryResources);
    // IE9 does not support java script Blob objects (legacy support for text transfer)
    for (Entry<String, String> property : uploadProperties.entrySet()) {
      if (property.getKey().matches("textTransferObject\\d+")) {
        byte[] bytes = property.getValue().getBytes(StandardCharsets.UTF_8);
        // anonymous text paste, no filename
        binaryResources.add(BinaryResources.create()
            .withContentType(MimeType.TXT.getType())
            .withContent(bytes)
            .build());
      }
    }
    // Pass binary resources to clipboard field
    getModel().setValue(binaryResources);
  }

  @Override
  public long getMaximumUploadSize() {
    return getModel().getMaximumSize();
  }
}
