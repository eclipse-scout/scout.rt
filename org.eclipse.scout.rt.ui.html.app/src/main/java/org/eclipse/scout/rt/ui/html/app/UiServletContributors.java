/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.app;

import java.util.List;

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.scout.rt.jetty.IServletContributor;
import org.eclipse.scout.rt.jetty.IServletFilterContributor;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.server.commons.HttpSessionMutex;
import org.eclipse.scout.rt.server.commons.healthcheck.HealthCheckServlet;
import org.eclipse.scout.rt.server.commons.servlet.filter.gzip.GzipServletFilter;
import org.eclipse.scout.rt.ui.html.UiHtmlConfigProperties.UiServletMultipartConfigProperty;
import org.eclipse.scout.rt.ui.html.UiServlet;

/**
 * {@link IServletContributor} and {@link IServletFilterContributor} for UI server.
 */
public final class UiServletContributors {

  private UiServletContributors() {
  }

  @Order(1000)
  public static class HttpSessionMutexFilterContributor implements IServletFilterContributor {

    @Override
    public void contribute(ServletContextHandler handler) {
      handler.addEventListener(new HttpSessionMutex());
    }
  }

  /**
   * Subclasses must register a filter on `"/*"` that takes care of authentication. If no such filter is registered, all
   * resources provided by registered servlets (e.g. {@link UiServletContributor}) are accessible without
   * authentication.
   * <p>
   * The paths provided by {@link #getFilterExcludes()} should be excluded from authentication.
   */
  @Order(2000)
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

  @Order(3000)
  public static class GzipFilterContributor implements IServletFilterContributor {

    @Override
    public void contribute(ServletContextHandler handler) {
      handler.addFilter(GzipServletFilter.class, "/*", null);
    }
  }

  @Order(1000)
  public static class StatusServletContributor implements IServletContributor {

    @Override
    public void contribute(ServletContextHandler handler) {
      handler.addServlet(HealthCheckServlet.class, "/status");
    }
  }

  @Order(2000)
  public static class UiServletContributor implements IServletContributor {

    @Override
    public void contribute(ServletContextHandler handler) {
      ServletHolder servletHolder = handler.addServlet(UiServlet.class, "/*");
      servletHolder.getRegistration().setMultipartConfig(CONFIG.getPropertyValue(UiServletMultipartConfigProperty.class));
    }
  }
}
