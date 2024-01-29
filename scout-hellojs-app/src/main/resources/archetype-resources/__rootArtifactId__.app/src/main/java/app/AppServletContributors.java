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
import org.eclipse.scout.rt.rest.RestApplication;
import org.eclipse.scout.rt.server.context.HttpServerRunContextFilter;
import org.eclipse.scout.rt.ui.html.app.UiServletContributors.GzipFilterContributor;
import org.eclipse.scout.rt.ui.html.app.UiServletContributors.UiServletContributor;
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

  // no auth filter on / for UiServlet required

  /**
   * Filters for API access.
   * <p>
   * After {@link GzipFilterContributor}.
   */
  @Order(4000)
  public static class RestAuthFilterContributor implements IServletFilterContributor {

    @Override
    public void contribute(ServletContextHandler handler) {
      handler.addFilter(RestAuthFilter.class, "/api/*", null);
    }
  }

  @Order(5000)
  public static class ApiServerRunContextFilterContributor implements IServletFilterContributor {

    @Override
    public void contribute(ServletContextHandler handler) {
      FilterHolder filter = handler.addFilter(HttpServerRunContextFilter.class, "/api/*", null);
      filter.setInitParameter("session", "false");
    }
  }

  /**
   * JAX-RS Jersey Servlet.
   * <p>
   * After {@link UiServletContributor}.
   */
  @Order(3000)
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
