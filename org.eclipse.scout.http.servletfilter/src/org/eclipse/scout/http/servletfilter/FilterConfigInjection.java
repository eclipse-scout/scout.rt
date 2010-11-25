/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.http.servletfilter;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.Filter;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.scout.commons.BundleContextUtility;
import org.eclipse.scout.commons.ConfigIniUtility;

/**
 * Wrapper to support filtering and replacing of init parameters using {@link ConfigIniUtility#getProperties(Class)}.
 * use inside {@link Filter#init(FilterConfig)}
 * <p>
 * This wrapper supports using config.ini property injection as in {@link BundleContextUtility#resolve(String)} and
 * {@link ConfigIniUtility#getProperties(Class)} to be used as filter init parameters.
 * <p>
 * This supports for using config.ini entries in the format filterClass#initPropertyName/filterAlias=value where value
 * can contain ${name} variables.
 * <p>
 * The special property "active" can be used to activate or deactivate the filter.
 * <p>
 * Examples of config.ini properties: org.myproject.filters.SecurityFilter#active=true <i>activates all filter of that
 * type (including subtypes) on al aliases</i> org.myproject.filters.SecurityFilter#active/shopping=true <i>activates
 * all filter of that type (including subtypes) on the alias /shopping</i>
 * org.myproject.filters.SecurityFilter#realm=acm.org <i>sets the init parameter "realm" to the value "acm.org" on all
 * filters of that type (including subtypes) on all aliases</i>
 * org.myproject.filters.SecurityFilter#realm/shopping=acm.org <i>sets the init parameter "realm" to the value "acm.org"
 * on all filters of that type (including subtypes) on the alias /shopping</i>
 * <p>
 * Example code
 * <p>
 * <code>
 * public class MyFilter implements Filter{
 *   private FilterConfigInjection m_injection;
 * 
 *   public void init(FilterConfig config0) throws ServletException {
 *     m_injection=new FilterConfigInjection(config0,getClass());
 *   }
 * 
 *   public void destroy() {
 *     m_injection=null;
 *   }
 * 
 *   public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
 *     //get injection config for this filter with this filter mapping
 *     FilterConfigInjection.FilterConfig config=m_injection.getConfig(req);
 *     if(!config.isActive(req)){
 *       chain.doFilter(req,res);
 *       return;
 *     }
 *     ...
 *   }
 * }
 * </code>
 */
public class FilterConfigInjection {
  private javax.servlet.FilterConfig m_config;
  private Class<? extends Filter> m_filterType;
  private Object m_configCacheLock;
  private Map<String, Map<String, String>> m_configCache;

  public FilterConfigInjection(javax.servlet.FilterConfig config, Class<? extends Filter> filterType) {
    m_config = config;
    m_filterType = filterType;
    m_configCacheLock = new Object();
    m_configCache = new HashMap<String, Map<String, String>>();
  }

  public FilterConfig getConfig(ServletRequest req) {
    if (req instanceof HttpServletRequest) {
      return getConfig(((HttpServletRequest) req).getServletPath());
    }
    else {
      return getConfig((String) null);
    }
  }

  /**
   * same as {@link #getConfig(null)}
   */
  public FilterConfig getAnyConfig() {
    return getConfig((String) null);
  }

  /**
   * @param path
   *          starting with / or a subpath or null for wildcard
   */
  public FilterConfig getConfig(String path) {
    if (path != null && path.startsWith("/")) {
      return new FilterConfig(getCachedPropertyMap(path));
    }
    else {
      return new FilterConfig(getCachedPropertyMap(null));
    }
  }

  private Map<String, String> getCachedPropertyMap(String path) {
    if (path == null) path = "";
    Map<String, String> map;
    synchronized (m_configCacheLock) {
      map = m_configCache.get(path);
      if (map == null) {
        if (path.length() == 0) {
          map = ConfigIniUtility.getProperties(m_filterType);
        }
        else {
          map = ConfigIniUtility.getProperties(m_filterType, path);
        }
        if (map == null) {
          map = new HashMap<String, String>();
        }
        m_configCache.put(path, map);
      }
    }
    return map;
  }

  public class FilterConfig implements javax.servlet.FilterConfig {
    private Map<String, String> m_injectedMap;

    public FilterConfig(Map<String, String> injectedMap) {
      m_injectedMap = injectedMap;
    }

    /**
     * Convenience for parameter "active"
     * 
     * @return true if there is no init parameter "active" or the init parameter
     *         "active" has the value "true"
     */
    public boolean isActive() {
      String activeText = getInitParameter("active");
      return activeText == null || activeText.equals("true");
    }

    public String getFilterName() {
      return m_config.getFilterName();
    }

    public ServletContext getServletContext() {
      return m_config.getServletContext();
    }

    public String getInitParameter(String name) {
      if (m_injectedMap.containsKey(name)) {
        return m_injectedMap.get(name);
      }
      else {
        String s = m_config.getInitParameter(name);
        return BundleContextUtility.resolve(s);
      }
    }

    @SuppressWarnings("unchecked")
    public Enumeration getInitParameterNames() {
      TreeSet<String> names = new TreeSet<String>(m_injectedMap.keySet());
      for (Enumeration en = m_config.getInitParameterNames(); en.hasMoreElements();) {
        names.add((String) en.nextElement());
      }
      return new Vector<String>(names).elements();
    }
  }
}
