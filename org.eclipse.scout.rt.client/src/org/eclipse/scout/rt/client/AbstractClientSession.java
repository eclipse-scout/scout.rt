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
package org.eclipse.scout.rt.client;

import java.beans.PropertyChangeListener;
import java.util.Map;

import javax.security.auth.Subject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.TypeCastUtility;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.ConfigPropertyValue;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.nls.DynamicNls;
import org.eclipse.scout.commons.prefs.UserScope;
import org.eclipse.scout.rt.client.services.common.clientnotification.ClientNotificationConsumerEvent;
import org.eclipse.scout.rt.client.services.common.clientnotification.IClientNotificationConsumerListener;
import org.eclipse.scout.rt.client.services.common.clientnotification.IClientNotificationConsumerService;
import org.eclipse.scout.rt.client.servicetunnel.IServiceTunnel;
import org.eclipse.scout.rt.client.ui.DataChangeListener;
import org.eclipse.scout.rt.client.ui.IIconLocator;
import org.eclipse.scout.rt.client.ui.IconLocator;
import org.eclipse.scout.rt.client.ui.desktop.DesktopListener;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.internal.VirtualDesktop;
import org.eclipse.scout.rt.shared.OfflineState;
import org.eclipse.scout.rt.shared.services.common.context.SharedContextChangedNotification;
import org.eclipse.scout.rt.shared.services.common.context.SharedVariableMap;
import org.eclipse.scout.rt.shared.services.common.security.SimplePrincipal;
import org.eclipse.scout.service.SERVICES;
import org.osgi.framework.Bundle;
import org.osgi.service.prefs.BackingStoreException;

public abstract class AbstractClientSession implements IClientSession {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractClientSession.class);

  // context
  private Bundle m_bundle;
  // state
  private final Object m_stateLock;
  private boolean m_active;
  private Throwable m_loadError;
  private int m_exitCode = IApplication.EXIT_OK;
  // model
  private IDesktop m_desktop;
  private VirtualDesktop m_virtualDesktop;
  private IServiceTunnel m_serviceTunnel;
  private Subject m_offlineSubject;
  private final SharedVariableMap m_sharedVariableMap;
  private boolean m_webSession;
  private IMemoryPolicy m_memoryPolicy;
  private IIconLocator m_iconLocator;

  public AbstractClientSession(boolean autoInitConfig) {
    m_stateLock = new Object();
    m_sharedVariableMap = new SharedVariableMap();
    if (autoInitConfig) {
      initConfig();
    }
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(100)
  @ConfigPropertyValue("false")
  protected boolean getConfiguredWebSession() {
    return false;
  }

  public String getUserId() {
    return getSharedContextVariable("userId", String.class);
  }

  /**
   * override this method to set the application specific texts implementation
   */
  @Override
  public DynamicNls getNlsTexts() {
    return null;
  }

  public Subject getOfflineSubject() {
    return m_offlineSubject;
  }

  public Bundle getBundle() {
    return m_bundle;
  }

  public boolean isActive() {
    return m_active;
  }

  private void setActive(boolean b) {
    synchronized (m_stateLock) {
      m_active = b;
      m_stateLock.notifyAll();
    }
  }

  public boolean isLoaded() {
    return m_active;
  }

  /**
   * do not use this internal method directly
   */
  protected <T> T getSharedContextVariable(String name, Class<T> type) {
    Object o = m_sharedVariableMap.get(name);
    return TypeCastUtility.castValue(o, type);
  }

  public final Throwable getLoadError() {
    return m_loadError;
  }

  public final Object getStateLock() {
    return m_stateLock;
  }

  /*
   * Properties
   */

  protected void initConfig() {
    m_webSession = getConfiguredWebSession();
    m_virtualDesktop = new VirtualDesktop();
    setMemoryPolicy(new LargeMemoryPolicy());
    // add client notification listener
    IClientNotificationConsumerService clientNotificationConsumerService = SERVICES.getService(IClientNotificationConsumerService.class);
    if (clientNotificationConsumerService != null) {
      clientNotificationConsumerService.addClientNotificationConsumerListener(new IClientNotificationConsumerListener() {
        public void handleEvent(final ClientNotificationConsumerEvent e, boolean sync) {
          if (e.getClientNotification().getClass() == SharedContextChangedNotification.class) {
            if (sync) {
              try {
                updateSharedVariableMap(((SharedContextChangedNotification) e.getClientNotification()).getSharedVariableMap());
              }
              catch (Throwable t) {
                LOG.error("update of shared contex", t);
                // nop
              }
            }
            else {
              new ClientSyncJob("Update shared context", ClientSyncJob.getCurrentSession()) {
                @Override
                protected void runVoid(IProgressMonitor monitor) throws Throwable {
                  updateSharedVariableMap(((SharedContextChangedNotification) e.getClientNotification()).getSharedVariableMap());
                }
              }.schedule();
            }
          }
        }
      });
    }
  }

  private void updateSharedVariableMap(SharedVariableMap newMap) {
    m_sharedVariableMap.updateInternal(newMap);
  }

  public final void startSession(Bundle bundle) {
    m_bundle = bundle;
    if (isActive()) throw new IllegalStateException("session is active");
    try {
      String policy = bundle.getBundleContext().getProperty("org.eclipse.scout.memory");
      if ("small".equals(policy)) {
        setMemoryPolicy(new SmallMemoryPolicy());
      }
      else if ("medium".equals(policy)) {
        setMemoryPolicy(new MediumMemoryPolicy());
      }
      m_iconLocator = createIconLocator();
      execLoadSession();
      setActive(true);
    }
    catch (Throwable t) {
      m_loadError = t;
      LOG.error("load session", t);
    }
  }

  @ConfigOperation
  @Order(10)
  protected void execLoadSession() throws ProcessingException {
  }

  @ConfigOperation
  @Order(20)
  protected void execStoreSession() throws ProcessingException {
  }

  public IDesktop getVirtualDesktop() {
    return m_desktop != null ? m_desktop : m_virtualDesktop;
  }

  public IDesktop getDesktop() {
    return m_desktop;
  }

  public void setDesktop(IDesktop a) throws ProcessingException {
    if (a == null) throw new IllegalArgumentException("argument must not be null");
    if (m_desktop != null) throw new IllegalStateException("desktop is active");
    m_desktop = a;
    if (m_desktop != null) {
      if (m_virtualDesktop != null) {
        for (DesktopListener listener : m_virtualDesktop.getDesktopListeners()) {
          m_desktop.addDesktopListener(listener);
        }
        for (Map.Entry<String, EventListenerList> e : m_virtualDesktop.getPropertyChangeListenerMap().entrySet()) {
          String propName = e.getKey();
          EventListenerList list = e.getValue();
          if (propName == null) {
            for (PropertyChangeListener listener : list.getListeners(PropertyChangeListener.class)) {
              m_desktop.addPropertyChangeListener(listener);
            }
          }
          else {
            for (PropertyChangeListener listener : list.getListeners(PropertyChangeListener.class)) {
              m_desktop.addPropertyChangeListener(propName, listener);
            }
          }
        }
        for (Map.Entry<Object, EventListenerList> e : m_virtualDesktop.getDataChangeListenerMap().entrySet()) {
          Object dataType = e.getKey();
          EventListenerList list = e.getValue();
          if (dataType == null) {
            for (DataChangeListener listener : list.getListeners(DataChangeListener.class)) {
              m_desktop.addDataChangeListener(listener);
            }
          }
          else {
            for (DataChangeListener listener : list.getListeners(DataChangeListener.class)) {
              m_desktop.addDataChangeListener(listener, dataType);
            }
          }
        }
        m_virtualDesktop = null;
      }
      m_desktop.initDesktop();
    }
    else {
      m_virtualDesktop = new VirtualDesktop();
    }
  }

  /**
   * Close the session
   */
  public void stopSession() {
    stopSession(IApplication.EXIT_OK);
  }

  public void stopSession(int exitCode) {
    m_exitCode = exitCode;
    try {
      execStoreSession();
    }
    catch (Throwable t) {
      LOG.error("store session", t);
    }
    if (m_desktop != null) {
      try {
        m_desktop.closeInternal();
      }
      catch (Throwable t) {
        LOG.error("close desktop", t);
      }
      m_desktop = null;
    }
    setActive(false);
    if (LOG.isInfoEnabled()) LOG.info("end session event loop");
  }

  public int getExitCode() {
    return m_exitCode;
  }

  public IServiceTunnel getServiceTunnel() {
    return m_serviceTunnel;
  }

  protected void setServiceTunnel(IServiceTunnel tunnel) {
    m_serviceTunnel = tunnel;
  }

  public IMemoryPolicy getMemoryPolicy() {
    return m_memoryPolicy;
  }

  public void setMemoryPolicy(IMemoryPolicy p) {
    if (m_memoryPolicy != null) {
      m_memoryPolicy.removeNotify();
    }
    m_memoryPolicy = p;
    if (m_memoryPolicy != null) {
      m_memoryPolicy.addNotify();
    }
  }

  public void goOnline() throws ProcessingException {
    if (OfflineState.isOfflineDefault()) {
      OfflineState.setOfflineDefault(false);
    }
  }

  public void goOffline() throws ProcessingException {
    IEclipsePreferences pref = new UserScope().getNode(Activator.PLUGIN_ID);
    if (getUserId() != null) {
      if (OfflineState.isOnlineDefault()) {
        try {
          pref.put("offline.user", getUserId());
          pref.flush();
        }
        catch (BackingStoreException e) {
          LOG.error("Could not write userId to preferences!");
        }
      }
    }
    // create new backend subject
    String offlineUser = pref.get("offline.user", "anonymous");
    m_offlineSubject = new Subject();
    m_offlineSubject.getPrincipals().add(new SimplePrincipal(offlineUser));
    OfflineState.setOfflineDefault(true);
  }

  public boolean isWebSession() {
    return m_webSession;
  }

  protected IIconLocator createIconLocator() {
    return new IconLocator(this);
  }

  public IIconLocator getIconLocator() {
    return m_iconLocator;
  }

}
