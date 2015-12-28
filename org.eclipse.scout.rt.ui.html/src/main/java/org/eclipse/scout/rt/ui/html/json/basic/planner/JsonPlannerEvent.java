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

import org.eclipse.scout.rt.client.ui.basic.planner.PlannerEvent;
import org.eclipse.scout.rt.ui.html.json.IJsonObject;
import org.json.JSONObject;

public class JsonPlannerEvent implements IJsonObject {

  private final PlannerEvent m_event;

  public JsonPlannerEvent(PlannerEvent event) {
    m_event = event;
  }

  public final PlannerEvent getEvent() {
    return m_event;
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = new JSONObject();
    // TODO [5.2] bsh: ActivityMap | Fill data
    return json;
  }
}
