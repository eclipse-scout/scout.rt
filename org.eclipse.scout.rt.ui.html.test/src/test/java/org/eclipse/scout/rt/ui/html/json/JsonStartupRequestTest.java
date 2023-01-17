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

import static org.junit.Assert.*;

import org.json.JSONObject;
import org.junit.Test;

public class JsonStartupRequestTest {

  @Test(expected = NullPointerException.class)
  public void testNull() {
    new JsonStartupRequest(null);
  }

  @Test
  public void testNotStartupRequest() {
    JSONObject json = new JSONObject();
    json.put("ping", true);
    JsonRequest jsonReq = new JsonRequest(json);

    JsonStartupRequest startupReq = new JsonStartupRequest(jsonReq);

    assertNull(startupReq.getAckSequenceNo());
    assertEquals("0", startupReq.getPartId());
    assertNull(startupReq.getUserAgent());
    assertEquals(0, startupReq.getSessionStartupParams().size());
  }

  @Test
  public void testStartupRequest() {
    JSONObject ua = new JSONObject();
    ua.put("deviceType", "DESKTOP");
    ua.put("touch", true);

    JSONObject params = new JSONObject();
    params.put("url", "http://localhost/xyz");
    params.put("noValue", JSONObject.NULL);

    JSONObject json = new JSONObject();
    json.put("startup", true);
    json.put("partId", 512);
    json.put("clientSessionId", "A-B-C");
    json.put("userAgent", ua);
    json.put("sessionStartupParams", params);
    JsonRequest jsonReq = new JsonRequest(json);

    JsonStartupRequest startupReq = new JsonStartupRequest(jsonReq);

    assertNull(startupReq.getAckSequenceNo());
    assertEquals("512", startupReq.getPartId());
    assertTrue(startupReq.getUserAgent().optBoolean("touch"));
    assertEquals("http://localhost/xyz", startupReq.getSessionStartupParams().get("url"));
    assertTrue(startupReq.getSessionStartupParams().containsKey("noValue"));
    assertNull(startupReq.getSessionStartupParams().get("noValue"));
  }
}
