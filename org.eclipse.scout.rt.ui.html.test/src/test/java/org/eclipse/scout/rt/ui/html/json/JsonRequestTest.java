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
    assertEquals(Long.valueOf(777), req.getAckResponseSequenceNo());
  }
}
