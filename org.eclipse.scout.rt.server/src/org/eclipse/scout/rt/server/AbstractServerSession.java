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
import java.security.AccessController;
import java.security.Principal;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.LocaleThreadLocal;
import org.eclipse.scout.commons.TypeCastUtility;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.ConfigPropertyValue;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.nls.DynamicNls;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.server.services.common.clientnotification.IClientNotificationService;
import org.eclipse.scout.rt.server.services.common.clientnotification.SessionFilter;
import org.eclipse.scout.rt.shared.services.common.context.SharedContextChangedNotification;
import org.eclipse.scout.rt.shared.services.common.context.SharedVariableMap;
import org.eclipse.scout.service.SERVICES;
import org.osgi.framework.Bundle;

public abstract class AbstractServerSession implements IServerSession {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractServerSession.class);

  private Bundle m_bundle;
  private boolean m_initialized;
  private boolean m_active;
  private NlsLocale m_nlsLocale;
  private Locale m_locale;
  private Pattern[] m_userIdSearchPatterns;
  private HashMap<Object, Object> m_attributes;
  private SharedVariableMap m_sharedVariableMap;
  private boolean m_webSession;

  public AbstractServerSession(boolean autoInitConfig) {
    m_userIdSearchPatterns = new Pattern[]{
        Pattern.compile(".*\\\\([^/@]+)"),
        Pattern.compile(".*\\\\([^/@]+)[/@].*"),
        Pattern.compile("([^/@]+)"),
        Pattern.compile("([^/@]+)[/@].*"),
        };
    m_locale = LocaleThreadLocal.get();
    if (m_locale == null) {
      m_locale = Locale.getDefault();
    }
    m_nlsLocale = NlsLocale.getDefault();
    m_attributes = new HashMap<Object, Object>();
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

  @Override
  public Map<String, Object> getSharedVariableMap() {
    return m_sharedVariableMap;
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
    String foundName = null;
    Subject s = Subject.getSubject(AccessController.getContext());
    if (s != null) {
      if (m_userIdSearchPatterns != null) {
        for (Principal p : s.getPrincipals()) {
          String name = p.getName().toLowerCase();
          for (Pattern pat : m_userIdSearchPatterns) {
            Matcher m = pat.matcher(name);
            if (m.matches()) {
              foundName = m.group(1);
              break;
            }
          }
        }
      }
    }
    if (foundName != null) {
      setUserIdInternal(foundName);
    }
    else {
      setUserIdInternal(null);
    }
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

  /**
   * see {@link #setUserIdSearchPatterns(Pattern...)}
   */
  protected Pattern[] getUserIdSearchPatterns() {
    return m_userIdSearchPatterns;
  }

  /**
   * see {@link #setUserIdSearchPatterns(Pattern...)}
   */
  protected void setUserIdSearchPatterns(Pattern... patterns) {
    m_userIdSearchPatterns = patterns;
    assignUserId();
  }

  /**
   * Set the pattern by which the userId is searched for in the list of jaas
   * principal names.<br>
   * The first group of the pattern is assumed to be the username.<br>
   * By default the following patterns are applied in this order:
   * <ul>
   * <li>".*\\\\([^/@]+)" matching "DOMAIN\\user" to "user"
   * <li>".*\\\\([^/@]+)[/@].*" matching "DOMAIN\\user@domain.com" to "user"
   * <li>"([^/@]+)" matching "user" to "user"
   * <li>"([^/@]+)[/@].*" matching "user@domain.com" to "user"
   * </ul>
   */
  protected void setUserIdSearchPatterns(String... patterns) {
    Pattern[] a = new Pattern[patterns.length];
    for (int i = 0; i < a.length; i++) {
      a[i] = Pattern.compile(patterns[i]);
    }
    setUserIdSearchPatterns(a);
  }

  @Override
  public NlsLocale getNlsLocale() {
    return m_nlsLocale;
  }

  @Override
  public void setNlsLocale(NlsLocale l) {
    if (l != null) {
      m_nlsLocale = l;
    }
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
   * override this method to set the application specific texts implementation
   */
  @Override
  public DynamicNls getNlsTexts() {
    return null;
  }

  @Override
  public Object getAttribute(Object key) {
    return m_attributes.get(key);
  }

  @Override
  public void setAttribute(Object key, Object value) {
    m_attributes.put(key, value);
  }

  protected void initConfig() {
    m_webSession = getConfiguredWebSession();
    if (!isWebSession()) {
      m_sharedVariableMap.addPropertyChangeListener(new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent e) {
          // notify this session
          SERVICES.getService(IClientNotificationService.class).putNotification(new SharedContextChangedNotification(new SharedVariableMap(m_sharedVariableMap)), new SessionFilter(AbstractServerSession.this, 60000L));
        }
      });
    }
    if (m_initialized) return;
    m_initialized = true;
  }

  @Override
  public Bundle getBundle() {
    return m_bundle;
  }

  @Override
  public final void loadSession(Bundle bundle) throws ProcessingException {
    if (isActive()) throw new IllegalStateException("session is active");
    if (bundle == null) throw new IllegalArgumentException("bundle must not be null");
    m_bundle = bundle;
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

  protected void execLocaleChanged() throws ProcessingException {
  }

  @Override
  public boolean isWebSession() {
    return m_webSession;
  }

}
