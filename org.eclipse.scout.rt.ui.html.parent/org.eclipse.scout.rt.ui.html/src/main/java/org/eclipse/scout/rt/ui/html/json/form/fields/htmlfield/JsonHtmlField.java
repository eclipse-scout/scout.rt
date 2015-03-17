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
package org.eclipse.scout.rt.ui.html.json.form.fields.htmlfield;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.scout.commons.HTMLUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.htmlfield.IHtmlField;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonObjectUtility;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.JsonResponse;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonValueField;

/**
 * This class creates JSON output for an <code>IHtmlField</code>.
 */
public class JsonHtmlField<T extends IHtmlField> extends JsonValueField<T> {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JsonHtmlField.class);

  // from UI
  public static final String EVENT_HYPERLINK_ACTION = "hyperlinkAction";

  public JsonHtmlField(T model, IJsonSession jsonSession, String id, IJsonAdapter<?> parent) {
    super(model, jsonSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "HtmlField";
  }

  @Override
  protected void initJsonProperties(T model) {
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
        return cleanHtmlValue((String) value);
      }
    });

    putJsonProperty(new JsonProperty<IHtmlField>(IHtmlField.PROP_SCROLLBARS_ENABLED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isScrollBarEnabled();
      }
    });
    putJsonProperty(new JsonProperty<IHtmlField>(IHtmlField.PROP_SCROLLBAR_SCROLL_TO_END, model) {
      @Override
      protected Object modelValue() {
        return null; // This property is not really a property, but an event, therefore it does not have a value
      }
    });
    putJsonProperty(new JsonProperty<IHtmlField>(IHtmlField.PROP_SCROLLBAR_SCROLL_TO_ANCHOR, model) {
      @Override
      protected String modelValue() {
        return getModel().getScrollToAnchor();
      }
    });
  }

  protected String cleanHtmlValue(String html) {
    if (html == null) {
      return null;
    }
    String cleanHtml = HTMLUtility.cleanupHtml(html, false, true, null, null);
    return getJsonSession().getCustomHtmlRenderer().convert(cleanHtml, true);
  }

  @Override
  public void handleUiEvent(JsonEvent event, JsonResponse res) {
    if (EVENT_HYPERLINK_ACTION.equals(event.getType())) {
      handleUiHyperlinkAction(event);
    }
    else {
      super.handleUiEvent(event, res);
    }
  }

  protected void handleUiHyperlinkAction(JsonEvent event) {
    URL url = null;
    try {
      url = new URL("http://local/" + JsonObjectUtility.getString(event.getData(), "hyperlink"));
    }
    catch (MalformedURLException e) {
      //TODO [15.0] imo change in scout and only send the path, not the complete url, also ignore the column! hyperlinks are per row only and use a path only [a href='path']text[/a]
      LOG.error("", e);
    }
    if (url != null) {
      getModel().getUIFacade().fireHyperlinkActionFromUI(url);
    }
  }
}
