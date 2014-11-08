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
package org.eclipse.scout.rt.ui.html.json;

import org.eclipse.scout.rt.client.IClientSession;
import org.json.JSONObject;

/**
 * The {@link JsonStartupRequest} contains all information used to create a new {@link IJsonSession} and a new
 * {@link IClientSession}.
 * The information sources are listed in the following table
 * <ul>
 * <li>tabId - calculated in scout.init() using the current timestamp</li>
 * <li>portletPartId - attribute 'data-partid' of scout html element</li>
 * <li>{@link JsonRequest#PROP_JSON_SESSION_ID} - concatenation as 'portletPartId:tabId'</li>
 * <li>{@link JsonStartupRequest#PROP_CLIENT_SESSION_ID} - first one defined: argument to scout.init(),
 * sessionStorage.getItem('scout:clientSessionId'), current timestamp</li>
 * <li>{@link JsonStartupRequest#PROP_USER_AGENT} - first one defined: argument to scout.init(), default
 * scout.UserAgent.DEVICE_TYPE_DESKTOP</li>
 * <li>{@link JsonStartupRequest#PROP_CUSTOM_PARAMS} - contains custom parameters to scout.init() as well as all
 * location url parameters</li>
 * </ul>
 */
public class JsonStartupRequest extends JsonRequest {

  public static final String PROP_CLIENT_SESSION_ID = "clientSessionId";
  public static final String PROP_USER_AGENT = "userAgent";
  public static final String PROP_CUSTOM_PARAMS = "customParams";

  public JsonStartupRequest(JsonRequest request) {
    super(request.getRequestObject());
  }

  public String getClientSessionId() {
    //required
    return JsonObjectUtility.getString(getRequestObject(), PROP_CLIENT_SESSION_ID);
  }

  public JSONObject getUserAgent() {
    //optional
    return getRequestObject().optJSONObject(PROP_USER_AGENT);
  }

  /**
   * These properties are passed to the {@link IClientSession#initCustomParams(Map<String, String>)}
   */
  public JSONObject getCustomParams() {
    //optional
    return getRequestObject().optJSONObject(PROP_CUSTOM_PARAMS);
  }

}
