package org.eclipse.scout.rt.ui.html.json.servlet;

import java.io.IOException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.service.IService;

/**
 * This interceptor contributes static web resources to the {@link AbstractJsonServlet}
 * <p>
 * {@link LocalBundleWebContentProvider} serve files from the bundles 'WebContent' folder
 */
public interface IServletWebContentProvider extends IService {

  /**
   * @return true if the request was consumed by the provider, no further action is then necessary
   */
  boolean handle(AbstractJsonServlet servlet, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException;

  /**
   * @return the URL to the specified resource, if it can be found in the bundle (or <code>null</code> if the resource
   *         does not exist in the bundle).
   */
  URL resolveBundleResource(String resourceName);
}
