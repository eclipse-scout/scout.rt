#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.ui.html.app;

import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.scout.rt.jetty.IServletContributor;
import org.eclipse.scout.rt.jetty.IServletFilterContributor;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.ui.html.app.UiServletContributors.AuthFilterContributor;

import ${package}.ui.html.UiServletFilter;

import java.util.Arrays;
import java.util.List;

/**
 * {@link IServletContributor} and {@link IServletFilterContributor} for UI server.
 */
public final class UiServletContributors {

  private UiServletContributors() {
  }

  @Replace
  public static class UiAuthFilterContributor extends AuthFilterContributor {

    @Override
    public void contribute(ServletContextHandler handler) {
      FilterHolder filter = handler.addFilter(UiServletFilter.class, "/*", null);
      filter.setInitParameter("filter-exclude", StringUtility.join("\n", getFilterExcludes()));
    }

    @Override
    protected List<String> getFilterExcludes() {
      List<String> filterExcludes = super.getFilterExcludes();
      filterExcludes.addAll(Arrays.asList(
        "/favicon/*",
        "/fonts/*",
        "/logo.png",
        "/*login*.js",
        "/*logout*.js",
        "/*${simpleArtifactName}-theme*.css"));
      return filterExcludes;
    }
  }
}
