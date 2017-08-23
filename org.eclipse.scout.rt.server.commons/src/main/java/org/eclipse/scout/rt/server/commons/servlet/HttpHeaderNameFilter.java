package org.eclipse.scout.rt.server.commons.servlet;

import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * Simple {@link IHttpHeaderFilter} implementation for {@link HttpProxy} to remove or replace headers by name.
 */
public class HttpHeaderNameFilter implements IHttpHeaderFilter {

  public final String m_name;
  public final String m_replacement;

  /**
   * Removing {@link IHttpHeaderFilter}.
   */
  public HttpHeaderNameFilter(String name) {
    this(name, null);
  }

  /**
   * Replacing {@link IHttpHeaderFilter}.
   */
  public HttpHeaderNameFilter(String name, String replacement) {
    m_name = name;
    m_replacement = replacement;
  }

  @Override
  public String filter(String name, String value) {
    return StringUtility.equalsIgnoreCase(m_name, name) ? m_replacement : value;
  }

  public String getName() {
    return m_name;
  }

  public String getReplacement() {
    return m_replacement;
  }

}
