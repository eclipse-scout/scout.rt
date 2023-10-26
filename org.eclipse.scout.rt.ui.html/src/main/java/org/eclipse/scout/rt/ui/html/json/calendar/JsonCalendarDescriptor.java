/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.calendar;

import org.eclipse.scout.rt.shared.services.common.calendar.ICalendarDescriptor;
import org.eclipse.scout.rt.ui.html.json.IJsonObject;
import org.json.JSONObject;

public class JsonCalendarDescriptor implements IJsonObject {

  private ICalendarDescriptor m_descriptor;

  public JsonCalendarDescriptor(ICalendarDescriptor descriptor) {
    this.m_descriptor = descriptor;
  }

  @Override
  public Object toJson() {
    if (m_descriptor == null) {
      return null;
    }
    JSONObject json = new JSONObject();
    json.put("calendarId", m_descriptor.getCalendarId());
    json.put("name", m_descriptor.getName());
    json.put("parentId", m_descriptor.getParentId());
    json.put("visible", m_descriptor.isVisible());
    json.put("selectable", m_descriptor.isSelectable());
    json.put("cssClass", m_descriptor.getCssClass());
    json.put("order", m_descriptor.getOrder());
    return json;
  }
}
