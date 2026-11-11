/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.server.commons.BufferedServletOutputStream;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.UiException;
import org.eclipse.scout.rt.ui.html.json.testing.JsonTestUtility;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class JsonMessageRequestHandlerTest {

  @Test
  public void testUnreadableRequest() throws IOException {
    assertEquals(JsonResponse.ERR_UI_PROCESSING, handlePostAndGetErrorCode("{invalid"));
  }

  @Test
  public void testFailingStartupRequest() throws IOException {
    JsonMessageRequestHandler handler = new JsonMessageRequestHandler() {
      @Override
      protected IUiSession createUiSession(HttpServletRequest req, HttpServletResponse resp, JsonStartupRequest jsonStartupRequest) {
        throw new UiException("Startup failed");
      }
    };
    assertEquals(JsonResponse.ERR_STARTUP_FAILED, handlePostAndGetErrorCode(handler, "{\"startup\":true}"));
  }

  protected int handlePostAndGetErrorCode(String jsonData) throws IOException {
    return handlePostAndGetErrorCode(new JsonMessageRequestHandler(), jsonData);
  }

  protected int handlePostAndGetErrorCode(JsonMessageRequestHandler handler, String jsonData) throws IOException {
    HttpServletRequest req = JsonTestUtility.createHttpServletRequest(JsonTestUtility.createHttpSession(new Object()), "/json", jsonData);
    try (BufferedServletOutputStream out = new BufferedServletOutputStream()) {
      handler.handlePost(req, JsonTestUtility.createHttpServletResponse(out));
      JSONObject response = new JSONObject(new String(out.getContent(), StandardCharsets.UTF_8));
      return response.getJSONObject(JsonResponse.PROP_ERROR).getInt(JsonResponse.PROP_ERROR_CODE);
    }
  }
}
