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
package org.eclipse.scout.rt.ui.swing;

import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.scout.rt.client.IClientSession;

/**
 * This class is the base class for a typical Scout Swing application. See <code>ExtensibleSwingApplication</code> when
 * you must run multiple Scout Swing features in a single eclipse application.
 * 
 * @see ExtensibleSwingApplication
 * @author awe
 */
public abstract class AbstractSwingApplication extends BaseSwingApplication {

  private ISwingEnvironment m_env;

  protected abstract IClientSession getClientSession();

  public AbstractSwingApplication() {
    initialize();
  }

  @Override
  protected Object startInSubject(IApplicationContext context) throws Exception {
    if (!isClientSessionValid(getClientSession())) {
      return EXIT_OK;
    }
    return super.startInSubject(context);
  }

  @Override
  int runWhileActive() throws InterruptedException {
    while (true) {
      IClientSession clientSession = getClientSession();
      synchronized (clientSession.getStateLock()) {
        if (clientSession.isActive()) {
          clientSession.getStateLock().wait();
        }
        else {
          return clientSession.getExitCode();
        }
      }
    }
  }

  @Override
  void startGUI() {
    IClientSession clientSession = getClientSession();
    m_env.showGUI(clientSession);
    execSwingStarted(clientSession);
  }

  /**
   * Called just after the application model was created and is showing in the gui.
   * <p>
   * This method is called in the swing thread.
   */
  protected void execSwingStarted(IClientSession clientSession) {
  }

  @Override
  ISwingEnvironment getSwingEnvironment() {
    return m_env;
  }

  @Override
  void initializeSwing() {
    m_env = createSwingEnvironment();
  }

  protected ISwingEnvironment createSwingEnvironment() {
    return new DefaultSwingEnvironment();
  }

  @Override
  public void stop() {
    getClientSession().stopSession();
  }

}
