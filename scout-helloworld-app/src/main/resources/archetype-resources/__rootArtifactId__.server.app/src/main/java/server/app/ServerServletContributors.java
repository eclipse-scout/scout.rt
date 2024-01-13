#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.server.app;

import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.scout.rt.jetty.IServletContributor;
import org.eclipse.scout.rt.jetty.IServletFilterContributor;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.server.ServiceTunnelServlet;
import org.eclipse.scout.rt.server.admin.diagnostic.DiagnosticServlet;

import ${package}.server.ServerServletFilter;

import java.util.Arrays;
import java.util.List;

/**
 * {@link IServletContributor} and {@link IServletFilterContributor} for backend server.
 */
public final class ServerServletContributors {

  private ServerServletContributors() {
  }

  @Order(10)
  public static class AuthFilterContributor implements IServletFilterContributor {

    @Override
    public void contribute(ServletContextHandler handler) {
      FilterHolder filter = handler.addFilter(ServerServletFilter.class, "/*", null);
      // values needs to be defined relative to application root path (which isn't always the same as servlet root path)
      List<String> filterExcludes = Arrays.asList(
        "/jaxws");
      filter.setInitParameter("filter-exclude", StringUtility.join("\n", filterExcludes));
    }
  }

  @Order(10)
  public static class ServiceTunnelServletContributor implements IServletContributor {

    @Override
    public void contribute(ServletContextHandler handler) {
      handler.addServlet(ServiceTunnelServlet.class, "/process");
    }
  }

  @Order(20)
  public static class DiagnosticsServletContributor implements IServletContributor {

    @Override
    public void contribute(ServletContextHandler handler) {
      handler.addServlet(DiagnosticServlet.class, "/diagnostics");
    }
  }
}
