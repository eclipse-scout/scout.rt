/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.form.fields.stringfield;

import org.eclipse.scout.rt.client.ui.form.fields.stringfield.IStringField;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonValueField;

public class JsonStringField<STRING_FIELD extends IStringField> extends JsonValueField<STRING_FIELD> {

  public static final String EVENT_CALL_ACTION = "callAction";
  public static final String EVENT_CALL_LINK_ACTION = "callLinkAction";
  public static final String EVENT_SELECTION_CHANGED = "selectionChanged";

  public JsonStringField(STRING_FIELD model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "StringField";
  }

  @Override
  protected void initJsonProperties(STRING_FIELD model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<IStringField>(IStringField.PROP_MULTILINE_TEXT, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isMultilineText();
      }
    });
    putJsonProperty(new JsonProperty<IStringField>(IStringField.PROP_UPDATE_DISPLAY_TEXT_ON_MODIFY, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isUpdateDisplayTextOnModify();
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
  }

  @Override
  public void handleUiEvent(JsonEvent event) {
    if (EVENT_CALL_ACTION.equals(event.getType())) {
      handleUiCallAction();
    }
    else if (EVENT_SELECTION_CHANGED.equals(event.getType())) {
      handleUiSelectionChanged(event);
    }
    else {
      super.handleUiEvent(event);
    }
  }

  // FIXME AWE: (display-text) rename to handleUiValueChanged after renaming in Scout RT
  @Override
  protected void handleUiTextChangedImpl(String displayText) {
    getModel().getUIFacade().parseAndSetValueFromUI(displayText);
  }

  private void handleUiCallAction() {
    getModel().getUIFacade().fireActionFromUI();
  }

  private void handleUiSelectionChanged(JsonEvent event) {
    int selectionStart = (int) event.getData().get(IStringField.PROP_SELECTION_START);
    int selectionEnd = (int) event.getData().get(IStringField.PROP_SELECTION_END);
    addPropertyEventFilterCondition(IStringField.PROP_SELECTION_START, selectionStart);
    addPropertyEventFilterCondition(IStringField.PROP_SELECTION_END, selectionEnd);
    getModel().getUIFacade().setSelectionFromUI(selectionStart, selectionEnd);
  }

  @Override
  protected void handleUiDisplayTextChangedImpl(String displayText) {
    getModel().getUIFacade().setDisplayTextFromUI(displayText);
  }

}
