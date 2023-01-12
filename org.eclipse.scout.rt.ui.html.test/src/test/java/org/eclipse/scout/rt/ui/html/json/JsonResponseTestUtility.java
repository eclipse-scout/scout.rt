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

import java.util.List;
import java.util.Map;

import org.json.JSONObject;

/**
 * Utility providing access to protected methods of {@link JsonResponse} (because it is in the same package).
 */
public final class JsonResponseTestUtility {

  private JsonResponseTestUtility() {
  }

  public static List<JsonEvent> eventList(JsonResponse response) {
    return response.eventList();
  }

  public static Map<String, IJsonAdapter<?>> adapterMap(JsonResponse response) {
    return response.adapterMap();
  }

  public static JSONObject startupData(JsonResponse response) {
    return response.startupData();
  }

  public static boolean error(JsonResponse response) {
    return response.error();
  }

  public static int errorCode(JsonResponse response) {
    return response.errorCode();
  }

  public static String errorMessage(JsonResponse response) {
    return response.errorMessage();
  }
}
