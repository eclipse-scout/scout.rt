/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.notification;

import org.eclipse.scout.rt.client.ui.notification.INotification;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonWidget;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonEventType;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.JsonStatus;

/**
 * @since 8.0
 */
public class JsonNotification<NOTIFICATION extends INotification> extends AbstractJsonWidget<NOTIFICATION> {

  public static final String EVENT_CLOSE = "close";

  public JsonNotification(NOTIFICATION model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "Notification";
  }

  @Override
  protected void initJsonProperties(NOTIFICATION model) {
    super.initJsonProperties(model);

    putJsonProperty(new JsonProperty<>("status", model) {
      @Override
      protected IStatus modelValue() {
        return getModel().getStatus();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return JsonStatus.toJson((IStatus) value);
      }
    });
    putJsonProperty(new JsonProperty<>("closable", model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isClosable();
      }
    });
    putJsonProperty(new JsonProperty<>("htmlEnabled", model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isHtmlEnabled();
      }
    });
  }

  @Override
  public void handleUiEvent(JsonEvent event) {
    if (EVENT_CLOSE.equals(event.getType())) {
      handleUiClose();
    }
    else if (JsonEventType.APP_LINK_ACTION.matches(event.getType())) {
      handleUiAppLinkAction(event);
    }
    else {
      super.handleUiEvent(event);
    }
  }

  protected void handleUiClose() {
    getModel().getUIFacade().fireClosedFromUI();
  }

  protected void handleUiAppLinkAction(JsonEvent event) {
    String ref = event.getData().optString("ref", null);
    getModel().getUIFacade().fireAppLinkActionFromUI(ref);
  }
}
