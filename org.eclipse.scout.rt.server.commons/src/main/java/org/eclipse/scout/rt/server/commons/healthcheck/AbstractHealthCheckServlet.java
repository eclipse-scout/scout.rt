/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.server.commons.healthcheck;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.server.commons.healthcheck.IHealthChecker.IHealthCheckCategory;
import org.eclipse.scout.rt.server.commons.servlet.AbstractHttpServlet;
import org.eclipse.scout.rt.server.commons.servlet.HttpServletControl;
import org.eclipse.scout.rt.server.commons.servlet.ServletExceptionTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>AbstractHealthCheckServlet</code> uses all available active {@link IHealthChecker} classes to determine the
 * application status. If the application status is OK, the servlet returns HTTP 200. In case any
 * <code>IHealthChecker</code> fails, the servlet returns HTTP 503.
 * <p>
 * This servlet can be used in combination with load balancers or reverse proxies that use an HTTP-GET or HTTP-HEAD
 * check method to determine the availability of the application.
 * <p>
 * An optional query parameter category may be specified, e.g. /status?category=startup, to run only some
 * {@link IHealthChecker} classes. See {@link IHealthChecker#acceptCategory(HealthCheckCategoryId)} for further
 * explanation of filtering.
 *
 * @since 6.1
 * @see AbstractHealthChecker
 */
public abstract class AbstractHealthCheckServlet extends AbstractHttpServlet {
  private static final long serialVersionUID = 1L;

  private static final Logger LOG = LoggerFactory.getLogger(AbstractHealthCheckServlet.class);

  public static final String QUERY_PARAMETER_NAME_CATEGORY = "category";

  protected abstract RunContext execCreateRunContext();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    disableCaching(req, resp);
    BEANS.get(HttpServletControl.class).doDefaults(this, req, resp);

    RunContext context;
    try {
      context = execCreateRunContext();
      if (context == null) {
        throw new IllegalArgumentException("context must not be null");
      }
    }
    catch (Throwable t) {
      LOG.error("Creating RunContext failed", t);
      throw BEANS.get(ServletExceptionTranslator.class).translate(t);
    }

    try {
      doChecks(context, req, resp);
    }
    catch (Throwable t) {
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

  /**
   * @param category
   *          nullable category to be checked; if category is null this parameter is ignored
   * @return <code>false</code> to ignore given <code>IHealthChecker</code>
   */
  protected boolean execAcceptCheck(IHealthChecker check, HealthCheckCategoryId category) {
    return (category == null || check.acceptCategory(category)) && check.isActive();
  }

  protected void doChecks(RunContext context, HttpServletRequest req, HttpServletResponse resp) throws IOException {
    HealthCheckCategoryId category = parseCategory(req);

    List<IHealthChecker> checks = getActiveHealthCheckers(category);
    List<IHealthChecker> failed = new ArrayList<>();

    for (IHealthChecker check : checks) {
      try {
        if (!check.checkHealth(context, category)) {
          failed.add(check);
        }
      }
      catch (Throwable t) {
        LOG.error("HealthChecker[{}] failed", check.getName(), t);
        failed.add(check);
      }
    }

    int statusCode = failed.isEmpty() ? HttpServletResponse.SC_OK : HttpServletResponse.SC_SERVICE_UNAVAILABLE;
    String output = generateOutput(statusCode, checks, failed, false);

    resp.setContentType("text/plain");
    resp.setStatus(statusCode);

    if (LOG.isDebugEnabled() || Platform.get().inDevelopmentMode()) {
      String detailedOutput = generateOutput(statusCode, checks, failed, true);
      LOG.debug(detailedOutput);
      resp.getWriter().print(Platform.get().inDevelopmentMode() ? detailedOutput : output);
    }
    else {
      resp.getWriter().print(output);
    }
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

  protected List<IHealthChecker> getActiveHealthCheckers(HealthCheckCategoryId category) {
    List<IHealthChecker> all = BEANS.all(IHealthChecker.class);
    List<IHealthChecker> actives = new ArrayList<>(all.size());
    for (IHealthChecker check : all) {
      try {
        if (execAcceptCheck(check, category)) {
          actives.add(check);
        }
        else {
          LOG.debug("HealthChecker[{}] was ignored (called with category {})", check.getName(), category);
        }
      }
      catch (Throwable t) {
        LOG.error("Active-check crashed with HealthChecker[{}]", check.getName(), t);
      }
    }
    return Collections.unmodifiableList(actives);
  }

  protected String generateOutput(int statusCode, List<IHealthChecker> checks, List<IHealthChecker> failed, boolean includeDetails) {
    StringBuilder buf = new StringBuilder();
    buf.append(statusCode);
    buf.append(' ');
    buf.append(failed.isEmpty() ? "OK" : "SERVICE_UNAVAILABLE");
    if (includeDetails) {
      for (IHealthChecker check : checks) {
        buf.append('\n');
        buf.append(check.getName());
        buf.append(':');
        buf.append(failed.contains(check) ? "ERROR" : "OK");
      }
    }
    return buf.toString();
  }

}
