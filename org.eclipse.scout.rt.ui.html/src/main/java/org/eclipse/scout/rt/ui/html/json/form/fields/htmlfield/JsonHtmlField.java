/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.form.fields.htmlfield;

import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.htmlfield.IHtmlField;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonEventType;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonValueField;
import org.eclipse.scout.rt.ui.html.res.BinaryResourceHolder;
import org.eclipse.scout.rt.ui.html.res.BinaryResourceUrlUtility;
import org.eclipse.scout.rt.ui.html.res.IBinaryResourceProvider;

/**
 * This class creates JSON output for an <code>IHtmlField</code>.
 */
public class JsonHtmlField<HTML_FIELD extends IHtmlField> extends JsonValueField<HTML_FIELD> implements IBinaryResourceProvider {

  public JsonHtmlField(HTML_FIELD model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "HtmlField";
  }

  @Override
  protected void initJsonProperties(HTML_FIELD model) {
    super.initJsonProperties(model);

    // Prevent sending the value to the UI --> only use the (cleaned) display text
    removeJsonProperty(IValueField.PROP_VALUE);
    putJsonProperty(new JsonProperty<IHtmlField>(IValueField.PROP_DISPLAY_TEXT, model) {
      @Override
      protected String modelValue() {
        return getModel().getDisplayText();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return BinaryResourceUrlUtility.replaceImageUrls(JsonHtmlField.this, (String) value);
      }
    });
    putJsonProperty(new JsonProperty<IHtmlField>(IHtmlField.PROP_SELECTABLE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isSelectable();
      }
    });
    putJsonProperty(new JsonProperty<IHtmlField>(IHtmlField.PROP_SCROLL_BAR_ENABLED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isScrollBarEnabled();
      }
    });
    putJsonProperty(new JsonProperty<IHtmlField>(IHtmlField.PROP_SCROLL_TO_END, model) {
      @Override
      protected Object modelValue() {
        return null; // This property is not really a property, but an event, therefore it does not have a value
      }
    });
    putJsonProperty(new JsonProperty<IHtmlField>(IHtmlField.PROP_SCROLL_TO_ANCHOR, model) {
      @Override
      protected String modelValue() {
        return getModel().getScrollToAnchor();
      }
    });
  }

  @Override
  public void handleUiEvent(JsonEvent event) {
    if (JsonEventType.APP_LINK_ACTION.matches(event.getType())) {
      handleUiAppLinkAction(event);
    }
    else {
      super.handleUiEvent(event);
    }
  }

  protected void handleUiAppLinkAction(JsonEvent event) {
    String ref = event.getData().optString("ref", null);
    getModel().getUIFacade().fireAppLinkActionFromUI(ref);
  }

  @Override
  public BinaryResourceHolder provideBinaryResource(String filename) {
    BinaryResource attachment = getModel().getAttachment(filename);
    return attachment == null ? null : new BinaryResourceHolder(attachment);
  }
}
