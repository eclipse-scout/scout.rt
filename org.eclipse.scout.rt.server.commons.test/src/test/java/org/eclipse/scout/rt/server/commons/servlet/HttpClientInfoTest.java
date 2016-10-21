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
package org.eclipse.scout.rt.server.commons.servlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.eclipse.scout.rt.server.commons.servlet.HttpClientInfo.Version;
import org.eclipse.scout.rt.shared.ui.UiEngineType;
import org.eclipse.scout.rt.shared.ui.UiSystem;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;

/**
 * The list of user agents for all tests that start with "bulkTests*" was initially retrieved from
 * http://techpatterns.com/downloads/firefox/useragentswitcher.xml on 2015-01-05. (See
 * http://techpatterns.com/forums/about304.html for details.)
 */
public class HttpClientInfoTest {

  private static final P_HttpClientInfoTestFlags FLAGS_NONE = P_HttpClientInfoTestFlags.create().notMobile().notTablet().notWindows().notLinux().notMac();
  private static final P_HttpClientInfoTestFlags FLAGS_DESKTOP_WINDOWS = FLAGS_NONE.copy().windows();
  private static final P_HttpClientInfoTestFlags FLAGS_DESKTOP_MAC = FLAGS_NONE.copy().mac();
  private static final P_HttpClientInfoTestFlags FLAGS_DESKTOP_LINUX = FLAGS_NONE.copy().linux();
  private static final P_HttpClientInfoTestFlags FLAGS_MOBILE_MOBILE = FLAGS_NONE.copy().mobile();
  private static final P_HttpClientInfoTestFlags FLAGS_MOBILE_TABLET = FLAGS_NONE.copy().tablet();
  private static final P_HttpClientInfoTestFlags FLAGS_MOBILE_WINDOWS_PHONE = FLAGS_MOBILE_MOBILE.copy().windows();

  private long m_failCount = 0;

  @Test
  public void bulkTests_EmptyData() {
    checkHttpClientInfoFlags(null, null);
    checkHttpClientInfoFlags(null, P_HttpClientInfoTestFlags.create());
    checkHttpClientInfoFlags("", null);
    checkHttpClientInfoFlags("", P_HttpClientInfoTestFlags.create());
    checkHttpClientInfoFlags("", FLAGS_NONE);

    assertPassed();
  }

  @Test
  public void bulkTests_Desktop_Windows() {
    // Browsers - Windows
    // Legacy Browsers
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US) AppleWebKit/527  (KHTML, like Gecko, Safari/419.3) Arora/0.6 (Change: )", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Avant Browser/1.2.789rel1 (http://www.avantbrowser.com)", FLAGS_NONE);
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/532.5 (KHTML, like Gecko) Chrome/4.0.249.0 Safari/532.5", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows; U; Windows NT 5.2; en-US) AppleWebKit/532.9 (KHTML, like Gecko) Chrome/5.0.310.0 Safari/532.9", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/534.7 (KHTML, like Gecko) Chrome/7.0.514.0 Safari/534.7", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US) AppleWebKit/534.14 (KHTML, like Gecko) Chrome/9.0.601.0 Safari/534.14", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/534.14 (KHTML, like Gecko) Chrome/10.0.601.0 Safari/534.14", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/534.20 (KHTML, like Gecko) Chrome/11.0.672.2 Safari/534.20", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.27 (KHTML, like Gecko) Chrome/12.0.712.0 Safari/534.27", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.24 Safari/535.1", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows NT 6.0) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.120 Safari/535.2", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.7 (KHTML, like Gecko) Chrome/16.0.912.36 Safari/535.7", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/18.6.872.0 Safari/535.2 UNTRUSTED/1.0 3gpp-gba UNTRUSTED/1.0", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows NT 6.2) AppleWebKit/536.3 (KHTML, like Gecko) Chrome/19.0.1061.1 Safari/536.3", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/536.6 (KHTML, like Gecko) Chrome/20.0.1092.0 Safari/536.6", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows NT 6.2) AppleWebKit/536.6 (KHTML, like Gecko) Chrome/20.0.1090.0 Safari/536.6", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows; U; Windows NT 6.0 x64; en-US; rv:1.9pre) Gecko/2008072421 Minefield/3.0.2pre", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.0.10) Gecko/2009042316 Firefox/3.0.10", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows; U; Windows NT 6.0; en-GB; rv:1.9.0.11) Gecko/2009060215 Firefox/3.0.11 (.NET CLR 3.5.30729)", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US; rv:1.9.1.6) Gecko/20091201 Firefox/3.5.6 GTB5", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows; U; Windows NT 5.1; tr; rv:1.9.2.8) Gecko/20100722 Firefox/3.6.8 ( .NET CLR 3.5.30729; .NET4.0E)", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows NT 6.1; rv:2.0.1) Gecko/20100101 Firefox/4.0.1", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:2.0.1) Gecko/20100101 Firefox/4.0.1", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows NT 5.1; rv:5.0) Gecko/20100101 Firefox/5.0", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:6.0a2) Gecko/20110622 Firefox/6.0a2", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:7.0.1) Gecko/20100101 Firefox/7.0.1", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:10.0.1) Gecko/20100101 Firefox/10.0.1", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows NT 6.1; rv:12.0) Gecko/20120403211507 Firefox/12.0", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows NT 6.2; rv:20.0) Gecko/20121202 Firefox/20.0", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:2.0b4pre) Gecko/20100815 Minefield/4.0b4pre", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/4.0 (compatible; MSIE 5.5; Windows NT 5.0 )", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/4.0 (compatible; MSIE 5.5; Windows 98; Win 9x 4.90)", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows; U; Windows XP) Gecko MultiZilla/1.6.1.0a", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/2.02E (Win95; U)", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/3.01Gold (Win95; I)", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/4.8 [en] (Windows NT 5.1; U)", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows; U; Win98; en-US; rv:1.4) Gecko Netscape/7.1 (ax)", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Opera/7.50 (Windows XP; U)", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Opera/7.50 (Windows ME; U) [en]", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Opera/7.51 (Windows NT 5.1; U) [en]", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0; en) Opera 8.0", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Opera/9.25 (Windows NT 6.0; U; en)", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Opera/9.80 (Windows NT 5.2; U; en) Presto/2.2.15 Version/10.10", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Opera/9.80 (Windows NT 5.1; U; zh-tw) Presto/2.8.131 Version/11.10", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Opera/9.80 (Windows NT 6.1; U; es-ES) Presto/2.9.181 Version/12.00", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows; U; WinNT4.0; en-US; rv:1.2b) Gecko/20021001 Phoenix/0.2", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/531.21.8 (KHTML, like Gecko) Version/4.0.4 Safari/531.21.10", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.23) Gecko/20090825 SeaMonkey/1.1.18", FLAGS_DESKTOP_WINDOWS);
    // Current
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows; U; ; en-NZ) AppleWebKit/527  (KHTML, like Gecko, Safari/419.3) Arora/0.8.0", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Avant Browser; Avant Browser; .NET CLR 1.0.3705; .NET CLR 1.1.4322; Media Center PC 4.0; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30)", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.8 (KHTML, like Gecko) Beamrise/17.2.0.9 Chrome/17.0.939.0 Safari/535.8", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.1 (KHTML, like Gecko) Chrome/22.0.1207.1 Safari/537.1", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML like Gecko) Chrome/28.0.1469.0 Safari/537.36", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.36 (KHTML like Gecko) Chrome/28.0.1469.0 Safari/537.36", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows NT 6.2; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1667.0 Safari/537.36", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/36.0.1985.67 Safari/537.36", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2049.0 Safari/537.36", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows NT 6.0; rv:14.0) Gecko/20100101 Firefox/14.0.1", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:15.0) Gecko/20120427 Firefox/15.0a1", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows NT 6.2; Win64; x64; rv:16.0) Gecko/16.0 Firefox/16.0", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows NT 6.2; rv:19.0) Gecko/20121129 Firefox/19.0", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows NT 6.1; rv:21.0) Gecko/20130401 Firefox/21.0", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/29.0", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows NT 5.1; rv:31.0) Gecko/20100101 Firefox/31.0", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:35.0) Gecko/20100101 Firefox/35.0", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("iTunes/9.0.2 (Windows; N)", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (compatible; Konqueror/4.5; Windows) KHTML/4.5.4 (like Gecko)", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.1; Trident/4.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; Maxthon 2.0)", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US) AppleWebKit/533.1 (KHTML, like Gecko) Maxthon/3.0.8.2 Safari/533.1", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.1 (KHTML like Gecko) Maxthon/4.0.0.2000 Chrome/22.0.1229.79 Safari/537.1", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1)", FLAGS_DESKTOP_WINDOWS.copy().engineType(UiEngineType.IE));
    checkHttpClientInfoFlags("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0)", FLAGS_DESKTOP_WINDOWS.copy().engineType(UiEngineType.IE));
    checkHttpClientInfoFlags("Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; .NET CLR 2.0.50727; .NET CLR 3.0.04506.648; .NET CLR 3.5.21022; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)",
        FLAGS_DESKTOP_WINDOWS.copy().engineType(UiEngineType.IE));
    checkHttpClientInfoFlags("Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; Trident/4.0)", FLAGS_DESKTOP_WINDOWS.copy().engineType(UiEngineType.IE));
    checkHttpClientInfoFlags("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0; Trident/4.0)", FLAGS_DESKTOP_WINDOWS.copy().engineType(UiEngineType.IE));
    checkHttpClientInfoFlags("Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.0; Trident/4.0)", FLAGS_DESKTOP_WINDOWS.copy().engineType(UiEngineType.IE));
    checkHttpClientInfoFlags("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0; Trident/5.0)", FLAGS_DESKTOP_WINDOWS.copy().engineType(UiEngineType.IE));
    checkHttpClientInfoFlags("Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0)", FLAGS_DESKTOP_WINDOWS.copy().engineType(UiEngineType.IE));
    checkHttpClientInfoFlags("Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.2; Trident/5.0)", FLAGS_DESKTOP_WINDOWS.copy().engineType(UiEngineType.IE));
    checkHttpClientInfoFlags("Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.2; WOW64; Trident/5.0)", FLAGS_DESKTOP_WINDOWS.copy().engineType(UiEngineType.IE));
    checkHttpClientInfoFlags("Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0; SLCC2; Media Center PC 6.0; InfoPath.3; MS-RTC LM 8; Zune 4.7)", FLAGS_DESKTOP_WINDOWS.copy().engineType(UiEngineType.IE));
    checkHttpClientInfoFlags("Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; WOW64; Trident/6.0)", FLAGS_DESKTOP_WINDOWS.copy().engineType(UiEngineType.IE));
    checkHttpClientInfoFlags("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.1; Trident/6.0)", FLAGS_DESKTOP_WINDOWS.copy().engineType(UiEngineType.IE));
    checkHttpClientInfoFlags("Mozilla/5.0 (compatible; MSIE 10.6; Windows NT 6.1; Trident/5.0; InfoPath.2; SLCC1; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729; .NET CLR 2.0.50727) 3gpp-gba UNTRUSTED/1.0",
        FLAGS_DESKTOP_WINDOWS.copy().engineType(UiEngineType.IE));
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0) like Gecko", FLAGS_DESKTOP_WINDOWS.copy().engineType(UiEngineType.IE));
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows NT 6.3; Trident/7.0; rv:11.0) like Gecko", FLAGS_DESKTOP_WINDOWS.copy().engineType(UiEngineType.IE));
    checkHttpClientInfoFlags("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.3; Trident/7.0; .NET4.0E; .NET4.0C)", FLAGS_DESKTOP_WINDOWS.copy().engineType(UiEngineType.IE));
    checkHttpClientInfoFlags("Opera/9.80 (Windows NT 6.1; U; en) Presto/2.7.62 Version/11.01", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Opera/9.80 (Windows NT 6.0) Presto/2.12.388 Version/12.14", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Opera/9.80 (Windows NT 6.1; WOW64) Presto/2.12.388 Version/12.16", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/27.0.1453.12 Safari/537.36 OPR/14.0.1116.4", FLAGS_DESKTOP_WINDOWS.copy().engineType(UiEngineType.CHROME));
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.29 Safari/537.36 OPR/15.0.1147.24 (Edition Next)", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.57 Safari/537.36 OPR/18.0.1284.49", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1700.76 Safari/537.36 OPR/19.0.1326.56", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.154 Safari/537.36 OPR/20.0.1387.91", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows; U; Windows NT 5.2; en-US) AppleWebKit/533.17.8 (KHTML, like Gecko) Version/5.0.1 Safari/533.17.8", FLAGS_DESKTOP_WINDOWS.copy().engineType(UiEngineType.SAFARI));
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/533.19.4 (KHTML, like Gecko) Version/5.0.2 Safari/533.18.5", FLAGS_DESKTOP_WINDOWS.copy().engineType(UiEngineType.SAFARI));
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows; U; Windows NT 6.2; es-US ) AppleWebKit/540.0 (KHTML like Gecko) Version/6.0 Safari/8900.00", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.71 (KHTML like Gecko) WebVideo/1.0.1.10 Version/7.0 Safari/537.71", FLAGS_DESKTOP_WINDOWS.copy().engineType(UiEngineType.SAFARI));
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.1.17) Gecko/20110123 (like Firefox/3.x) SeaMonkey/2.0.12", FLAGS_DESKTOP_WINDOWS.copy().engineType(UiEngineType.FIREFOX));
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows NT 5.2; rv:10.0.1) Gecko/20100101 Firefox/10.0.1 SeaMonkey/2.7.1", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:12.0) Gecko/20120422 Firefox/12.0 SeaMonkey/2.9", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows NT 10.0; <64-bit tags>) AppleWebKit/<WebKit Rev> (KHTML, like Gecko) Chrome/<Chrome Rev> Safari/<WebKit Rev> Edge/<EdgeHTML Rev>.<Windows Build>", FLAGS_DESKTOP_WINDOWS);

    assertPassed();
  }

  @Test
  public void bulkTests_Desktop_Mac() {
    // Browsers - Mac
    // Legacy
    checkHttpClientInfoFlags("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_5_8; en-US) AppleWebKit/532.8 (KHTML, like Gecko) Chrome/4.0.302.2 Safari/532.8", FLAGS_DESKTOP_MAC);
    checkHttpClientInfoFlags("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_4; en-US) AppleWebKit/534.3 (KHTML, like Gecko) Chrome/6.0.464.0 Safari/534.3", FLAGS_DESKTOP_MAC);
    checkHttpClientInfoFlags("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_5; en-US) AppleWebKit/534.13 (KHTML, like Gecko) Chrome/9.0.597.15 Safari/534.13", FLAGS_DESKTOP_MAC);
    checkHttpClientInfoFlags("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_2) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/14.0.835.186 Safari/535.1", FLAGS_DESKTOP_MAC);
    checkHttpClientInfoFlags("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_6_8) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.54 Safari/535.2", FLAGS_DESKTOP_MAC);
    checkHttpClientInfoFlags("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_6_8) AppleWebKit/535.7 (KHTML, like Gecko) Chrome/16.0.912.36 Safari/535.7", FLAGS_DESKTOP_MAC);
    checkHttpClientInfoFlags("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_0) AppleWebKit/536.3 (KHTML, like Gecko) Chrome/19.0.1063.0 Safari/536.3", FLAGS_DESKTOP_MAC);
    checkHttpClientInfoFlags("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_2) AppleWebKit/537.4 (KHTML like Gecko) Chrome/22.0.1229.79 Safari/537.4", FLAGS_DESKTOP_MAC);
    checkHttpClientInfoFlags("Mozilla/5.0 (Macintosh; U; Mac OS X Mach-O; en-US; rv:2.0a) Gecko/20040614 Firefox/3.0.0 ", FLAGS_DESKTOP_MAC);
    checkHttpClientInfoFlags("Mozilla/5.0 (Macintosh; U; PPC Mac OS X 10.5; en-US; rv:1.9.0.3) Gecko/2008092414 Firefox/3.0.3", FLAGS_DESKTOP_MAC);
    checkHttpClientInfoFlags("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.5; en-US; rv:1.9.1) Gecko/20090624 Firefox/3.5", FLAGS_DESKTOP_MAC);
    checkHttpClientInfoFlags("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.6; en-US; rv:1.9.2.14) Gecko/20110218 AlexaToolbar/alxf-2.0 Firefox/3.6.14", FLAGS_DESKTOP_MAC);
    checkHttpClientInfoFlags("Mozilla/5.0 (Macintosh; U; PPC Mac OS X 10.5; en-US; rv:1.9.2.15) Gecko/20110303 Firefox/3.6.15", FLAGS_DESKTOP_MAC);
    checkHttpClientInfoFlags("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.6; rv:2.0.1) Gecko/20100101 Firefox/4.0.1", FLAGS_DESKTOP_MAC);
    checkHttpClientInfoFlags("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.6; rv:5.0) Gecko/20100101 Firefox/5.0", FLAGS_DESKTOP_MAC);
    checkHttpClientInfoFlags("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.6; rv:9.0) Gecko/20100101 Firefox/9.0", FLAGS_DESKTOP_MAC);
    checkHttpClientInfoFlags("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_2; rv:10.0.1) Gecko/20100101 Firefox/10.0.1", FLAGS_DESKTOP_MAC);
    checkHttpClientInfoFlags("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20120813 Firefox/16.0", FLAGS_DESKTOP_MAC);
    checkHttpClientInfoFlags("Mozilla/4.0 (compatible; MSIE 5.15; Mac_PowerPC)", FLAGS_DESKTOP_MAC);
    checkHttpClientInfoFlags("Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en-US) AppleWebKit/125.4 (KHTML, like Gecko, Safari) OmniWeb/v563.15", FLAGS_DESKTOP_MAC);
    checkHttpClientInfoFlags("Opera/9.0 (Macintosh; PPC Mac OS X; U; en)", FLAGS_DESKTOP_MAC);
    checkHttpClientInfoFlags("Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en) AppleWebKit/125.2 (KHTML, like Gecko) Safari/85.8", FLAGS_DESKTOP_MAC);
    checkHttpClientInfoFlags("Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en) AppleWebKit/125.2 (KHTML, like Gecko) Safari/125.8", FLAGS_DESKTOP_MAC);
    checkHttpClientInfoFlags("Mozilla/5.0 (Macintosh; U; PPC Mac OS X; fr-fr) AppleWebKit/312.5 (KHTML, like Gecko) Safari/312.3", FLAGS_DESKTOP_MAC);
    checkHttpClientInfoFlags("Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en) AppleWebKit/418.8 (KHTML, like Gecko) Safari/419.3", FLAGS_DESKTOP_MAC);
    // Current
    checkHttpClientInfoFlags("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.6; rv:2.0.1) Gecko/20100101 Firefox/4.0.1 Camino/2.2.1", FLAGS_DESKTOP_MAC);
    checkHttpClientInfoFlags("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.6; rv:2.0b6pre) Gecko/20100907 Firefox/4.0b6pre Camino/2.2a1pre", FLAGS_DESKTOP_MAC);
    checkHttpClientInfoFlags("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_4) AppleWebKit/537.31 (KHTML like Gecko) Chrome/26.0.1410.63 Safari/537.31", FLAGS_DESKTOP_MAC);
    checkHttpClientInfoFlags("Mozilla/5.0 (Macintosh; Intel Mac OS X 1083) AppleWebKit/537.36 (KHTML like Gecko) Chrome/28.0.1469.0 Safari/537.36", FLAGS_DESKTOP_MAC);
    checkHttpClientInfoFlags("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1664.3 Safari/537.36", FLAGS_DESKTOP_MAC);
    checkHttpClientInfoFlags("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/36.0.1944.0 Safari/537.36", FLAGS_DESKTOP_MAC);
    checkHttpClientInfoFlags("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.7; rv:20.0) Gecko/20100101 Firefox/20.0", FLAGS_DESKTOP_MAC);
    checkHttpClientInfoFlags("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:21.0) Gecko/20100101 Firefox/21.0", FLAGS_DESKTOP_MAC);
    checkHttpClientInfoFlags("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.6; rv:25.0) Gecko/20100101 Firefox/25.0", FLAGS_DESKTOP_MAC);
    checkHttpClientInfoFlags("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.9; rv:35.0) Gecko/20100101 Firefox/35.0", FLAGS_DESKTOP_MAC);
    checkHttpClientInfoFlags("iTunes/4.2 (Macintosh; U; PPC Mac OS X 10.2)", FLAGS_DESKTOP_MAC);
    checkHttpClientInfoFlags("iTunes/9.0.3 (Macintosh; U; Intel Mac OS X 10_6_2; en-ca)", FLAGS_DESKTOP_MAC);
    checkHttpClientInfoFlags("Mozilla/5.0 (Macintosh; U; Intel Mac OS X; en-US) AppleWebKit/528.16 (KHTML, like Gecko, Safari/528.16) OmniWeb/v622.8.0.112941", FLAGS_DESKTOP_MAC);
    checkHttpClientInfoFlags("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_5_6; en-US) AppleWebKit/528.16 (KHTML, like Gecko, Safari/528.16) OmniWeb/v622.8.0", FLAGS_DESKTOP_MAC);
    checkHttpClientInfoFlags("Opera/9.20 (Macintosh; Intel Mac OS X; U; en)", FLAGS_DESKTOP_MAC);
    checkHttpClientInfoFlags("Opera/9.64 (Macintosh; PPC Mac OS X; U; en) Presto/2.1.1", FLAGS_DESKTOP_MAC);
    checkHttpClientInfoFlags("Opera/9.80 (Macintosh; Intel Mac OS X; U; en) Presto/2.6.30 Version/10.61", FLAGS_DESKTOP_MAC);
    checkHttpClientInfoFlags("Opera/9.80 (Macintosh; Intel Mac OS X 10.4.11; U; en) Presto/2.7.62 Version/11.00", FLAGS_DESKTOP_MAC);
    checkHttpClientInfoFlags("Opera/9.80 (Macintosh; Intel Mac OS X 10.6.8; U; fr) Presto/2.9.168 Version/11.52", FLAGS_DESKTOP_MAC);
    checkHttpClientInfoFlags("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_2; en-us) AppleWebKit/531.21.8 (KHTML, like Gecko) Version/4.0.4 Safari/531.21.10", FLAGS_DESKTOP_MAC);
    checkHttpClientInfoFlags("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_5; de-de) AppleWebKit/534.15  (KHTML, like Gecko) Version/5.0.3 Safari/533.19.4", FLAGS_DESKTOP_MAC);
    checkHttpClientInfoFlags("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_6; en-us) AppleWebKit/533.20.25 (KHTML, like Gecko) Version/5.0.4 Safari/533.20.27", FLAGS_DESKTOP_MAC);
    checkHttpClientInfoFlags("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_7; en-us) AppleWebKit/534.20.8 (KHTML, like Gecko) Version/5.1 Safari/534.20.8", FLAGS_DESKTOP_MAC);
    checkHttpClientInfoFlags("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_3) AppleWebKit/534.55.3 (KHTML, like Gecko) Version/5.1.3 Safari/534.53.10", FLAGS_DESKTOP_MAC);
    checkHttpClientInfoFlags("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_6_8) AppleWebKit/537.13+ (KHTML, like Gecko) Version/5.1.7 Safari/534.57.2", FLAGS_DESKTOP_MAC);
    checkHttpClientInfoFlags("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/536.26.17 (KHTML like Gecko) Version/6.0.2 Safari/536.26.17", FLAGS_DESKTOP_MAC);
    checkHttpClientInfoFlags("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_5) AppleWebKit/537.78.1 (KHTML like Gecko) Version/7.0.6 Safari/537.78.1", FLAGS_DESKTOP_MAC);
    checkHttpClientInfoFlags("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.5; rv:10.0.1) Gecko/20100101 Firefox/10.0.1 SeaMonkey/2.7.1", FLAGS_DESKTOP_MAC);
    checkHttpClientInfoFlags("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_3; en-us; Silk/1.0.13.81_10003810) AppleWebKit/533.16 (KHTML, like Gecko) Version/5.0 Safari/533.16 Silk-Accelerated=true", FLAGS_DESKTOP_MAC);

    assertPassed();
  }

  @Test
  public void bulkTests_Desktop_Linux() {
    // Browsers - Linux
    // Console Browsers
    checkHttpClientInfoFlags("ELinks (0.4pre5; Linux 2.6.10-ac7 i686; 80x33)", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("ELinks/0.9.3 (textmode; Linux 2.6.9-kanotix-8 i686; 127x41)", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("ELinks/0.12~pre5-4", FLAGS_NONE);
    checkHttpClientInfoFlags("Links/0.9.1 (Linux 2.4.24; i386;)", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Links (2.1pre15; Linux 2.4.26 i686; 158x61)", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Links (2.3pre1; Linux 2.6.38-8-generic x86_64; 170x48)", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Lynx/2.8.5rel.1 libwww-FM/2.14 SSL-MM/1.4.1 GNUTLS/0.8.12", FLAGS_NONE);
    checkHttpClientInfoFlags("w3m/0.5.1", FLAGS_NONE);
    // Legacy Browsers
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; U; Linux i686; en-US) AppleWebKit/532.4 (KHTML, like Gecko) Chrome/4.0.237.0 Safari/532.4 Debian", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; U; Linux i686; en-US) AppleWebKit/532.8 (KHTML, like Gecko) Chrome/4.0.277.0 Safari/532.8", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; U; Linux x86_64; en-US) AppleWebKit/532.9 (KHTML, like Gecko) Chrome/5.0.309.0 Safari/532.9", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; U; Linux x86_64; en-US) AppleWebKit/534.7 (KHTML, like Gecko) Chrome/7.0.514.0 Safari/534.7", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; U; Linux x86_64; en-US) AppleWebKit/540.0 (KHTML, like Gecko) Ubuntu/10.10 Chrome/9.1.0.0 Safari/540.0", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; U; Linux x86_64; en-US) AppleWebKit/534.15 (KHTML, like Gecko) Chrome/10.0.613.0 Safari/534.15", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; U; Linux i686; en-US) AppleWebKit/534.15 (KHTML, like Gecko) Ubuntu/10.10 Chromium/10.0.613.0 Chrome/10.0.613.0 Safari/534.15", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/534.24 (KHTML, like Gecko) Ubuntu/10.10 Chromium/12.0.703.0 Chrome/12.0.703.0 Safari/534.24", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.20 Safari/535.1", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 Slackware/13.37 (X11; U; Linux x86_64; en-US) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.41", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; Linux i686) AppleWebKit/535.1 (KHTML, like Gecko) Ubuntu/11.04 Chromium/14.0.825.0 Chrome/14.0.825.0 Safari/535.1", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; Linux i686) AppleWebKit/535.2 (KHTML, like Gecko) Ubuntu/11.10 Chromium/15.0.874.120 Chrome/15.0.874.120 Safari/535.2", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; U; Linux; i686; en-US; rv:1.6) Gecko Epiphany/1.2.5", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; U; Linux i586; en-US; rv:1.7.3) Gecko/20040924 Epiphany/1.4.4 (Ubuntu)", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.6) Gecko/20040614 Firefox/0.8", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; U; Linux x86_64; sv-SE; rv:1.8.1.12) Gecko/20080207 Ubuntu/7.10 (gutsy) Firefox/2.0.0.12", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.11) Gecko/2009060309 Ubuntu/9.10 (karmic) Firefox/3.0.11", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.1.2) Gecko/20090803 Ubuntu/9.04 (jaunty) Shiretoko/3.5.2", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.1.5) Gecko/20091107 Firefox/3.5.5", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.1.3) Gecko/20091020 Linux Mint/8 (Helena) Firefox/3.5.3", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.2.9) Gecko/20100915 Gentoo Firefox/3.6.9", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; U; Linux i686; pl-PL; rv:1.9.0.2) Gecko/20121223 Ubuntu/9.25 (jaunty) Firefox/3.8", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; Linux i686; rv:2.0b6pre) Gecko/20100907 Firefox/4.0b6pre", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; Linux i686 on x86_64; rv:2.0.1) Gecko/20100101 Firefox/4.0.1", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; Linux i686; rv:2.0.1) Gecko/20100101 Firefox/4.0.1", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; Linux x86_64; rv:2.0.1) Gecko/20100101 Firefox/4.0.1", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; Linux x86_64; rv:2.2a1pre) Gecko/20100101 Firefox/4.2a1pre", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; Linux i686; rv:5.0) Gecko/20100101 Firefox/5.0", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; Linux i686; rv:6.0) Gecko/20100101 Firefox/6.0", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; Linux x86_64; rv:7.0a1) Gecko/20110623 Firefox/7.0a1", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; Linux i686; rv:8.0) Gecko/20100101 Firefox/8.0", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; Linux x86_64; rv:10.0.1) Gecko/20100101 Firefox/10.0.1", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.1.16) Gecko/20120421 Gecko Firefox/11.0", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; Linux i686; rv:12.0) Gecko/20100101 Firefox/12.0 ", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:14.0) Gecko/20100101 Firefox/14.0.1", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; U; Linux; i686; en-US; rv:1.6) Gecko Galeon/1.3.14", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; U; Linux ppc; en-US; rv:1.8.1.13) Gecko/20080313 Iceape/1.1.9 (Debian-1.1.9-5)", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; U; Linux i686; pt-PT; rv:1.9.2.3) Gecko/20100402 Iceweasel/3.6.3 (like Firefox/3.6.3) GTB7.0", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; Linux x86_64; rv:5.0) Gecko/20100101 Firefox/5.0 Iceweasel/5.0", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; Linux i686; rv:6.0a2) Gecko/20110615 Firefox/6.0a2 Iceweasel/6.0a2", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Konqueror/3.0-rc4; (Konqueror/3.0-rc4; i686 Linux;;datecode)", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (compatible; Konqueror/3.3; Linux 2.6.8-gentoo-r3; X11;", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (compatible; Konqueror/3.5; Linux 2.6.30-7.dmz.1-liquorix-686; X11) KHTML/3.5.10 (like Gecko) (Debian package 4:3.5.10.dfsg.1-1 b1)", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (compatible; Konqueror/3.5; Linux; en_US) KHTML/3.5.6 (like Gecko) (Kubuntu)", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; Linux x86_64; en-US; rv:2.0b2pre) Gecko/20100712 Minefield/4.0b2pre", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; U; Linux; i686; en-US; rv:1.6) Gecko Debian/1.6-7", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("MSIE (MSIE 6.0; X11; Linux; i686) Opera 7.23", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.1.17) Gecko/20110123 SeaMonkey/2.0.12", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.8.1) Gecko/20061024 Firefox/2.0 (Swiftfox)", FLAGS_DESKTOP_LINUX);
    // Current
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; U; Linux; en-US) AppleWebKit/527  (KHTML, like Gecko, Safari/419.3) Arora/0.10.1", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/536.5 (KHTML, like Gecko) Chrome/19.0.1084.9 Safari/536.5", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; CrOS i686 2268.111.0) AppleWebKit/536.11 (KHTML, like Gecko) Chrome/20.0.1132.57 Safari/536.11", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.4 (KHTML like Gecko) Chrome/22.0.1229.56 Safari/537.4", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; Linux i686) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1478.0 Safari/537.36", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; CrOS x86_64 5841.83.0) AppleWebKit/537.36 (KHTML like Gecko) Chrome/36.0.1985.138 Safari/537.36", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; Linux i686) AppleWebKit/537.22 (KHTML like Gecko) Ubuntu Chromium/25.0.1364.160 Chrome/25.0.1364.160 Safari/537.22", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML like Gecko) Chrome/36.0.1985.125 Safari/537.36", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; Linux i686) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2166.2 Safari/537.36", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/4.0 (compatible; Dillo 3.0)", FLAGS_NONE);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; U; Linux i686; en-us) AppleWebKit/528.5  (KHTML, like Gecko, Safari/528.5 ) lt-GtkLauncher", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; Linux i686; rv:16.0) Gecko/20100101 Firefox/16.0", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; U; Linux i686; rv:19.0) Gecko/20100101 Slackware/13 Firefox/19.0", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:20.0) Gecko/20100101 Firefox/20.0", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; Linux i686; rv:20.0) Gecko/20100101 Firefox/20.0", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; Linux i686; rv:25.0) Gecko/20100101 Firefox/25.0", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; Linux i686; rv:28.0) Gecko/20100101 Firefox/28.0", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; Linux i686; rv:32.0) Gecko/20100101 Firefox/32.0", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:35.0) Gecko/20100101 Firefox/35.0", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.8) Gecko Galeon/2.0.6 (Ubuntu 2.0.6-2)", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.8.1.16) Gecko/20080716 (Gentoo) Galeon/2.0.6", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.1.13) Gecko/20100916 Iceape/2.0.8", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; Linux i686; rv:14.0) Gecko/20100101 Firefox/14.0.1 Iceweasel/14.0.1", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; Linux x86_64; rv:15.0) Gecko/20120724 Debian Iceweasel/15.02", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; Linux x86_64; rv:19.0) Gecko/20100101 Firefox/19.0 Iceweasel/19.0.2", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (compatible; Konqueror/4.2; Linux) KHTML/4.2.4 (like Gecko) Slackware/13.0", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (compatible; Konqueror/4.3; Linux) KHTML/4.3.1 (like Gecko) Fedora/4.3.1-3.fc11", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (compatible; Konqueror/4.4; Linux) KHTML/4.4.1 (like Gecko) Fedora/4.4.1-1.fc12", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (compatible; Konqueror/4.4; Linux 2.6.32-22-generic; X11; en_US) KHTML/4.4.3 (like Gecko) Kubuntu", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (compatible; Konqueror/4.4; Linux 2.6.32-22-generic; X11; en_US) KHTML/4.4.3 (like Gecko) Kubuntu", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; Linux 3.8-6.dmz.1-liquorix-686) KHTML/4.8.4 (like Gecko) Konqueror/4.8", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; Linux) KHTML/4.9.1 (like Gecko) Konqueror/4.9", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Midori/0.1.10 (X11; Linux i686; U; en-us) WebKit/(531).(2) ", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.0.3) Gecko/2008092814 (Debian-3.0.1-1)", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9a3pre) Gecko/20070330", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Opera/9.64 (X11; Linux i686; U; Linux Mint; nb) Presto/2.1.1", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Opera/9.80 (X11; Linux i686; U; en) Presto/2.2.15 Version/10.10", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Opera/9.80 (X11; Linux x86_64; U; pl) Presto/2.7.62 Version/11.00", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Opera/9.80 (X11; Linux i686) Presto/2.12.388 Version/12.16", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.166 Safari/537.36 OPR/20.0.1396.73172", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; Linux i686) AppleWebKit/534.34 (KHTML, like Gecko) QupZilla/1.2.0 Safari/534.34", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; Linux i686; rv:10.0.1) Gecko/20100101 Firefox/10.0.1 SeaMonkey/2.7.1", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; Linux i686; rv:12.0) Gecko/20120502 Firefox/12.0 SeaMonkey/2.9.1", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; U; Linux x86_64; us; rv:1.9.1.19) Gecko/20110430 shadowfox/7.0 (like Firefox/7.0", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; U; Linux i686; it; rv:1.9.2.3) Gecko/20100406 Firefox/3.6.3 (Swiftfox)", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Uzbl (Webkit 1.3) (Linux i686 [i686])", FLAGS_DESKTOP_LINUX);

    // Browsers - Unix
    // Console Browsers
    checkHttpClientInfoFlags("ELinks (0.4.3; NetBSD 3.0.2PATCH sparc64; 141x19)", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Links (2.1pre15; FreeBSD 5.3-RELEASE i386; 196x84)", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Lynx/2.8.7dev.4 libwww-FM/2.14 SSL-MM/1.4.1 OpenSSL/0.9.8d", FLAGS_NONE);
    checkHttpClientInfoFlags("w3m/0.5.1", FLAGS_NONE);
    // Legacy Browsers
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; U; FreeBSD i386; en-US) AppleWebKit/532.0 (KHTML, like Gecko) Chrome/4.0.207.0 Safari/532.0", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; U; OpenBSD i386; en-US) AppleWebKit/533.3 (KHTML, like Gecko) Chrome/5.0.359.0 Safari/533.3", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; U; FreeBSD x86_64; en-US) AppleWebKit/534.16 (KHTML, like Gecko) Chrome/10.0.648.204 Safari/534.16", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; U; SunOS sun4m; en-US; rv:1.4b) Gecko/20030517 Mozilla Firebird/0.6", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; U; SunOS i86pc; en-US; rv:1.9.1b3) Gecko/20090429 Firefox/3.1b3", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; U; OpenBSD i386; en-US; rv:1.9.1) Gecko/20090702 Firefox/3.5", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; U; FreeBSD i386; de-CH; rv:1.9.2.8) Gecko/20100729 Firefox/3.6.8", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; FreeBSD amd64; rv:5.0) Gecko/20100101 Firefox/5.0", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; U; FreeBSD i386; en-US; rv:1.6) Gecko/20040406 Galeon/1.3.15", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (compatible; Konqueror/3.5; NetBSD 4.0_RC3; X11) KHTML/3.5.7 (like Gecko)", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (compatible; Konqueror/3.5; SunOS) KHTML/3.5.1 (like Gecko)", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; U; FreeBSD; i386; en-US; rv:1.7) Gecko", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/4.77 [en] (X11; I; IRIX;64 6.5 IP30)", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/4.8 [en] (X11; U; SunOS; 5.7 sun4u)", FLAGS_DESKTOP_LINUX);
    // Current
    checkHttpClientInfoFlags("Mozilla/5.0 (Unknown; U; UNIX BSD/SYSV system; C -) AppleWebKit/527  (KHTML, like Gecko, Safari/419.3) Arora/0.10.2", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; FreeBSD amd64) AppleWebKit/536.5 (KHTML like Gecko) Chrome/19.0.1084.56 Safari/536.5", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; FreeBSD amd64) AppleWebKit/537.4 (KHTML like Gecko) Chrome/22.0.1229.79 Safari/537.4", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; NetBSD) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/27.0.1453.116 Safari/537.36", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; U; OpenBSD arm; en-us) AppleWebKit/531.2  (KHTML, like Gecko) Safari/531.2  Epiphany/2.30.0", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; NetBSD amd64; rv:16.0) Gecko/20121102 Firefox/16.0", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; OpenBSD amd64; rv:28.0) Gecko/20100101 Firefox/28.0", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (compatible; Konqueror/4.1; DragonFly) KHTML/4.1.4 (like Gecko)", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (compatible; Konqueror/4.1; OpenBSD) KHTML/4.1.4 (like Gecko)", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (compatible; Konqueror/4.5; NetBSD 5.0.2; X11; amd64; en_US) KHTML/4.5.4 (like Gecko)", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (compatible; Konqueror/4.5; FreeBSD) KHTML/4.5.4 (like Gecko)", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; U; NetBSD amd64; en-US; rv:1.9.2.15) Gecko/20110308 Namoroka/3.6.15", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("NetSurf/1.2 (NetBSD; amd64)", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Opera/9.80 (X11; FreeBSD 8.1-RELEASE i386; Edition Next) Presto/2.12.388 Version/12.10", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; U; SunOS i86pc; en-US; rv:1.8.1.12) Gecko/20080303 SeaMonkey/1.1.8", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; FreeBSD i386; rv:28.0) Gecko/20100101 Firefox/28.0 SeaMonkey/2.25", FLAGS_DESKTOP_LINUX);

    assertPassed();
  }

  @Test
  public void bulkTests_Mobile_Browsers() {
    // Mobile Devices
    // Browsers
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; BOLT/2.800) AppleWebKit/534.6 (KHTML, like Gecko) Version/5.0 Safari/534.6.3", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (Linux; Android 4.4.2; SAMSUNG-SM-T537A Build/KOT49H) AppleWebKit/537.36 (KHTML like Gecko) Chrome/35.0.1916.141 Safari/537.36", FLAGS_MOBILE_TABLET);
    checkHttpClientInfoFlags("Mozilla/5.0 (Android; Mobile; rv:35.0) Gecko/35.0 Firefox/35.0", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("Mozilla/4.0 (compatible; MSIE 6.0; Windows CE; IEMobile 6.12; Microsoft ZuneHD 4.3)", FLAGS_MOBILE_WINDOWS_PHONE);
    checkHttpClientInfoFlags("Mozilla/4.0 (compatible; MSIE 6.0; Windows CE; IEMobile 7.11) ", FLAGS_MOBILE_WINDOWS_PHONE);
    checkHttpClientInfoFlags("Mozilla/4.0 (compatible; MSIE 7.0; Windows Phone OS 7.0; Trident/3.1; IEMobile/7.0) Asus;Galaxy6", FLAGS_MOBILE_WINDOWS_PHONE);
    checkHttpClientInfoFlags("Mozilla/5.0 (compatible; MSIE 9.0; Windows Phone OS 7.5; Trident/5.0; IEMobile/9.0)", FLAGS_MOBILE_WINDOWS_PHONE);
    checkHttpClientInfoFlags("Mozilla/5.0 (compatible; MSIE 9.0; Windows Phone OS 7.5; Trident/5.0; IEMobile/9.0)", FLAGS_MOBILE_WINDOWS_PHONE);
    checkHttpClientInfoFlags("Mozilla/5.0 (compatible; MSIE 10.0; Windows Phone 8.0; Trident/6.0; IEMobile/10.0; ARM; Touch) ", FLAGS_MOBILE_WINDOWS_PHONE);
    checkHttpClientInfoFlags("Mozilla/1.22 (compatible; MSIE 5.01; PalmOS 3.0) EudoraWeb 2.1", FLAGS_NONE);
    checkHttpClientInfoFlags("Mozilla/5.0 (WindowsCE 6.0; rv:2.0.1) Gecko/20100101 Firefox/4.0.1", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; U; Linux armv61; en-US; rv:1.9.1b2pre) Gecko/20081015 Fennec/1.0a1", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (Maemo; Linux armv7l; rv:2.0.1) Gecko/20100101 Firefox/4.0.1 Fennec/2.0.1", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (Maemo; Linux armv7l; rv:10.0.1) Gecko/20100101 Firefox/10.0.1 Fennec/10.0.1", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (Windows; U; Windows CE 5.1; rv:1.8.1a3) Gecko/20060610 Minimo/0.016", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; U; Linux armv6l; rv 1.8.1.5pre) Gecko/20070619 Minimo/0.020", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; U; Linux arm7tdmi; rv:1.8.1.11) Gecko/20071130 Minimo/0.025", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/4.0 (PDA; PalmOS/sony/model prmr/Revision:1.1.54 (en)) NetFront/3.0", FLAGS_NONE);
    checkHttpClientInfoFlags("Opera/9.51 Beta (Microsoft Windows; PPC; Opera Mobi/1718; U; en)", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Opera/9.60 (J2ME/MIDP; Opera Mini/4.1.11320/608; U; en) Presto/2.2.0", FLAGS_NONE);
    checkHttpClientInfoFlags("Opera/9.60 (J2ME/MIDP; Opera Mini/4.2.14320/554; U; cs) Presto/2.2.0", FLAGS_NONE);
    checkHttpClientInfoFlags("Opera/9.80 (S60; SymbOS; Opera Mobi/499; U; ru) Presto/2.4.18 Version/10.00", FLAGS_NONE);
    checkHttpClientInfoFlags("Opera/10.61 (J2ME/MIDP; Opera Mini/5.1.21219/19.999; en-US; rv:1.9.3a5) WebKit/534.5 Presto/2.6.30", FLAGS_NONE);
    checkHttpClientInfoFlags("Opera/9.80 (Android; Opera Mini/7.5.33361/31.1543; U; en) Presto/2.8.119 Version/11.1010", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("POLARIS/6.01 (BREW 3.1.5; U; en-us; LG; LX265; POLARIS/6.01/WAP) MMP/2.0 profile/MIDP-2.1 Configuration/CLDC-1.1", FLAGS_NONE);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; U; Linux x86_64; en-gb) AppleWebKit/534.35 (KHTML, like Gecko) Chrome/11.0.696.65 Safari/534.35 Puffin/2.9174AP", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; U; Linux x86_64; en-us) AppleWebKit/534.35 (KHTML, like Gecko) Chrome/11.0.696.65 Safari/534.35 Puffin/2.9174AT", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (iPod; U; CPU iPhone OS 6_1 like Mac OS X; en-HK) AppleWebKit/534.35 (KHTML, like Gecko) Chrome/11.0.696.65 Safari/534.35 Puffin/3.9174IP Mobile ", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; U; Linux x86_64; en-AU) AppleWebKit/534.35 (KHTML, like Gecko) Chrome/11.0.696.65 Safari/534.35 Puffin/3.9174IT ", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; U; Linux i686; en-gb) AppleWebKit/534.35 (KHTML, like Gecko) Chrome/11.0.696.65 Safari/534.35 Puffin/2.0.5603M", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (Linux; U; Android 2.0; en-us; Droid Build/ESD20) AppleWebKit/530.17 (KHTML, like Gecko) Version/4.0 Mobile Safari/530.17", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("Mozilla/5.0 (iPad; U; CPU OS 4_2_1 like Mac OS X; ja-jp) AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8C148 Safari/6533.18.5", FLAGS_MOBILE_TABLET);
    checkHttpClientInfoFlags("Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_2_1 like Mac OS X; da-dk) AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8C148 Safari/6533.18.5", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("Mozilla/5.0 (iPad; CPU OS 6_0 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/6.0 Mobile/10A5355d Safari/8536.25", FLAGS_MOBILE_TABLET);
    checkHttpClientInfoFlags("Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0; XBLWP7; ZuneWP7) UCBrowser/2.9.0.263", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (Linux; U; Android 2.3.3; en-us ; LS670 Build/GRI40) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1/UCBrowser/8.6.1.262/145/355", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags(
        "Mozilla/5.0 (Windows Phone 10.0; Android <Android Version>; <Device Manufacturer>; <Device Model>) AppleWebKit/<WebKit Rev> (KHTML, like Gecko) Chrome/<Chrome Rev> Mobile Safari/<WebKit Rev> Edge/<EdgeHTML Rev>.<Windows Build>",
        FLAGS_MOBILE_WINDOWS_PHONE);

    assertPassed();
  }

  @Test
  public void bulkTests_Mobile_Devices() {
    // Devices > Tablets
    checkHttpClientInfoFlags("Mozilla/5.0 (Linux; U; Android 3.0.1; fr-fr; A500 Build/HRI66) AppleWebKit/534.13 (KHTML, like Gecko) Version/4.0 Safari/534.13", FLAGS_MOBILE_TABLET);
    checkHttpClientInfoFlags("Mozilla/5.0 (Linux; U; Android 4.1; en-us; sdk Build/MR1) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.1 Safari/534.30", FLAGS_MOBILE_TABLET);
    checkHttpClientInfoFlags("Mozilla/5.0 (Linux; U; Android 4.2; en-us; sdk Build/MR1) AppleWebKit/535.19 (KHTML, like Gecko) Version/4.2 Safari/535.19", FLAGS_MOBILE_TABLET);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; U; Linux x86_64; en-us) AppleWebKit/534.35 (KHTML, like Gecko) Chrome/11.0.696.65 Safari/534.35 Puffin/2.9174AT", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; U; Linux x86_64; en-AU) AppleWebKit/534.35 (KHTML, like Gecko) Chrome/11.0.696.65 Safari/534.35 Puffin/3.9174IT", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (iPad; U; CPU OS 3_2 like Mac OS X; en-us) AppleWebKit/531.21.10 (KHTML, like Gecko) Version/4.0.4 Mobile/7B334b Safari/531.21.10", FLAGS_MOBILE_TABLET);
    checkHttpClientInfoFlags("Mozilla/5.0 (iPad; U; CPU OS 4_2_1 like Mac OS X; ja-jp) AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8C148 Safari/6533.18.5", FLAGS_MOBILE_TABLET);
    checkHttpClientInfoFlags("Mozilla/5.0 (iPad; U; CPU OS 4_3 like Mac OS X; en-us) AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8F190 Safari/6533.18.5", FLAGS_MOBILE_TABLET);
    checkHttpClientInfoFlags("Mozilla/5.0 (iPad; CPU OS 6_0 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/6.0 Mobile/10A5355d Safari/8536.25", FLAGS_MOBILE_TABLET);
    checkHttpClientInfoFlags("Mozilla/5.0 (iPad; CPU OS 8_0_2 like Mac OS X) AppleWebKit/600.1.4 (KHTML like Gecko) Mobile/12A405 Version/7.0 Safari/9537.53", FLAGS_MOBILE_TABLET);
    checkHttpClientInfoFlags("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_5_7;en-us) AppleWebKit/530.17 (KHTML, like Gecko) Version/4.0 Safari/530.17", FLAGS_DESKTOP_MAC);
    checkHttpClientInfoFlags("Mozilla/5.0 (hp-tablet; Linux; hpwOS/3.0.2; U; de-DE) AppleWebKit/534.6 (KHTML, like Gecko) wOSBrowser/234.40.1 Safari/534.6 TouchPad/1.0", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (Linux; U; Android 4.0.3; en-us; KFTT Build/IML74K) AppleWebKit/535.19 (KHTML, like Gecko) Silk/2.1 Mobile Safari/535.19 Silk-Accelerated=true", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("Mozilla/5.0 (Linux; U; Android 3.0; en-us; Xoom Build/HRI39) AppleWebKit/525.10  (KHTML, like Gecko) Version/3.0.4 Mobile Safari/523.12.2", FLAGS_MOBILE_TABLET);
    checkHttpClientInfoFlags("Mozilla/5.0 (PlayBook; U; RIM Tablet OS 2.1.0; en-US) AppleWebKit/536.2+ (KHTML like Gecko) Version/7.2.1.0 Safari/536.2+", FLAGS_NONE);
    checkHttpClientInfoFlags("Mozilla/5.0 (Linux; U; Android 1.5; de-de; Galaxy Build/CUPCAKE) AppleWebKit/528.5  (KHTML, like Gecko) Version/3.1.2 Mobile Safari/525.20.1", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("Mozilla/5.0 (Linux; U; Android 2.2; en-ca; GT-P1000M Build/FROYO) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("Mozilla/5.0 (Linux; U; Android 2.2; en-us; SCH-I800 Build/FROYO) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("Mozilla/5.0 (Linux; U; Android 3.0.1; en-us; GT-P7100 Build/HRI83) AppleWebkit/534.13 (KHTML, like Gecko) Version/4.0 Safari/534.13", FLAGS_MOBILE_TABLET);
    // Devices > Amazon (Kindle)
    checkHttpClientInfoFlags("Mozilla/4.0 (compatible; Linux 2.6.22) NetFront/3.4 Kindle/2.0 (screen 600x800)", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (Linux U; en-US)  AppleWebKit/528.5  (KHTML, like Gecko, Safari/528.5 ) Version/4.0 Kindle/3.0 (screen 600x800; rotate)", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (Linux; U; Android 4.0.3; en-us; KFTT Build/IML74K) AppleWebKit/535.19 (KHTML, like Gecko) Silk/2.1 Mobile Safari/535.19 Silk-Accelerated=true", FLAGS_MOBILE_MOBILE);
    // Devices > Acer
    checkHttpClientInfoFlags("Mozilla/5.0 (Linux; U; Android 3.0.1; fr-fr; A500 Build/HRI66) AppleWebKit/534.13 (KHTML, like Gecko) Version/4.0 Safari/534.13", FLAGS_MOBILE_TABLET);
    // Devices > Apple (iPhone etc)
    checkHttpClientInfoFlags("Mozilla/5.0 (iPad; U; CPU OS 3_2 like Mac OS X; en-us) AppleWebKit/531.21.10 (KHTML, like Gecko) Version/4.0.4 Mobile/7B334b Safari/531.21.10", FLAGS_MOBILE_TABLET);
    checkHttpClientInfoFlags("Mozilla/5.0 (iPad; U; CPU OS 4_2_1 like Mac OS X; ja-jp) AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8C148 Safari/6533.18.5", FLAGS_MOBILE_TABLET);
    checkHttpClientInfoFlags("Mozilla/5.0 (iPad; U; CPU OS 4_3 like Mac OS X; en-us) AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8F190 Safari/6533.18.5", FLAGS_MOBILE_TABLET);
    checkHttpClientInfoFlags("Mozilla/5.0 (iPad; U; CPU iPad OS 5_0_1 like Mac OS X; en-us) AppleWebKit/535.1+ (KHTML like Gecko) Version/7.2.0.0 Safari/6533.18.5", FLAGS_MOBILE_TABLET);
    checkHttpClientInfoFlags("Mozilla/5.0 (iPad; CPU OS 6_0 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/6.0 Mobile/10A5355d Safari/8536.25", FLAGS_MOBILE_TABLET);
    checkHttpClientInfoFlags("Mozilla/5.0 (iPad; CPU OS 7_0 like Mac OS X) AppleWebKit/537.51.1 (KHTML, like Gecko) CriOS/30.0.1599.12 Mobile/11A465 Safari/8536.25 (3B92C18B-D9DE-4CB7-A02A-22FD2AF17C8F)", FLAGS_MOBILE_TABLET);
    checkHttpClientInfoFlags("Mozilla/5.0 (iPad; CPU OS 8_0_2 like Mac OS X) AppleWebKit/600.1.4 (KHTML like Gecko) Mobile/12A405 Version/7.0 Safari/9537.53", FLAGS_MOBILE_TABLET);
    checkHttpClientInfoFlags("Mozilla/5.0 (iPhone; U; CPU like Mac OS X; en) AppleWebKit/420  (KHTML, like Gecko) Version/3.0 Mobile/1A543a Safari/419.3", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("Mozilla/5.0 (iPhone; U; CPU iPhone OS 2_0 like Mac OS X; en-us) AppleWebKit/525.18.1 (KHTML, like Gecko) Version/3.1.1 Mobile/5A347 Safari/525.200", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("Mozilla/5.0 (iPhone; U; CPU iPhone OS 3_0 like Mac OS X; en-us) AppleWebKit/528.18 (KHTML, like Gecko) Version/4.0 Mobile/7A341 Safari/528.16", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_0 like Mac OS X; en-us) AppleWebKit/532.9 (KHTML, like Gecko) Version/4.0.5 Mobile/8A293 Safari/531.22.7", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_2_1 like Mac OS X; da-dk) AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8C148 Safari/6533.18.5", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("Mozilla/5.0 (iPhone; U; CPU iPhone OS 5_1_1 like Mac OS X; da-dk) AppleWebKit/534.46.0 (KHTML, like Gecko) CriOS/19.0.1084.60 Mobile/9B206 Safari/7534.48.3", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("UCWEB/8.8 (iPhone; CPU OS_6; en-US)AppleWebKit/534.1 U3/3.0.0 Mobile", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("Mozilla/5.0 (iPhone; CPU iPhone OS 7_1_2 like Mac OS X) AppleWebKit/537.51.2 (KHTML like Gecko) Version/7.0 Mobile/11D257 Safari/9537.53", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("Mozilla/5.0 (iPod; U; CPU iPhone OS 2_2_1 like Mac OS X; en-us) AppleWebKit/525.18.1 (KHTML, like Gecko) Version/3.1.1 Mobile/5H11a Safari/525.20", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("Mozilla/5.0 (iPod; U; CPU iPhone OS 3_1_1 like Mac OS X; en-us) AppleWebKit/528.18 (KHTML, like Gecko) Mobile/7C145", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("Mozilla/5.0 (iPod touch; CPU iPhone OS 7_1 like Mac OS X) AppleWebKit/537.51.2 (KHTML like Gecko) Version/7.0 Mobile/11D167 Safari/123E71C", FLAGS_MOBILE_MOBILE);
    // Devices > Barnes and Noble
    checkHttpClientInfoFlags("nook browser/1.0", FLAGS_NONE);
    checkHttpClientInfoFlags("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_5_7;en-us) AppleWebKit/530.17 (KHTML, like Gecko) Version/4.0 Safari/530.17", FLAGS_DESKTOP_MAC);
    checkHttpClientInfoFlags("Mozilla/5.0 (Linux; U; Android 2.3.4; en-us; BNTV250 Build/GINGERBREAD) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Safari/533.1", FLAGS_MOBILE_MOBILE);
    // Devices > Blackberry (RIM)
    checkHttpClientInfoFlags("BlackBerry7100i/4.1.0 Profile/MIDP-2.0 Configuration/CLDC-1.1 VendorID/103", FLAGS_NONE);
    checkHttpClientInfoFlags("BlackBerry8300/4.2.2 Profile/MIDP-2.0 Configuration/CLDC-1.1 VendorID/107 UP.Link/6.2.3.15.0", FLAGS_NONE);
    checkHttpClientInfoFlags("BlackBerry8320/4.2.2 Profile/MIDP-2.0 Configuration/CLDC-1.1 VendorID/100", FLAGS_NONE);
    checkHttpClientInfoFlags("BlackBerry8330/4.3.0 Profile/MIDP-2.0 Configuration/CLDC-1.1 VendorID/105", FLAGS_NONE);
    checkHttpClientInfoFlags("BlackBerry9000/4.6.0.167 Profile/MIDP-2.0 Configuration/CLDC-1.1 VendorID/102", FLAGS_NONE);
    checkHttpClientInfoFlags("BlackBerry9530/4.7.0.167 Profile/MIDP-2.0 Configuration/CLDC-1.1 VendorID/102 UP.Link/6.3.1.20.0", FLAGS_NONE);
    checkHttpClientInfoFlags("BlackBerry9700/5.0.0.351 Profile/MIDP-2.1 Configuration/CLDC-1.1 VendorID/123", FLAGS_NONE);
    checkHttpClientInfoFlags("Mozilla/5.0 (BlackBerry; U; BlackBerry 9800; en) AppleWebKit/534.1  (KHTML, Like Gecko) Version/6.0.0.141 Mobile Safari/534.1", FLAGS_NONE);
    checkHttpClientInfoFlags("Mozilla/5.0 (PlayBook; U; RIM Tablet OS 2.1.0; en-US) AppleWebKit/536.2+ (KHTML like Gecko) Version/7.2.1.0 Safari/536.2+", FLAGS_NONE);
    checkHttpClientInfoFlags("Mozilla/5.0 (BB10; Touch) AppleWebKit/537.10+ (KHTML, like Gecko) Version/10.1.0.2342 Mobile Safari/537.10+", FLAGS_NONE);
    // Devices > Google (Nexus etc.)
    checkHttpClientInfoFlags("Mozilla/5.0 (Linux; U; Android 1.5; en-us; sdk Build/CUPCAKE) AppleWebkit/528.5  (KHTML, like Gecko) Version/3.1.2 Mobile Safari/525.20.1", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("Mozilla/5.0 (Linux; U; Android 2.1; en-us; Nexus One Build/ERD62) AppleWebKit/530.17 (KHTML, like Gecko) Version/4.0 Mobile Safari/530.17", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("Mozilla/5.0 (Linux; U; Android 2.2; en-us; Nexus One Build/FRF91) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("Mozilla/5.0 (Linux; Android 4.4; Nexus 5 Build/BuildID) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/30.0.0.0 Mobile Safari/537.36", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("Mozilla/5.0 (Linux; Android 4.4.4; Nexus 7 Build/KTU84P) AppleWebKit/537.36 (KHTML like Gecko) Chrome/36.0.1985.135 Safari/537.36", FLAGS_MOBILE_TABLET);
    // Devices > HP
    checkHttpClientInfoFlags("Mozilla/5.0 (hp-tablet; Linux; hpwOS/3.0.2; U; de-DE) AppleWebKit/534.6 (KHTML, like Gecko) wOSBrowser/234.40.1 Safari/534.6 TouchPad/1.0", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/5.0 (Linux; webOS/2.2.4; U; en-US) AppleWebKit/534.6 (KHTML, like Gecko) webOSBrowser/221.56 Safari/534.6 Pre/3.0 ", FLAGS_DESKTOP_LINUX);
    // Devices > HTC
    checkHttpClientInfoFlags("Mozilla/4.0 (compatible; MSIE 6.0; Windows CE; IEMobile 7.11) Sprint:PPC6800 ", FLAGS_MOBILE_WINDOWS_PHONE);
    checkHttpClientInfoFlags("Mozilla/4.0 (compatible; MSIE 6.0; Windows CE; IEMobile 7.11) XV6800 ", FLAGS_MOBILE_WINDOWS_PHONE);
    checkHttpClientInfoFlags("Mozilla/5.0 (Linux; U; Android 1.5; en-us; htc_bahamas Build/CRB17) AppleWebKit/528.5  (KHTML, like Gecko) Version/3.1.2 Mobile Safari/525.20.1", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("Mozilla/5.0 (Linux; U; Android 2.1-update1; de-de; HTC Desire 1.19.161.5 Build/ERE27) AppleWebKit/530.17 (KHTML, like Gecko) Version/4.0 Mobile Safari/530.17", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("HTC_Dream Mozilla/5.0 (Linux; U; Android 1.5; en-ca; Build/CUPCAKE) AppleWebKit/528.5  (KHTML, like Gecko) Version/3.1.2 Mobile Safari/525.20.1", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("Mozilla/5.0 (Linux; U; Android 2.2; en-us; Sprint APA9292KT Build/FRF91) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("Mozilla/5.0 (Linux; U; Android 1.5; de-ch; HTC Hero Build/CUPCAKE) AppleWebKit/528.5  (KHTML, like Gecko) Version/3.1.2 Mobile Safari/525.20.1", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("Mozilla/5.0 (Linux; U; Android 2.2; en-us; ADR6300 Build/FRF91) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("Mozilla/5.0 (Linux; U; Android 2.1; en-us; HTC Legend Build/cupcake) AppleWebKit/530.17 (KHTML, like Gecko) Version/4.0 Mobile Safari/530.17", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("Mozilla/5.0 (Linux; U; Android 1.5; de-de; HTC Magic Build/PLAT-RC33) AppleWebKit/528.5  (KHTML, like Gecko) Version/3.1.2 Mobile Safari/525.20.1 FirePHP/0.3", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("Mozilla/5.0 (Linux; U; Android 4.0.3; de-ch; HTC Sensation Build/IML74K) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("HTC-ST7377/1.59.502.3 (67150) Opera/9.50 (Windows NT 5.1; U; en) UP.Link/6.3.1.17.0", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (Linux; U; Android 1.6; en-us; HTC_TATTOO_A3288 Build/DRC79) AppleWebKit/528.5  (KHTML, like Gecko) Version/3.1.2 Mobile Safari/525.20.1", FLAGS_MOBILE_MOBILE);
    // Devices > LG
    checkHttpClientInfoFlags("LG-LX550 AU-MIC-LX550/2.0 MMP/2.0 Profile/MIDP-2.0 Configuration/CLDC-1.1", FLAGS_NONE);
    checkHttpClientInfoFlags("POLARIS/6.01(BREW 3.1.5;U;en-us;LG;LX265;POLARIS/6.01/WAP;)MMP/2.0 profile/MIDP-201 Configuration /CLDC-1.1", FLAGS_NONE);
    checkHttpClientInfoFlags("LG-GC900/V10a Obigo/WAP2.0 Profile/MIDP-2.1 Configuration/CLDC-1.1", FLAGS_NONE);
    // Devices > MDA - T-Mobile
    checkHttpClientInfoFlags("Mozilla/4.0 (compatible; MSIE 4.01; Windows CE; PPC; MDA Pro/1.0 Profile/MIDP-2.0 Configuration/CLDC-1.1)", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (Linux; U; Android 1.0; en-us; dream) AppleWebKit/525.10  (KHTML, like Gecko) Version/3.0.4 Mobile Safari/523.12.2", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("Mozilla/5.0 (Linux; U; Android 1.5; en-us; T-Mobile G1 Build/CRB43) AppleWebKit/528.5  (KHTML, like Gecko) Version/3.1.2 Mobile Safari 525.20.1", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("Mozilla/5.0 (Linux; U; Android 1.5; en-gb; T-Mobile_G2_Touch Build/CUPCAKE) AppleWebKit/528.5  (KHTML, like Gecko) Version/3.1.2 Mobile Safari/525.20.1", FLAGS_MOBILE_MOBILE);
    // Devices > Motorola
    checkHttpClientInfoFlags("Mozilla/5.0 (Linux; U; Android 2.0; en-us; Droid Build/ESD20) AppleWebKit/530.17 (KHTML, like Gecko) Version/4.0 Mobile Safari/530.17", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("Mozilla/5.0 (Linux; U; Android 2.2; en-us; Droid Build/FRG22D) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("MOT-L7v/08.B7.5DR MIB/2.2.1 Profile/MIDP-2.0 Configuration/CLDC-1.1 UP.Link/6.3.0.0.0", FLAGS_NONE);
    checkHttpClientInfoFlags("Mozilla/5.0 (Linux; U; Android 2.0; en-us; Milestone Build/ SHOLS_U2_01.03.1) AppleWebKit/530.17 (KHTML, like Gecko) Version/4.0 Mobile Safari/530.17", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("Mozilla/5.0 (Linux; U; Android 2.0.1; de-de; Milestone Build/SHOLS_U2_01.14.0) AppleWebKit/530.17 (KHTML, like Gecko) Version/4.0 Mobile Safari/530.17", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("MOT-V9mm/00.62 UP.Browser/6.2.3.4.c.1.123 (GUI) MMP/2.0", FLAGS_NONE);
    checkHttpClientInfoFlags("MOTORIZR-Z8/46.00.00 Mozilla/4.0 (compatible; MSIE 6.0; Symbian OS; 356) Opera 8.65 [it] UP.Link/6.3.0.0.0", FLAGS_NONE);
    checkHttpClientInfoFlags("MOT-V177/0.1.75 UP.Browser/6.2.3.9.c.12 (GUI) MMP/2.0 UP.Link/6.3.1.13.0", FLAGS_NONE);
    checkHttpClientInfoFlags("Mozilla/5.0 (Linux; U; Android 3.0; en-us; Xoom Build/HRI39) AppleWebKit/525.10  (KHTML, like Gecko) Version/3.0.4 Mobile Safari/523.12.2", FLAGS_MOBILE_TABLET);
    // Devices > Nec
    checkHttpClientInfoFlags("portalmmm/2.0 N410i(c20;TB) ", FLAGS_NONE);
    // Devices > Nokia
    checkHttpClientInfoFlags("Nokia3230/2.0 (5.0614.0) SymbianOS/7.0s Series60/2.1 Profile/MIDP-2.0 Configuration/CLDC-1.0", FLAGS_NONE);
    checkHttpClientInfoFlags("Mozilla/5.0 (SymbianOS/9.2; U; Series60/3.1 Nokia5700/3.27; Profile/MIDP-2.0 Configuration/CLDC-1.1) AppleWebKit/413 (KHTML, like Gecko) Safari/413", FLAGS_NONE);
    checkHttpClientInfoFlags("Mozilla/5.0 (SymbianOS/9.2; U; Series60/3.1 Nokia6120c/3.70; Profile/MIDP-2.0 Configuration/CLDC-1.1) AppleWebKit/413 (KHTML, like Gecko) Safari/413", FLAGS_NONE);
    checkHttpClientInfoFlags("Nokia6230/2.0 (04.44) Profile/MIDP-2.0 Configuration/CLDC-1.1", FLAGS_NONE);
    checkHttpClientInfoFlags("Nokia6230i/2.0 (03.80) Profile/MIDP-2.0 Configuration/CLDC-1.1", FLAGS_NONE);
    checkHttpClientInfoFlags("Mozilla/4.1 (compatible; MSIE 5.0; Symbian OS; Nokia 6600;452) Opera 6.20 [en-US]", FLAGS_NONE);
    checkHttpClientInfoFlags("Nokia6630/1.0 (2.39.15) SymbianOS/8.0 Series60/2.6 Profile/MIDP-2.0 Configuration/CLDC-1.1", FLAGS_NONE);
    checkHttpClientInfoFlags("Nokia7250/1.0 (3.14) Profile/MIDP-1.0 Configuration/CLDC-1.0", FLAGS_NONE);
    checkHttpClientInfoFlags("Mozilla/4.0 (compatible; MSIE 5.0; Series80/2.0 Nokia9500/4.51 Profile/MIDP-2.0 Configuration/CLDC-1.1)", FLAGS_NONE);
    checkHttpClientInfoFlags("Mozilla/5.0 (Symbian/3; Series60/5.2 NokiaC6-01/011.010; Profile/MIDP-2.1 Configuration/CLDC-1.1 ) AppleWebKit/525 (KHTML, like Gecko) Version/3.0 BrowserNG/7.2.7.2 3gpp-gba", FLAGS_NONE);
    checkHttpClientInfoFlags("Mozilla/5.0 (Symbian/3; Series60/5.2 NokiaC7-00/012.003; Profile/MIDP-2.1 Configuration/CLDC-1.1 ) AppleWebKit/525 (KHTML, like Gecko) Version/3.0 BrowserNG/7.2.7.3 3gpp-gba", FLAGS_NONE);
    checkHttpClientInfoFlags("Mozilla/5.0 (SymbianOS/9.1; U; en-us) AppleWebKit/413 (KHTML, like Gecko) Safari/413 es50", FLAGS_NONE);
    checkHttpClientInfoFlags("Mozilla/5.0 (Symbian/3; Series60/5.2 NokiaE6-00/021.002; Profile/MIDP-2.1 Configuration/CLDC-1.1) AppleWebKit/533.4 (KHTML, like Gecko) NokiaBrowser/7.3.1.16 Mobile Safari/533.4 3gpp-gba", FLAGS_NONE);
    checkHttpClientInfoFlags("UCWEB/8.8 (SymbianOS/9.2; U; en-US; NokiaE63) AppleWebKit/534.1 UCBrowser/8.8.0.245 Mobile", FLAGS_NONE);
    checkHttpClientInfoFlags("Mozilla/5.0 (SymbianOS/9.1; U; en-us) AppleWebKit/413 (KHTML, like Gecko) Safari/413 es65", FLAGS_NONE);
    checkHttpClientInfoFlags("Mozilla/5.0 (Symbian/3; Series60/5.2 NokiaE7-00/010.016; Profile/MIDP-2.1 Configuration/CLDC-1.1 ) AppleWebKit/525 (KHTML, like Gecko) Version/3.0 BrowserNG/7.2.7.3 3gpp-gba", FLAGS_NONE);
    checkHttpClientInfoFlags("Mozilla/5.0 (SymbianOS/9.1; U; en-us) AppleWebKit/413 (KHTML, like Gecko) Safari/413 es70", FLAGS_NONE);
    checkHttpClientInfoFlags("Mozilla/5.0 (SymbianOS/9.2; U; Series60/3.1 NokiaE90-1/07.24.0.3; Profile/MIDP-2.0 Configuration/CLDC-1.1 ) AppleWebKit/413 (KHTML, like Gecko) Safari/413 UP.Link/6.2.3.18.0", FLAGS_NONE);
    checkHttpClientInfoFlags("Mozilla/5.0 (compatible; MSIE 10.0; Windows Phone 8.0; Trident/6.0; IEMobile/10.0; ARM; Touch; NOKIA; Lumia 920)", FLAGS_MOBILE_WINDOWS_PHONE);
    checkHttpClientInfoFlags("NokiaN70-1/5.0609.2.0.1 Series60/2.8 Profile/MIDP-2.0 Configuration/CLDC-1.1 UP.Link/6.3.1.13.0", FLAGS_NONE);
    checkHttpClientInfoFlags("Mozilla/5.0 (SymbianOS/9.1; U; en-us) AppleWebKit/413 (KHTML, like Gecko) Safari/413", FLAGS_NONE);
    checkHttpClientInfoFlags("NokiaN73-1/3.0649.0.0.1 Series60/3.0 Profile/MIDP2.0 Configuration/CLDC-1.1", FLAGS_NONE);
    checkHttpClientInfoFlags("Mozilla/5.0 (Symbian/3; Series60/5.2 NokiaN8-00/014.002; Profile/MIDP-2.1 Configuration/CLDC-1.1; en-us) AppleWebKit/525 (KHTML, like Gecko) Version/3.0 BrowserNG/7.2.6.4 3gpp-gba", FLAGS_NONE);
    checkHttpClientInfoFlags("Mozilla/5.0 (SymbianOS/9.1; U; en-us) AppleWebKit/413 (KHTML, like Gecko) Safari/413", FLAGS_NONE);
    checkHttpClientInfoFlags("Mozilla/5.0 (MeeGo; NokiaN9) AppleWebKit/534.13 (KHTML, like Gecko) NokiaBrowser/8.5.0 Mobile Safari/534.13", FLAGS_NONE);
    checkHttpClientInfoFlags("Mozilla/5.0 (SymbianOS/9.1; U; de) AppleWebKit/413 (KHTML, like Gecko) Safari/413", FLAGS_NONE);
    checkHttpClientInfoFlags("Mozilla/5.0 (SymbianOS/9.2; U; Series60/3.1 NokiaN95/10.0.018; Profile/MIDP-2.0 Configuration/CLDC-1.1) AppleWebKit/413 (KHTML, like Gecko) Safari/413 UP.Link/6.3.0.0.0", FLAGS_NONE);
    checkHttpClientInfoFlags("Mozilla/5.0 (MeeGo; NokiaN950-00/00) AppleWebKit/534.13 (KHTML, like Gecko) NokiaBrowser/8.5.0 Mobile Safari/534.13", FLAGS_NONE);
    checkHttpClientInfoFlags("Mozilla/5.0 (SymbianOS/9.4; Series60/5.0 NokiaN97-1/10.0.012; Profile/MIDP-2.1 Configuration/CLDC-1.1; en-us) AppleWebKit/525 (KHTML, like Gecko) WicKed/7.1.12344", FLAGS_NONE);
    checkHttpClientInfoFlags("Mozilla/5.0 (Symbian/3; Series60/5.2 NokiaX7-00/021.004; Profile/MIDP-2.1 Configuration/CLDC-1.1 ) AppleWebKit/533.4 (KHTML, like Gecko) NokiaBrowser/7.3.1.21 Mobile Safari/533.4 3gpp-gba", FLAGS_NONE);
    // Devices > Palm
    checkHttpClientInfoFlags("Mozilla/5.0 (webOS/1.3; U; en-US) AppleWebKit/525.27.1 (KHTML, like Gecko) Version/1.0 Safari/525.27.1 Desktop/1.0", FLAGS_NONE);
    checkHttpClientInfoFlags("Mozilla/4.0 (compatible; MSIE 6.0; Windows 98; PalmSource/hspr-H102; Blazer/4.0) 16;320x320", FLAGS_DESKTOP_WINDOWS);
    // Devices > Samsung
    checkHttpClientInfoFlags("SEC-SGHE900/1.0 NetFront/3.2 Profile/MIDP-2.0 Configuration/CLDC-1.1 Opera/8.01 (J2ME/MIDP; Opera Mini/2.0.4509/1378; nl; U; ssr)", FLAGS_NONE);
    checkHttpClientInfoFlags("Mozilla/5.0 (Linux; U; Android 1.5; de-de; Galaxy Build/CUPCAKE) AppleWebKit/528.5  (KHTML, like Gecko) Version/3.1.2 Mobile Safari/525.20.1", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("Mozilla/5.0 (Linux; U; Android 2.2; en-ca; GT-P1000M Build/FROYO) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("Mozilla/5.0 (Linux; U; Android 2.2; en-us; SCH-I800 Build/FROYO) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("Mozilla/5.0 (Linux; U; Android 4.0.3; de-de; Galaxy S II Build/GRJ22) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("User agent: Mozilla/5.0 (Linux; Android 4.3; SPH-L710 Build/JSS15J) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1700.99 Mobile Safari/537.36", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("Mozilla/5.0 (Linux; U; Android 3.0.1; en-us; GT-P7100 Build/HRI83) AppleWebkit/534.13 (KHTML, like Gecko) Version/4.0 Safari/534.13", FLAGS_MOBILE_TABLET);
    checkHttpClientInfoFlags("SAMSUNG-S8000/S8000XXIF3 SHP/VPP/R5 Jasmine/1.0 Nextreaming SMM-MMS/1.2.0 profile/MIDP-2.1 configuration/CLDC-1.1 FirePHP/0.3", FLAGS_NONE);
    checkHttpClientInfoFlags("Mozilla/5.0 (Linux; U; Android 1.5; en-us; SPH-M900 Build/CUPCAKE) AppleWebKit/528.5  (KHTML, like Gecko) Version/3.1.2 Mobile Safari/525.20.1", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("SAMSUNG-SGH-A867/A867UCHJ3 SHP/VPP/R5 NetFront/35 SMM-MMS/1.2.0 profile/MIDP-2.0 configuration/CLDC-1.1 UP.Link/6.3.0.0.0", FLAGS_NONE);
    checkHttpClientInfoFlags("SEC-SGHX210/1.0 UP.Link/6.3.1.13.0", FLAGS_NONE);
    checkHttpClientInfoFlags("Mozilla/5.0 (Linux; Android 4.4.2; SAMSUNG-SM-T537A Build/KOT49H) AppleWebKit/537.36 (KHTML like Gecko) Chrome/35.0.1916.141 Safari/537.36", FLAGS_MOBILE_TABLET);
    checkHttpClientInfoFlags("Mozilla/5.0 (Linux; U; Android 1.5; fr-fr; GT-I5700 Build/CUPCAKE) AppleWebKit/528.5  (KHTML, like Gecko) Version/3.1.2 Mobile Safari/525.20.1", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("SEC-SGHX820/1.0 NetFront/3.2 Profile/MIDP-2.0 Configuration/CLDC-1.1", FLAGS_NONE);
    // Devices > SonyEricson
    checkHttpClientInfoFlags("SonyEricssonK310iv/R4DA Browser/NetFront/3.3 Profile/MIDP-2.0 Configuration/CLDC-1.1 UP.Link/6.3.1.13.0", FLAGS_NONE);
    checkHttpClientInfoFlags("SonyEricssonK550i/R1JD Browser/NetFront/3.3 Profile/MIDP-2.0 Configuration/CLDC-1.1", FLAGS_NONE);
    checkHttpClientInfoFlags("SonyEricssonK610i/R1CB Browser/NetFront/3.3 Profile/MIDP-2.0 Configuration/CLDC-1.1", FLAGS_NONE);
    checkHttpClientInfoFlags("SonyEricssonK750i/R1CA Browser/SEMC-Browser/4.2 Profile/MIDP-2.0 Configuration/CLDC-1.1", FLAGS_NONE);
    checkHttpClientInfoFlags("Opera/9.80 (J2ME/MIDP; Opera Mini/5.0.16823/1428; U; en) Presto/2.2.0", FLAGS_NONE);
    checkHttpClientInfoFlags("SonyEricssonK800i/R1CB Browser/NetFront/3.3 Profile/MIDP-2.0 Configuration/CLDC-1.1 UP.Link/6.3.0.0.0", FLAGS_NONE);
    checkHttpClientInfoFlags("SonyEricssonK810i/R1KG Browser/NetFront/3.3 Profile/MIDP-2.0 Configuration/CLDC-1.1", FLAGS_NONE);
    checkHttpClientInfoFlags("Opera/8.01 (J2ME/MIDP; Opera Mini/1.0.1479/HiFi; SonyEricsson P900; no; U; ssr)", FLAGS_NONE);
    checkHttpClientInfoFlags("SonyEricssonS500i/R6BC Browser/NetFront/3.3 Profile/MIDP-2.0 Configuration/CLDC-1.1", FLAGS_NONE);
    checkHttpClientInfoFlags("Mozilla/5.0 (SymbianOS/9.4; U; Series60/5.0 SonyEricssonP100/01; Profile/MIDP-2.1 Configuration/CLDC-1.1) AppleWebKit/525 (KHTML, like Gecko) Version/3.0 Safari/525", FLAGS_NONE);
    checkHttpClientInfoFlags("SonyEricssonT68/R201A", FLAGS_NONE);
    checkHttpClientInfoFlags("SonyEricssonT100/R101", FLAGS_NONE);
    checkHttpClientInfoFlags("SonyEricssonT610/R201 Profile/MIDP-1.0 Configuration/CLDC-1.0", FLAGS_NONE);
    checkHttpClientInfoFlags("SonyEricssonT650i/R7AA Browser/NetFront/3.3 Profile/MIDP-2.0 Configuration/CLDC-1.1", FLAGS_NONE);
    checkHttpClientInfoFlags("SonyEricssonW580i/R6BC Browser/NetFront/3.3 Profile/MIDP-2.0 Configuration/CLDC-1.1", FLAGS_NONE);
    checkHttpClientInfoFlags("SonyEricssonW660i/R6AD Browser/NetFront/3.3 Profile/MIDP-2.0 Configuration/CLDC-1.1", FLAGS_NONE);
    checkHttpClientInfoFlags("SonyEricssonW810i/R4EA Browser/NetFront/3.3 Profile/MIDP-2.0 Configuration/CLDC-1.1 UP.Link/6.3.0.0.0", FLAGS_NONE);
    checkHttpClientInfoFlags("SonyEricssonW850i/R1ED Browser/NetFront/3.3 Profile/MIDP-2.0 Configuration/CLDC-1.1", FLAGS_NONE);
    checkHttpClientInfoFlags("SonyEricssonW950i/R100 Mozilla/4.0 (compatible; MSIE 6.0; Symbian OS; 323) Opera 8.60 [en-US]", FLAGS_NONE);
    checkHttpClientInfoFlags("SonyEricssonW995/R1EA Profile/MIDP-2.1 Configuration/CLDC-1.1 UNTRUSTED/1.0", FLAGS_NONE);
    checkHttpClientInfoFlags("Mozilla/5.0 (Linux; U; Android 1.6; es-es; SonyEricssonX10i Build/R1FA016) AppleWebKit/528.5  (KHTML, like Gecko) Version/3.1.2 Mobile Safari/525.20.1", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("Mozilla/5.0 (Linux; U; Android 1.6; en-us; SonyEricssonX10i Build/R1AA056) AppleWebKit/528.5  (KHTML, like Gecko) Version/3.1.2 Mobile Safari/525.20.1", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("Opera/9.5 (Microsoft Windows; PPC; Opera Mobi; U) SonyEricssonX1i/R2AA Profile/MIDP-2.0 Configuration/CLDC-1.1", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("SonyEricssonZ800/R1Y Browser/SEMC-Browser/4.1 Profile/MIDP-2.0 Configuration/CLDC-1.1 UP.Link/6.3.0.0.0", FLAGS_NONE);
    // Devices > Zune (Microsoft)
    checkHttpClientInfoFlags("Mozilla/4.0 (compatible; MSIE 6.0; Windows CE; IEMobile 6.12; Microsoft ZuneHD 4.3)", FLAGS_MOBILE_WINDOWS_PHONE);

    assertPassed();
  }

  @Test
  public void bulkTests_Mobile_OS() {
    // OS > Android
    checkHttpClientInfoFlags("Opera/9.80 (Android; Opera Mini/7.5.33361/31.1543; U; en) Presto/2.8.119 Version/11.1010", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("Mozilla/5.0 (Android; Mobile; rv:35.0) Gecko/35.0 Firefox/35.0", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("Mozilla/5.0 (Linux; U; Android 0.5; en-us) AppleWebKit/522  (KHTML, like Gecko) Safari/419.3", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("Mozilla/5.0 (Linux; U; Android 1.1; en-gb; dream) AppleWebKit/525.10  (KHTML, like Gecko) Version/3.0.4 Mobile Safari/523.12.2", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("HTC_Dream Mozilla/5.0 (Linux; U; Android 1.5; en-ca; Build/CUPCAKE) AppleWebKit/528.5  (KHTML, like Gecko) Version/3.1.2 Mobile Safari/525.20.1", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("Mozilla/5.0 (Linux; U; Android 2.0; en-us; Droid Build/ESD20) AppleWebKit/530.17 (KHTML, like Gecko) Version/4.0 Mobile Safari/530.17", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("Mozilla/5.0 (Linux; U; Android 2.1; en-us; Nexus One Build/ERD62) AppleWebKit/530.17 (KHTML, like Gecko) Version/4.0 Mobile Safari/530.17", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("Mozilla/5.0 (Linux; U; Android 2.2; en-us; Sprint APA9292KT Build/FRF91) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("Mozilla/5.0 (Linux; U; Android 2.2; en-us; ADR6300 Build/FRF91) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("Mozilla/5.0 (Linux; U; Android 2.2; en-ca; GT-P1000M Build/FROYO) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("Mozilla/5.0 (Android; Linux armv7l; rv:2.0.1) Gecko/20100101 Firefox/4.0.1 Fennec/2.0.1", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("Mozilla/5.0 (Linux; U; Android 3.0.1; fr-fr; A500 Build/HRI66) AppleWebKit/534.13 (KHTML, like Gecko) Version/4.0 Safari/534.13", FLAGS_MOBILE_TABLET);
    checkHttpClientInfoFlags("Mozilla/5.0 (Linux; U; Android 3.0; en-us; Xoom Build/HRI39) AppleWebKit/525.10  (KHTML, like Gecko) Version/3.0.4 Mobile Safari/523.12.2", FLAGS_MOBILE_TABLET);
    checkHttpClientInfoFlags("Mozilla/5.0 (Linux; U; Android 4.0.3; de-ch; HTC Sensation Build/IML74K) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("Mozilla/5.0 (Linux; U; Android 4.0.3; de-de; Galaxy S II Build/GRJ22) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("Opera/9.80 (Android 4.0.4; Linux; Opera Mobi/ADR-1205181138; U; pl) Presto/2.10.254 Version/12.00", FLAGS_MOBILE_TABLET);
    checkHttpClientInfoFlags("Mozilla/5.0 (Android; Linux armv7l; rv:10.0.1) Gecko/20100101 Firefox/10.0.1 Fennec/10.0.1", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("Mozilla/5.0 (Linux; Android 4.1.2; SHV-E250S Build/JZO54K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/30.0.1599.82 Mobile Safari/537.36", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("Mozilla/5.0 (Android 4.2; rv:19.0) Gecko/20121129 Firefox/19.0", FLAGS_MOBILE_TABLET);
    checkHttpClientInfoFlags("Mozilla/5.0 (Linux; U; Android 4.3; en-us; sdk Build/MR1) AppleWebKit/536.23 (KHTML, like Gecko) Version/4.3 Mobile Safari/536.23", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("Mozilla/5.0 (Linux; Android 4.4; Nexus 5 Build/BuildID) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/30.0.0.0 Mobile Safari/537.36", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("Mozilla/5.0 (Linux; Android 4.4.2; SAMSUNG-SM-T537A Build/KOT49H) AppleWebKit/537.36 (KHTML like Gecko) Chrome/35.0.1916.141 Safari/537.36", FLAGS_MOBILE_TABLET);
    // OS > iOS
    checkHttpClientInfoFlags("Mozilla/5.0 (iPhone; U; CPU like Mac OS X; en) AppleWebKit/420  (KHTML, like Gecko) Version/3.0 Mobile/1A543a Safari/419.3", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("Mozilla/5.0 (iPhone; U; CPU iPhone OS 2_0 like Mac OS X; en-us) AppleWebKit/525.18.1 (KHTML, like Gecko) Version/3.1.1 Mobile/5A347 Safari/525.200", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("Mozilla/5.0 (iPod; U; CPU iPhone OS 2_2_1 like Mac OS X; en-us) AppleWebKit/525.18.1 (KHTML, like Gecko) Version/3.1.1 Mobile/5H11a Safari/525.20", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("Mozilla/5.0 (iPhone; U; CPU iPhone OS 3_0 like Mac OS X; en-us) AppleWebKit/528.18 (KHTML, like Gecko) Version/4.0 Mobile/7A341 Safari/528.16", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("Mozilla/5.0 (iPad; U; CPU OS 3_2 like Mac OS X; en-us) AppleWebKit/531.21.10 (KHTML, like Gecko) Version/4.0.4 Mobile/7B334b Safari/531.21.10", FLAGS_MOBILE_TABLET);
    checkHttpClientInfoFlags("Mozilla/5.0 (iPad; U; CPU OS 4_2_1 like Mac OS X; ja-jp) AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8C148 Safari/6533.18.5", FLAGS_MOBILE_TABLET);
    checkHttpClientInfoFlags("Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_2_1 like Mac OS X; da-dk) AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8C148 Safari/6533.18.5", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_3 like Mac OS X; de-de) AppleWebKit/533.17.9 (KHTML, like Gecko) Mobile/8F190", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("Mozilla/5.0 (iPhone; U; CPU iPhone OS 5_1_1 like Mac OS X; da-dk) AppleWebKit/534.46.0 (KHTML, like Gecko) CriOS/19.0.1084.60 Mobile/9B206 Safari/7534.48.3", FLAGS_MOBILE_MOBILE);
    // OS > Linux
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; Linux i686 on x86_64; rv:2.0.1) Gecko/20100101 Firefox/4.0.1 Fennec/2.0.1", FLAGS_DESKTOP_LINUX);
    // OS > Maemo
    checkHttpClientInfoFlags("Mozilla/5.0 (Maemo; Linux armv7l; rv:2.0.1) Gecko/20100101 Firefox/4.0.1 Fennec/2.0.1", FLAGS_DESKTOP_LINUX);
    // OS > Palm
    checkHttpClientInfoFlags("Mozilla/5.0 (webOS/1.3; U; en-US) AppleWebKit/525.27.1 (KHTML, like Gecko) Version/1.0 Safari/525.27.1 Desktop/1.0", FLAGS_NONE);
    checkHttpClientInfoFlags("Mozilla/4.0 (compatible; MSIE 6.0; Windows 98; PalmSource/hspr-H102; Blazer/4.0) 16;320x320", FLAGS_DESKTOP_WINDOWS);
    // OS > Symbian
    checkHttpClientInfoFlags("Mozilla/5.0 (Symbian/3; Series60/5.2 NokiaN8-00/014.002; Profile/MIDP-2.1 Configuration/CLDC-1.1; en-us) AppleWebKit/525 (KHTML, like Gecko) Version/3.0 BrowserNG/7.2.6.4 3gpp-gba", FLAGS_NONE);
    checkHttpClientInfoFlags("Mozilla/5.0 (Symbian/3; Series60/5.2 NokiaX7-00/021.004; Profile/MIDP-2.1 Configuration/CLDC-1.1 ) AppleWebKit/533.4 (KHTML, like Gecko) NokiaBrowser/7.3.1.21 Mobile Safari/533.4 3gpp-gba", FLAGS_NONE);
    checkHttpClientInfoFlags("Mozilla/5.0 (SymbianOS/9.2; U; Series60/3.1 NokiaE90-1/07.24.0.3; Profile/MIDP-2.0 Configuration/CLDC-1.1 ) AppleWebKit/413 (KHTML, like Gecko) Safari/413 UP.Link/6.2.3.18.0", FLAGS_NONE);
    checkHttpClientInfoFlags("Mozilla/5.0 (SymbianOS 9.4; Series60/5.0 NokiaN97-1/10.0.012; Profile/MIDP-2.1 Configuration/CLDC-1.1; en-us) AppleWebKit/525 (KHTML, like Gecko) WicKed/7.1.12344", FLAGS_NONE);
    checkHttpClientInfoFlags("Opera/9.80 (S60; SymbOS; Opera Mobi/499; U; ru) Presto/2.4.18 Version/10.00", FLAGS_NONE);
    // OS > Windows
    checkHttpClientInfoFlags("Mozilla/4.0 (compatible; MSIE 6.0; Windows CE; IEMobile 6.12; Microsoft ZuneHD 4.3)", FLAGS_MOBILE_WINDOWS_PHONE);
    checkHttpClientInfoFlags("Mozilla/4.0 (compatible; MSIE 6.0; Windows CE; IEMobile 7.11)", FLAGS_MOBILE_WINDOWS_PHONE);
    checkHttpClientInfoFlags("Mozilla/4.0 (compatible; MSIE 6.0; Windows CE; IEMobile 7.11) Sprint:PPC6800", FLAGS_MOBILE_WINDOWS_PHONE);
    checkHttpClientInfoFlags("Mozilla/4.0 (compatible; MSIE 6.0; Windows CE; IEMobile 8.12; MSIEMobile6.0)", FLAGS_MOBILE_WINDOWS_PHONE);
    checkHttpClientInfoFlags("Mozilla/4.0 (compatible; MSIE 7.0; Windows Phone OS 7.0; Trident/3.1; IEMobile/7.0) Asus;Galaxy6", FLAGS_MOBILE_WINDOWS_PHONE);
    checkHttpClientInfoFlags("Mozilla/4.0 (compatible; MSIE 7.0; Windows Phone OS 7.0; Trident/3.1; IEMobile/7.0)", FLAGS_MOBILE_WINDOWS_PHONE);
    checkHttpClientInfoFlags("Mozilla/5.0 (compatible; MSIE 9.0; Windows Phone OS 7.5; Trident/5.0; IEMobile/9.0)", FLAGS_MOBILE_WINDOWS_PHONE);
    checkHttpClientInfoFlags("Mozilla/5.0 (compatible; MSIE 10.0; Windows Phone 8.0; Trident/6.0; IEMobile/10.0; ARM; Touch) ", FLAGS_MOBILE_WINDOWS_PHONE);
    checkHttpClientInfoFlags("Mozilla/5.0 (compatible; MSIE 10.0; Windows Phone 8.0; Trident/6.0; IEMobile/10.0; ARM; Touch; NOKIA; Lumia 920)", FLAGS_MOBILE_WINDOWS_PHONE);
    // Services
    checkHttpClientInfoFlags("DoCoMo/2.0 SH901iC(c100;TB;W24H12)", FLAGS_NONE);
    checkHttpClientInfoFlags("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.8.0.7) Gecko/20060909 Firefox/1.5.0.7 MG(Novarra-Vision/6.9)", FLAGS_DESKTOP_LINUX);
    checkHttpClientInfoFlags("Mozilla/4.0 (compatible; MSIE 6.0; j2me) ReqwirelessWeb/3.5", FLAGS_NONE);
    checkHttpClientInfoFlags("Vodafone/1.0/V802SE/SEJ001 Browser/SEMC-Browser/4.1", FLAGS_NONE);
    // WAP Phones
    checkHttpClientInfoFlags("BlackBerry7520/4.0.0 Profile/MIDP-2.0 Configuration/CLDC-1.1 UP.Browser/5.0.3.3 UP.Link/5.1.2.12 (Google WAP Proxy/1.0)", FLAGS_NONE);
    checkHttpClientInfoFlags("Nokia6100/1.0 (04.01) Profile/MIDP-1.0 Configuration/CLDC-1.0", FLAGS_NONE);
    checkHttpClientInfoFlags("Nokia6630/1.0 (2.3.129) SymbianOS/8.0 Series60/2.6 Profile/MIDP-2.0 Configuration/CLDC-1.1", FLAGS_NONE);

    assertPassed();
  }

  @Test
  public void bulkTests_Other() {
    // Spiders - Search
    checkHttpClientInfoFlags("Mozilla/2.0 (compatible; Ask Jeeves/Teoma)", FLAGS_NONE);
    checkHttpClientInfoFlags("Baiduspider ( http://www.baidu.com/search/spider.htm)", FLAGS_NONE);
    checkHttpClientInfoFlags("Mozilla/5.0 (compatible; bingbot/2.0  http://www.bing.com/bingbot.htm)", FLAGS_NONE);
    checkHttpClientInfoFlags("Mozilla/5.0 (compatible; Exabot/3.0;  http://www.exabot.com/go/robot) ", FLAGS_NONE);
    checkHttpClientInfoFlags("FAST-WebCrawler/3.8 (crawler at trd dot overture dot com; http://www.alltheweb.com/help/webmaster/crawler)", FLAGS_NONE);
    checkHttpClientInfoFlags("AdsBot-Google ( http://www.google.com/adsbot.html)", FLAGS_NONE);
    checkHttpClientInfoFlags("Mozilla/5.0 (compatible; Googlebot/2.1;  http://www.google.com/bot.html)", FLAGS_NONE);
    checkHttpClientInfoFlags("Googlebot/2.1 ( http://www.googlebot.com/bot.html)", FLAGS_NONE);
    checkHttpClientInfoFlags("Googlebot-Image/1.0", FLAGS_NONE);
    checkHttpClientInfoFlags("Mediapartners-Google", FLAGS_NONE);
    checkHttpClientInfoFlags("DoCoMo/2.0 N905i(c100;TB;W24H16) (compatible; Googlebot-Mobile/2.1;  http://www.google.com/bot.html)", FLAGS_NONE);
    checkHttpClientInfoFlags("Mozilla/5.0 (iPhone; U; CPU iPhone OS) (compatible; Googlebot-Mobile/2.1;  http://www.google.com/bot.html)", FLAGS_MOBILE_MOBILE);
    checkHttpClientInfoFlags("SAMSUNG-SGH-E250/1.0 Profile/MIDP-2.0 Configuration/CLDC-1.1 UP.Browser/6.2.3.3.c.1.101 (GUI) MMP/2.0 (compatible; Googlebot-Mobile/2.1;  http://www.google.com/bot.html)", FLAGS_NONE);
    checkHttpClientInfoFlags("Googlebot-News", FLAGS_NONE);
    checkHttpClientInfoFlags("Googlebot-Video/1.0", FLAGS_NONE);
    checkHttpClientInfoFlags("Mozilla/4.0 (compatible; GoogleToolbar 4.0.1019.5266-big; Windows XP 5.1; MSIE 6.0.2900.2180)", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("Mozilla/5.0 (en-us) AppleWebKit/525.13 (KHTML, like Gecko; Google Web Preview) Version/3.1 Safari/525.13", FLAGS_NONE);
    checkHttpClientInfoFlags("msnbot/1.0 ( http://search.msn.com/msnbot.htm)", FLAGS_NONE);
    checkHttpClientInfoFlags("msnbot/1.1 ( http://search.msn.com/msnbot.htm)", FLAGS_NONE);
    checkHttpClientInfoFlags("msnbot/0.11 ( http://search.msn.com/msnbot.htm)", FLAGS_NONE);
    checkHttpClientInfoFlags("msnbot-media/1.1 ( http://search.msn.com/msnbot.htm)", FLAGS_NONE);
    checkHttpClientInfoFlags("Mozilla/5.0 (compatible; Yahoo! Slurp; http://help.yahoo.com/help/us/ysearch/slurp)", FLAGS_NONE);
    checkHttpClientInfoFlags("Mozilla/5.0 (compatible; Yahoo! Slurp China; http://misc.yahoo.com.cn/help.html)", FLAGS_NONE);

    // Miscellaneous
    // Bots - Spiders
    checkHttpClientInfoFlags("EmailWolf 1.00", FLAGS_NONE);
    checkHttpClientInfoFlags("Gaisbot/3.0 (robot@gais.cs.ccu.edu.tw; http://gais.cs.ccu.edu.tw/robot.php)", FLAGS_NONE);
    checkHttpClientInfoFlags("grub-client-1.5.3; (grub-client-1.5.3; Crawl your own stuff with http://grub.org)", FLAGS_NONE);
    checkHttpClientInfoFlags("Gulper Web Bot 0.2.4 (www.ecsl.cs.sunysb.edu/~maxim/cgi-bin/Link/GulperBot)", FLAGS_NONE);
    // Browsers - Beos
    checkHttpClientInfoFlags("Mozilla/3.0 (compatible; NetPositive/2.1.1; BeOS)", FLAGS_NONE);
    checkHttpClientInfoFlags("Mozilla/5.0 (BeOS; U; BeOS BePC; en-US; rv:1.9a1) Gecko/20060702 SeaMonkey/1.5a", FLAGS_NONE);
    // Browsers - OS/2
    checkHttpClientInfoFlags("Mozilla/5.0 (OS/2; Warp 4.5; rv:10.0.12) Gecko/20100101 Firefox/10.0.12", FLAGS_NONE);
    checkHttpClientInfoFlags("Mozilla/5.0 (OS/2; Warp 4.5; rv:10.0.12) Gecko/20130108 Firefox/10.0.12 SeaMonkey/2.7.2", FLAGS_NONE);
    checkHttpClientInfoFlags("Mozilla/5.0 (OS/2; U; OS/2; en-US) AppleWebKit/533.3 (KHTML, like Gecko) Arora/0.11.0 Safari/533.3 ", FLAGS_NONE);
    checkHttpClientInfoFlags("Mozilla/5.0 (OS/2; U; OS/2; en-US) AppleWebKit/533.3 (KHTML, like Gecko) QupZilla/1.3.1 Safari/533.3 ", FLAGS_NONE);
    // Downloaders
    checkHttpClientInfoFlags("Download Demon/3.5.0.11", FLAGS_NONE);
    checkHttpClientInfoFlags("Offline Explorer/2.5", FLAGS_NONE);
    checkHttpClientInfoFlags("SuperBot/4.4.0.60 (Windows XP)", FLAGS_DESKTOP_WINDOWS);
    checkHttpClientInfoFlags("WebCopier v4.6", FLAGS_NONE);
    checkHttpClientInfoFlags("Web Downloader/6.9", FLAGS_NONE);
    checkHttpClientInfoFlags("WebZIP/3.5 (http://www.spidersoft.com)", FLAGS_NONE);
    checkHttpClientInfoFlags("Wget/1.9 cvs-stable (Red Hat modified)", FLAGS_NONE);
    checkHttpClientInfoFlags("Wget/1.9.1", FLAGS_NONE);
    // Feed Readers
    checkHttpClientInfoFlags("Bloglines/3.1 (http://www.bloglines.com)", FLAGS_NONE);
    checkHttpClientInfoFlags("everyfeed-spider/2.0 (http://www.everyfeed.com)", FLAGS_NONE);
    checkHttpClientInfoFlags("FeedFetcher-Google; ( http://www.google.com/feedfetcher.html)", FLAGS_NONE);
    checkHttpClientInfoFlags("Gregarius/0.5.2 ( http://devlog.gregarius.net/docs/ua)", FLAGS_NONE);
    // Game Consoles
    checkHttpClientInfoFlags("Mozilla/5.0 (PLAYSTATION 3; 2.00)", FLAGS_NONE);
    checkHttpClientInfoFlags("Mozilla/5.0 (PLAYSTATION 3; 1.10)", FLAGS_NONE);
    checkHttpClientInfoFlags("Mozilla/4.0 (PSP (PlayStation Portable); 2.00)", FLAGS_NONE);
    checkHttpClientInfoFlags("Opera/9.30 (Nintendo Wii; U; ; 2047-7; en)", FLAGS_NONE);
    checkHttpClientInfoFlags("wii libnup/1.0", FLAGS_NONE);
    // Libraries
    checkHttpClientInfoFlags("Java/1.6.0_13", FLAGS_NONE);
    checkHttpClientInfoFlags("libwww-perl/5.820", FLAGS_NONE);
    checkHttpClientInfoFlags("Peach/1.01 (Ubuntu 8.04 LTS; U; en)", FLAGS_NONE);
    checkHttpClientInfoFlags("Python-urllib/2.5", FLAGS_NONE);
    // Validators
    checkHttpClientInfoFlags("HTMLParser/1.6", FLAGS_NONE);
    checkHttpClientInfoFlags("Jigsaw/2.2.5 W3C_CSS_Validator_JFouffa/2.0", FLAGS_NONE);
    checkHttpClientInfoFlags("W3C_Validator/1.654", FLAGS_NONE);
    checkHttpClientInfoFlags("W3C_Validator/1.305.2.12 libwww-perl/5.64", FLAGS_NONE);
    checkHttpClientInfoFlags("P3P Validator", FLAGS_NONE);
    checkHttpClientInfoFlags("CSSCheck/1.2.2", FLAGS_NONE);
    checkHttpClientInfoFlags("WDG_Validator/1.6.2", FLAGS_NONE);
    // Miscellaneous
    checkHttpClientInfoFlags("facebookscraper/1.0( http://www.facebook.com/sharescraper_help.php)", FLAGS_NONE);
    checkHttpClientInfoFlags("grub-client-1.5.3; (grub-client-1.5.3; Crawl your own stuff with http://grub.org)", FLAGS_NONE);
    checkHttpClientInfoFlags("iTunes/4.2 (Macintosh; U; PPC Mac OS X 10.2)", FLAGS_DESKTOP_MAC);
    checkHttpClientInfoFlags("Microsoft URL Control - 6.00.8862", FLAGS_NONE);
    checkHttpClientInfoFlags("SearchExpress", FLAGS_NONE);
    // Java HTTP client
    checkHttpClientInfoFlags("Java/1.8.0_74", FLAGS_NONE);

    assertPassed();
  }

  private void checkHttpClientInfoFlags(String userAgentString, P_HttpClientInfoTestFlags flags) {
    if (flags == null) {
      return;
    }

    HttpClientInfo info = newHttpClientInfo(userAgentString);

    List<String> failedTests = new ArrayList<>();
    if (flags.isMobile() != null && flags.isMobile() != info.isMobile()) {
      failedTests.add("MOBILE [expected " + flags.m_isMobile + ", but is " + info.isMobile() + "]");
    }
    if (flags.isTablet() != null && flags.m_isTablet != info.isTablet()) {
      failedTests.add("TABLET [expected " + flags.m_isTablet + ", but is " + info.isTablet() + "]");
    }
    boolean isWindows = (info.getSystem() == UiSystem.WINDOWS);
    if (flags.isWindows() != null && flags.isWindows() != isWindows) {
      failedTests.add("WINDOWS [expected " + flags.m_isWindows + ", but is " + isWindows + "; system=" + info.getSystem() + "]");
    }
    boolean isMac = (info.getSystem() == UiSystem.OSX);
    if (flags.isMac() != null && flags.isMac() != isMac) {
      failedTests.add("MAC [expected " + flags.m_isMac + ", but is " + isMac + "; system=" + info.getSystem() + "]");
    }
    boolean isLinux = (info.getSystem() == UiSystem.UNIX);
    if (flags.isLinux() != null && flags.isLinux() != isLinux) {
      failedTests.add("LINUX [expected " + flags.m_isLinux + ", but is " + isLinux + "; system=" + info.getSystem() + "]");
    }
    if (flags.getEngineType() != null && flags.getEngineType() != info.getEngineType()) {
      failedTests.add("ENGINE TYPE [expected " + flags.m_engineType + ", but is " + info.getEngineType() + "]");
    }

    if (!failedTests.isEmpty()) {
      m_failCount++;
      StackTraceElement st = Thread.currentThread().getStackTrace()[2];
      System.err.println("Some checks failed at (" + st.getFileName() + ":" + st.getLineNumber() + "):");
      System.err.println("  User agent string : " + userAgentString);
      System.err.println("  Failed flags      : " + failedTests.size());
      for (String failedTest : failedTests) {
        System.err.println("  - " + failedTest);
      }
      System.err.println();
    }
  }

  private void assertPassed() {
    assertEquals(m_failCount + " tests did not pass, see console log for details", 0, m_failCount);
  }

  private static class P_HttpClientInfoTestFlags {

    private Boolean m_isMobile;
    private Boolean m_isTablet;
    private Boolean m_isWindows;
    private Boolean m_isMac;
    private Boolean m_isLinux;
    private UiEngineType m_engineType;

    public P_HttpClientInfoTestFlags() {
    }

    public static P_HttpClientInfoTestFlags create() {
      return new P_HttpClientInfoTestFlags();
    }

    public P_HttpClientInfoTestFlags copy() {
      P_HttpClientInfoTestFlags copy = new P_HttpClientInfoTestFlags();
      copy.m_isMobile = this.m_isMobile;
      copy.m_isTablet = this.m_isTablet;
      copy.m_isWindows = this.m_isWindows;
      copy.m_isMac = this.m_isMac;
      copy.m_isLinux = this.m_isLinux;
      copy.m_engineType = this.m_engineType;
      return copy;
    }

    public Boolean isMobile() {
      return m_isMobile;
    }

    public Boolean isTablet() {
      return m_isTablet;
    }

    public Boolean isWindows() {
      return m_isWindows;
    }

    public Boolean isMac() {
      return m_isMac;
    }

    public Boolean isLinux() {
      return m_isLinux;
    }

    public UiEngineType getEngineType() {
      return m_engineType;
    }

    public P_HttpClientInfoTestFlags mobile() {
      m_isMobile = true;
      return this;
    }

    public P_HttpClientInfoTestFlags notMobile() {
      m_isMobile = false;
      return this;
    }

    public P_HttpClientInfoTestFlags tablet() {
      m_isTablet = true;
      return this;
    }

    public P_HttpClientInfoTestFlags notTablet() {
      m_isTablet = false;
      return this;
    }

    public P_HttpClientInfoTestFlags windows() {
      m_isWindows = true;
      return this;
    }

    public P_HttpClientInfoTestFlags notWindows() {
      m_isWindows = false;
      return this;
    }

    public P_HttpClientInfoTestFlags mac() {
      m_isMac = true;
      return this;
    }

    public P_HttpClientInfoTestFlags notMac() {
      m_isMac = false;
      return this;
    }

    public P_HttpClientInfoTestFlags linux() {
      m_isLinux = true;
      return this;
    }

    public P_HttpClientInfoTestFlags notLinux() {
      m_isLinux = false;
      return this;
    }

    public P_HttpClientInfoTestFlags engineType(UiEngineType engineType) {
      m_engineType = engineType;
      return this;
    }
  }

  //  +----- (Old tests, originally copied from org.eclipse.scout.rt.ui.rap.test)
  //  |
  // \|/
  //  '

  @Test
  public void testCreateHttpClientInfo() {
    Map<String, HttpClientInfo> testMap = initTestMap();
    for (String userAgent : testMap.keySet()) {
      HttpClientInfo createdHttpClientInfo = newHttpClientInfo(userAgent);
      HttpClientInfo expectedHttpClientInfo = testMap.get(userAgent);

      //Ignore versions if not explicitly set
      if (expectedHttpClientInfo.getEngineVersion() == null) {
        createdHttpClientInfo.setEngineVersion(null);
      }
      if (expectedHttpClientInfo.getSystemVersion() == null) {
        createdHttpClientInfo.setSystemVersion(null);
      }

      assertEquals(expectedHttpClientInfo, createdHttpClientInfo);
    }
  }

  private static final String TEST_USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.143 Safari/537.36";

  @Test
  public void testGetFromRequest() {
    HttpClientInfo httpClientInfoOnSession = newHttpClientInfo(TEST_USER_AGENT);
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    HttpSession session = Mockito.mock(HttpSession.class);
    Mockito.when(request.getSession(false)).thenReturn(session);
    Mockito.when(request.getHeader("User-Agent")).thenReturn(TEST_USER_AGENT);
    Mockito.when(session.getAttribute(Mockito.eq(HttpClientInfo.HTTP_CLIENT_INFO_ATTRIBUTE_NAME))).thenReturn(null, httpClientInfoOnSession);

    HttpClientInfo httpClientInfo = HttpClientInfo.get(request);
    assertHttpClientInfo(httpClientInfo);

    HttpClientInfo httpClientInfo2 = HttpClientInfo.get(request);
    assertHttpClientInfo(httpClientInfo2);
    assertSame(httpClientInfoOnSession, httpClientInfo2);

    Mockito.verify(session).setAttribute(Mockito.matches(HttpClientInfo.HTTP_CLIENT_INFO_ATTRIBUTE_NAME), Mockito.argThat(new ArgumentMatcher<HttpClientInfo>() {
      @Override
      public boolean matches(Object argument) {
        assertHttpClientInfo((HttpClientInfo) argument);
        return argument instanceof HttpClientInfo;
      }
    }));
  }

  protected void assertHttpClientInfo(HttpClientInfo httpClientInfo) {
    assertTrue(httpClientInfo.isDesktop());
    assertTrue(httpClientInfo.isWebkit());
    assertFalse(httpClientInfo.isStandalone());
    assertFalse(httpClientInfo.isGecko());
    assertFalse(httpClientInfo.isMobile());
    assertFalse(httpClientInfo.isMshtml());
    assertFalse(httpClientInfo.isOpera());
    assertFalse(httpClientInfo.isTablet());
  }

  private Map<String, HttpClientInfo> initTestMap() {
    Map<String, HttpClientInfo> testMap = new HashMap<String, HttpClientInfo>();

    putIosBrowsers(testMap);
    putAndroidBrowsers(testMap);
    putWindowsBrowsers(testMap);

    return testMap;
  }

  private void putIosBrowsers(Map<String, HttpClientInfo> testMap) {
    //iPhone 4S
    String userAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 6_0 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/6.0 Mobile/10A403 Safari/8536.25";
    HttpClientInfo httpClientInfo = newHttpClientInfo(userAgent);
    httpClientInfo.setEngineType(UiEngineType.SAFARI);
    httpClientInfo.setSystem(UiSystem.IOS);
    httpClientInfo.setWebkit(true);
    httpClientInfo.setMobile(true);
    httpClientInfo.setSystemVersion(new Version(6, 0, 0));
    testMap.put(userAgent, httpClientInfo);

    //iPad 3
    userAgent = "Mozilla/5.0 (iPad; CPU OS 5_1 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9B176 Safari/7534.48.3";
    httpClientInfo = newHttpClientInfo(userAgent);
    httpClientInfo.setEngineType(UiEngineType.SAFARI);
    httpClientInfo.setSystem(UiSystem.IOS);
    httpClientInfo.setWebkit(true);
    httpClientInfo.setTablet(true);
    httpClientInfo.setSystemVersion(new HttpClientInfo.Version(5, 1, 0));
    testMap.put(userAgent, httpClientInfo);

    //iPad 3 (home screen icon mode)
    userAgent = "Mozilla/5.0 (iPad; CPU OS 5_1 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Mobile/9B176";
    httpClientInfo = newHttpClientInfo(userAgent);
    httpClientInfo.setEngineType(UiEngineType.SAFARI);
    httpClientInfo.setSystem(UiSystem.IOS);
    httpClientInfo.setWebkit(true);
    httpClientInfo.setTablet(true);
    httpClientInfo.setStandalone(true);
    httpClientInfo.setSystemVersion(new HttpClientInfo.Version(5, 1, 0));
    testMap.put(userAgent, httpClientInfo);
  }

  private void putAndroidBrowsers(Map<String, HttpClientInfo> testMap) {
    //Samsung tablet GT P7500
    String userAgent = "Mozilla/5.0 (Linux; U; Android 3.2; de-de; GT-P7500 Build/HTJ85B) AppleWebKit/534.13 (KHTML, like Gecko) Version/4.0 Safari/534.13";
    HttpClientInfo httpClientInfo = newHttpClientInfo(userAgent);
    httpClientInfo.setEngineType(UiEngineType.ANDROID);
    httpClientInfo.setSystem(UiSystem.ANDROID);
    httpClientInfo.setWebkit(true);
    httpClientInfo.setTablet(true);
    httpClientInfo.setSystemVersion(new HttpClientInfo.Version(3, 2, 0));
    testMap.put(userAgent, httpClientInfo);

    //Samsung Galaxy S2 Android Browser
    userAgent = "Mozilla/5.0 (Linux; U; Android 4.0.3; de-ch; SAMSUNG GT-I9100/I9100BULPD Build/IML74K) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30";
    httpClientInfo = newHttpClientInfo(userAgent);
    httpClientInfo.setEngineType(UiEngineType.ANDROID);
    httpClientInfo.setSystem(UiSystem.ANDROID);
    httpClientInfo.setWebkit(true);
    httpClientInfo.setMobile(true);
    httpClientInfo.setSystemVersion(new HttpClientInfo.Version(4, 0, 3));
    testMap.put(userAgent, httpClientInfo);

    //Samsung Galaxy S2 Google Chrome
    userAgent = "Mozilla/5.0 (Linux; Android 4.0.3; GT-I9100 Build/IML74K) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.166 Mobile Safari/535.19";
    httpClientInfo = newHttpClientInfo(userAgent);
    httpClientInfo.setEngineType(UiEngineType.CHROME);
    httpClientInfo.setSystem(UiSystem.ANDROID);
    httpClientInfo.setWebkit(true);
    httpClientInfo.setMobile(true);
    httpClientInfo.setSystemVersion(new HttpClientInfo.Version(4, 0, 3));
    testMap.put(userAgent, httpClientInfo);

    //Samsung Galaxy S2 Dolphin Browser
    userAgent = "Mozilla/5.0 (Linux; U; Android 4.0.3; de-ch; GT-I9100 Build/IML74K) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30";
    httpClientInfo = newHttpClientInfo(userAgent);
    httpClientInfo.setEngineType(UiEngineType.ANDROID);
    httpClientInfo.setSystem(UiSystem.ANDROID);
    httpClientInfo.setWebkit(true);
    httpClientInfo.setMobile(true);
    httpClientInfo.setSystemVersion(new HttpClientInfo.Version(4, 0, 3));
    testMap.put(userAgent, httpClientInfo);

    //Samsung Nexus S Firefox, Android 4.1.2
    userAgent = "Mozilla/5.0 (Android; Mobile; rv:17.0) Gecko/17.0 Firefox/17.0";
    httpClientInfo = newHttpClientInfo(userAgent);
    httpClientInfo.setEngineType(UiEngineType.FIREFOX);
    httpClientInfo.setSystem(UiSystem.ANDROID);
    httpClientInfo.setGecko(true);
    httpClientInfo.setMobile(true);
    httpClientInfo.setSystemVersion(null);
    testMap.put(userAgent, httpClientInfo);
  }

  private void putWindowsBrowsers(Map<String, HttpClientInfo> testMap) {
    //Windows 7, IE 9
    String userAgent = "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0)";
    HttpClientInfo httpClientInfo = newHttpClientInfo(userAgent);
    httpClientInfo.setEngineType(UiEngineType.IE);
    httpClientInfo.setEngineVersion(new HttpClientInfo.Version(9, 0, 0));
    httpClientInfo.setSystem(UiSystem.WINDOWS);
    httpClientInfo.setMshtml(true);
    httpClientInfo.setSystemVersion(new HttpClientInfo.Version(6, 1, 0));
    testMap.put(userAgent, httpClientInfo);

    //Windows 7, Google Chrome 22.0.1229.94
    userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.4 (KHTML, like Gecko) Chrome/22.0.1229.94 Safari/537.4";
    httpClientInfo = newHttpClientInfo(userAgent);
    httpClientInfo.setEngineType(UiEngineType.CHROME);
    httpClientInfo.setSystem(UiSystem.WINDOWS);
    httpClientInfo.setWebkit(true);
    httpClientInfo.setSystemVersion(new HttpClientInfo.Version(6, 1, 0));
    testMap.put(userAgent, httpClientInfo);

    //Nokia Lumia 800
    userAgent = "Mozilla/5.0 (compatible; MSIE 9.0; Windows Phone OS 7.5; Trident/5.0; IEMobile/9.0; NOKIA; Lumia 800)";
    httpClientInfo = newHttpClientInfo(userAgent);
    httpClientInfo.setEngineType(UiEngineType.IE);
    httpClientInfo.setEngineVersion(new HttpClientInfo.Version(9, 0, 0));
    httpClientInfo.setSystem(UiSystem.WINDOWS);
    httpClientInfo.setMshtml(true);
    httpClientInfo.setMobile(true);
    httpClientInfo.setSystemVersion(new HttpClientInfo.Version(7, 5, 0));
    testMap.put(userAgent, httpClientInfo);

    //Windows Phone 8 HTC
    userAgent = "Mozilla/5.0 (compatible; MSIE 10.0; Windows Phone 8.0; Trident/6.0; IEMobile/10.0; ARM; Touch; HTC; Windows Phone 8X by HTC)";
    httpClientInfo = newHttpClientInfo(userAgent);
    httpClientInfo.setEngineType(UiEngineType.IE);
    httpClientInfo.setEngineVersion(new HttpClientInfo.Version(10, 0, 0));
    httpClientInfo.setSystem(UiSystem.WINDOWS);
    httpClientInfo.setMshtml(true);
    httpClientInfo.setMobile(true);
    httpClientInfo.setSystemVersion(new HttpClientInfo.Version(8, 0, 0));
    testMap.put(userAgent, httpClientInfo);
  }

  protected HttpClientInfo newHttpClientInfo(String userAgent) {
    return new HttpClientInfo().init(userAgent);
  }
}
