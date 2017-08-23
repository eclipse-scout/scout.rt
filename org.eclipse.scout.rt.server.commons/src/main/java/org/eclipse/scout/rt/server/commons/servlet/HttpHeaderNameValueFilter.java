package org.eclipse.scout.rt.server.commons.servlet;

import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * Simple {@link IHttpHeaderFilter} implementation for {@link HttpProxy} to remove or replace headers by name and value.
 */
public class HttpHeaderNameValueFilter implements IHttpHeaderFilter {

  public final String m_name;
  public final String m_value;
  public final String m_replacement;

  /**
   * Removing {@link IHttpHeaderFilter}.
   */
  public HttpHeaderNameValueFilter(String name, String value) {
    this(name, value, null);
  }

  /**
   * Replacing {@link IHttpHeaderFilter}.
   */
  public HttpHeaderNameValueFilter(String name, String value, String replacement) {
    m_name = name;
    m_value = value;
    m_replacement = replacement;
  }

  @Override
  public String filter(String name, String value) {
    return StringUtility.equalsIgnoreCase(m_name, name) && ObjectUtility.equals(m_value, value) ? m_replacement : value;
  }

  public String getName() {
    return m_name;
  }

  public String getValue() {
    return m_value;
  }

  public String getReplacement() {
    return m_replacement;
  }

}
