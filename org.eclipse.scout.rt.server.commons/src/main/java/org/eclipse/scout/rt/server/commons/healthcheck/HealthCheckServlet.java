/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons.healthcheck;

import java.io.IOException;
import java.util.Objects;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.util.LazyValue;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.server.commons.healthcheck.IHealthChecker.IHealthCheckCategory;
import org.eclipse.scout.rt.server.commons.servlet.AbstractHttpServlet;
import org.eclipse.scout.rt.server.commons.servlet.HttpServletControl;
import org.eclipse.scout.rt.server.commons.servlet.ServletExceptionTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>HealthCheckServlet</code> uses {@link HealthCheckService} classes to determine the application status. If
 * the application status is OK, the servlet returns HTTP 200. In case any <code>IHealthChecker</code> fails, the
 * servlet returns HTTP 503.
 * <p>
 * This servlet can be used in combination with load balancers or reverse proxies that use an HTTP-GET or HTTP-HEAD
 * check method to determine the availability of the application.
 * <p>
 * An optional query parameter category may be specified, e.g. /status?category=startup, to run only some
 * {@link IHealthChecker} classes. See {@link IHealthChecker#acceptCategory(HealthCheckCategoryId)} for further
 * explanation of filtering.
 *
 * @since 6.1
 * @see HealthCheckService
 * @see AbstractHealthChecker
 */
public class HealthCheckServlet extends AbstractHttpServlet {
  private static final long serialVersionUID = 1L;

  private static final Logger LOG = LoggerFactory.getLogger(HealthCheckServlet.class);

  public static final String QUERY_PARAMETER_NAME_CATEGORY = "category";

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    disableCaching(req, resp);
    BEANS.get(HttpServletControl.class).doDefaults(this, req, resp);

    try {
      doChecks(req, resp);
    }
    catch (Throwable t) { //NOSONAR
      LOG.error("HealthChecking crashed", t);
      throw BEANS.get(ServletExceptionTranslator.class).translate(t);
    }
  }

  protected void disableCaching(HttpServletRequest req, HttpServletResponse resp) {
    // Never cache status requests.
    resp.setHeader("Cache-Control", "private, no-store, no-cache, max-age=0"); // HTTP 1.1
    resp.setHeader("Pragma", "no-cache"); // HTTP 1.0
    resp.setDateHeader("Expires", 0); // prevents caching at the proxy server
  }

  protected void doChecks(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    HealthCheckCategoryId category = parseCategory(req);
    HealthCheckResult result = BEANS.get(HealthCheckService.class).check(category);

    int statusCode = result.getFailedChecks().isEmpty() ? HttpServletResponse.SC_OK : HttpServletResponse.SC_SERVICE_UNAVAILABLE;
    String output = generateOutput(statusCode, result, false);

    resp.setContentType("text/plain");
    resp.setStatus(statusCode);

    LazyValue<String> detailedOutput = new LazyValue<>(() -> generateOutput(statusCode, result, true));
    boolean isDevelopmentMode = Platform.get().inDevelopmentMode();
    if (statusCode != HttpServletResponse.SC_OK) {
      LOG.warn("Status {}", StringUtility.replaceNewLines(detailedOutput.get(), ", "));
    }
    else if (LOG.isDebugEnabled() || isDevelopmentMode) {
      LOG.debug("Status {}", StringUtility.replaceNewLines(detailedOutput.get(), ", "));
    }
    resp.getWriter().print(isDevelopmentMode ? detailedOutput.get() : output);
  }

  /**
   * Extract the category (if any) from the request.
   *
   * @return <code>null</code> if no (valid?) category was provided
   */
  protected HealthCheckCategoryId parseCategory(HttpServletRequest req) {
    String inputCategory = StringUtility.nullIfEmpty(req.getParameter(QUERY_PARAMETER_NAME_CATEGORY));
    HealthCheckCategoryId category = null;
    if (inputCategory != null) {
      category = BEANS.all(IHealthCheckCategory.class)
          .stream()
          .map(IHealthCheckCategory::getId)
          .filter(c -> Objects.equals(c.unwrap(), inputCategory))
          .findFirst()
          .orElse(null);
      if (category == null) {
        LOG.error("Ignoring invalid category {} for health check", inputCategory);
      }
    }
    return category;
  }

  protected String generateOutput(int statusCode, HealthCheckResult result, boolean includeDetails) {
    StringBuilder buf = new StringBuilder();
    buf.append(statusCode);
    buf.append(' ');
    buf.append(result.isSuccess() ? "OK" : "SERVICE_UNAVAILABLE");
    if (includeDetails) {
      for (IHealthChecker check : result.getAllChecks()) {
        buf.append('\n');
        buf.append(check.getName());
        buf.append(':');
        buf.append(result.getFailedChecks().contains(check) ? "ERROR" : "OK");
      }
    }
    return buf.toString();
  }
}
