/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.calendar;

import org.eclipse.scout.rt.shared.services.common.calendar.RecurrencePattern;
import org.eclipse.scout.rt.ui.html.json.IJsonObject;
import org.eclipse.scout.rt.ui.html.json.JsonDate;
import org.json.JSONObject;

public class JsonRecurrencePattern implements IJsonObject {

  private final RecurrencePattern m_recurrencePattern;

  public JsonRecurrencePattern(RecurrencePattern recurrencePattern) {
    m_recurrencePattern = recurrencePattern;
  }

  public final RecurrencePattern getRecurrencePattern() {
    return m_recurrencePattern;
  }

  @Override
  public JSONObject toJson() {
    if (m_recurrencePattern == null) {
      return null;
    }
    JSONObject json = new JSONObject();
    json.put("lastModified", m_recurrencePattern.getLastModified());
    json.put("regenerate", m_recurrencePattern.isRegenerate());
    json.put("startTimeMinutes", m_recurrencePattern.getStartTimeMinutes());
    json.put("endTimeMinutes", m_recurrencePattern.getEndTimeMinutes());
    json.put("durationMinutes", m_recurrencePattern.getDurationMinutes());
    json.put("firstDate", new JsonDate(m_recurrencePattern.getFirstDate()).asJsonString());
    json.put("lastDate", new JsonDate(m_recurrencePattern.getLastDate()).asJsonString());
    json.put("occurrences", m_recurrencePattern.getOccurrences());
    json.put("noEndDate", m_recurrencePattern.getNoEndDate());
    json.put("type", m_recurrencePattern.getType());
    json.put("interval", m_recurrencePattern.getInterval());
    json.put("instance", m_recurrencePattern.getInstance());
    json.put("dayOfWeekBits", m_recurrencePattern.getDayOfWeek());
    json.put("dayOfMonth", m_recurrencePattern.getDayOfMonth());
    json.put("monthOfYear", m_recurrencePattern.getMonthOfYear());
    // TODO [5.2] bsh: Add RecurrenceException?
    return json;
  }
}
