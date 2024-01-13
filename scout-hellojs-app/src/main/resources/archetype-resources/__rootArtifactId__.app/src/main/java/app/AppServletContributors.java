#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.app;

import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.scout.rt.jetty.IServletContributor;
import org.eclipse.scout.rt.jetty.IServletFilterContributor;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.rest.RestApplication;
import org.eclipse.scout.rt.server.commons.HttpSessionMutex;
import org.eclipse.scout.rt.server.context.HttpServerRunContextFilter;
import org.eclipse.scout.rt.server.commons.servlet.filter.gzip.GzipServletFilter;
import org.eclipse.scout.rt.ui.html.UiHtmlConfigProperties.UiServletMultipartConfigProperty;
import org.eclipse.scout.rt.ui.html.UiServlet;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.servlet.ServletProperties;

import ${package}.api.RestAuthFilter;

/**
 * {@link IServletContributor} and {@link IServletFilterContributor} for app.
 */
public final class AppServletContributors {

  private AppServletContributors() {
  }

  @Order(10)
  public static class HttpSessionMutexFilterContributor implements IServletFilterContributor {

    @Override
    public void contribute(ServletContextHandler handler) {
      handler.addEventListener(new HttpSessionMutex());
    }
  }

  @Order(20)
  public static class GzipFilterContributor implements IServletFilterContributor {

    @Override
    public void contribute(ServletContextHandler handler) {
      handler.addFilter(GzipServletFilter.class, "/*", null);
    }
  }

  /**
   * Filters for API access.
   */
  @Order(30)
  public static class RestAuthFilterContributor implements IServletFilterContributor {

    @Override
    public void contribute(ServletContextHandler handler) {
      handler.addFilter(RestAuthFilter.class, "/api/*", null);
    }
  }

  @Order(40)
  public static class ApiServerRunContextFilterContributor implements IServletFilterContributor {

    @Override
    public void contribute(ServletContextHandler handler) {
      FilterHolder filter = handler.addFilter(HttpServerRunContextFilter.class, "/api/*", null);
      filter.setInitParameter("session", "false");
    }
  }

  /**
   * UI Servlet that provides resources (js, html, css, ...).
   */
  @Order(10)
  public static class UiServletContributor implements IServletContributor {

    @Override
    public void contribute(ServletContextHandler handler) {
      ServletHolder servletHolder = handler.addServlet(UiServlet.class, "/*");
      servletHolder.getRegistration().setMultipartConfig(CONFIG.getPropertyValue(UiServletMultipartConfigProperty.class));
    }
  }

  /**
   * JAX-RS Jersey Servlet.
   */
  @Order(20)
  public static class ApiServletContributor implements IServletContributor {

    @Override
    public void contribute(ServletContextHandler handler) {
      ServletHolder servlet = handler.addServlet(ServletContainer.class, "/api/*");
      servlet.setInitParameter(ServerProperties.WADL_FEATURE_DISABLE, Boolean.TRUE.toString());
      servlet.setInitParameter(ServletProperties.JAXRS_APPLICATION_CLASS, RestApplication.class.getName());
      servlet.setInitOrder(1); // load-on-startup
    }
  }
}
