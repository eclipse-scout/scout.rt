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
package org.eclipse.scout.rt.ui.rap.util;

import java.util.Enumeration;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.osgi.framework.Version;

/**
 * @since 3.8.2
 */
public class BrowserInfoBuilder {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(BrowserInfoBuilder.class);

  public BrowserInfo createBrowserInfo(HttpServletRequest request) {
    BrowserInfo info;
    if (LOG.isInfoEnabled()) {
      Enumeration headerNames = request.getHeaderNames();
      while (headerNames.hasMoreElements()) {
        String headerName = (String) headerNames.nextElement();
        String header = request.getHeader(headerName);
        headerName = headerName + (headerName.length() <= 11 ? "\t\t" : "\t");
        LOG.info(headerName + header);
      }
    }

    String userAgent = request.getHeader("User-Agent");
    info = createBrowserInfo(userAgent, request.getLocale());

    if (LOG.isInfoEnabled()) {
      LOG.info(info.toString());
    }
    return info;
  }

  public BrowserInfo createBrowserInfo(String userAgent, Locale locale) {
    BrowserInfo info = createBrowserInfo(userAgent);
    info.setUserAgent(userAgent);
    info.setLocale(locale);

    if (userAgent.indexOf("Windows") != -1
        || userAgent.indexOf("Win32") != -1
        || userAgent.indexOf("Win64") != -1) {
      info.setSystem(BrowserInfo.System.WINDOWS);
      if (userAgent.indexOf("Windows Phone") != -1
          || userAgent.indexOf("IEMobile") != -1) {
        info.setSystemVersion(parseWindowsPhoneVersion(userAgent));
        info.setMobile(true);
      }
      else {
        info.setSystemVersion(parseWindowsVersion(userAgent));
      }
    }
    else if (userAgent.indexOf("Macintosh") != -1
        || userAgent.indexOf("MacPPC") != -1
        || userAgent.indexOf("MacIntel") != -1) {//FIXME
      info.setSystem(BrowserInfo.System.OSX);
    }
    else if (userAgent.indexOf("Android") != -1) {
      info.setSystem(BrowserInfo.System.ANDROID);
      info.setSystemVersion(parseAndroidVersion(userAgent));
      initAndroidMobileFlags(info);
    }
    else if (userAgent.indexOf("X11") != -1
        || userAgent.indexOf("Linux") != -1
        || userAgent.indexOf("BSD") != -1) {//FIXME
      info.setSystem(BrowserInfo.System.UNIX);
    }
    else if (userAgent.indexOf("iPad") != -1) {
      info.setSystem(BrowserInfo.System.IOS);
      info.setSystemVersion(parseIosVersion(userAgent));
      info.setTablet(true);
    }
    else if (userAgent.indexOf("iPhone") != -1
        || userAgent.indexOf("iPod") != -1) {
      info.setSystem(BrowserInfo.System.IOS);
      info.setSystemVersion(parseIosVersion(userAgent));
      info.setMobile(true);
    }
    else {
      info.setSystem(BrowserInfo.System.UNKNOWN);
    }

    return info;
  }

  private void initAndroidMobileFlags(BrowserInfo info) {
    if (info.getSystemVersion() == null) {
      info.setMobile(true);
      return;
    }

    if (info.getSystemVersion().getMajor() <= 2) {
      info.setMobile(true);
    }
    else if (info.getSystemVersion().getMajor() == 3) {
      info.setTablet(true);
    }
    else {
      //Android 4 is used on smartphones and tablets
      if (info.getUserAgent().indexOf("Mobile") != -1) {
        info.setMobile(true);
      }
      else {
        info.setTablet(true);
      }
    }
  }

  private BrowserInfo createBrowserInfo(String userAgent) {
    BrowserInfo info = null;
    Version v = null;

    //Opera
    String regex = "Opera[\\s\\/]([0-9\\.]*)";
    boolean isOpera = StringUtility.contains(userAgent, regex);
    if (isOpera) {
      v = parseBrowserVersion(userAgent, regex);
      info = new BrowserInfo(BrowserInfo.Type.OPERA, v);
      info.setOpera(isOpera);
      return info;
    }

    //Konqueror
    regex = "KHTML\\/([0-9-\\.]*)";
    boolean isKonqueror = StringUtility.contains(userAgent, regex);
    if (isKonqueror) {
      v = parseBrowserVersion(userAgent, regex);
      info = new BrowserInfo(BrowserInfo.Type.KONQUEROR, null);
      info.setWebkit(isKonqueror);
      return info;
    }

    //Webkit Browsers
    regex = "AppleWebKit\\/([^ ]+)";
    boolean isWebkit = userAgent.indexOf("AppleWebKit") != -1
        && StringUtility.contains(userAgent, regex);
    if (isWebkit) {
      v = parseBrowserVersion(userAgent, regex);
      if (userAgent.indexOf("Chrome") != -1) {
        info = new BrowserInfo(BrowserInfo.Type.GOOGLE_CHROME, v);
      }
      else if (userAgent.indexOf("Safari") != -1) {
        if (userAgent.indexOf("Android") != -1) {
          info = new BrowserInfo(BrowserInfo.Type.ANDROID, v);
        }
        else {
          info = new BrowserInfo(BrowserInfo.Type.APPLE_SAFARI, v);
        }
      }
      else if (userAgent.indexOf("OmniWeb") != -1) {
        info = new BrowserInfo(BrowserInfo.Type.OMNI_WEB, v);
      }
      else if (userAgent.indexOf("Shiira") != -1) {
        info = new BrowserInfo(BrowserInfo.Type.SHIRA, v);
      }
      else if (userAgent.indexOf("NetNewsWire") != -1) {
        info = new BrowserInfo(BrowserInfo.Type.BLACKPIXEL_NETNEWSWIRE, v);
      }
      else if (userAgent.indexOf("RealPlayer") != -1) {
        info = new BrowserInfo(BrowserInfo.Type.REALNETWORKS_REALPLAYER, v);
      }
      else if (userAgent.indexOf("Mobile") != -1) {
        // iPad reports this in fullscreen mode
        info = new BrowserInfo(BrowserInfo.Type.APPLE_SAFARI, v);
      }
      else {
        info = new BrowserInfo(BrowserInfo.Type.UNKNOWN, null);
      }
      info.setWebkit(isWebkit);
      return info;
    }

    //Gecko Browsers (Firefox)
    regex = "rv\\:([^\\);]+)(\\)|;)";
    boolean isGecko = userAgent.indexOf("Gecko") != -1
        && StringUtility.contains(userAgent, regex);
    if (isGecko) {
      v = parseBrowserVersion(userAgent, regex);
      if (userAgent.indexOf("Firefox") != -1) {
        info = new BrowserInfo(BrowserInfo.Type.MOZILLA_FIREFOX, v);
      }
      else if (userAgent.indexOf("Camino") != -1) {
        info = new BrowserInfo(BrowserInfo.Type.MOZILLA_CAMINO, v);
      }
      else if (userAgent.indexOf("Galeon") != -1) {
        info = new BrowserInfo(BrowserInfo.Type.GNOME_GALOEN, v);
      }
      else {
        info = new BrowserInfo(BrowserInfo.Type.UNKNOWN, null);
      }
      info.setGecko(isGecko);
      return info;
    }

    //Internet Explorer
    regex = "MSIE\\s+([^\\);]+)(\\)|;)";
    boolean isMshtml = StringUtility.contains(userAgent, regex);
    if (isMshtml) {
      v = parseBrowserVersion(userAgent, regex);
      if (userAgent.indexOf("MSIE") != -1) {
        info = new BrowserInfo(BrowserInfo.Type.IE, v);
      }
      else {
        info = new BrowserInfo(BrowserInfo.Type.UNKNOWN, null);
      }
      info.setMshtml(isMshtml);
      return info;
    }

    return new BrowserInfo(BrowserInfo.Type.UNKNOWN, null);
  }

  private Version parseBrowserVersion(String userAgent, String regex) {
    Matcher matcher = Pattern.compile(".*" + regex + ".*", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL).matcher(userAgent);
    if (matcher.matches()) {
      String s = matcher.group(1);
      return createVersion(s);
    }

    return null;
  }

  private Version parseSystemVersion(String userAgent, String regex, int group) {
    Matcher matcher = Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(userAgent);
    if (matcher.find()) {
      String s = matcher.group(group);
      return createVersion(s);
    }

    return null;
  }

  private Version parseSystemVersion(String userAgent, String regex) {
    return parseSystemVersion(userAgent, regex, 1);
  }

  private Version parseAndroidVersion(String userAgent) {
    return parseSystemVersion(userAgent, "Android\\s([^\\s;]+)");
  }

  private Version parseIosVersion(String userAgent) {
    userAgent = userAgent.replace("_", ".");
    return parseSystemVersion(userAgent, "\\sOS\\s([^\\s;]+)");
  }

  private Version parseWindowsPhoneVersion(String userAgent) {
    return parseSystemVersion(userAgent, "Windows\\sPhone\\s(OS )?([^\\s;]+)", 2);
  }

  private Version parseWindowsVersion(String userAgent) {
    return parseSystemVersion(userAgent, "Windows\\sNT\\s([^\\s;]+)");
  }

  private Version createVersion(String versionString) {
    versionString = versionString.replaceAll("^[/\\s]*", "");

    int[] vArr = new int[]{0, 0, 0};
    //Searches for 3 groups containing numbers separated with a dot.
    //Group 3 is optional (MSIE only has a major and a minor version, no micro)
    Matcher m = Pattern.compile("([0-9]+)\\.([0-9]+)[\\.]?([0-9]*)").matcher(versionString);

//    // Fix Opera version to match wikipedia style
//    version = version.substring( 0, 3 ) + "." + version.substring ( 3);FIXME sle
    if (m.find()) {
      for (int i = 1; i <= 3; i++) {
        String versionPart = m.group(i);
        if (StringUtility.hasText(versionPart)) {
          vArr[i - 1] = Integer.valueOf(versionPart);
        }
      }
    }

    return new Version(vArr[0], vArr[1], vArr[2]);
  }

}
