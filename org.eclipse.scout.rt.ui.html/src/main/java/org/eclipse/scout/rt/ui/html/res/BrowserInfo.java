/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.res;

import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.ui.UiEngineType;
import org.eclipse.scout.rt.shared.ui.UiSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper to retrieve information about the HTTP client by parsing the user agent string.
 * <p>
 * This class was originally copied from org.eclipse.scout.rt.ui.rap.
 */
public class BrowserInfo {
  private static final Logger LOG = LoggerFactory.getLogger(BrowserInfo.class);

  public static class BrowserVersion implements Comparable<BrowserVersion> {

    private final int m_major;
    private final int m_minor;
    private final int m_micro;

    public BrowserVersion(int major, int minor, int micro) {
      m_major = major;
      m_minor = minor;
      m_micro = micro;
    }

    protected int getMajor() {
      return m_major;
    }

    protected int getMinor() {
      return m_minor;
    }

    protected int getMicro() {
      return m_micro;
    }

    @Override
    public String toString() {
      return m_major + "." + m_minor + "." + m_micro;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + m_major;
      result = prime * result + m_micro;
      result = prime * result + m_minor;
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
      BrowserVersion other = (BrowserVersion) obj;
      if (m_major != other.m_major) {
        return false;
      }
      if (m_micro != other.m_micro) {
        return false;
      }
      if (m_minor != other.m_minor) {
        return false;
      }
      return true;
    }

    @Override
    public int compareTo(BrowserVersion o) {
      if (o == this) {
        return 0;
      }
      if (o == null) {
        return 1;
      }
      int result = (m_major - o.m_major);
      if (result != 0) {
        return result;
      }
      result = (m_minor - o.m_minor);
      if (result != 0) {
        return result;
      }
      result = (m_micro - o.m_micro);
      if (result != 0) {
        return result;
      }
      return 0;
    }
  }

  private final String m_userAgent;

  private UiEngineType m_engineType = UiEngineType.UNKNOWN;
  private BrowserVersion m_engineVersion;
  // Basic engine types
  private boolean m_isWebkit = false;
  private boolean m_isGecko = false;
  private boolean m_isMshtml = false;
  private boolean m_isOpera = false;

  private UiSystem m_system = UiSystem.UNKNOWN;
  private BrowserVersion m_systemVersion;

  // Flags
  private boolean m_isMobile = false;
  private boolean m_isTablet = false;
  private boolean m_isStandalone = false;

  public BrowserInfo(String userAgent) {
    m_userAgent = (userAgent == null ? "" : userAgent);

    initInfos();
  }

  protected void initInfos() {
    initBrowserInfo();
    initSystemInfo();
  }

  protected void initBrowserInfo() {
    // Opera
    String regex = "Opera[\\s\\/]([0-9\\.]*)";
    boolean isOpera = StringUtility.contains(m_userAgent, regex);
    if (isOpera) {
      setOpera(true);
      setEngineType(UiEngineType.OPERA);
      setEngineVersion(extractVersion(m_userAgent, regex));
      return;
    }

    // Konqueror
    regex = "KHTML\\/([0-9-\\.]*)";
    boolean isKonqueror = StringUtility.contains(m_userAgent, regex);
    if (isKonqueror) {
      setWebkit(true);
      setEngineType(UiEngineType.KONQUEROR);
      setEngineVersion(extractVersion(m_userAgent, regex));
      return;
    }

    // Webkit Browsers
    regex = "AppleWebKit\\/([^ ]+)";
    boolean isWebkit = (m_userAgent.indexOf("AppleWebKit") != -1) && StringUtility.contains(m_userAgent, regex);
    if (isWebkit) {
      setWebkit(true);
      if (m_userAgent.indexOf("Chrome") != -1) {
        setEngineType(UiEngineType.CHROME);
      }
      else if (m_userAgent.indexOf("Safari") != -1) {
        if (m_userAgent.indexOf("Android") != -1) {
          setEngineType(UiEngineType.ANDROID);
        }
        else {
          setEngineType(UiEngineType.SAFARI);
        }
      }
      else if (m_userAgent.indexOf("Mobile") != -1) {
        // iPad reports this in fullscreen mode
        setEngineType(UiEngineType.SAFARI);
        setStandalone(true);
      }
      setEngineVersion(extractVersion(m_userAgent, regex));
      return;
    }

    // Internet Explorer
    regex = "(?:MSIE\\s+|Trident/)([^\\);]+)(\\)|;)";
    boolean isMshtml = StringUtility.contains(m_userAgent, regex);
    if (isMshtml) {
      setMshtml(true);
      setEngineType(UiEngineType.IE);
      setEngineVersion(extractVersion(m_userAgent, regex));
      return;
    }

    // Gecko Browsers (Mozilla)
    regex = "rv\\:([^\\);]+)(\\)|;)";
    boolean isGecko = (m_userAgent.indexOf("Gecko") != -1) && StringUtility.contains(m_userAgent, regex);
    if (isGecko) {
      setGecko(true);
      if (m_userAgent.indexOf("Firefox") != -1) {
        setEngineType(UiEngineType.FIREFOX);
      }
      setEngineVersion(extractVersion(m_userAgent, regex));
      return;
    }
  }

  protected void initSystemInfo() {
    if (m_userAgent.indexOf("Windows") != -1 || m_userAgent.indexOf("Win32") != -1 || m_userAgent.indexOf("Win64") != -1 || m_userAgent.indexOf("Win95") != -1) {
      setSystem(UiSystem.WINDOWS);
      if (m_userAgent.indexOf("Windows Phone") != -1 || m_userAgent.indexOf("IEMobile") != -1) {
        setSystemVersion(parseWindowsPhoneVersion(m_userAgent));
        setMobile(true);
      }
      else {
        setSystemVersion(parseWindowsVersion(m_userAgent));
      }
    }
    else if (m_userAgent.indexOf("Macintosh") != -1 || m_userAgent.indexOf("MacPPC") != -1 || m_userAgent.indexOf("MacIntel") != -1 || m_userAgent.indexOf("Mac_PowerPC") != -1) {
      setSystem(UiSystem.OSX);
    }
    else if (m_userAgent.indexOf("Android") != -1) {
      setSystem(UiSystem.ANDROID);
      setSystemVersion(parseAndroidVersion(m_userAgent));

      // Update mobile/tablet flags based on android version
      if (getSystemVersion() == null || getSystemVersion().getMajor() <= 2) {
        setMobile(true);
      }
      else if (getSystemVersion().getMajor() == 3) {
        setTablet(true);
      }
      else {
        // Android 4 is used on smartphones and tablets
        if (getUserAgent().indexOf("Mobile") != -1) {
          setMobile(true);
        }
        else {
          setTablet(true);
        }
      }
    }
    else if (m_userAgent.indexOf("X11") != -1 || m_userAgent.indexOf("Linux") != -1 || m_userAgent.indexOf("BSD") != -1 || m_userAgent.indexOf("SunOS") != -1 || m_userAgent.indexOf("DragonFly") != -1) {
      setSystem(UiSystem.UNIX);
    }
    else if (m_userAgent.indexOf("iPad") != -1) {
      setSystem(UiSystem.IOS);
      setSystemVersion(parseIosVersion(m_userAgent));
      setTablet(true);
    }
    else if (m_userAgent.indexOf("iPhone") != -1 || m_userAgent.indexOf("iPod") != -1) {
      setSystem(UiSystem.IOS);
      setSystemVersion(parseIosVersion(m_userAgent));
      setMobile(true);
    }
  }

  public String getUserAgent() {
    return m_userAgent;
  }

  public boolean isWebkit() {
    return m_isWebkit;
  }

  protected void setWebkit(boolean isWebkit) {
    m_isWebkit = isWebkit;
  }

  public boolean isGecko() {
    return m_isGecko;
  }

  protected void setGecko(boolean isGecko) {
    m_isGecko = isGecko;
  }

  public boolean isMshtml() {
    return m_isMshtml;
  }

  protected void setMshtml(boolean isMshtml) {
    m_isMshtml = isMshtml;
  }

  public boolean isOpera() {
    return m_isOpera;
  }

  protected void setOpera(boolean isOpera) {
    m_isOpera = isOpera;
  }

  public boolean isMobile() {
    return m_isMobile;
  }

  protected void setMobile(boolean isMobile) {
    m_isMobile = isMobile;
  }

  public boolean isTablet() {
    return m_isTablet;
  }

  protected void setTablet(boolean isTablet) {
    m_isTablet = isTablet;
  }

  /**
   * True when running in home screen mode on ios
   */
  public boolean isStandalone() {
    return m_isStandalone;
  }

  protected void setStandalone(boolean isStandalone) {
    m_isStandalone = isStandalone;
  }

  public UiEngineType getEngineType() {
    return m_engineType;
  }

  protected void setEngineType(UiEngineType engineType) {
    m_engineType = engineType;
  }

  public UiSystem getSystem() {
    return m_system;
  }

  protected void setSystem(UiSystem system) {
    m_system = system;
  }

  public BrowserVersion getEngineVersion() {
    return m_engineVersion;
  }

  protected void setEngineVersion(BrowserVersion engineVersion) {
    m_engineVersion = engineVersion;
  }

  public BrowserVersion getSystemVersion() {
    return m_systemVersion;
  }

  protected void setSystemVersion(BrowserVersion systemVersion) {
    m_systemVersion = systemVersion;
  }

  public boolean isDesktop() {
    return !isTablet() && !isMobile();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (m_isGecko ? 1231 : 1237);
    result = prime * result + (m_isMobile ? 1231 : 1237);
    result = prime * result + (m_isMshtml ? 1231 : 1237);
    result = prime * result + (m_isOpera ? 1231 : 1237);
    result = prime * result + (m_isStandalone ? 1231 : 1237);
    result = prime * result + (m_isTablet ? 1231 : 1237);
    result = prime * result + (m_isWebkit ? 1231 : 1237);
    result = prime * result + ((m_system == null) ? 0 : m_system.hashCode());
    result = prime * result + ((m_systemVersion == null) ? 0 : m_systemVersion.hashCode());
    result = prime * result + ((m_engineType == null) ? 0 : m_engineType.hashCode());
    result = prime * result + ((m_userAgent == null) ? 0 : m_userAgent.hashCode());
    result = prime * result + ((m_engineVersion == null) ? 0 : m_engineVersion.hashCode());
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
    BrowserInfo other = (BrowserInfo) obj;
    if (m_isGecko != other.m_isGecko) {
      return false;
    }
    if (m_isMobile != other.m_isMobile) {
      return false;
    }
    if (m_isMshtml != other.m_isMshtml) {
      return false;
    }
    if (m_isOpera != other.m_isOpera) {
      return false;
    }
    if (m_isStandalone != other.m_isStandalone) {
      return false;
    }
    if (m_isTablet != other.m_isTablet) {
      return false;
    }
    if (m_isWebkit != other.m_isWebkit) {
      return false;
    }
    if (m_system != other.m_system) {
      return false;
    }
    if (m_systemVersion == null) {
      if (other.m_systemVersion != null) {
        return false;
      }
    }
    else if (!m_systemVersion.equals(other.m_systemVersion)) {
      return false;
    }
    if (m_engineType != other.m_engineType) {
      return false;
    }
    if (m_userAgent == null) {
      if (other.m_userAgent != null) {
        return false;
      }
    }
    else if (!m_userAgent.equals(other.m_userAgent)) {
      return false;
    }
    if (m_engineVersion == null) {
      if (other.m_engineVersion != null) {
        return false;
      }
    }
    else if (!m_engineVersion.equals(other.m_engineVersion)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("System: ").append(getSystem());
    sb.append(" / SystemVersion: ").append(getSystemVersion());
    if (isWebkit()) {
      sb.append(" / isWebkit");
    }
    else if (isGecko()) {
      sb.append(" / isGecko");
    }
    else if (isMshtml()) {
      sb.append(" / isMshtml");
    }
    if (isTablet()) {
      sb.append(" / isTablet");
    }
    else if (isMobile()) {
      sb.append(" / isMobile");
    }
    sb.append(" / EngineType: ").append(getEngineType());
    sb.append(" / EngineVersion: ").append(getEngineVersion());
    sb.append(" / UserAgent: ").append(m_userAgent);
    return sb.toString();
  }

  public static BrowserInfo createFrom(HttpServletRequest request) {
    if (LOG.isTraceEnabled()) {
      Enumeration headerNames = request.getHeaderNames();
      while (headerNames.hasMoreElements()) {
        String headerName = (String) headerNames.nextElement();
        String header = request.getHeader(headerName);
        headerName = headerName + (headerName.length() <= 11 ? "\t\t" : "\t");
        LOG.trace(headerName + header);
      }
    }

    String userAgent = request.getHeader("User-Agent");
    return createFrom(userAgent);
  }

  public static BrowserInfo createFrom(String userAgent) {
    BrowserInfo info = new BrowserInfo(userAgent);

    if (LOG.isTraceEnabled()) {
      LOG.trace(info.toString());
    }
    return info;
  }

  /**
   * Applies the given regular expression to the userAgent string. If it matches, the first group is used to create a
   * version object.
   * <p>
   * The regex pattern is automatically enclosed in <code>.*?</code> and <code>.*</code>. This ensures that for mulitple
   * matches, the 'most left' is used to construct the version.
   */
  protected static BrowserVersion extractVersion(String userAgent, String regex) {
    Matcher matcher = Pattern.compile(".*?" + regex + ".*", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL).matcher(userAgent);
    if (matcher.matches()) {
      String s = matcher.group(1);
      return createVersion(s);
    }

    return null;
  }

  protected static BrowserVersion parseAndroidVersion(String userAgent) {
    return extractVersion(userAgent, "Android\\s([^\\s;]+)");
  }

  protected static BrowserVersion parseIosVersion(String userAgent) {
    userAgent = userAgent.replace('_', '.');
    return extractVersion(userAgent, "\\sOS\\s([^\\s;]+)");
  }

  protected static BrowserVersion parseWindowsPhoneVersion(String userAgent) {
    return extractVersion(userAgent, "Windows\\sPhone\\s(?:OS )?([^\\s;]+)");
  }

  protected static BrowserVersion parseWindowsVersion(String userAgent) {
    return extractVersion(userAgent, "Windows\\sNT\\s([^\\s;]+)");
  }

  protected static BrowserVersion createVersion(String versionString) {
    // Searches for 3 groups containing numbers separated with a dot.
    // Group 3 is optional (MSIE only has a major and a minor version, no micro)
    versionString = versionString.replaceAll("^[/\\s]*", "");
    Matcher matcher = Pattern.compile("([0-9]+)\\.([0-9]+)[\\.]?([0-9]*)").matcher(versionString);

    int[] vArr = new int[]{0, 0, 0};
    if (matcher.matches()) {
      for (int i = 1; i <= 3; i++) {
        String versionPart = matcher.group(i);
        if (StringUtility.hasText(versionPart)) {
          vArr[i - 1] = Integer.parseInt(versionPart);
        }
      }
    }
    return new BrowserVersion(vArr[0], vArr[1], vArr[2]);
  }
}
