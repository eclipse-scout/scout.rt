package org.eclipse.scout.rt.ui.html.json.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.service.IService;

/**
 * This interceptor contributes static web resources to the {@link AbstractJsonServlet}
 * <p>
 * {@link LocalBundleResourceProvider} serve files from the bundles 'WebContent' folder
 */
public interface IServletResourceProvider extends IService {
  /**
   * @return true if the request was consumed by the provider, no further action is then necessary
   */
  boolean handle(AbstractJsonServlet servlet, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException;

}
