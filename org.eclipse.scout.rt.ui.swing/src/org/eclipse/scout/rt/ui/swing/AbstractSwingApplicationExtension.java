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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.shared.ui.UiDeviceType;
import org.eclipse.scout.rt.shared.ui.UiLayer;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.eclipse.scout.rt.ui.swing.extension.ISwingApplicationExtension;

/**
 * Abstract base class for swing application extensions.
 * 
 * @author awe
 */
public abstract class AbstractSwingApplicationExtension implements ISwingApplicationExtension {

  private String m_extensionId;

  private IClientSession m_clientSession;

  private ISwingEnvironment m_environment;

  public AbstractSwingApplicationExtension(String extensionId) {
    m_extensionId = extensionId;
  }

  @Override
  public String getExtensionId() {
    return m_extensionId;
  }

  @Override
  public void initializeSwing() {
    m_environment = createEnvironment();
  }

  private UserAgent getUserAgent() {
    return UserAgent.create(UiLayer.SWING, UiDeviceType.DESKTOP);
  }

  /**
   * Implement to provide a project specific client session instance.
   * 
   * @param userAgent
   * @return
   */
  abstract protected IClientSession createClientSession(UserAgent userAgent);

  /**
   * Implement to provide a project specific Swing environment.
   * 
   * @return
   */
  abstract protected ISwingEnvironment createEnvironment();

  @Override
  public Object execStart(IApplicationContext context, IProgressMonitor progressMonitor) throws Exception {
    return null;
  }

  @Override
  public Object execStartInSubject(IApplicationContext context, IProgressMonitor progressMonitor) throws Exception {
    m_clientSession = createClientSession(getUserAgent());
    return null;
  }

  @Override
  public IClientSession getClientSession() {
    return m_clientSession;
  }

  @Override
  public IDesktop getDesktop() {
    return m_clientSession.getDesktop();
  }

  @Override
  public ISwingEnvironment getEnvironment() {
    return m_environment;
  }

}
