/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.ui;

import java.io.Serializable;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.Assertions;

/**
 * Holds information about the kind of user interface used on the client side like {@link IUiLayer} and
 * {@link IUiDeviceType}. <br/>
 * There is also a device id ({@link UserAgent#getUiDeviceId()} which holds even more information about the used device.
 * <p>
 * In order to export the user agent data as string you can use {@link #createIdentifier()} which uses
 * {@link IUserAgentParser}.
 * </p>
 *
 * @since 3.8.0
 */
public final class UserAgent implements Serializable {

  /**
   * The {@link UserAgent} which is currently associated with the current thread.
   */
  public static final ThreadLocal<UserAgent> CURRENT = new ThreadLocal<>();

  private static final long serialVersionUID = 6194949468393137650L;

  private final IUiLayer m_uiLayer;
  private final IUiDeviceType m_uiDeviceType;
  private final IUiEngineType m_uiEngineType;
  private final IUiSystem m_uiSystem;
  private final String m_uiDeviceId;
  private final boolean m_touch;
  private final boolean m_standalone;

  UserAgent(IUiLayer uiLayer, IUiDeviceType uiDeviceType, IUiEngineType uiEngineType, IUiSystem uiSystem, String uiDeviceId, boolean touch, boolean standalone) {
    m_uiLayer = Assertions.assertNotNull(uiLayer, "UI Layer must not be null");
    m_uiDeviceType = Assertions.assertNotNull(uiDeviceType, "UI device type must not be null");
    m_uiDeviceId = Assertions.assertNotNull(uiDeviceId, "UI device id must not be null");
    m_uiEngineType = Assertions.assertNotNull(uiEngineType, "UI engineType must not be null");
    m_uiSystem = Assertions.assertNotNull(uiSystem, "UI system must not be null");
    m_touch = touch;
    m_standalone = standalone;
  }

  public IUiDeviceType getUiDeviceType() {
    return m_uiDeviceType;
  }

  public IUiLayer getUiLayer() {
    return m_uiLayer;
  }

  public IUiEngineType getUiEngineType() {
    return m_uiEngineType;
  }

  public String getUiDeviceId() {
    return m_uiDeviceId;
  }

  public IUiSystem getUiSystem() {
    return m_uiSystem;
  }

  public boolean isTouch() {
    return m_touch;
  }

  /**
   * @return true if the user agent is running in standalone mode (equivalent to navigator.standalone in JS)
   */
  public boolean isStandalone() {
    return m_standalone;
  }

  @Override
  public String toString() {
    return createIdentifier();
  }

  public String createIdentifier() {
    return createIdentifier(BEANS.get(IUserAgentParser.class));
  }

  public String createIdentifier(IUserAgentParser parser) {
    return parser.createIdentifier(this);
  }

  /**
   * Associates the current thread with the given {@link UserAgent}.
   */
  public static void set(final UserAgent userAgent) {
    if (userAgent == null) {
      CURRENT.remove();
    }
    else {
      CURRENT.set(userAgent);
    }
  }

  /**
   * @return {@link UserAgent} associated with the current thread.
   */
  public static UserAgent get() {
    return CURRENT.get();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (m_touch ? 1231 : 1237);
    result = prime * result + ((m_uiDeviceId == null) ? 0 : m_uiDeviceId.hashCode());
    result = prime * result + ((m_uiDeviceType == null) ? 0 : m_uiDeviceType.hashCode());
    result = prime * result + ((m_uiEngineType == null) ? 0 : m_uiEngineType.hashCode());
    result = prime * result + ((m_uiLayer == null) ? 0 : m_uiLayer.hashCode());
    result = prime * result + ((m_uiSystem == null) ? 0 : m_uiSystem.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    UserAgent other = (UserAgent) obj;
    if (m_touch != other.m_touch) {
      return false;
    }
    if (m_uiDeviceId == null) {
      if (other.m_uiDeviceId != null) {
        return false;
      }
    }
    else if (!m_uiDeviceId.equals(other.m_uiDeviceId)) {
      return false;
    }
    if (m_uiDeviceType == null) {
      if (other.m_uiDeviceType != null) {
        return false;
      }
    }
    else if (!m_uiDeviceType.equals(other.m_uiDeviceType)) {
      return false;
    }
    if (m_uiEngineType == null) {
      if (other.m_uiEngineType != null) {
        return false;
      }
    }
    else if (!m_uiEngineType.equals(other.m_uiEngineType)) {
      return false;
    }
    if (m_uiLayer == null) {
      if (other.m_uiLayer != null) {
        return false;
      }
    }
    else if (!m_uiLayer.equals(other.m_uiLayer)) {
      return false;
    }
    if (m_uiSystem == null) {
      if (other.m_uiSystem != null) {
        return false;
      }
    }
    else if (!m_uiSystem.equals(other.m_uiSystem)) {
      return false;
    }
    return true;
  }

}
