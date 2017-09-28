/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
