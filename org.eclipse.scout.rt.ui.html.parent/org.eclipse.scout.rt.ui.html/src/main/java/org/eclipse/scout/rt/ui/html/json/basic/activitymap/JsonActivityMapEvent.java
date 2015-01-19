/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.basic.activitymap;

import org.eclipse.scout.rt.client.ui.basic.activitymap.ActivityMapEvent;
import org.eclipse.scout.rt.ui.html.json.IJsonMapper;
import org.json.JSONObject;

public class JsonActivityMapEvent implements IJsonMapper {

  private final ActivityMapEvent m_event;

  public JsonActivityMapEvent(ActivityMapEvent event) {
    m_event = event;
  }

  public final ActivityMapEvent getEvent() {
    return m_event;
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = new JSONObject();
    // TODO ActivityMap | Fill data
    return json;
  }
}
