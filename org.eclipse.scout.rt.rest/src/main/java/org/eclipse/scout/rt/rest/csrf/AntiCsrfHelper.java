/*
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.rest.csrf;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.container.ContainerRequestContext;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.rest.client.AntiCsrfClientFilter;
import org.eclipse.scout.rt.rest.container.AntiCsrfContainerFilter;

/**
 * Helper bean to include and validate the {@code X-Requested-With} HTTP header.
 * <p>
 * The default implementation assumes that
 * <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.1.1">RFC2616, section 9.1.1</a> is not violated,
 * by using GET, HEAD or OPTIONS requests for state changing operations.
 * <p>
 * The difference to the Jersey CsrfProtectionFilter is that this filter uses the standard header
 * {@code X-Requested-With} instead of {@code X-Requested-By} the Jersey filter uses.
 *
 * @see AntiCsrfContainerFilter
 * @see AntiCsrfClientFilter
 * @see https://www.owasp.org/index.php/Cross-Site_Request_Forgery_(CSRF)_Prevention_Cheat_Sheet#Use_of_Custom_Request_Headers
 * @see https://markitzeroday.com/x-requested-with/cors/2017/06/29/csrf-mitigation-for-ajax-requests.html
 * @see http://seclab.stanford.edu/websec/csrf/csrf.pdf
 */
@ApplicationScoped
public class AntiCsrfHelper {

  public static final String REQUESTED_WITH_HEADER = "X-Requested-With";
  public static final String REQUESTED_WITH_VALUE = "";

  public AntiCsrfHelper() {

  }

  /**
   * Adds the {@value #REQUESTED_WITH_HEADER} header to the request if necessary.
   * <p>
   * This header prevents CSRF attacks on REST services if the server validates the existence of the header.
   * {@link #isValidRequest(ContainerRequestContext)} can be used for this
   */
  public void prepareRequest(ClientRequestContext requestContext) {
    if (BEANS.all(IAntiCsrfFilterExclusion.class).stream().map(f -> f.isIgnored(requestContext)).findAny().isPresent()) {
      return;
    }

    requestContext
        .getHeaders()
        .add(REQUESTED_WITH_HEADER, REQUESTED_WITH_VALUE);
  }

  /**
   * Only allows the request if the {@value #REQUESTED_WITH_HEADER} header is present or the HTTP method can be ignored.
   * <p>
   * This header must be added by clients.<br>
   * For Java clients use {@link #prepareRequest(ClientRequestContext)}.<br>
   * For AJAX requests from JavaScript the header is automatically included by jQuery.
   */
  public boolean isValidRequest(ContainerRequestContext requestContext) {
    if (BEANS.all(IAntiCsrfFilterExclusion.class).stream().anyMatch(f -> f.isIgnored(requestContext))) {
      return true;
    }

    return requestContext.getHeaders().containsKey(REQUESTED_WITH_HEADER); // don't care about the value of the header
  }
}
