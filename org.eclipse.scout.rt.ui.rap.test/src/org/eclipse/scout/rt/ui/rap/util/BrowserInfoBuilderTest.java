/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.rap.util;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.Version;

/**
 * Tests for {@link BrowserInfoBuilder}.
 */
public class BrowserInfoBuilderTest {

  @Test
  public void testCreateBrowserInfo() {
    Map<String, BrowserInfo> testMap = initTestMap();
    BrowserInfoBuilder builder = new BrowserInfoBuilder();
    for (String userAgent : testMap.keySet()) {
      BrowserInfo createdBrowserInfo = builder.createBrowserInfo(userAgent, null);
      BrowserInfo expectedBrowserInfo = testMap.get(userAgent);

      //Ignore versions if not explicitly set
      if (expectedBrowserInfo.getVersion() == null) {
        createdBrowserInfo.setVersion(null);
      }
      if (expectedBrowserInfo.getSystemVersion() == null) {
        createdBrowserInfo.setSystemVersion(null);
      }

      Assert.assertEquals(expectedBrowserInfo, createdBrowserInfo);
    }
  }

  private Map<String, BrowserInfo> initTestMap() {
    Map<String, BrowserInfo> testMap = new HashMap<String, BrowserInfo>();

    putIosBrowsers(testMap);
    putAndroidBrowsers(testMap);
    putWindowsBrowsers(testMap);

    return testMap;
  }

  private void putIosBrowsers(Map<String, BrowserInfo> testMap) {
    //iPhone 4S
    String userAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 6_0 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/6.0 Mobile/10A403 Safari/8536.25";
    BrowserInfo browserInfo = new BrowserInfo(BrowserInfo.Type.APPLE_SAFARI, null, BrowserInfo.System.IOS);
    browserInfo.setUserAgent(userAgent);
    browserInfo.setWebkit(true);
    browserInfo.setMobile(true);
    browserInfo.setSystemVersion(new Version(6, 0, 0));
    testMap.put(userAgent, browserInfo);

    //iPad 3
    userAgent = "Mozilla/5.0 (iPad; CPU OS 5_1 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9B176 Safari/7534.48.3";
    browserInfo = new BrowserInfo(BrowserInfo.Type.APPLE_SAFARI, null, BrowserInfo.System.IOS);
    browserInfo.setUserAgent(userAgent);
    browserInfo.setWebkit(true);
    browserInfo.setTablet(true);
    browserInfo.setSystemVersion(new Version(5, 1, 0));
    testMap.put(userAgent, browserInfo);

    //iPad 3 (home screen icon mode)
    userAgent = "Mozilla/5.0 (iPad; CPU OS 5_1 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Mobile/9B176";
    browserInfo = new BrowserInfo(BrowserInfo.Type.APPLE_SAFARI, null, BrowserInfo.System.IOS);
    browserInfo.setUserAgent(userAgent);
    browserInfo.setWebkit(true);
    browserInfo.setTablet(true);
    browserInfo.setSystemVersion(new Version(5, 1, 0));
    testMap.put(userAgent, browserInfo);
  }

  private void putAndroidBrowsers(Map<String, BrowserInfo> testMap) {
    //Samsung tablet GT P7500
    String userAgent = "Mozilla/5.0 (Linux; U; Android 3.2; de-de; GT-P7500 Build/HTJ85B) AppleWebKit/534.13 (KHTML, like Gecko) Version/4.0 Safari/534.13";
    BrowserInfo browserInfo = new BrowserInfo(BrowserInfo.Type.ANDROID, null, BrowserInfo.System.ANDROID);
    browserInfo.setUserAgent(userAgent);
    browserInfo.setWebkit(true);
    browserInfo.setTablet(true);
    browserInfo.setSystemVersion(new Version(3, 2, 0));
    testMap.put(userAgent, browserInfo);

    //Samsung Galaxy S2 Android Browser
    userAgent = "Mozilla/5.0 (Linux; U; Android 4.0.3; de-ch; SAMSUNG GT-I9100/I9100BULPD Build/IML74K) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30";
    browserInfo = new BrowserInfo(BrowserInfo.Type.ANDROID, null, BrowserInfo.System.ANDROID);
    browserInfo.setUserAgent(userAgent);
    browserInfo.setWebkit(true);
    browserInfo.setMobile(true);
    browserInfo.setSystemVersion(new Version(4, 0, 3));
    testMap.put(userAgent, browserInfo);

    //Samsung Galaxy S2 Google Chrome
    userAgent = "Mozilla/5.0 (Linux; Android 4.0.3; GT-I9100 Build/IML74K) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.166 Mobile Safari/535.19";
    browserInfo = new BrowserInfo(BrowserInfo.Type.GOOGLE_CHROME, null, BrowserInfo.System.ANDROID);
    browserInfo.setUserAgent(userAgent);
    browserInfo.setWebkit(true);
    browserInfo.setMobile(true);
    browserInfo.setSystemVersion(new Version(4, 0, 3));
    testMap.put(userAgent, browserInfo);

    //Samsung Galaxy S2 Dolphin Browser
    userAgent = "Mozilla/5.0 (Linux; U; Android 4.0.3; de-ch; GT-I9100 Build/IML74K) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30";
    browserInfo = new BrowserInfo(BrowserInfo.Type.ANDROID, null, BrowserInfo.System.ANDROID);
    browserInfo.setUserAgent(userAgent);
    browserInfo.setWebkit(true);
    browserInfo.setMobile(true);
    browserInfo.setSystemVersion(new Version(4, 0, 3));
    testMap.put(userAgent, browserInfo);

    //Samsung Nexus S Firefox, Android 4.1.2
    userAgent = "Mozilla/5.0 (Android; Mobile; rv:17.0) Gecko/17.0 Firefox/17.0";
    browserInfo = new BrowserInfo(BrowserInfo.Type.MOZILLA_FIREFOX, null, BrowserInfo.System.ANDROID);
    browserInfo.setUserAgent(userAgent);
    browserInfo.setGecko(true);
    browserInfo.setMobile(true);
    browserInfo.setSystemVersion(null);
    testMap.put(userAgent, browserInfo);

  }

  private void putWindowsBrowsers(Map<String, BrowserInfo> testMap) {
    //Windows 7, IE 9
    String userAgent = "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0)";
    BrowserInfo browserInfo = new BrowserInfo(BrowserInfo.Type.IE, new Version(9, 0, 0), BrowserInfo.System.WINDOWS);
    browserInfo.setUserAgent(userAgent);
    browserInfo.setMshtml(true);
    browserInfo.setSystemVersion(new Version(6, 1, 0));
    testMap.put(userAgent, browserInfo);

    //Windows 7, Google Chrome 22.0.1229.94
    userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.4 (KHTML, like Gecko) Chrome/22.0.1229.94 Safari/537.4";
    browserInfo = new BrowserInfo(BrowserInfo.Type.GOOGLE_CHROME, null, BrowserInfo.System.WINDOWS);
    browserInfo.setUserAgent(userAgent);
    browserInfo.setWebkit(true);
    browserInfo.setSystemVersion(new Version(6, 1, 0));
    testMap.put(userAgent, browserInfo);

    //Nokia Lumia 800
    userAgent = "Mozilla/5.0 (compatible; MSIE 9.0; Windows Phone OS 7.5; Trident/5.0; IEMobile/9.0; NOKIA; Lumia 800)";
    browserInfo = new BrowserInfo(BrowserInfo.Type.IE, new Version(9, 0, 0), BrowserInfo.System.WINDOWS);
    browserInfo.setUserAgent(userAgent);
    browserInfo.setMshtml(true);
    browserInfo.setMobile(true);
    browserInfo.setSystemVersion(new Version(7, 5, 0));
    testMap.put(userAgent, browserInfo);

    //Windows Phone 8 HTC
    userAgent = "Mozilla/5.0 (compatible; MSIE 10.0; Windows Phone 8.0; Trident/6.0; IEMobile/10.0; ARM; Touch; HTC; Windows Phone 8X by HTC)";
    browserInfo = new BrowserInfo(BrowserInfo.Type.IE, new Version(10, 0, 0), BrowserInfo.System.WINDOWS);
    browserInfo.setUserAgent(userAgent);
    browserInfo.setMshtml(true);
    browserInfo.setMobile(true);
    browserInfo.setSystemVersion(new Version(8, 0, 0));
    testMap.put(userAgent, browserInfo);

  }
}
