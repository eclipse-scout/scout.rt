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

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.IClientSession;

/**
 * This class is the base class for a typical Scout Swing application.
 *
 * @author awe
 */
public abstract class AbstractSwingApplication extends BaseSwingApplication {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractSwingApplication.class);

  private ISwingEnvironment m_env;

  /**
   * @return The client session for this application. </br>
   *         May return a new instance every time it is called.
   */
  protected abstract IClientSession getClientSession() throws ProcessingException;

  public AbstractSwingApplication() {
  }

  @Override
  protected IClientSession startInSubject() throws Exception {
    initialize();

    final IClientSession clientSession;
    try {
      clientSession = getClientSession();
    }
    catch (Exception e) {
      LOG.warn("Unexpected error while getting client session", e);
      showLoadError(e);
      return null;
    }
    // Post-condition: session is active and loaded
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
    return clientSession;
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

}
