/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.opentelemetry;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.scout.rt.platform.opentelemetry.ISpanAttributeMapper;

import io.opentelemetry.api.trace.Span;

public class HttpServletRequestSpanAttributeMapper implements ISpanAttributeMapper<HttpServletRequest> {

  @Override
  public void addAttribute(Span span, HttpServletRequest source) {
    span.setAttribute("http.request.method", source.getMethod());
    span.setAttribute("url.full", source.getRequestURI());
  }
}
