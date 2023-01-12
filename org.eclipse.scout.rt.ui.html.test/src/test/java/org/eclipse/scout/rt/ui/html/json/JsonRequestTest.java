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

import static org.junit.Assert.assertEquals;

import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.json.JSONObject;
import org.junit.Test;

public class JsonRequestTest {

  @Test(expected = NullPointerException.class)
  public void testNull() {
    new JsonRequest(null);
  }

  @Test(expected = AssertionException.class)
  public void testMissingUiSessionId() {
    JSONObject json = new JSONObject();
    new JsonRequest(json);
  }

  @Test
  public void testStartupAndPingWithoutUiSessionId() {
    JSONObject json = new JSONObject();
    json.put("startup", true);
    new JsonRequest(json);

    json = new JSONObject();
    json.put("ping", true);
    new JsonRequest(json);
  }

  @Test
  public void testHasUiSessionId() {
    JSONObject json = new JSONObject();
    json.put("uiSessionId", "123");
    new JsonRequest(json);
  }

  @Test
  public void testAckNo() {
    JSONObject json = new JSONObject();
    json.put("uiSessionId", "123");
    json.put("#ACK", "777");
    JsonRequest req = new JsonRequest(json);
    assertEquals(Long.valueOf(777), req.getAckSequenceNo());
  }
}
