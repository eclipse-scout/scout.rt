///*******************************************************************************
// * Copyright (c) 2010 BSI Business Systems Integration AG.
// * All rights reserved. This program and the accompanying materials
// * are made available under the terms of the Eclipse Public License v1.0
// * which accompanies this distribution, and is available at
// * http://www.eclipse.org/legal/epl-v10.html
// *
// * Contributors:
// *     BSI Business Systems Integration AG - initial API and implementation
// ******************************************************************************/
//package org.eclipse.scout.rt.ui.json.testing;
//
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Locale;
//
//import javax.servlet.http.HttpServletRequest;
//
//import org.eclipse.scout.rt.ui.json.IJsonSession;
//import org.eclipse.scout.rt.ui.json.JsonEvent;
//import org.eclipse.scout.rt.ui.json.JsonResponse;
//import org.json.JSONException;
//import org.json.JSONObject;
//import org.mockito.Mockito;
//
//public class JsonTestUtility {
//
//  public static IJsonSession createAndInitializeJsonSession() {
//    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
//    Mockito.when(request.getLocale()).thenReturn(new Locale("de_CH"));
//
//    IJsonSession jsonSession = new TestEnvironmentJsonSession();
//    jsonSession.init(request, "1.1");
//
//    return jsonSession;
//  }
//
//  public static JsonEvent createJsonEvent(String type) throws JSONException {
//    return createJsonEvent(type, null);
//  }
//
//  public static JsonEvent createJsonEvent(String type, String id) throws JSONException {
//    JSONObject jsonObject = new JSONObject();
//    jsonObject.put(JsonEvent.TYPE, type);
//    jsonObject.put(JsonEvent.ID, id);
//    return new JsonEvent(jsonObject);
//  }
//
//  /**
//   * @return all events of the given type. Never null.
//   */
//  public static List<JSONObject> extractEventsFromResponse(JsonResponse response, String eventType) throws JSONException {
//    List<JSONObject> list = new LinkedList<>();
//    for (JSONObject responseEvent : response.getEventList()) {
//      if (eventType.equals(responseEvent.getString(JsonEvent.TYPE))) {
//        list.add(responseEvent);
//      }
//    }
//    return list;
//  }
//
//}
