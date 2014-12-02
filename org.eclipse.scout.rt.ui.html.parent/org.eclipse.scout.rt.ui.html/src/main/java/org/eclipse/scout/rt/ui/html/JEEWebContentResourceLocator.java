package org.eclipse.scout.rt.ui.html;

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
  public Script getScriptSource(String scriptPath) {
    if (scriptPath.startsWith("/")) {
      scriptPath = scriptPath.substring(1);
    }
    URL url = getResourceImpl(scriptPath);
    if (url != null) {
      return new Script(scriptPath, url, this);
    }
    url = getResourceImpl("src/main/js/" + scriptPath);
    if (url != null) {
      return new Script(scriptPath, url, this);
    }
    return null;
  }

  @Override
  public URL getWebContentResource(String resourcePath) {
    if (resourcePath.startsWith("/")) {
      resourcePath = resourcePath.substring(1);
    }
    return getResourceImpl("WebContent/" + resourcePath);
  }

  protected URL getResourceImpl(String resourcePath) {
    try {
      return m_servletContext.getResource("/" + resourcePath);
    }
    catch (MalformedURLException ex) {
      LOG.warn("resourcePath: " + resourcePath, ex);
      return null;
    }
  }
}
