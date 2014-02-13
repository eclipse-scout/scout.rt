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
package org.eclipse.scout.rt.ui.json;

import java.beans.PropertyChangeListener;
import java.security.AccessController;
import java.util.UUID;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.services.common.session.IClientSessionRegistryService;
import org.eclipse.scout.rt.client.ui.desktop.DesktopListener;
import org.eclipse.scout.rt.shared.ui.UiDeviceType;
import org.eclipse.scout.rt.shared.ui.UiLayer;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.eclipse.scout.service.SERVICES;
import org.json.JSONObject;

public abstract class AbstractJsonEnvironment implements IJsonEnvironment {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractJsonEnvironment.class);

  private IClientSession m_clientSession;
  private Class<? extends IClientSession> m_clientSessionClass;
  private DesktopListener m_scoutDesktopListener;
  private PropertyChangeListener m_desktopPropertyListener;
  private JsonDesktop m_jsonDesktop;

  public AbstractJsonEnvironment(Class<? extends IClientSession> clientSessionClass) {
    m_clientSessionClass = clientSessionClass;
  }

  @Override
  public void init() throws ProcessingException {
    UserAgent userAgent = createUserAgent();
    IClientSession clientSession = createClientSession(userAgent);
    if (!clientSession.isActive()) {
      throw new ProcessingException("ClientSession is not active, there must be a problem with loading or starting");
    }
    m_clientSession = clientSession;
    m_jsonDesktop = new JsonDesktop(m_clientSession.getDesktop(), this);
    m_jsonDesktop.init();
    LOG.info("JsonEnvironment initialized.");
  }

  public void dispose() throws ProcessingException {
    //TODO call dispose (from session invalidation listener and desktop close event?)
    m_jsonDesktop.dispose();
  }

  protected IClientSession createClientSession(UserAgent userAgent) {
    Subject subject = Subject.getSubject(AccessController.getContext());
    if (subject == null) {
      throw new SecurityException("/json request is not authenticated with a Subject");
    }
    return SERVICES.getService(IClientSessionRegistryService.class).newClientSession(m_clientSessionClass, subject, UUID.randomUUID().toString(), userAgent);
  }

  protected UserAgent createUserAgent() {
    //TODO create UiLayer.Json, or better let deliver from real gui -> html?
    return UserAgent.create(UiLayer.RAP, UiDeviceType.DESKTOP);
  }

  @Override
  public JSONObject processRequest(JSONObject json) throws ProcessingException {
    return m_jsonDesktop.toJson();
  }

  @Override
  public IClientSession getClientSession() {
    return m_clientSession;
  }

}
