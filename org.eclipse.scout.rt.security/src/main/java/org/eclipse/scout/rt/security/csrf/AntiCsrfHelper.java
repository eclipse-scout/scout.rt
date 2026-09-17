/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.security.csrf;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;

/**
 * Helper bean to include and validate the {@code X-Requested-With} HTTP header.
 * <p>
 * The default implementation assumes that
 * <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.1.1">RFC2616, section 9.1.1</a> is not violated,
 * by using GET, HEAD or OPTIONS requests for state changing operations.
 * <p>
 * The difference to the Jersey CsrfProtectionFilter is that this filter uses the standard header
 * {@code X-Requested-With} instead of {@code X-Requested-By} the Jersey filter uses.<br>
 * See also:
 * <ul>
 * <li><a href=
 * "https://cheatsheetseries.owasp.org/cheatsheets/Cross-Site_Request_Forgery_Prevention_Cheat_Sheet.html#Use_of_Custom_Request_Headers">OWASP
 * Cross-Site-Request-Forgery Prevention Cheat Sheet</a></li>
 * <li><a href="https://markitzeroday.com/x-requested-with/cors/2017/06/29/csrf-mitigation-for-ajax-requests.html">CSRF
 * Mitigation for AJAX Requests</a></li>
 * <li><a href="http://seclab.stanford.edu/websec/csrf/csrf.pdf">Robust Defenses for Cross-Site Request Forgery</a></li>
 * </ul>
 *
 */
@ApplicationScoped
public class AntiCsrfHelper {

  public static final String REQUESTED_WITH_HEADER = "X-Requested-With";
  public static final String REQUESTED_WITH_VALUE = "XMLHttpRequest";

  /**
   * Adds the {@value #REQUESTED_WITH_HEADER} header to the request if necessary.
   * <p>
   * This header prevents CSRF attacks on services if the server validates the existence
   * of the header. {@link #isValidRequest(Predicate, String, String)} can be used
   * for this purpose.
   *
   * @param headerWriter
   *     writer used to add HTTP headers to the outgoing request
   * @param method
   *     the HTTP method of the request (for example GET, POST, PUT, DELETE). It is used to determine if request is excluded
   * @param path
   *     the request path used to determine whether the request is excluded
   */
  public void prepareRequest(BiConsumer<String, Object> headerWriter, String method, String path) {
    if (isExcludedRequest(method, path)) {
      return;
    }

    headerWriter.accept(REQUESTED_WITH_HEADER, REQUESTED_WITH_VALUE);
  }

  /**
   * Checks if the request is excluded from validation or if the {@value #REQUESTED_WITH_HEADER} header is present and thus valid
   * <p>
   * This header must be added by clients.<br>
   * For Java clients use {@link #prepareRequest(BiConsumer, String, String)}.<br>
   * For AJAX requests from JavaScript the header is automatically included by jQuery.
   *
   * @param headerPredicate
   *     used to check whether the required request header is present
   * @param method
   *     the HTTP method of the request (for example GET, POST, PUT, DELETE). It is used to determine if request is excluded.
   * @param path
   *     the request path used to determine whether the request is excluded
   * @return {@code true} if the request is excluded from validation or contains the
   * required header; otherwise {@code false}
   */
  public boolean isValidRequest(Predicate<String> headerPredicate, String method, String path) {
    if (isExcludedRequest(method, path)) {
      return true;
    }

    return headerPredicate.test(REQUESTED_WITH_HEADER); // don't care about the value of the header
  }

  protected boolean isExcludedRequest(String method, String path) {
    return BEANS.all(IAntiCsrfFilterExclusion.class).stream().anyMatch(f -> f.isIgnored(method, path));
  }
}
