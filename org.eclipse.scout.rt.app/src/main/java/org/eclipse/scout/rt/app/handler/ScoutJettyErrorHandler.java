/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.app.handler;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.scout.rt.platform.Bean;
import org.json.JSONObject;

/**
 * This custom implementation omits sensitive information from error messages.
 * Specifically, it does not expose the servlet name, the internal URI, or stack traces.
 */
@Bean
public class ScoutJettyErrorHandler extends ErrorHandler {

  public ScoutJettyErrorHandler() {
    setShowMessageInTitle(false);
    setShowStacks(false);
  }

  @Override
  protected void writeErrorJson(Request request, PrintWriter writer, int code, String message, Throwable cause) {
    JSONObject json = new JSONObject();
    json.put("status", Integer.toString(code));
    json.put("message", message);
    writer.append(json.toString());
  }

  @Override
  protected void writeErrorHtmlMessage(Request request, Writer writer, int code, String message, Throwable cause, String uri) throws IOException {
    // suppress uri in html error message
    super.writeErrorHtmlMessage(request, writer, code, message, cause, "n/a");
  }

  @Override
  protected void writeErrorPlain(Request request, PrintWriter writer, int code, String message, Throwable cause) {
    writer.write("HTTP ERROR ");
    writer.write(Integer.toString(code));
    writer.write("\n");
    writer.printf("STATUS: %s%n", code);
    writer.printf("MESSAGE: %s%n", message);
  }
}
