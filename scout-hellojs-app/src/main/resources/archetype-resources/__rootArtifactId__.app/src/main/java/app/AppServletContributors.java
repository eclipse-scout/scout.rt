#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.app;

import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.scout.rt.jetty.IServletContributor;
import org.eclipse.scout.rt.jetty.IServletFilterContributor;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.rest.RestApplication;
import org.eclipse.scout.rt.rest.ServletConstants;
import org.eclipse.scout.rt.server.context.ServerHttpRunContextFilter;
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
   */
  @Order(4000)
  public static class RestAuthFilterContributor implements IServletFilterContributor {

    @Override
    public void contribute(ServletContextHandler handler) {
      handler.addFilter(RestAuthFilter.class, ServletConstants.API_PATH_WITH_WILDCARD, null);
    }
  }

  @Order(5000)
  public static class ApiServerRunContextFilterContributor implements IServletFilterContributor {

    @Override
    public void contribute(ServletContextHandler handler) {
      handler.addFilter(ServerHttpRunContextFilter.class, ServletConstants.API_PATH_WITH_WILDCARD, null);
    }
  }

  /**
   * Register Jakarta RESTful Web Services Servlet from Jersey.
   */
  @Order(3000)
  public static class ApiServletContributor implements IServletContributor {

    @Override
    public void contribute(ServletContextHandler handler) {
      ServletHolder servlet = handler.addServlet(ServletContainer.class, ServletConstants.API_PATH_WITH_WILDCARD);
      servlet.setInitParameter(ServerProperties.WADL_FEATURE_DISABLE, Boolean.TRUE.toString());
      servlet.setInitParameter(ServletProperties.JAXRS_APPLICATION_CLASS, RestApplication.class.getName());
      servlet.setInitOrder(1); // load-on-startup
    }
  }
}
