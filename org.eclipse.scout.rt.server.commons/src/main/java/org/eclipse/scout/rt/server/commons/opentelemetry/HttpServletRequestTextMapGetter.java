/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons.opentelemetry;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.scout.rt.platform.ApplicationScoped;

import io.opentelemetry.context.propagation.TextMapGetter;

@ApplicationScoped
public class HttpServletRequestTextMapGetter implements TextMapGetter<HttpServletRequest> {

  @Override
  public Iterable<String> keys(HttpServletRequest carrier) {
    return () -> carrier.getHeaderNames().asIterator();
  }

  @Nullable
  @Override
  public String get(@Nullable HttpServletRequest carrier, String key) {
    String parameterValue = null;
    if (carrier != null) {
      parameterValue = carrier.getHeader(key);
    }
    return parameterValue;
  }
}
