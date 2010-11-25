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
package org.eclipse.scout.rt.server.servlet.filter;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.commons.Base64Utility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.http.servletfilter.FilterConfigInjection;
import org.eclipse.scout.rt.shared.services.common.security.SimplePrincipal;

/**
 * <h4>LDAPSecurityFilter</h4> The following properties can be set in the <code>config.ini</code> file:
 * <ul>
 * <li><code>&lt;fully qualified name of class&gt;#active=true/false</code> <b>might be set in the extension point</b></li>
 * <li><code>&lt;fully qualified name of class&gt;#realm=abcde</code> <b>required</b></li>
 * <li><code>&lt;fully qualified name of class&gt;#failover=true/false</code> <b>default false</b></li>
 * <li>
 * <code>&lt;fully qualified name of class&gt;#ldapServer=[e.g. ldap://100.100.29.4]</code> <b>required</b></li>
 * <li>
 * <code>&lt;fully qualified name of class&gt;#ldapBaseDN=[e.g. o=bsiag]</code> <b>required</b></li>
 * <li>
 * <code>&lt;fully qualified name of class&gt;#lDAPgroupDN=[e.g. ou=bsi_baden,ou=bsi_bern]</code> <b>required</b></li>
 * <li>
 * <code>&lt;fully qualified name of class&gt;#lDAPgroupAttributeId=[e.g. cn]</code> <b>required</b></li>
 * </ul>
 * <p>
 * 
 * @since 1.0.0 02.07.2008
 */
public class LDAPSecurityFilter extends AbstractChainableSecurityFilter {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(LDAPSecurityFilter.class);
  public static final String PROP_BASIC_ATTEMPT = "LDAPSecurityFilter.basicAttempt";

  private String m_serverUrl;
  private String m_baseDn;
  private String m_groupDn;
  private String m_groupAttr;

  public LDAPSecurityFilter() {
  }

  @Override
  public void init(FilterConfig config0) throws ServletException {
    super.init(config0);
    FilterConfigInjection.FilterConfig config = new FilterConfigInjection(config0, getClass()).getAnyConfig();
    m_serverUrl = getParam(config, "ldapServer", false);
    m_baseDn = getParam(config, "ldapBaseDN", true);
    m_groupDn = getParam(config, "lDAPgroupDN", true);
    m_groupAttr = getParam(config, "lDAPgroupAttributeId", true);
  }

  protected String getParam(FilterConfig filterConfig, String paramName, boolean nullAllowed) throws ServletException {
    String paramValue = filterConfig.getInitParameter(paramName);
    boolean exists = false;
    if (paramValue == null && nullAllowed) { // check if parameter exists
      Enumeration initParameterNames = filterConfig.getInitParameterNames();
      while (initParameterNames.hasMoreElements() && exists == false) {
        String object = (String) initParameterNames.nextElement();
        exists = object.equals(paramName);
      }
    }
    if (paramValue == null && !exists) throw new ServletException("Missing init-param with name '" + paramName + "'.");
    return paramValue;
  }

  @Override
  protected int negotiate(HttpServletRequest req, HttpServletResponse resp, PrincipalHolder holder) throws IOException, ServletException {
    String h = req.getHeader("Authorization");
    if (h != null && h.matches("Basic .*")) {
      String[] a = new String(Base64Utility.decode(h.substring(6)), "ISO-8859-1").split(":", 2);
      String user = a[0].toLowerCase();
      String pass = a[1];
      if (user != null && pass != null) {
        if (ldapLogin(m_serverUrl, m_baseDn, m_groupDn, m_groupAttr, user, pass, false/*
                                                                                       * show
                                                                                       * exceptions
                                                                                       */)) {
          // success
          holder.setPrincipal(new SimplePrincipal(user));
          return STATUS_CONTINUE_WITH_PRINCIPAL;
        }
      }
    }
    int attempts = getBasicAttempt(req);
    if (attempts > 2) {
      return STATUS_CONTINUE_CHAIN;
    }
    else {
      setBasicAttept(req, attempts + 1);
      resp.setHeader("WWW-Authenticate", "Basic realm=\"" + getRealm() + "\"");
      return STATUS_CONTINUE_CHAIN;
    }
  }

  private int getBasicAttempt(HttpServletRequest req) {
    int basicAtttempt = 0;
    Object attribute = req.getSession().getAttribute(PROP_BASIC_ATTEMPT);
    if (attribute instanceof Integer) {
      basicAtttempt = ((Integer) attribute).intValue();
    }
    return basicAtttempt;
  }

  private void setBasicAttept(HttpServletRequest req, int attempts) {
    req.getSession().setAttribute(PROP_BASIC_ATTEMPT, attempts);
  }

  /**
   * @param username
   *          e.g. KippingSte
   * @param server
   *          e.g. ldap://172.20.1.21
   * @param baseDN
   *          e.g. o=hermes
   * @param groupDN
   *          e.g. cn=AGENTS,ou=ks,ou=lm,ou=hlg,ou=zentrale <br>
   *          The user DN is defined within this group
   * @param attributeId
   *          e.g. equivalentToMe <br>
   *          Name of the user DN attribute
   * @return something like
   *         "cn=KippingSte,ou=KS,ou=LM,ou=HLG,ou=ZENTRALE,o=HERMES"
   * @throws ServletException
   */
  @SuppressWarnings("unchecked")
  protected String getUserDN(String username, String server, String baseDN, String groupDN, String attributeId) throws ServletException {

    String userDN = "";
    Hashtable env = new Hashtable();
    env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
    env.put(Context.PROVIDER_URL, server + "/" + baseDN);
    env.put(Context.SECURITY_PRINCIPAL, "" + "=" + "" + "," + "" + "," + baseDN);
    env.put(Context.SECURITY_CREDENTIALS, "");
    env.put(Context.LANGUAGE, "de");

    try {
      DirContext ldap = new InitialDirContext(env);
      Attributes attrs = ldap.getAttributes(groupDN, new String[]{attributeId});
      NamingEnumeration<? extends Attribute> equivalentToMe = attrs.getAll();

      Attribute attr = null;
      NamingEnumeration<?> allValues = null;
      String dn = null;
      String un = "";
      String[] parts;
      while (equivalentToMe.hasMore()) {
        attr = equivalentToMe.next();

        allValues = attr.getAll();

        while (allValues.hasMore()) {
          // dn is something like
          // "cn=KippingSte,ou=KS,ou=LM,ou=HLG,ou=ZENTRALE,o=HERMES"
          dn = (String) allValues.next();
          // extract userName (e.g. "BulinskyMir")
          if (dn.length() > 4) {
            un = dn.substring(3);
            parts = un.split(",");
            if (parts.length > 1) {
              un = parts[0].toLowerCase();
              if (username.equals(un)) {
                userDN = dn;
                break;
              }
            }
          }
        }
      }

    }
    catch (NamingException ne) {
      LOG.error("Exception in getting user DN from LDAP: " + ne);
      throw new SecurityException(ne.getMessage(), ne);
    }
    return userDN;
  }

  @SuppressWarnings("unchecked")
  private boolean ldapLogin(
      String server,
      String baseDN,
      String groupDN,
      String groupAttr,
      String username,
      String password,
      boolean showexceptions) throws ServletException {

    String userDN = getUserDN(username, server, baseDN, groupDN, groupAttr);
    if (userDN.equals("")) {
      return false;
    }
    Hashtable env = new Hashtable();
    env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
    env.put(Context.PROVIDER_URL, server + "/" + baseDN);
    env.put(Context.SECURITY_PRINCIPAL, userDN);
    env.put(Context.SECURITY_CREDENTIALS, password);
    env.put(Context.LANGUAGE, "de");

    try {
      new InitialDirContext(env);
      return true;
    }
    catch (NamingException ne) {
      if (showexceptions) ne.printStackTrace();
      return false;
    }

  }
}
