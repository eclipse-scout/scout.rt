/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons.servlet;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.util.FinalValue;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.ui.UiDeviceType;
import org.eclipse.scout.rt.shared.ui.UiEngineType;
import org.eclipse.scout.rt.shared.ui.UiLayer;
import org.eclipse.scout.rt.shared.ui.UiSystem;
import org.eclipse.scout.rt.shared.ui.UserAgents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper to retrieve information about the HTTP client by parsing the user agent string.
 * <p>
 * This class was originally copied from org.eclipse.scout.rt.ui.rap.
 * <p>
 * Use {@link #get(HttpServletRequest)} to get the {@link HttpClientInfo} cached on the {@link HttpSession}
 */
@Bean
public class HttpClientInfo {
  private static final Logger LOG = LoggerFactory.getLogger(HttpClientInfo.class);

  public static class Version implements Comparable<Version> {

    private final int m_major;
    private final int m_minor;
    private final int m_micro;

    public Version(int major, int minor, int micro) {
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
      Version other = (Version) obj;
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
    public int compareTo(Version o) {
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

  protected static final String HTTP_CLIENT_INFO_ATTRIBUTE_NAME = HttpClientInfo.class.getName();

  private final FinalValue<String> m_userAgent = new FinalValue<>();

  private UiEngineType m_engineType = UiEngineType.UNKNOWN;
  private Version m_engineVersion;
  // Basic engine types
  private boolean m_isWebkit = false;
  private boolean m_isGecko = false;
  private boolean m_isMshtml = false;
  private boolean m_isOpera = false;

  private UiSystem m_system = UiSystem.UNKNOWN;
  private Version m_systemVersion;

  // Flags
  private boolean m_isMobile = false;
  private boolean m_isTablet = false;
  private boolean m_isStandalone = false;

  public HttpClientInfo init(String userAgent) {
    m_userAgent.set(StringUtility.emptyIfNull(userAgent));
    initInfos();
    return this;
  }

  protected void initInfos() {
    initHttpClientInfo();
    initSystemInfo();
  }

  protected void initHttpClientInfo() {
    String userAgent = getUserAgent();
    // Opera
    String regex = "Opera[\\s\\/]([0-9\\.]*)";
    boolean isOpera = StringUtility.containsRegEx(userAgent, regex);
    if (isOpera) {
      setOpera(true);
      setEngineType(UiEngineType.OPERA);
      setEngineVersion(extractVersion(userAgent, regex));
      return;
    }

    // Konqueror
    regex = "KHTML\\/([0-9-\\.]*)";
    boolean isKonqueror = StringUtility.containsRegEx(userAgent, regex);
    if (isKonqueror) {
      setWebkit(true);
      setEngineType(UiEngineType.KONQUEROR);
      setEngineVersion(extractVersion(userAgent, regex));
      return;
    }

    // Edge
    regex = "Edge\\/([^ ]+)$";
    boolean isEdge = StringUtility.containsRegEx(userAgent, regex);
    if (isEdge) {
      setEngineType(UiEngineType.EDGE);
      setEngineVersion(parseEdgeVersion(userAgent));
      return;
    }

    // Webkit Browsers
    regex = "AppleWebKit\\/([^ ]+)";
    boolean isWebkit = StringUtility.containsRegEx(userAgent, regex);
    if (isWebkit) {
      setWebkit(true);
      if (userAgent.contains("Chrome")) {
        setEngineType(UiEngineType.CHROME);
      }
      else if (userAgent.contains("Safari")) {
        if (userAgent.contains("Android")) {
          setEngineType(UiEngineType.ANDROID);
        }
        else {
          setEngineType(UiEngineType.SAFARI);
        }
      }
      else if (userAgent.contains("Mobile")) {
        // iPad reports this in fullscreen mode
        setEngineType(UiEngineType.SAFARI);
        setStandalone(true);
      }
      setEngineVersion(extractVersion(userAgent, regex));
      return;
    }

    // Internet Explorer
    regex = "(?:MSIE\\s+|Trident/)([^\\);]+)(\\)|;)";
    boolean isMshtml = StringUtility.containsRegEx(userAgent, regex);
    if (isMshtml) {
      setMshtml(true);
      setEngineType(UiEngineType.IE);
      setEngineVersion(extractVersion(userAgent, regex));
      return;
    }

    // Gecko Browsers (Mozilla)
    regex = "rv\\:([^\\);]+)(\\)|;)";
    boolean isGecko = (userAgent.contains("Gecko")) && StringUtility.containsRegEx(userAgent, regex);
    if (isGecko) {
      setGecko(true);
      if (userAgent.contains("Firefox")) {
        setEngineType(UiEngineType.FIREFOX);
      }
      setEngineVersion(extractVersion(userAgent, regex));
      return;
    }
  }

  protected void initSystemInfo() {
    String userAgent = getUserAgent();
    if (userAgent.contains("Windows") || userAgent.contains("Win32") || userAgent.contains("Win64") || userAgent.contains("Win95")) {
      setSystem(UiSystem.WINDOWS);
      if (userAgent.contains("Windows Phone") || userAgent.contains("IEMobile")) {
        setSystemVersion(parseWindowsPhoneVersion(userAgent));
        setMobile(true);
      }
      else {
        setSystemVersion(parseWindowsVersion(userAgent));
      }
    }
    else if (userAgent.contains("Macintosh") || userAgent.contains("MacPPC") || userAgent.contains("MacIntel") || userAgent.contains("Mac_PowerPC")) {
      setSystem(UiSystem.OSX);
    }
    else if (userAgent.contains("Android")) {
      setSystem(UiSystem.ANDROID);
      setSystemVersion(parseAndroidVersion(userAgent));

      // Update mobile/tablet flags based on android version
      if (getSystemVersion() == null || getSystemVersion().getMajor() <= 2) {
        setMobile(true);
      }
      else if (getSystemVersion().getMajor() == 3) {
        setTablet(true);
      }
      else {
        // Android 4 is used on smartphones and tablets
        if (getUserAgent().contains("Mobile")) {
          setMobile(true);
        }
        else {
          setTablet(true);
        }
      }
    }
    else if (userAgent.contains("X11") || userAgent.contains("Linux") || userAgent.contains("BSD") || userAgent.contains("SunOS") || userAgent.contains("DragonFly")) {
      setSystem(UiSystem.UNIX);
    }
    else if (userAgent.contains("iPad")) {
      setSystem(UiSystem.IOS);
      setSystemVersion(parseIosVersion(userAgent));
      setTablet(true);
    }
    else if (userAgent.contains("iPhone") || userAgent.contains("iPod")) {
      setSystem(UiSystem.IOS);
      setSystemVersion(parseIosVersion(userAgent));
      setMobile(true);
    }
  }

  /**
   * @return never <code>null</code>
   * @throws AssertionException
   *           if object was not initialized using {@link #init(String)}
   */
  public String getUserAgent() {
    Assertions.assertTrue(m_userAgent.isSet());
    return m_userAgent.get();
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

  /**
   * @return {@code true} if the HTTP client is Microsoft Internet Explorer. Returns {@code false} for other browsers
   *         including Microsoft Edge.
   */
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

  public Version getEngineVersion() {
    return m_engineVersion;
  }

  protected void setEngineVersion(Version engineVersion) {
    m_engineVersion = engineVersion;
  }

  public Version getSystemVersion() {
    return m_systemVersion;
  }

  protected void setSystemVersion(Version systemVersion) {
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
    HttpClientInfo other = (HttpClientInfo) obj;
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
    if (ObjectUtility.notEquals(m_userAgent.get(), other.m_userAgent.get())) {
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
    sb.append(" / UserAgent: ").append(m_userAgent.get());
    return sb.toString();
  }

  public UserAgents toUserAgents() {
    return UserAgents
        .create()
        .withUiLayer(UiLayer.HTML)
        .withUiDeviceType(getDeviceType())
        .withUiEngineType(getEngineType())
        .withUiSystem(getSystem())
        .withStandalone(isStandalone())
        .withTouch(isMobile() || isTablet())
        .withDeviceId(getUserAgent());
  }

  protected UiDeviceType getDeviceType() {
    if (isMobile()) {
      return UiDeviceType.MOBILE;
    }
    if (isTablet()) {
      return UiDeviceType.TABLET;
    }
    if (isDesktop()) {
      return UiDeviceType.DESKTOP;
    }
    return UiDeviceType.UNKNOWN;
  }

  /**
   * Creates {@link HttpClientInfo} based on user agent on {@code request}.
   * <p>
   * The {@link HttpClientInfo} is cached on the {@link HttpSession} if a session exists.
   */
  public static HttpClientInfo get(HttpServletRequest request) {
    // HttpClientInfo is cached on HTTP session
    HttpSession session = request.getSession(false);
    if (session != null) {
      HttpClientInfo httpClientInfo = (HttpClientInfo) session.getAttribute(HTTP_CLIENT_INFO_ATTRIBUTE_NAME);
      if (httpClientInfo != null) {
        return httpClientInfo;
      }
    }
    String userAgent = request.getHeader("User-Agent");
    HttpClientInfo httpClientInfo = createFrom(userAgent);
    if (session != null) {
      session.setAttribute(HTTP_CLIENT_INFO_ATTRIBUTE_NAME, httpClientInfo);
    }
    return httpClientInfo;
  }

  public static HttpClientInfo createFrom(String userAgent) {
    HttpClientInfo info = BEANS.get(HttpClientInfo.class).init(userAgent);
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
  protected static Version extractVersion(String userAgent, String regex) {
    Matcher matcher = Pattern.compile(".*?" + regex + ".*", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL).matcher(userAgent);
    if (matcher.matches()) {
      String s = matcher.group(1);
      return createVersion(s);
    }

    return null;
  }

  protected static Version parseAndroidVersion(String userAgent) {
    return extractVersion(userAgent, "Android\\s([^\\s;]+)");
  }

  protected static Version parseIosVersion(String userAgent) {
    userAgent = userAgent.replace('_', '.');
    return extractVersion(userAgent, "\\sOS\\s([^\\s;]+)");
  }

  protected static Version parseWindowsPhoneVersion(String userAgent) {
    return extractVersion(userAgent, "Windows\\sPhone\\s(?:OS )?([^\\s;]+)");
  }

  protected static Version parseWindowsVersion(String userAgent) {
    return extractVersion(userAgent, "Windows\\sNT\\s([^\\s;]+)");
  }

  protected static Version parseEdgeVersion(String userAgent) {
    return extractVersion(userAgent, "Edge\\/([^\\s;]+)$");
  }

  protected static Version createVersion(String versionString) {
    // Searches for 3 groups containing numbers separated with a dot.
    // Group 3 is optional (MSIE only has a major and a minor version, no micro)
    versionString = versionString.replaceAll("^[/\\s]*", "");
    Matcher matcher = Pattern.compile("([0-9]+)\\.([0-9]+)[\\.]?([0-9]*)").matcher(versionString);

    int[] vArr = {0, 0, 0};
    if (matcher.matches()) {
      for (int i = 1; i <= 3; i++) {
        String versionPart = matcher.group(i);
        if (StringUtility.hasText(versionPart)) {
          vArr[i - 1] = Integer.parseInt(versionPart);
        }
      }
    }
    return new Version(vArr[0], vArr[1], vArr[2]);
  }
}
