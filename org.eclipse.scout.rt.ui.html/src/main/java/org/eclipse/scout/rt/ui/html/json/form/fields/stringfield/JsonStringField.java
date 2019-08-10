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
package org.eclipse.scout.rt.ui.html.json.form.fields.stringfield;

import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.client.ui.dnd.IDNDSupport;
import org.eclipse.scout.rt.client.ui.dnd.ResourceListTransferObject;
import org.eclipse.scout.rt.client.ui.form.fields.IBasicField;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.IStringField;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.util.BooleanUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonBasicField;
import org.eclipse.scout.rt.ui.html.res.IBinaryResourceConsumer;
import org.json.JSONObject;

public class JsonStringField<T extends IStringField> extends JsonBasicField<T> implements IBinaryResourceConsumer {

  public static final String EVENT_ACTION = "action";
  public static final String EVENT_SELECTION_CHANGE = "selectionChange";

  /**
   * This property is only relevant for UI to distinguish between real display text and obfuscated display text. There
   * is no representation in the Java model.
   */
  private static final String PROP_INPUT_OBFUSCATED = "inputObfuscated";

  /**
   * Characters to use for obfuscated display text
   */
  public static final String OBFUSCATED_DISPLAY_TEXT = "----------";

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
    removeJsonProperty(IValueField.PROP_DISPLAY_TEXT);
    putJsonProperty(new JsonProperty<IStringField>(IValueField.PROP_DISPLAY_TEXT, model) {
      @Override
      protected String modelValue() {
        if (isObfuscateDisplayTextRequired()) {
          // Use obfuscated input if input is masked (never send existing display text to UI).
          return OBFUSCATED_DISPLAY_TEXT;
        }
        return getModel().getDisplayText();
      }
    });
  }

  @Override
  public void handleUiEvent(JsonEvent event) {
    if (EVENT_ACTION.equals(event.getType())) {
      handleUiAction();
    }
    else if (EVENT_SELECTION_CHANGE.equals(event.getType())) {
      handleUiSelectionChange(event);
    }
    else {
      super.handleUiEvent(event);
    }
  }

  protected void handleUiAction() {
    getModel().getUIFacade().fireActionFromUI();
  }

  protected void handleUiSelectionChange(JsonEvent event) {
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
  public long getMaximumUploadSize() {
    return getModel().getDropMaximumSize();
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    if (isObfuscateDisplayTextRequired()) {
      json.put(PROP_INPUT_OBFUSCATED, true);
    }
    return json;
  }

  @Override
  protected void handleModelPropertyChange(String propertyName, Object oldValue, Object newValue) {
    super.handleModelPropertyChange(propertyName, oldValue, newValue);

    if (IStringField.PROP_VALUE.equals(propertyName) && isObfuscateDisplayTextRequired()) {
      PropertyChangeEvent event = new PropertyChangeEvent(getModel(), IStringField.PROP_DISPLAY_TEXT, null, getModel().getDisplayText());
      if (filterPropertyChangeEvent(event) != null) {
        // Filtering of event is required, otherwise if the user types text, the model would send the obfuscated display text to the UI instead of leaving the typed one.
        addPropertyChangeEvent(PROP_INPUT_OBFUSCATED, true);
        addPropertyChangeEvent(IStringField.PROP_DISPLAY_TEXT, OBFUSCATED_DISPLAY_TEXT);
      }
    }
  }

  @Override
  protected void handleModelPropertyChange(PropertyChangeEvent event) {
    super.handleModelPropertyChange(event);

    if (IStringField.PROP_INPUT_MASKED.equals(event.getPropertyName()) && !BooleanUtility.nvl((Boolean) event.getNewValue())) {
      // Send display text if input is not masked anymore (otherwise obfuscated display text might be made visible instead of real one).
      // Disable obfuscation because it still active and user focuses field, the content would be cleared.
      JsonProperty<?> displayTextProperty = getJsonProperty(IBasicField.PROP_DISPLAY_TEXT);
      addPropertyChangeEvent(PROP_INPUT_OBFUSCATED, false);
      addPropertyChangeEvent(displayTextProperty, getModel().getDisplayText());
    }
  }

  protected boolean isObfuscateDisplayTextRequired() {
    return !getModel().isMultilineText() // Multiline text doesn't support input masked.
        && getModel().isInputMasked()
        && !StringUtility.isNullOrEmpty(getModel().getDisplayText()); // Only obfuscate text when there is a display text.
  }
}
