/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.log;

import java.io.IOException;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;

/**
 * Dynamically installed request filter that sets the {@link #NO_LOG_REQUEST_ATTRIBUTE} flag.
 */
public class NoLogFilter implements ContainerRequestFilter {

  /**
   * Name of the attribute that is set to the current request by {@link NoLogFilter}. The attribute value is irrelevant.
   * <b>Note:</b> Keep in sync with {@link org.eclipse.scout.rt.server.commons.servlet.filter.LogFilter#NO_LOG_REQUEST_ATTRIBUTE}
   */
  protected static final String NO_LOG_REQUEST_ATTRIBUTE = "scout.noLog";

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    requestContext.setProperty(NO_LOG_REQUEST_ATTRIBUTE, "X");
  }
}
