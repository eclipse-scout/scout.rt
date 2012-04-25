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
    GOOGLE_CHROME, APPLE_SAFARI,
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

  private boolean m_isWebkit = false;
  private boolean m_isGecko = false;
  private boolean m_isMshtml = false;
  private boolean m_isOpera = false;

  private boolean m_isMobile = false;
  private boolean m_isTablet = false;

  private System m_system;
  private Locale m_locale = null;

  BrowserInfo(Type type, Version version) {
    this(type, version, System.UNKNOWN);
  }

  BrowserInfo(Type type, Version version, System system) {
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

  public boolean isDesktop() {
    return !isTablet()
        && !isMobile();
  }

  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer("System: ").append(getSystem());
    if (isWebkit()) {
      sb.append("/ isWebkit");
    }
    else if (isGecko()) {
      sb.append("/ isGecko");
    }
    else if (isMshtml()) {
      sb.append("/ isMshtml");
    }
    sb.append("/ Browser: ").append(getType());
    sb.append("/ EngineVersion: ").append(getVersion());
    sb.append("\nUserAgent: ").append(m_userAgent);
    return sb.toString();
  }
}
