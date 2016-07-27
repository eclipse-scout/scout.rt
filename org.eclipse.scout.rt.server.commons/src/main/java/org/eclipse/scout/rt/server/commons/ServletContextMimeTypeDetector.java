package org.eclipse.scout.rt.server.commons;

import java.nio.file.Path;

import javax.servlet.ServletContext;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.IMimeTypeDetector;

/**
 * Resolve mime types using the servlet context
 * <p>
 * In tomcat this is the conf/web.xml
 * <p>
 * see {@link IMimeTypeDetector}
 *
 * @author BSI AG
 * @since 5.2
 */
@Order(10)
@ApplicationScoped
public class ServletContextMimeTypeDetector implements IMimeTypeDetector {

  @Override
  public String getMimeType(Path path) {
    if (path == null) {
      return null;
    }
    ServletContext servletContext = BEANS.opt(ServletContext.class);
    if (servletContext == null) {
      return null;
    }
    String name = path.getFileName().toString();
    return servletContext.getMimeType(name);
  }
}
