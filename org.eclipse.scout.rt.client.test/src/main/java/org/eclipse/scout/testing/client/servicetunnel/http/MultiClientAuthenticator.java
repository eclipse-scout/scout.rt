/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.testing.client.servicetunnel.http;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;

/**
 * Authenticator implementation used for testing. It holds a session-to-user and a user-to-password mapping.
 * <p>
 * <b>Note</b>: the authenticator must be set as default authenticator:
 *
 * <pre>
 * Authenticator.setDefault(new MultiClientAuthenticator());
 * // set user-to-password mapping
 * MultiClientAuthenticator.addUser(&quot;alice&quot;, &quot;alice&quot;);
 * MultiClientAuthenticator.addUser(&quot;bob&quot;, &quot;bob&quot;);
 * // default user if no client session mapping is available
 * MultiClientAuthenticator.setDefaultUser(&quot;alice&quot;);
 * </pre>
 *
 * @deprecated will be removed in 'O' release.
 */
@Deprecated
public class MultiClientAuthenticator extends Authenticator {

  private static final Map<String, String> USER_PASSWORDS = new HashMap<String, String>();
  private static final Map<IClientSession, String> LOGIN_INFOS = new WeakHashMap<IClientSession, String>();
  private static String s_defaultUser;

  /**
   * Sets the default user if there is no session-to-user mapping available for the current session.
   *
   * @param username
   */
  public static void setDefaultUser(String username) {
    s_defaultUser = username;
  }

  /**
   * Adds a user along with its password.
   *
   * @param username
   * @param password
   */
  public static void addUser(String username, String password) {
    USER_PASSWORDS.put(username, password);
  }

  /**
   * Assigns the given session with a user.
   *
   * @param clientSession
   * @param username
   */
  public static void assignSessionToUser(IClientSession clientSession, String username) {
    LOGIN_INFOS.put(clientSession, username);
  }

  @Override
  protected PasswordAuthentication getPasswordAuthentication() {
    IClientSession currentSession = ClientSessionProvider.currentSession();
    String user = LOGIN_INFOS.get(currentSession);
    if (user == null) {
      user = s_defaultUser;
    }
    if (user == null) {
      return null;
    }
    String password = USER_PASSWORDS.get(user);
    return new PasswordAuthentication(user, password == null ? null : password.toCharArray());
  }
}
