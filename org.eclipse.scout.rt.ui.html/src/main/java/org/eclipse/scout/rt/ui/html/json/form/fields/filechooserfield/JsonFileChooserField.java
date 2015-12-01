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
package org.eclipse.scout.rt.ui.html.json.form.fields.filechooserfield;

import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.client.ui.form.fields.filechooserfield.IFileChooserField;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonValueField;
import org.eclipse.scout.rt.ui.html.res.IBinaryResourceConsumer;

public class JsonFileChooserField<FILE_CHOOSER_FIELD extends IFileChooserField> extends JsonValueField<FILE_CHOOSER_FIELD> implements IBinaryResourceConsumer {

  public static final String EVENT_CHOOSE_FILE = "chooseFile";

  public JsonFileChooserField(FILE_CHOOSER_FIELD model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  protected void initJsonProperties(FILE_CHOOSER_FIELD model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<FILE_CHOOSER_FIELD>(IFileChooserField.PROP_MAXIMUM_UPLOAD_SIZE, model) {
      @Override
      protected Long modelValue() {
        return getModel().getMaximumUploadSize();
      }
    });
  }

  @Override
  public String getObjectType() {
    return "FileChooserField";
  }

  @Override
  public void handleUiEvent(JsonEvent event) {
    if (EVENT_CHOOSE_FILE.equals(event.getType())) {
      handleUiChooseFile();
    }
    else {
      super.handleUiEvent(event);
    }
  }

  @Override
  protected void handleUiTextChangedImpl(String displayText) {
    getModel().getUIFacade().parseAndSetValueFromUI(displayText);
  }

  private void handleUiChooseFile() {
    getModel().getUIFacade().startFileChooserFromUI();
  }

  @Override
  public void consumeBinaryResource(List<BinaryResource> binaryResources, Map<String, String> uploadProperties) {
    if (!CollectionUtility.isEmpty(binaryResources)) {
      getModel().setValue(CollectionUtility.firstElement(binaryResources));
    }
  }

  @Override
  public long getMaximumBinaryResourceUploadSize() {
    return getModel().getMaximumUploadSize();
  }
}
