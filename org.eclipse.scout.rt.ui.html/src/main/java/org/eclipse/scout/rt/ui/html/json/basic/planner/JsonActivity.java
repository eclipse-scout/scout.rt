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
package org.eclipse.scout.rt.ui.html.json.basic.planner;

import org.eclipse.scout.rt.client.ui.basic.planner.Activity;
import org.eclipse.scout.rt.ui.html.json.IIdProvider;
import org.eclipse.scout.rt.ui.html.json.IJsonObject;
import org.eclipse.scout.rt.ui.html.json.JsonDate;
import org.eclipse.scout.rt.ui.html.json.JsonObjectUtility;
import org.json.JSONObject;

public class JsonActivity implements IJsonObject {

  private final IIdProvider<Activity<?, ?>> m_idProvider;
  private final Activity<?, ?> m_activity;

  public JsonActivity(Activity<?, ?> cell, IIdProvider<Activity<?, ?>> idProvider) {
    m_activity = cell;
    m_idProvider = idProvider;
  }

  public Activity<?, ?> getActivityCell() {
    return m_activity;
  }

  @Override
  public JSONObject toJson() {
    if (m_activity == null) {
      return null;
    }
    JSONObject json = new JSONObject();
    json.put("id", m_idProvider.getId(m_activity));
    json.put("beginTime", new JsonDate(m_activity.getBeginTime()).asJsonString());
    json.put("endTime", new JsonDate(m_activity.getEndTime()).asJsonString());
    json.put("text", m_activity.getText());
    json.put("backgroundColor", m_activity.getBackgroundColor());
    json.put("foregroundColor", m_activity.getForegroundColor());
    json.put("level", m_activity.getLevel());
    json.put("levelColor", m_activity.getLevelColor());
    json.put("tooltipText", m_activity.getTooltipText());
    json.put("cssClass", m_activity.getCssClass());
    JsonObjectUtility.filterDefaultValues(json, "Activity");
    return json;
  }
}
