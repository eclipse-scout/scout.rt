package org.eclipse.scout.rt.ui.html.res;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletContext;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

/**
 * Default implementation of a {@link IWebContentResourceLocator} that searches in the complete jee environment (war
 * file)
 */
public class JEEWebContentResourceLocator implements IWebContentResourceLocator {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JEEWebContentResourceLocator.class);

  private ServletContext m_servletContext;

  public JEEWebContentResourceLocator(ServletContext servletContext) {
    m_servletContext = servletContext;
  }

  @Override
  public URL getScriptSource(String path) {
    if (path.startsWith("/")) {
      path = path.substring(1);
    }
    return getResourceImpl("/js/" + path);
  }

  @Override
  public URL getWebContentResource(String path) {
    if (path.startsWith("/")) {
      path = path.substring(1);
    }
    return getResourceImpl("/WebContent/" + path);
  }

  protected URL getResourceImpl(String resourcePath) {
    //disable hacker attacks using '..'
    if (resourcePath.contains("..")) {
      throw new IllegalArgumentException("path must not contain any '..'");
    }
    try {
      URL url = m_servletContext.getResource(resourcePath);
      if (url != null && LOG.isTraceEnabled()) {
        LOG.trace("locate resource '" + resourcePath + "' -> " + url);
      }
      return url;
    }
    catch (MalformedURLException e) {
      LOG.warn("Invalid URL: " + resourcePath, e);
      return null;
    }
  }
}
