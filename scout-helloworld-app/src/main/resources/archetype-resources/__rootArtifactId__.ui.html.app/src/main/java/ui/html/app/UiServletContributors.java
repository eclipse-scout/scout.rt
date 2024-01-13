#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.ui.html.app;

import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.scout.rt.jetty.IServletContributor;
import org.eclipse.scout.rt.jetty.IServletFilterContributor;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.server.commons.HttpSessionMutex;
import org.eclipse.scout.rt.server.commons.servlet.filter.gzip.GzipServletFilter;
import org.eclipse.scout.rt.ui.html.UiHtmlConfigProperties.UiServletMultipartConfigProperty;
import org.eclipse.scout.rt.ui.html.UiServlet;

import ${package}.ui.html.UiServletFilter;

import java.util.Arrays;
import java.util.List;


/**
 * {@link IServletContributor} and {@link IServletFilterContributor} for UI server.
 */
public final class UiServletContributors {

  private UiServletContributors() {
  }

  @Order(10)
  public static class HttpSessionMutexFilterContributor implements IServletFilterContributor {

    @Override
    public void contribute(ServletContextHandler handler) {
      handler.addEventListener(new HttpSessionMutex());
    }
  }

  @Order(20)
  public static class AuthFilterContributor implements IServletFilterContributor {

    @Override
    public void contribute(ServletContextHandler handler) {
      FilterHolder filter = handler.addFilter(UiServletFilter.class, "/*", null);
      // values needs to be defined relative to application root path (which isn't always the same as servlet root path)
      List<String> filterExcludes = Arrays.asList(
        "/favicon/*",
        "/fonts/*",
        "/logo.png",
        "/*login*.js",
        "/*logout*.js",
        "/*${simpleArtifactName}-theme*.css");
      filter.setInitParameter("filter-exclude", StringUtility.join("\n", filterExcludes));
    }
  }

  @Order(30)
  public static class GzipFilterContributor implements IServletFilterContributor {

    @Override
    public void contribute(ServletContextHandler handler) {
      handler.addFilter(GzipServletFilter.class, "/*", null);
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
}
