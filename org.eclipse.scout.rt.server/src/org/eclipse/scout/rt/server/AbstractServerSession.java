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
package org.eclipse.scout.rt.server;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.eclipse.scout.commons.LocaleThreadLocal;
import org.eclipse.scout.commons.TypeCastUtility;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.server.services.common.clientnotification.IClientNotificationService;
import org.eclipse.scout.rt.server.services.common.clientnotification.SessionFilter;
import org.eclipse.scout.rt.shared.OfflineState;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.TextsThreadLocal;
import org.eclipse.scout.rt.shared.services.common.context.SharedContextChangedNotification;
import org.eclipse.scout.rt.shared.services.common.context.SharedVariableMap;
import org.eclipse.scout.rt.shared.services.common.security.IAccessControlService;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.eclipse.scout.service.SERVICES;
import org.osgi.framework.Bundle;

public abstract class AbstractServerSession implements IServerSession {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractServerSession.class);

  private Bundle m_bundle;
  private boolean m_initialized;
  private boolean m_active;
  private Locale m_locale;
  private final HashMap<String, Object> m_attributes;
  private final Object m_attributesLock;
  private final SharedVariableMap m_sharedVariableMap;
  private boolean m_singleThreadSession;
  private ScoutTexts m_scoutTexts;
  private UserAgent m_userAgent;

  public AbstractServerSession(boolean autoInitConfig) {
    m_locale = LocaleThreadLocal.get();
    m_attributesLock = new Object();
    m_attributes = new HashMap<String, Object>();
    m_sharedVariableMap = new SharedVariableMap();
    if (autoInitConfig) {
      initConfig();
    }
  }

  /**
   * @deprecated use {@link #getConfiguredSingleThreadSession()} instead. Will be removed in Release 3.10.
   */
  @Deprecated
  protected boolean getConfiguredWebSession() {
    return getConfiguredSingleThreadSession();
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(100)
  protected boolean getConfiguredSingleThreadSession() {
    return false;
  }

  @Override
  public Map<String, Object> getSharedVariableMap() {
    return Collections.unmodifiableMap(m_sharedVariableMap);
  }

  /**
   * do not use this internal method directly
   */
  protected <T> T getSharedContextVariable(String name, Class<T> type) {
    Object o = m_sharedVariableMap.get(name);
    return TypeCastUtility.castValue(o, type);
  }

  /**
   * do not use this internal method directly
   */
  protected <T> void setSharedContextVariable(String name, Class<T> type, T value) {
    T typedValue = TypeCastUtility.castValue(value, type);
    m_sharedVariableMap.put(name, typedValue);
  }

  private void assignUserId() {
    String userId = SERVICES.getService(IAccessControlService.class).getUserIdOfCurrentSubject();
    setUserIdInternal(userId);
  }

  /**
   * The session is running in its event loop
   */
  @Override
  public boolean isActive() {
    return m_active;
  }

  @Override
  public final String getUserId() {
    return getSharedContextVariable("userId", String.class);
  }

  private void setUserIdInternal(String newValue) {
    setSharedContextVariable("userId", String.class, newValue);
  }

  @Override
  public Locale getLocale() {
    return m_locale;
  }

  @Override
  public void setLocale(Locale l) {
    if (l != null) {
      m_locale = l;
    }
  }

  /**
   * <p>
   * Returns the {@link ScoutTexts} instance assigned to the type (class) of the current ServerSession.
   * </p>
   * <p>
   * Override this method to set the application specific texts implementation
   * </p>
   */
  @Override
  public ScoutTexts getTexts() {
    return m_scoutTexts;
  }

  @Override
  public Object getData(String key) {
    synchronized (m_attributesLock) {
      return m_attributes.get(key);
    }
  }

  @Override
  public void setData(String key, Object value) {
    synchronized (m_attributesLock) {
      m_attributes.put(key, value);
    }
  }

  protected void initConfig() {
    m_singleThreadSession = getConfiguredSingleThreadSession();
    if (!isWebSession()) {
      m_sharedVariableMap.addPropertyChangeListener(new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent e) {
          if (OfflineState.isOfflineDefault() == OfflineState.isOfflineInCurrentThread()) {
            // notify this session
            SERVICES.getService(IClientNotificationService.class).putNotification(new SharedContextChangedNotification(new SharedVariableMap(m_sharedVariableMap)), new SessionFilter(AbstractServerSession.this, 60000L));
          }
        }
      });
    }
    if (m_initialized) {
      return;
    }
    m_initialized = true;
  }

  @Override
  public Bundle getBundle() {
    return m_bundle;
  }

  @Override
  public final void loadSession(Bundle bundle) throws ProcessingException {
    if (isActive()) {
      throw new IllegalStateException("session is active");
    }
    if (bundle == null) {
      throw new IllegalArgumentException("bundle must not be null");
    }
    m_bundle = bundle;
    m_active = true;
    m_scoutTexts = new ScoutTexts();
    // explicitly set the just created instance to the ThreadLocal because it was not available yet, when the job was started.
    TextsThreadLocal.set(m_scoutTexts);
    assignUserId();
    execLoadSession();
  }

  /**
   * 1. Identify (authenticate) user by its credentials
   * <ul>
   * <li>success: nop
   * <li>failure: throws wrapped ProcessingException(SecurityException)
   * </ul>
   * 2. Load session data such as properties, permissions
   */
  @ConfigOperation
  @Order(10)
  protected void execLoadSession() throws ProcessingException {
  }

  /**
   * @deprecated never called by the framework. Will be removed in Release 3.10.
   */
  @Deprecated
  protected void execLocaleChanged() throws ProcessingException {
  }

  /**
   * @deprecated use #isSingleThreadSession instead. Will be removed in Release 3.10.
   */
  @Deprecated
  public boolean isWebSession() {
    return isSingleThreadSession();
  }

  @Override
  public boolean isSingleThreadSession() {
    return m_singleThreadSession;
  }

  @Override
  public UserAgent getUserAgent() {
    return m_userAgent;
  }

  @Override
  public void setUserAgent(UserAgent userAgent) {
    m_userAgent = userAgent;
  }

}
