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

import org.eclipse.scout.rt.platform.opentelemetry.ISpanAttributeMapper;
import org.eclipse.scout.rt.ui.html.json.JsonRequest;

import io.opentelemetry.api.trace.Span;

public class JsonRequestSpanAttributeMapper implements ISpanAttributeMapper<JsonRequest> {

  @Override
  public void addAttribute(Span span, JsonRequest source) {
    span.setAttribute("scout.client.json.request", source.toString());
  }
}
