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
package org.eclipse.scout.rt.ui.html.json;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * The {@link JsonStartupRequest} contains all information used to create a new {@link IUiSession} and a new
 * {@link IClientSession}. The information sources are listed in the following table
 * <ul>
 * <li>{@link JsonStartupRequest#PROP_PART_ID} - portlet part ID (attribute 'data-partid' of scout html element)</li>
 * <li>{@link JsonStartupRequest#PROP_CLIENT_SESSION_ID} - first one defined: argument to scout.init(),
 * sessionStorage.getItem('scout:clientSessionId'), current timestamp</li>
 * <li>{@link JsonStartupRequest#PROP_USER_AGENT} - first one defined: argument to scout.init(), default
 * scout.UserAgent.DEVICE_TYPE_DESKTOP</li>
 * <li>{@link JsonStartupRequest#PROP_SESSION_STARTUP_PARAMS} - contains session startup parameters to scout.init() as
 * well as all location url parameters and the url itself with key 'url'</li>
 * </ul>
 */
public class JsonStartupRequest extends JsonRequest {

  public static final String PROP_PART_ID = "partId";
  public static final String PROP_CLIENT_SESSION_ID = "clientSessionId";
  public static final String PROP_USER_AGENT = "userAgent";
  public static final String PROP_SESSION_STARTUP_PARAMS = "sessionStartupParams";

  private final Map<String, String> m_sessionStartupParams;

  public JsonStartupRequest(JsonRequest request) {
    super(request.getRequestObject());
    m_sessionStartupParams = parseSessionStartupParams(request.getRequestObject());
  }

  /**
   * @return partId or <code>"0"</code> (mandatory attribute)
   */
  public String getPartId() {
    return getRequestObject().optString(PROP_PART_ID, "0");
  }

  /**
   * @return clientSessionId or <code>null</code> (optional attribute)
   */
  public String getClientSessionId() {
    return getRequestObject().optString(PROP_CLIENT_SESSION_ID, null);
  }

  /**
   * @return userAgent or <code>null</code> (optional attribute)
   */
  public JSONObject getUserAgent() {
    return getRequestObject().optJSONObject(PROP_USER_AGENT);
  }

  /**
   * @return session startup parameters, or an empty {@link Map} if not provided (optional attribute)
   */
  public Map<String, String> getSessionStartupParams() {
    return m_sessionStartupParams;
  }

  protected Map<String, String> parseSessionStartupParams(JSONObject object) {
    JSONObject params = getRequestObject().optJSONObject(PROP_SESSION_STARTUP_PARAMS);
    if (params == null) {
      return Collections.emptyMap();
    }
    Map<String, String> map = new HashMap<>(params.length());
    JSONArray names = params.names();
    if (names != null) {
      for (int i = 0; i < names.length(); i++) {
        String name = names.getString(i);
        map.put(name, params.optString(name, null));
      }
    }
    return Collections.unmodifiableMap(map);
  }
}
