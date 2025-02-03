/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json;

import java.util.Date;

import org.eclipse.scout.rt.platform.util.Range;
import org.json.JSONObject;

public class JsonDateRange implements IJsonObject {

  private final Range<Date> m_range;

  public JsonDateRange(Range<Date> range) {
    m_range = range;
  }

  public JsonDateRange(Date from, Date to) {
    m_range = new Range<>(from, to);
  }

  @Override
  public Object toJson() {
    JSONObject json = new JSONObject();
    json.put("from", new JsonDate(m_range.getFrom()).asJsonString());
    json.put("to", new JsonDate(m_range.getTo()).asJsonString());
    return json;
  }
}
