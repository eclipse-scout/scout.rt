/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.app;

import java.util.List;

import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.scout.rt.app.filter.ExceptionFilter;
import org.eclipse.scout.rt.jetty.IServletContributor;
import org.eclipse.scout.rt.jetty.IServletFilterContributor;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.rest.ApiRestApplication;
import org.eclipse.scout.rt.rest.ServletConstants;
import org.eclipse.scout.rt.server.commons.healthcheck.HealthCheckServlet;
import org.eclipse.scout.rt.server.context.ServerHttpRunContextFilter;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.servlet.ServletProperties;

/**
 * {@link IServletContributor} and {@link IServletFilterContributor} for backend server.
 */
public final class ServerServletContributors {

  private ServerServletContributors() {
  }

  @Order(750)
  public static class ExceptionFilterContributor implements IServletFilterContributor {

    @Override
    public void contribute(ServletContextHandler handler) {
      handler.addFilter(ExceptionFilter.class, "/*", null);
    }
  }

  /**
   * Subclasses must register a filter on `"/*"` that takes care of authentication. If no such filter is registered, all
   * resources provided by registered servlets are accessible without authentication.
   * <p>
   * The paths provided by {@link #getFilterExcludes()} should be excluded from authentication.
   */
  @Order(1000)
  public static class AuthFilterContributor implements IServletFilterContributor {

    /**
     * Use {@link #getFilterExcludes()} for a list of paths to exclude from authentication filter.
     */
    @Override
    public void contribute(ServletContextHandler handler) {
      // implement auth filter registration in subclass
    }

    /**
     * Values needs to be defined relative to application root path (which isn't always the same as servlet root path).
     *
     * @return Mutable list of filter excludes for authentication filter.
     */
    protected List<String> getFilterExcludes() {
      return CollectionUtility.arrayList(
          "/status" // see StatusServletContributor
      );
    }
  }

  /**
   * After {@link AuthFilterContributor}.
   */
  @Order(2000)
  public static class ApiServerRunContextFilterContributor implements IServletFilterContributor {

    @Override
    public void contribute(ServletContextHandler handler) {
      handler.addFilter(ServerHttpRunContextFilter.class, ServletConstants.API_PATH_WITH_WILDCARD, null);
    }
  }

  @Order(1000)
  public static class StatusServletContributor implements IServletContributor {

    @Override
    public void contribute(ServletContextHandler handler) {
      handler.addServlet(HealthCheckServlet.class, "/status");
    }
  }

  /**
   * JAX-RS Jersey Servlet.
   */
  @Order(3000)
  public static class ApiServletContributor implements IServletContributor {

    @Override
    public void contribute(ServletContextHandler handler) {
      ServletHolder servlet = handler.addServlet(ServletContainer.class, ServletConstants.API_PATH_WITH_WILDCARD);
      servlet.setInitParameter(ServerProperties.WADL_FEATURE_DISABLE, Boolean.TRUE.toString());
      servlet.setInitParameter(ServletProperties.JAXRS_APPLICATION_CLASS, ApiRestApplication.class.getName());
      servlet.setInitOrder(1); // load-on-startup
    }
  }
}
