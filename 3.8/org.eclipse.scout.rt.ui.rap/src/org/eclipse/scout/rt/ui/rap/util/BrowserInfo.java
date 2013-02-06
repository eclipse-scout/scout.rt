/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.util;

import java.util.Locale;

import org.osgi.framework.Version;

public class BrowserInfo {
  public enum Type {
    ANDROID, GOOGLE_CHROME, APPLE_SAFARI,
    OMNI_WEB, SHIRA,
    BLACKPIXEL_NETNEWSWIRE, REALNETWORKS_REALPLAYER,
    MOZILLA_FIREFOX, MOZILLA_CAMINO, GNOME_GALOEN,
    IE, OPERA, KONQUEROR,
    UNKNOWN
  }

  public enum System {
    WINDOWS, UNIX, OSX, IOS, IPHONE, IPOD, ANDROID, UNKNOWN
  }

  private String m_userAgent = "";

  private final Type m_type;
  private Version m_version;
  private Version m_systemVersion;

  private boolean m_isWebkit = false;
  private boolean m_isGecko = false;
  private boolean m_isMshtml = false;
  private boolean m_isOpera = false;

  private boolean m_isMobile = false;
  private boolean m_isTablet = false;

  private System m_system;
  private Locale m_locale = null;

  public BrowserInfo(Type type, Version version) {
    this(type, version, System.UNKNOWN);
  }

  public BrowserInfo(Type type, Version version, System system) {
    m_type = type;
    m_version = version;
    m_system = system;
  }

  public String getUserAgent() {
    return m_userAgent;
  }

  public void setUserAgent(String userAgent) {
    m_userAgent = userAgent;
  }

  public boolean isWebkit() {
    return m_isWebkit;
  }

  public void setWebkit(boolean isWebkit) {
    m_isWebkit = isWebkit;
  }

  public boolean isGecko() {
    return m_isGecko;
  }

  public void setGecko(boolean isGecko) {
    m_isGecko = isGecko;
  }

  public boolean isMshtml() {
    return m_isMshtml;
  }

  public void setMshtml(boolean isMshtml) {
    m_isMshtml = isMshtml;
  }

  public boolean isOpera() {
    return m_isOpera;
  }

  public void setOpera(boolean isOpera) {
    m_isOpera = isOpera;
  }

  public boolean isMobile() {
    return m_isMobile;
  }

  public void setMobile(boolean isMobile) {
    m_isMobile = isMobile;
  }

  public boolean isTablet() {
    return m_isTablet;
  }

  public void setTablet(boolean isTablet) {
    m_isTablet = isTablet;
  }

  public Type getType() {
    return m_type;
  }

  public System getSystem() {
    return m_system;
  }

  public void setSystem(System system) {
    m_system = system;
  }

  public Locale getLocale() {
    return m_locale;
  }

  public void setLocale(Locale locale) {
    m_locale = locale;
  }

  public boolean isInternetExplorer() {
    return m_type == Type.IE;
  }

  public boolean isMozillaFirefox() {
    return m_type == Type.MOZILLA_FIREFOX;
  }

  public boolean isGoogleChrome() {
    return m_type == Type.GOOGLE_CHROME;
  }

  public boolean isAppleSafari() {
    return m_type == Type.APPLE_SAFARI;
  }

  public boolean isUnknown() {
    return m_type == Type.UNKNOWN;
  }

  public Version getVersion() {
    return m_version;
  }

  public void setVersion(Version version) {
    m_version = version;
  }

  public Version getSystemVersion() {
    return m_systemVersion;
  }

  public void setSystemVersion(Version systemVersion) {
    m_systemVersion = systemVersion;
  }

  public boolean isDesktop() {
    return !isTablet()
        && !isMobile();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (m_isGecko ? 1231 : 1237);
    result = prime * result + (m_isMobile ? 1231 : 1237);
    result = prime * result + (m_isMshtml ? 1231 : 1237);
    result = prime * result + (m_isOpera ? 1231 : 1237);
    result = prime * result + (m_isTablet ? 1231 : 1237);
    result = prime * result + (m_isWebkit ? 1231 : 1237);
    result = prime * result + ((m_locale == null) ? 0 : m_locale.hashCode());
    result = prime * result + ((m_system == null) ? 0 : m_system.hashCode());
    result = prime * result + ((m_type == null) ? 0 : m_type.hashCode());
    result = prime * result + ((m_userAgent == null) ? 0 : m_userAgent.hashCode());
    result = prime * result + ((m_version == null) ? 0 : m_version.hashCode());
    result = prime * result + ((m_systemVersion == null) ? 0 : m_systemVersion.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    BrowserInfo other = (BrowserInfo) obj;
    if (m_isGecko != other.m_isGecko) return false;
    if (m_isMobile != other.m_isMobile) return false;
    if (m_isMshtml != other.m_isMshtml) return false;
    if (m_isOpera != other.m_isOpera) return false;
    if (m_isTablet != other.m_isTablet) return false;
    if (m_isWebkit != other.m_isWebkit) return false;
    if (m_locale == null) {
      if (other.m_locale != null) return false;
    }
    else if (!m_locale.equals(other.m_locale)) return false;
    if (m_system != other.m_system) return false;
    if (m_type != other.m_type) return false;
    if (m_userAgent == null) {
      if (other.m_userAgent != null) return false;
    }
    else if (!m_userAgent.equals(other.m_userAgent)) return false;
    if (m_version == null) {
      if (other.m_version != null) return false;
    }
    else if (!m_version.equals(other.m_version)) return false;
    if (m_systemVersion == null) {
      if (other.m_systemVersion != null) return false;
    }
    else if (!m_systemVersion.equals(other.m_systemVersion)) return false;
    return true;
  }

  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer("System: ").append(getSystem());
    sb.append("/ SystemVersion: ").append(getSystemVersion());
    if (isWebkit()) {
      sb.append("/ isWebkit");
    }
    else if (isGecko()) {
      sb.append("/ isGecko");
    }
    else if (isMshtml()) {
      sb.append("/ isMshtml");
    }

    if (isTablet()) {
      sb.append("/ isTablet");
    }
    else if (isMobile()) {
      sb.append("/ isMobile");
    }

    sb.append("/ Browser: ").append(getType());
    sb.append("/ EngineVersion: ").append(getVersion());
    sb.append("\nUserAgent: ").append(m_userAgent);
    return sb.toString();
  }
}
