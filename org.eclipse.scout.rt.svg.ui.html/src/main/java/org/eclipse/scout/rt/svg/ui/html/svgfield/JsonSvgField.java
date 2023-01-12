/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.svg.ui.html.svgfield;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.svg.client.SVGUtility;
import org.eclipse.scout.rt.svg.client.svgfield.ISvgField;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.UiException;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonEventType;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonFormField;
import org.w3c.dom.svg.SVGDocument;

public class JsonSvgField extends JsonFormField<ISvgField> {
  private static final String SVG_ENCODING = StandardCharsets.UTF_8.name();

  public JsonSvgField(ISvgField model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "SvgField";
  }

  @Override
  protected void initJsonProperties(ISvgField model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<>(ISvgField.PROP_SVG_DOCUMENT, model) {
      @Override
      protected SVGDocument modelValue() {
        return getModel().getSvgDocument();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        if (value == null) {
          return null;
        }
        return svgToString((SVGDocument) value);
      }
    });
  }

  protected String svgToString(SVGDocument svg) {
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      SVGUtility.writeSVGDocument(svg, out, SVG_ENCODING);
      return out.toString(SVG_ENCODING);
    }
    catch (UnsupportedEncodingException | ProcessingException e) {
      throw new UiException("Failed to write SVG document", e);
    }
  }

  @Override
  public void handleUiEvent(JsonEvent event) {
    if (JsonEventType.APP_LINK_ACTION.matches(event)) {
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
}
