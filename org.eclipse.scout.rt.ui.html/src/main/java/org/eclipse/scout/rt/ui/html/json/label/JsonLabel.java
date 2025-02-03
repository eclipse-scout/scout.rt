/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.label;

import org.eclipse.scout.rt.client.ui.label.ILabel;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonWidget;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonEventType;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.res.BinaryResourceHolder;
import org.eclipse.scout.rt.ui.html.res.BinaryResourceUrlUtility;
import org.eclipse.scout.rt.ui.html.res.IBinaryResourceProvider;

public class JsonLabel<T extends ILabel> extends AbstractJsonWidget<T> implements IBinaryResourceProvider {

  public JsonLabel(T model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "Label";
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<ILabel>(ILabel.PROP_VALUE, model) {
      @Override
      protected String modelValue() {
        return getModel().getValue();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return BinaryResourceUrlUtility.replaceImageUrls(JsonLabel.this, (String) value);
      }
    });
    putJsonProperty(new JsonProperty<ILabel>(ILabel.PROP_HTML_ENABLED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isHtmlEnabled();
      }
    });
    putJsonProperty(new JsonProperty<ILabel>(ILabel.PROP_SCROLLABLE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isScrollable();
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
