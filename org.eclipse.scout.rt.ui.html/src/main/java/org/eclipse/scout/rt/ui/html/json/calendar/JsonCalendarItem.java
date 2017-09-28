/*******************************************************************************
 * Copyright (c) 2014-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.calendar;

import org.eclipse.scout.rt.shared.services.common.calendar.ICalendarItem;
import org.eclipse.scout.rt.ui.html.json.IJsonObject;
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
    json.put("subject", m_item.getSubject());
    json.put("description", m_item.getDescription());
    json.put("cssClass", m_item.getCssClass());
    json.put("recurrencePattern", new JsonRecurrencePattern(m_item.getRecurrencePattern()).toJson());
    return json;
  }
}
