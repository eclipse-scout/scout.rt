/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.uuidpool;

import java.util.UUID;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.client.uuidpool.IUuidPool;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.json.JSONArray;
import org.json.JSONObject;

public class JsonUuidPool extends AbstractJsonAdapter<IUuidPool> {

  private static final String EVENT_REFILL = "refill";

  public JsonUuidPool(IUuidPool model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "UuidPool";
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    json.put("uuids", generateUuidsForJson(getModel().getSize()));
    return json;
  }

  protected JSONArray generateUuidsForJson(int count) {
    return new JSONArray(getModel().generateUuids(count).stream()
        .map(UUID::toString)
        .collect(Collectors.toList()));
  }

  @Override
  public void handleUiEvent(JsonEvent event) {
    if (EVENT_REFILL.equals(event.getType())) {
      handleUiRefill(event);
    }
    else {
      super.handleUiEvent(event);
    }
  }

  protected void handleUiRefill(JsonEvent event) {
    int count = event.getData().getInt("count");
    JSONObject jsonEvent = new JSONObject();
    jsonEvent.put("uuids", generateUuidsForJson(count));
    addActionEvent(EVENT_REFILL, jsonEvent);
  }
}
