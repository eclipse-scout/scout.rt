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
package org.eclipse.scout.rt.ui.html.json.notification;

import org.eclipse.scout.rt.client.ui.notification.INotification;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonWidget;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.JsonStatus;

/**
 * @since 8.0
 */
public class JsonNotification<NOTIFICATION extends INotification> extends AbstractJsonWidget<NOTIFICATION> {

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

    putJsonProperty(new JsonProperty<NOTIFICATION>("status", model) {
      @Override
      protected IStatus modelValue() {
        return getModel().getStatus();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return JsonStatus.toJson((IStatus) value);
      }
    });
  }
}
