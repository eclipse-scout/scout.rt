/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Device} from '../../src/index';

describe('Device', () => {

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
    let device = new Device({
      userAgent: userAgent
    });
    device.bootstrap();
    return device;
  }

  describe('scout.device', () => {

    it('is initialized automatically', () => {
      expect(Device.get()).toBeDefined();
      expect(Device.get().browser).toBeDefined();
    });

  });

  describe('isWindowsTabletMode', () => {

    it('returns true if system is windows and scrollbarWidth is 0', () => {
      Device.get().system = Device.System.WINDOWS;
      Device.get().systemVersion = 10.0;
      let origMatchMedia = window.matchMedia;
      try {
        // Patch native "matchMedia" function to simulate different device capabilities
        let matches = false;
        window.matchMedia = mediaQuery => {
          return /** @type {MediaQueryList} */ {
            matches: matches
          };
        };
        expect(Device.get().isWindowsTabletMode()).toBe(false);

        matches = true;
        expect(Device.get().isWindowsTabletMode()).toBe(true);

        Device.get().system = Device.System.UNKNOWN;
        expect(Device.get().isWindowsTabletMode()).toBe(false);
      } finally {
        window.matchMedia = origMatchMedia;
      }
    });
  });

  describe('user agent parsing', () => {

    it('recognizes iOS devices', () => {
      // iPhone 4S
      test('Mozilla/5.0 (iPhone; CPU iPhone OS 6_0 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/6.0 Mobile/10A403 Safari/8536.25', {
        system: Device.System.IOS,
        systemVersion: 6,
        type: Device.Type.MOBILE,
        browser: Device.Browser.SAFARI,
        browserVersion: 6
      });

      // iPad 3
      test('Mozilla/5.0 (iPad; CPU OS 5_1 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9B176 Safari/7534.48.3', {
        system: Device.System.IOS,
        systemVersion: 5.1,
        type: Device.Type.TABLET,
        browser: Device.Browser.SAFARI,
        browserVersion: 5.1
      });

      // iPad 3 (home screen icon mode)
      test('Mozilla/5.0 (iPad; CPU OS 5_1 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Mobile/9B176', {
        system: Device.System.IOS,
        systemVersion: 5.1,
        type: Device.Type.TABLET,
        browser: Device.Browser.UNKNOWN
      });

      // iPad with Chrome for iOS
      test('Mozilla/5.0 (iPad; CPU OS 13_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/87.0.4280.77 Mobile/15E148 Safari/604.1', {
        system: Device.System.IOS,
        systemVersion: 13.3,
        type: Device.Type.TABLET,
        browser: Device.Browser.SAFARI,
        browserVersion: 13.3
      });

      // iPhone with Firefox for iOS
      test('Mozilla/5.0 (iPhone; CPU iPhone OS 8_3 like Mac OS X) AppleWebKit/600.1.4 (KHTML, like Gecko) FxiOS/1.0 Mobile/12F69 Safari/600.1.4', {
        system: Device.System.IOS,
        systemVersion: 8.3,
        type: Device.Type.MOBILE,
        browser: Device.Browser.SAFARI,
        browserVersion: 8.3
      });

      // iPhone with Edge for iOS
      test('Mozilla/5.0 (iPhone; CPU iPhone OS 15_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) EdgiOS/97.0.1072.80 Version/15.0 Mobile/15E148 Safari/604.1', {
        system: Device.System.IOS,
        systemVersion: 15.3,
        type: Device.Type.MOBILE,
        browser: Device.Browser.SAFARI,
        browserVersion: 15.0
      });
    });

    it('recognizes Android devices', () => {
      // Samsung Galaxy S4
      test('Mozilla/5.0 (Linux; Android 4.4.2; GT-I9505 Build/KVT49L) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.170 Mobile Safari/537.36', {
        system: Device.System.ANDROID,
        type: Device.Type.MOBILE,
        browser: Device.Browser.CHROME,
        browserVersion: 33
      });

      // Google Nexus 10 Tablet
      test('Mozilla/5.0 (Linux; Android 4.3; Nexus 10 Build/JWR66Y) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/30.0.1599.82 Safari/537.36', {
        system: Device.System.ANDROID,
        type: Device.Type.TABLET,
        browser: Device.Browser.CHROME,
        browserVersion: 30
      });
    });

    it('recognizes Windows devices', () => {
      // Windows with Firefox browser
      test('Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.134 Safari/537.36', {
        system: Device.System.WINDOWS,
        systemVersion: 6.1, // -> Windows 7
        type: Device.Type.DESKTOP
      });

      test('Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.79 Safari/537.36 Edge/14.14393', {
        system: Device.System.WINDOWS,
        systemVersion: 10.0,
        type: Device.Type.DESKTOP
      });
    });

    // Note: cannot detect Surface tablet reliable with Jasmine test, since scrollbar width
    // measurement depends on the browser that runs the spec.

    it('recognizes supported browsers', () => {
      // Microsoft Edge 12
      _test('Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.135 Safari/537.36 Edge/12.10240',
        Device.Browser.EDGE, 12.10240);

      // Internet Explorer 11
      _test('Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; AS; rv:11.0) like Gecko',
        Device.Browser.INTERNET_EXPLORER, 11.0);
      // Internet Explorer 11 - as used by Outlook - note the additional ; and text after the version-no (rv).
      _test('Mozilla/5.0 (Windows NT 6.1; WOW65; Trident/7.0; rv:11.0; Microsoft Outlook 14.0.7155)',
        Device.Browser.INTERNET_EXPLORER, 11.0);

      // Internet Explorer 8
      _test('Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.0; WOW64; Trident/4.0; SLCC1; .NET CLR 2.0.50727; Media Center PC 5.0; InfoPath.2; .NET CLR 3.5.30729; .NET CLR 3.0.30729)',
        Device.Browser.INTERNET_EXPLORER, 8.0);

      // Safari (6)
      _test('Mozilla/5.0 (iPad; CPU OS 6_0 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/6.0 Mobile/10A5355d Safari/8536.25',
        Device.Browser.SAFARI, 6.0);

      // Firefox (21) from v21 Firefox supports ECMA 5
      _test('Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:16.0.1) Gecko/20121011 Firefox/21.0.1',
        Device.Browser.FIREFOX, 21.0);

      // Chrome (23) from v23 Chrome supports ECMA 5
      _test('Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.6 Safari/537.11',
        Device.Browser.CHROME, 23.0);

      function _test(userAgent, expectedBrowser, expectedVersion) {
        let device = new Device({
          userAgent: userAgent
        });
        expect(device.browser).toBe(expectedBrowser);
        expect(device.browserVersion).toEqual(expectedVersion);
      }
    });
  });
});
