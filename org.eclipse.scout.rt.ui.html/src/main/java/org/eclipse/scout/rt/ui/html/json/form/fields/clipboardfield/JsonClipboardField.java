/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.form.fields.clipboardfield;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
      @SuppressWarnings("unchecked")
      public Object prepareValueForToJson(Object value) {
        return new JSONArray((List<String>) value); // Do NOT remove the cast! It is required to use the correct constructor.
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
    binaryResources = new ArrayList<BinaryResource>(binaryResources);
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
  public long getMaximumBinaryResourceUploadSize() {
    return getModel().getMaximumSize();
  }
}
