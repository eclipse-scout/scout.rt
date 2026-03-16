/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons.healthcheck;

import java.io.IOException;
import java.io.Serial;
import java.util.Objects;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.dataobject.id.IIds;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.util.LazyValue;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.server.commons.healthcheck.IHealthChecker.IHealthCheckCategory;
import org.eclipse.scout.rt.server.commons.servlet.AbstractHttpServlet;
import org.eclipse.scout.rt.server.commons.servlet.HttpServletControl;
import org.eclipse.scout.rt.server.commons.servlet.ServletExceptionTranslator;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheControl;
import org.eclipse.scout.rt.server.commons.servlet.filter.LogFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

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
 * @see HealthCheckService
 * @see AbstractHealthChecker
 * @since 6.1
 */
public class HealthCheckServlet extends AbstractHttpServlet {
  @Serial
  private static final long serialVersionUID = 1L;

  private static final Logger LOG = LoggerFactory.getLogger(HealthCheckServlet.class);

  public static final String QUERY_PARAMETER_NAME_CATEGORY = "category";

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    req.setAttribute(LogFilter.NO_LOG_REQUEST_ATTRIBUTE, "X"); // prevent logging of calls to health servlet in LogFilter

    BEANS.get(HttpCacheControl.class).disableCaching(resp); // Never cache status requests.
    BEANS.get(HttpServletControl.class).doDefaults(this, req, resp);

    try {
      doChecks(req, resp);
    }
    catch (Throwable t) { //NOSONAR
      LOG.error("HealthChecking crashed", t);
      throw BEANS.get(ServletExceptionTranslator.class).translate(t);
    }
  }

  protected void doChecks(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    HealthCheckCategoryId category = parseCategory(req);
    HealthCheckResult result = BEANS.get(HealthCheckService.class).check(category);

    int statusCode = result.getFailedChecks().isEmpty() ? HttpServletResponse.SC_OK : HttpServletResponse.SC_SERVICE_UNAVAILABLE;
    String output = generateOutput(statusCode, result, category, false);

    resp.setContentType("text/plain");
    resp.setStatus(statusCode);

    LazyValue<String> detailedOutput = new LazyValue<>(() -> generateOutput(statusCode, result, category, true));
    boolean isDevelopmentMode = Platform.get().inDevelopmentMode();
    if (statusCode != HttpServletResponse.SC_OK) {
      LOG.warn("Status {}", StringUtility.replaceNewLines(detailedOutput.get(), ", "));
    }
    else if (LOG.isDebugEnabled() || isDevelopmentMode) {
      LOG.atLevel(isDevelopmentMode ? Level.INFO : Level.DEBUG).log("Status {}", StringUtility.replaceNewLines(detailedOutput.get(), ", "));
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

  protected String generateOutput(int statusCode, HealthCheckResult result, HealthCheckCategoryId category, boolean includeDetails) {
    StringBuilder buf = new StringBuilder();
    buf.append(statusCode);
    buf.append(' ');
    buf.append(result.isSuccess() ? "OK" : "SERVICE_UNAVAILABLE");
    buf.append("\nCategory:");
    buf.append(ObjectUtility.nvl(IIds.toString(category), "-"));
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
