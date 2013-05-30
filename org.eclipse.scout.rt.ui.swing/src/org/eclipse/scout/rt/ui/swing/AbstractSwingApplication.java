/*******************************************************************************
 * Copyright (c) 2010,2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swing;

import javax.swing.SwingUtilities;

import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.IClientSession;

/**
 * This class is the base class for a typical Scout Swing application. See <code>ExtensibleSwingApplication</code> when
 * you must run multiple Scout
 * Swing features in a single eclipse application.
 * 
 * @see ExtensibleSwingApplication
 * @author awe
 */
public abstract class AbstractSwingApplication extends BaseSwingApplication {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractSwingApplication.class);

  private ISwingEnvironment m_env;

  /**
   * @return The client session for this application. </br>
   *         May return a new instance every time it is called.
   */
  protected abstract IClientSession getClientSession();

  public AbstractSwingApplication() {
    initialize();
  }

  @Override
  protected Object startInSubject(IApplicationContext context) throws Exception {
    final IClientSession clientSession = getClientSession();
    if (!isClientSessionValid(clientSession)) {
      return EXIT_OK;
    }
    // Post-condition: session is active and loaded
    context.applicationRunning();
    stopSplashScreen();
    try {
      SwingUtilities.invokeAndWait(
          new Runnable() {
            @Override
            public void run() {
              m_env.showGUI(clientSession);
              execSwingStarted(clientSession);
            }
          }
          );
    }
    catch (Exception e) {
      LOG.warn("Error Starting GUI", e);
      System.exit(0);
    }
    return runWhileActive(clientSession);
  }

  /**
   * Blocks the main thread as as the client session is active.
   * 
   * @param clientSession
   * @return exit code {@link org.eclipse.equinox.app.IApplication#EXIT_OK EXIT_OK},
   *         {@link org.eclipse.equinox.app.IApplication#EXIT_RELAUNCH EXIT_RELAUNCH},
   *         {@link org.eclipse.equinox.app.IApplication#EXIT_RESTART EXIT_RESTART}
   * @throws InterruptedException
   */
  private int runWhileActive(IClientSession clientSession) throws InterruptedException {
    Object stateLock = clientSession.getStateLock();
    while (true) {
      synchronized (stateLock) {
        if (clientSession.isActive()) {
          stateLock.wait();
        }
        else {
          return clientSession.getExitCode();
        }
      }
    }
  }

  /**
   * Called just after the application model was created and is showing in the gui.
   * <p>
   * This method is called in the swing thread.
   */
  protected void execSwingStarted(IClientSession clientSession) {
  }

  @Override
  public ISwingEnvironment getSwingEnvironment() {
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
