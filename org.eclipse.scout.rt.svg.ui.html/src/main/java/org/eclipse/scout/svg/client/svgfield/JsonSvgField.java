package org.eclipse.scout.svg.client.svgfield;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonEventType;
import org.eclipse.scout.rt.ui.html.json.JsonException;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonFormField;
import org.eclipse.scout.svg.client.SVGUtility;
import org.w3c.dom.svg.SVGDocument;

public class JsonSvgField extends JsonFormField<ISvgField> {

  private static final String SVG_ENCODING = "UTF-8";

  public JsonSvgField(ISvgField model, IJsonSession jsonSession, String id, IJsonAdapter<?> parent) {
    super(model, jsonSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "SvgField";
  }

  @Override
  protected void initJsonProperties(ISvgField model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<ISvgField>(ISvgField.PROP_SVG_DOCUMENT, model) {
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

  private String svgToString(SVGDocument svg) {
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      SVGUtility.writeSVGDocument(svg, out, SVG_ENCODING);
      return new String(out.toByteArray(), SVG_ENCODING);
    }
    catch (UnsupportedEncodingException | ProcessingException e) {
      throw new JsonException("Failed to write SVG document", e);
    }
  }

  @Override
  public void handleUiEvent(JsonEvent event) {
    if (JsonEventType.APP_LINK.matches(event)) {
      handleUiAppLinkAction(event);
    }
    else {
      super.handleUiEvent(event);
    }
  }

  private void handleUiAppLinkAction(JsonEvent event) {
    String ref = event.getData().optString("ref");
    try {
      getModel().doAppLinkAction(ref);
    }
    catch (ProcessingException e) {
      throw new JsonException(e);
    }
  }

}
