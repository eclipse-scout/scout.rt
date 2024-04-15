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

import javax.servlet.http.HttpServletRequest;

import org.eclipse.scout.rt.platform.ApplicationScoped;

import com.google.api.client.http.HttpRequest;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapSetter;

/**
 * A helper to provide a {@link TextMapSetter} and {@link TextMapGetter} for open telemetry context propagation.
 * <p>
 * A trace usually goes over multiple applications. To be able to continue the trace in another application, the context
 * has to be passed. The context properties are passed in the header of the HTTP request. The TextMapSetter/Getter
 * enables to write/extract the context into/from the request header.
 * </p>
 */
@ApplicationScoped
public interface IContextPropagationHelper {

  /**
   * Provides a setter for adding an {@link Context} to the request header of a {@link HttpRequest}.
   *
   * @return an implementation of the setter
   */
  TextMapSetter<HttpRequest> createHttpRequestTextMapSetter();

  /**
   * Provides a getter for extracting an {@link Context} out of the request header of a {@link HttpServletRequest}.
   *
   * @return an implementation of the getter
   */
  TextMapGetter<HttpServletRequest> createServletRequestTextMapGetter();
}
