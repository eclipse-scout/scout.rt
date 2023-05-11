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

import java.util.stream.Collectors;

import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.services.common.calendar.ICalendarItem;
import org.eclipse.scout.rt.ui.html.json.IJsonObject;
import org.json.JSONArray;
import org.json.JSONObject;

public class JsonCalendarItem implements IJsonObject {

  private final ICalendarItem m_item;

  public JsonCalendarItem(ICalendarItem item) {
    m_item = item;
  }

  public final ICalendarItem getItem() {
    return m_item;
  }

  @Override
  public JSONObject toJson() {
    if (m_item == null) {
      return null;
    }
    JSONObject json = new JSONObject();
    json.put("exists", m_item.exists());
    json.put("lastModified", m_item.getLastModified());
    json.put("itemId", m_item.getItemId());
    json.put("owner", m_item.getOwner());
    json.put("calendarId", m_item.getCalendarId());
    json.put("subject", m_item.getSubject());
    json.put("description", m_item.getDescription());
    json.put("cssClass", m_item.getCssClass());
    json.put("recurrencePattern", new JsonRecurrencePattern(m_item.getRecurrencePattern()).toJson());
    json.put("subjectLabel", m_item.getSubjectLabel());
    json.put("subjectAppLink", m_item.getSubjectAppLink());
    json.put("subjectIconId", m_item.getSubjectIconId());
    if (CollectionUtility.hasElements(m_item.getDescriptionElements())) {
      json.put("descriptionElements", new JSONArray(m_item.getDescriptionElements().stream().map(element -> {
        JSONObject jsonElement = new JSONObject();
        jsonElement.put("text", element.getText());
        jsonElement.put("iconId", element.getIconId());
        jsonElement.put("appLink", element.getAppLink());
        return jsonElement;
      }).collect(Collectors.toList())));
    }
    return json;
  }
}
