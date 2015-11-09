/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.commons.servlet.filter.authentication;

import java.io.IOException;
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

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.security.SimplePrincipal;
import org.eclipse.scout.rt.platform.util.Base64Utility;
import org.eclipse.scout.rt.server.commons.cache.IHttpSessionCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <h4>LDAPSecurityFilter</h4> The following properties can be set in the <code>config.properties</code> file:
 * <ul>
 * <li><code>&lt;fully qualified name of class&gt;#active=true/false</code> <b>might be set in the extension point</b>
 * </li>
 * <li><code>&lt;fully qualified name of class&gt;#realm=abcde</code> <b>required</b></li>
 * <li><code>&lt;fully qualified name of class&gt;#failover=true/false</code> <b>default false</b></li>
 * <li><code>&lt;fully qualified name of class&gt;#ldapServer=[e.g. ldap://100.100.29.4]</code> <b>required</b></li>
 * <li><code>&lt;fully qualified name of class&gt;#ldapBaseDN=[e.g. o=bsiag]</code> <b>required</b></li>
 * <li><code>&lt;fully qualified name of class&gt;#lDAPgroupDN=[e.g. ou=bsi_baden,ou=bsi_bern]</code> <b>required</b>
 * </li>
 * <li><code>&lt;fully qualified name of class&gt;#lDAPgroupAttributeId=[e.g. cn]</code> <b>required</b></li>
 * </ul>
 * <p>
 *
 * @deprecated will be removed in release 6.0; is to be replaced with a project specific ServletFilter with the
 *             authenticators chained yourself; see depreciation note of {@link AbstractChainableSecurityFilter}
 */
@SuppressWarnings("deprecation")
@Deprecated
public class LDAPSecurityFilter extends AbstractChainableSecurityFilter {
  private static final Logger LOG = LoggerFactory.getLogger(LDAPSecurityFilter.class);
  public static final String PROP_BASIC_ATTEMPT = "LDAPSecurityFilter.basicAttempt";

  private String m_serverUrl;
  private String m_baseDn;
  private String m_groupDn;
  private String m_groupAttr;

  @Override
  public void destroy() {
  }

  @Override
  public void init(FilterConfig config) throws ServletException {
    super.init(config);
    m_serverUrl = config.getInitParameter("ldapServer");
    m_baseDn = config.getInitParameter("ldapBaseDN");
    m_groupDn = config.getInitParameter("ldapGroupDN");
    m_groupAttr = config.getInitParameter("ldapGroupAttributeId");
  }

  @Override
  protected int negotiate(HttpServletRequest req, HttpServletResponse resp, PrincipalHolder holder) throws IOException, ServletException {
    String h = req.getHeader("Authorization");
    if (h != null && h.matches("Basic .*")) {
      String[] a = new String(Base64Utility.decode(h.substring(6)), "ISO-8859-1").split(":", 2);
      String user = a[0].toLowerCase();
      String pass = a[1];
      if (user != null && pass != null) {
        if (ldapLogin(m_serverUrl, m_baseDn, m_groupDn, m_groupAttr, user, pass, false/* show exceptions */)) {
          // success
          holder.setPrincipal(new SimplePrincipal(user));
          return STATUS_CONTINUE_WITH_PRINCIPAL;
        }
      }
    }
    int attempts = getBasicAttempt(req, resp);
    if (attempts > 2) {
      return STATUS_CONTINUE_CHAIN;
    }
    else {
      setBasicAttept(req, resp, attempts + 1);
      resp.setHeader("WWW-Authenticate", "Basic realm=\"" + getRealm() + "\"");
      return STATUS_CONTINUE_CHAIN;
    }
  }

  private int getBasicAttempt(HttpServletRequest req, HttpServletResponse res) {
    int basicAtttempt = 0;
    Object attribute = BEANS.get(IHttpSessionCacheService.class).getAndTouch(PROP_BASIC_ATTEMPT, req, res);
    if (attribute instanceof Integer) {
      basicAtttempt = ((Integer) attribute).intValue();
    }
    return basicAtttempt;
  }

  private void setBasicAttept(HttpServletRequest req, HttpServletResponse res, int attempts) {
    BEANS.get(IHttpSessionCacheService.class).put(PROP_BASIC_ATTEMPT, attempts, req, res);
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
   * @return something like "cn=KippingSte,ou=KS,ou=LM,ou=HLG,ou=ZENTRALE,o=HERMES"
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
      LOG.error("Exception in getting user DN from LDAP: ", ne);
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
      if (showexceptions) {
        ne.printStackTrace();
      }
      return false;
    }

  }
}
