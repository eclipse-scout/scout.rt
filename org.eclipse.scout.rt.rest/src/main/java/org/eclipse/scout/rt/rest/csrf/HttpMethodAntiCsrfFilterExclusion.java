/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.csrf;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.container.ContainerRequestContext;

public class HttpMethodAntiCsrfFilterExclusion implements IAntiCsrfFilterExclusion {
  private final Set<String> m_methodsToIgnore;

  public HttpMethodAntiCsrfFilterExclusion() {
    Set<String> methodsToIgnore = new HashSet<>(3);
    methodsToIgnore.add("GET");
    methodsToIgnore.add("OPTIONS");
    methodsToIgnore.add("HEAD");
    methodsToIgnore.add("TRACE");
    adaptMethodsToIgnore(methodsToIgnore);
    m_methodsToIgnore = Collections.unmodifiableSet(methodsToIgnore);
  }

  @Override
  public boolean isIgnored(ContainerRequestContext requestContext) {
    return ignoreMethod(requestContext.getMethod());
  }

  @Override
  public boolean isIgnored(ClientRequestContext requestContext) {
    return ignoreMethod(requestContext.getMethod());
  }

  /**
   * Callback to modify the live list with HTTP methods to ignore. By default 'GET', 'OPTIONS' and 'HEAD' are ignored.
   * <p>
   * This default list is correct as long as that
   * <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.1.1">RFC2616, section 9.1.1</a> is not
   * violated, by using GET requests for state changing operations. As soon as GET, OPTIONS or HEAD methods are used for
   * state changing operations, this list must be adapted accordingly!
   * <p>
   * The methods must be added in upper case.
   */
  protected void adaptMethodsToIgnore(Set<String> methodsToIgnore) {
    // nop
  }

  protected boolean ignoreMethod(ClientRequestContext requestContext) {
    return ignoreMethod(requestContext.getMethod());
  }

  protected boolean ignoreMethod(String method) {
    return m_methodsToIgnore.contains(method.toUpperCase());
  }
}
