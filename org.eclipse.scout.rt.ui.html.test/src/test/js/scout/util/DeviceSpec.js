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
describe('Device', function() {

  function test(userAgent, expectedDevice) {
    return verify(bootstrapDevice(userAgent), expectedDevice);
  }

  function verify(actual, expected) {
    if (expected.system) {
      expect(actual.system).toBe(expected.system);
    }
    if (expected.systemVersion >= 0) {
      expect(actual.systemVersion).toBe(expected.systemVersion);
    }
    if (expected.type) {
      expect(actual.type).toBe(expected.type);
    }
    if (expected.browser) {
      expect(actual.browser).toBe(expected.browser);
    }
    if (expected.browserVersion >= 0) {
      expect(actual.browserVersion).toBe(expected.browserVersion);
    }
  }

  function bootstrapDevice(userAgent) {
    var device = new scout.Device({
      userAgent: userAgent
    });
    device.bootstrap();
    return device;
  }

  describe('scout.device', function() {

    it('is initialized automatically', function() {
      expect(scout.device).toBeDefined();
      expect(scout.device.browser).toBeDefined();
      expect(scout.device.unselectableAttribute).toBeDefined();
    });

  });

  describe('isWindowsTablet', function() {

    it('returns true when browser is Edge and scrollbarWidth is 0', function() {
      scout.device.scrollbarWidth = 0;
      scout.device.browser = scout.Device.Browser.EDGE;
      expect(scout.device.isWindowsTablet()).toBe(true);
    });

  });

  describe('user agent parsing', function() {

    it('recognizes iOS devices', function() {
      // iPhone 4S
      test('Mozilla/5.0 (iPhone; CPU iPhone OS 6_0 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/6.0 Mobile/10A403 Safari/8536.25', {
        system: scout.Device.System.IOS,
        systemVersion: 6,
        type: scout.Device.Type.MOBILE,
        browser: scout.Device.Browser.SAFARI,
        browserVersion: 6
      });

      // iPad 3
      test('Mozilla/5.0 (iPad; CPU OS 5_1 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9B176 Safari/7534.48.3', {
        system: scout.Device.System.IOS,
        systemVersion: 5.1,
        type: scout.Device.Type.TABLET,
        browser: scout.Device.Browser.SAFARI,
        browserVersion: 5.1
      });

      // iPad 3 (home screen icon mode)
      test('Mozilla/5.0 (iPad; CPU OS 5_1 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Mobile/9B176', {
        system: scout.Device.System.IOS,
        systemVersion: 5.1,
        type: scout.Device.Type.TABLET,
        browser: scout.Device.Browser.UNKNOWN
      });

    });

    it('recognizes Android devices', function() {
      // Samsung Galaxy S4
      test('Mozilla/5.0 (Linux; Android 4.4.2; GT-I9505 Build/KVT49L) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.170 Mobile Safari/537.36', {
        system: scout.Device.System.ANDROID,
        type: scout.Device.Type.MOBILE,
        browser: scout.Device.Browser.CHROME,
        browserVersion: 33
      });

      // Google Nexus 10 Tablet
      test('Mozilla/5.0 (Linux; Android 4.3; Nexus 10 Build/JWR66Y) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/30.0.1599.82 Safari/537.36', {
        system: scout.Device.System.ANDROID,
        type: scout.Device.Type.TABLET,
        browser: scout.Device.Browser.CHROME,
        browserVersion: 30
      });
    });

    it('recognizes normal Windows PCs', function() {
      // Windows with Firefox browser
      test('Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.134 Safari/537.36', {
        system: scout.Device.System.UNKNOWN,
        type: scout.Device.Type.DESKTOP
      });
    });

    // Note: cannot detect Surface tablet reliable with Jasmine test, since scrollbar width
    // measurement depends on the browser that runs the spec.

    it('recognizes supported browsers', function() {
      var userAgent, device;

      // Microsoft Edge 12
      _test('Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.135 Safari/537.36 Edge/12.10240',
        scout.Device.Browser.EDGE, 12.10240);

      // Internet Explorer 11
      _test('Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; AS; rv:11.0) like Gecko',
        scout.Device.Browser.INTERNET_EXPLORER, 11.0);
      // Internet Explorer 11 - as used by Outlook - note the additional ; and text after the version-no (rv).
      _test('Mozilla/5.0 (Windows NT 6.1; WOW65; Trident/7.0; rv:11.0; Microsoft Outlook 14.0.7155)',
        scout.Device.Browser.INTERNET_EXPLORER, 11.0);

      // Internet Explorer 8
      _test('Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.0; WOW64; Trident/4.0; SLCC1; .NET CLR 2.0.50727; Media Center PC 5.0; InfoPath.2; .NET CLR 3.5.30729; .NET CLR 3.0.30729)',
        scout.Device.Browser.INTERNET_EXPLORER, 8.0);

      // Safari (6)
      _test('Mozilla/5.0 (iPad; CPU OS 6_0 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/6.0 Mobile/10A5355d Safari/8536.25',
        scout.Device.Browser.SAFARI, 6.0);

      // Firefox (21) from v21 Firefox supports ECMA 5
      _test('Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:16.0.1) Gecko/20121011 Firefox/21.0.1',
        scout.Device.Browser.FIREFOX, 21.0);

      // Chrome (23) from v23 Chrome supports ECMA 5
      _test('Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.6 Safari/537.11',
        scout.Device.Browser.CHROME, 23.0);

      function _test(userAgent, expectedBrowser, expectedVersion) {
        var device = new scout.Device({
          userAgent: userAgent
        });
        expect(device.browser).toBe(expectedBrowser);
        expect(device.browserVersion).toEqual(expectedVersion);
      }
    });

  });

});
