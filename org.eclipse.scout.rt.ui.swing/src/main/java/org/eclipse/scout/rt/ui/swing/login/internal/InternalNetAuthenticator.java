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
package org.eclipse.scout.rt.ui.swing.login.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.HashSet;
import java.util.Set;

import javax.swing.SwingUtilities;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

public class InternalNetAuthenticator extends Authenticator {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(InternalNetAuthenticator.class);

  private Set<String> m_visitedKeys;

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
    try {
      showModalDialog(status);
    }
    catch (Throwable t) {
      LOG.error(getRequestingURL().toExternalForm(), t);
    }
    //
    if (status.isOk()) {
      if (status.isSavePassword()) {
        visitedKey = status.getUsername() + "@" + path;
        m_visitedKeys.add(visitedKey);
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

  private void showModalDialog(final AuthStatus status) throws Throwable {
    if (!SwingUtilities.isEventDispatchThread()) {
      try {
        SwingUtilities.invokeAndWait(
            new Runnable() {
              @Override
              public void run() {
                try {
                  showModalDialog(status);
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
    final LoginDialog dlg = new LoginDialog(status);
    dlg.setModal(true);
    dlg.setLocationRelativeTo(null);
    dlg.setVisible(true);
    // wait
    dlg.dispose();
  }
}
