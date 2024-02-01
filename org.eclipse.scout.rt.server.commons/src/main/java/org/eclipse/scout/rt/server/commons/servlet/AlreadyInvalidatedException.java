/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons.servlet;

import java.lang.reflect.Method;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.exception.PlatformException;

/**
 * Exception thrown by {@link AbstractHttpServlet} when trying to access {@link HttpServletRequest} or
 * {@link HttpServletResponse} methods after the response has already been completed.
 */
public class AlreadyInvalidatedException extends PlatformException {

  private static final long serialVersionUID = -1;

  public AlreadyInvalidatedException(Method method, Object origin) {
    super("Access to '{}' is not allowed because {} is no longer valid (request has been completed).", method, origin != null ? origin.getClass() : null);
  }
}
