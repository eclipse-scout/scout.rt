/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.ui.html.json.desktop;

import org.eclipse.scout.rt.client.ui.desktop.notification.IDesktopNotification;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.JsonStatus;
import org.eclipse.scout.rt.ui.html.json.notification.JsonNotification;
import org.json.JSONObject;

public class JsonDesktopNotification<DESKTOP_NOTIFICATION extends IDesktopNotification> extends JsonNotification<DESKTOP_NOTIFICATION> {

  public JsonDesktopNotification(DESKTOP_NOTIFICATION model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "DesktopNotification";
  }

  @Override
  protected void initJsonProperties(DESKTOP_NOTIFICATION model) {
    super.initJsonProperties(model);

    putJsonProperty(new JsonProperty<>("duration", model) {
      @Override
      protected Long modelValue() {
        return getModel().getDuration();
      }
    });

    putJsonProperty(new JsonProperty<>(IDesktopNotification.PROP_NATIVE_ONLY, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isNativeOnly();
      }
    });

    putJsonProperty(new JsonProperty<>(IDesktopNotification.PROP_NATIVE_NOTIFICATION_TITLE, model) {
      @Override
      protected String modelValue() {
        return getModel().getNativeNotificationTitle();
      }
    });

    putJsonProperty(new JsonProperty<>(IDesktopNotification.PROP_NATIVE_NOTIFICATION_STATUS, model) {
      @Override
      protected IStatus modelValue() {
        return getModel().getNativeNotificationStatus();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return JsonStatus.toJson((IStatus) value);
      }
    });

    putJsonProperty(new JsonProperty<>(IDesktopNotification.PROP_NATIVE_NOTIFICATION_VISIBILITY, model) {
      @Override
      protected String modelValue() {
        return getModel().getNativeNotificationVisibility();
      }
    });

    putJsonProperty(new JsonProperty<>(IDesktopNotification.PROP_NATIVE_NOTIFICATION_SHOWN, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isNativeNotificationShown();
      }
    });
  }

  @Override
  protected void handleUiPropertyChange(String propertyName, JSONObject data) {
    if (IDesktopNotification.PROP_NATIVE_NOTIFICATION_SHOWN.equals(propertyName)) {
      boolean shown = data.getBoolean(propertyName);
      addPropertyEventFilterCondition(IDesktopNotification.PROP_NATIVE_NOTIFICATION_SHOWN, shown);
      getModel().getUIFacade().setNativeNotificationShownFromUI(shown);
    }
    else {
      super.handleUiPropertyChange(propertyName, data);
    }
  }
}
