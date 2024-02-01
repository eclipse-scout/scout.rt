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

import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.ServletHolder.Registration;
import org.eclipse.scout.rt.jetty.IServletContributor;
import org.eclipse.scout.rt.jetty.IServletFilterContributor;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.server.commons.HttpSessionMutex;
import org.eclipse.scout.rt.server.commons.healthcheck.HealthCheckServlet;
import org.eclipse.scout.rt.server.commons.servlet.filter.gzip.GzipServletFilter;
import org.eclipse.scout.rt.ui.html.UiServlet;
import org.eclipse.scout.rt.ui.html.app.filter.UiServletMultipartConfigFilter;

/**
 * {@link IServletContributor} and {@link IServletFilterContributor} for UI server.
 */
public final class UiServletContributors {

  private UiServletContributors() {
  }

  /**
   * First filter to be registered (low order on purpose); if the filter would be registered using
   * {@link Registration#setMultipartConfig(MultipartConfigElement)} it would as well be registered before any filters
   * are applied, see {@link ServletHolder#prepare(Request, ServletRequest, ServletResponse)}.
   * <p>
   * Early registration is also necessary as filter may already call {@link ServletRequest#getParameter(String)} which
   * would evaluate the multipart config property in {@link Request#extractContentParameters()}.
   *
   * @see UiServletMultipartConfigFilter
   */
  @Order(100)
  public static class UiServletMultipartConfigFilterContributor implements IServletFilterContributor {

    @Override
    public void contribute(ServletContextHandler handler) {
      handler.addFilter(UiServletMultipartConfigFilter.class, "/*", null);
    }
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
      handler.addServlet(UiServlet.class, "/*");
    }
  }
}
