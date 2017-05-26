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
package org.eclipse.scout.rt.ui.html.json.form.fields.stringfield;

import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.client.ui.dnd.IDNDSupport;
import org.eclipse.scout.rt.client.ui.dnd.ResourceListTransferObject;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.IStringField;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonBasicField;
import org.eclipse.scout.rt.ui.html.res.IBinaryResourceConsumer;

public class JsonStringField<T extends IStringField> extends JsonBasicField<T> implements IBinaryResourceConsumer {

  public static final String EVENT_CALL_ACTION = "callAction";
  public static final String EVENT_CALL_LINK_ACTION = "callLinkAction";
  public static final String EVENT_SELECTION_CHANGED = "selectionChanged";

  public JsonStringField(T model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "StringField";
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<IStringField>(IStringField.PROP_MULTILINE_TEXT, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isMultilineText();
      }
    });
    putJsonProperty(new JsonProperty<IStringField>(IStringField.PROP_INPUT_MASKED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isInputMasked();
      }
    });
    putJsonProperty(new JsonProperty<IStringField>(IStringField.PROP_INSERT_TEXT, model) {
      @Override
      protected String modelValue() {
        return (String) getModel().getProperty(IStringField.PROP_INSERT_TEXT);
      }
    });
    putJsonProperty(new JsonProperty<IStringField>(IStringField.PROP_WRAP_TEXT, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isWrapText();
      }
    });
    putJsonProperty(new JsonProperty<IStringField>(IStringField.PROP_FORMAT, model) {
      @Override
      protected String modelValue() {
        return getModel().getFormat();
      }
    });
    putJsonProperty(new JsonProperty<IStringField>(IStringField.PROP_SPELL_CHECK_ENABLED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isSpellCheckEnabled();
      }
    });
    putJsonProperty(new JsonProperty<IStringField>(IStringField.PROP_HAS_ACTION, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isHasAction();
      }
    });
    putJsonProperty(new JsonProperty<IStringField>(IStringField.PROP_SELECTION_START, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getSelectionStart();
      }
    });
    putJsonProperty(new JsonProperty<IStringField>(IStringField.PROP_SELECTION_END, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getSelectionEnd();
      }
    });
    putJsonProperty(new JsonProperty<IStringField>(IStringField.PROP_SELECTION_TRACKING_ENABLED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isSelectionTrackingEnabled();
      }
    });
    putJsonProperty(new JsonProperty<IStringField>(IStringField.PROP_DROP_TYPE, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getDropType();
      }
    });
    putJsonProperty(new JsonProperty<IStringField>(IStringField.PROP_DROP_MAXIMUM_SIZE, model) {
      @Override
      protected Long modelValue() {
        return getModel().getDropMaximumSize();
      }
    });
    putJsonProperty(new JsonProperty<IStringField>(IStringField.PROP_MAX_LENGTH, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getMaxLength();
      }
    });
    putJsonProperty(new JsonProperty<IStringField>(IStringField.PROP_TRIM_TEXT_ON_VALIDATE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isTrimText();
      }
    });
  }

  @Override
  public void handleUiEvent(JsonEvent event) {
    if (EVENT_CALL_ACTION.equals(event.getType())) {
      handleUiCallAction();
    }
    else if (EVENT_SELECTION_CHANGED.equals(event.getType())) {
      handleUiSelectionChanged(event);
    }
    else if (EVENT_EXPORT_TO_CLIPBOARD.equals(event.getType())) {
      handleUiExportToClipboard();
    }
    else {
      super.handleUiEvent(event);
    }
  }

  protected void handleUiCallAction() {
    getModel().getUIFacade().fireActionFromUI();
  }

  protected void handleUiSelectionChanged(JsonEvent event) {
    int selectionStart = event.getData().getInt(IStringField.PROP_SELECTION_START);
    int selectionEnd = event.getData().getInt(IStringField.PROP_SELECTION_END);
    addPropertyEventFilterCondition(IStringField.PROP_SELECTION_START, selectionStart);
    addPropertyEventFilterCondition(IStringField.PROP_SELECTION_END, selectionEnd);
    getModel().getUIFacade().setSelectionFromUI(selectionStart, selectionEnd);
  }

  @Override
  protected void handleUiAcceptInputWhileTyping(String displayText) {
    getModel().getUIFacade().setDisplayTextFromUI(displayText);
  }

  @Override
  protected void handleUiAcceptInputAfterTyping(String displayText) {
    getModel().getUIFacade().parseAndSetValueFromUI(displayText);
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
