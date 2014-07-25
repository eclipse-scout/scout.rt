/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.login.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.HashSet;

import org.eclipse.scout.commons.SecurePreferencesUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.ui.rap.Activator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class InternalNetAuthenticator extends Authenticator {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(InternalNetAuthenticator.class);

  public static final boolean NET_AUTHENTICATION_CACHE_ENABLED;

  static {
    String s = Activator.getDefault().getBundle().getBundleContext().getProperty("java.net.authenticate.cache.enabled");
    NET_AUTHENTICATION_CACHE_ENABLED = s != null ? "true".equals(s) : false;
  }

  private HashSet<String> m_visitedKeys;

  public InternalNetAuthenticator() {
    m_visitedKeys = new HashSet<String>();
  }

  @Override
  protected PasswordAuthentication getPasswordAuthentication() {
    AuthStatus status = new AuthStatus();
    status.setUrl(getRequestingURL());
    status.setProxy(getRequestorType() == RequestorType.PROXY);
    String path = getRequestingURL().getHost() + getRequestingURL().getPath();
    String visitedKey = null;
    // check auto-login with user-saved credentials
    if (NET_AUTHENTICATION_CACHE_ENABLED) {
      try {
        String[] a = SecurePreferencesUtility.loadCredentials(path);
        if (a != null) {
          status.setUsername(a[0]);
          status.setPassword(a[1]);
          visitedKey = status.getUsername() + "@" + path;
          if (!m_visitedKeys.contains(visitedKey)) {
            m_visitedKeys.add(visitedKey);
            return new PasswordAuthentication(status.getUsername(), status.getPassword().toCharArray());
          }
        }
      }
      catch (Throwable t) {
        LOG.error(getRequestingURL().toExternalForm(), t);
      }
    }
    //
    try {
      Display display = Display.getDefault();
      showModalDialog(status, display);
    }
    catch (Throwable t) {
      LOG.error(getRequestingURL().toExternalForm(), t);
    }
    //
    if (status.isOk()) {
      if (status.isSavePassword()) {
        visitedKey = status.getUsername() + "@" + path;
        m_visitedKeys.add(visitedKey);
        if (status.isSavePassword()) {
          try {
            SecurePreferencesUtility.storeCredentials(path, status.getUsername(), status.getPassword());
          }
          catch (Throwable t) {
            LOG.error(getRequestingURL().toExternalForm(), t);
          }
        }
      }
      return new PasswordAuthentication(status.getUsername(), status.getPassword().toCharArray());
    }
    else if (status.isCancel()) {
      System.exit(0);
      return null;
    }
    else {
      return null;
    }
  }

  private void showModalDialog(final AuthStatus status, final Display display) throws Throwable {
    if (display.getThread() != Thread.currentThread()) {
      try {
        display.syncExec(
            new Runnable() {
              @Override
              public void run() {
                try {
                  showModalDialog(status, display);
                }
                catch (Throwable e) {
                  throw new UndeclaredThrowableException(e);
                }
              }
            }
            );
      }
      catch (Throwable ex) {
        Throwable t = ex;
        if (t instanceof InvocationTargetException) {
          if (t.getCause() != null) {
            t = t.getCause();
          }
        }
        else if (t instanceof UndeclaredThrowableException) {
          if (t.getCause() != null) {
            t = t.getCause();
          }
        }
        throw t;
      }
      return;
    }
    //
    Shell shell = new Shell(display);
    LoginDialog dlg = new LoginDialog(shell, SWT.NONE, status);
    dlg.open();
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch()) {
        // If no more entries in event queue
        display.sleep();
      }
    }
  }
}
