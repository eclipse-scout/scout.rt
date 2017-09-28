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
