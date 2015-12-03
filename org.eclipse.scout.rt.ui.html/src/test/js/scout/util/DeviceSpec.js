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

  function verifyDevices(actual, expected) {
    expect(actual.system).toBe(expected.system);
    expect(actual.type).toBe(expected.type);
  }

  function createDevice(system, type) {
    var device = new scout.Device();
    device.system = system;
    device.type = type;
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

    it ('returns true when browser is Edge and scrollbarWidth is 0', function() {
      scout.device.scrollbarWidth = 0;
      scout.device.browser = scout.Device.Browser.EDGE;
      expect(scout.device.isWindowsTablet()).toBe(true);
    });

  });

  describe('new scout.Device instance detects system and type after bootstrap() has been called', function() {

    function bootstrapDevice(userAgent) {
      var device = new scout.Device(userAgent);
      device.bootstrap();
      return device;
    }

    it('recognizes iOS devices', function() {
      var userAgent, expectedDevice;

      // iPhone 4S
      userAgent = 'Mozilla/5.0 (iPhone; CPU iPhone OS 6_0 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/6.0 Mobile/10A403 Safari/8536.25';
      expectedDevice = createDevice(scout.Device.System.IOS, scout.Device.Type.MOBILE);
      verifyDevices(bootstrapDevice(userAgent), expectedDevice);

      // iPad 3
      userAgent = 'Mozilla/5.0 (iPad; CPU OS 5_1 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9B176 Safari/7534.48.3';
      expectedDevice = createDevice(scout.Device.System.IOS, scout.Device.Type.TABLET);
      verifyDevices(bootstrapDevice(userAgent), expectedDevice);

      // iPad 3 (home screen icon mode)
      userAgent = 'Mozilla/5.0 (iPad; CPU OS 5_1 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Mobile/9B176';
      expectedDevice = createDevice(scout.Device.System.IOS, scout.Device.Type.TABLET);
      verifyDevices(bootstrapDevice(userAgent), expectedDevice);
    });

    it('recognizes Android devices', function() {
      var userAgent, expectedDevice;

      // Samsung Galaxy S4
      userAgent = 'Mozilla/5.0 (Linux; Android 4.4.2; GT-I9505 Build/KVT49L) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.170 Mobile Safari/537.36';
      expectedDevice = createDevice(scout.Device.System.ANDROID, scout.Device.Type.MOBILE);
      verifyDevices(bootstrapDevice(userAgent), expectedDevice);

      // Google Nexus 10 Tablet
      userAgent = 'Mozilla/5.0 (Linux; Android 4.3; Nexus 10 Build/JWR66Y) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/30.0.1599.82 Safari/537.36';
      expectedDevice = createDevice(scout.Device.System.ANDROID, scout.Device.Type.TABLET);
      verifyDevices(bootstrapDevice(userAgent), expectedDevice);
    });

    it('recognizes normal Windows PCs', function() {
      // Windows with Firefox browser
      var userAgent = 'Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.134 Safari/537.36';
      var expectedDevice = createDevice(scout.Device.System.UNKNOWN, scout.Device.Type.DESKTOP);
      verifyDevices(bootstrapDevice(userAgent), expectedDevice);
    });

    // Note: cannot detect Surface tablet reliable with Jasmine test, since scrollbar width
    // measurement depends on the browser that runs the spec.

    it('recognizes supported browsers', function() {
      var userAgent, device;

      // Microsoft Edge 12
      test('Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.135 Safari/537.36 Edge/12.10240',
          scout.Device.Browser.EDGE, 12.10240);

      // Internet Explorer 11
      test('Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; AS; rv:11.0) like Gecko',
          scout.Device.Browser.INTERNET_EXPLORER, 11.0);
      // Internet Explorer 11 - as used by Outlook - note the additional ; and text after the version-no (rv).
      test('Mozilla/5.0 (Windows NT 6.1; WOW65; Trident/7.0; rv:11.0; Microsoft Outlook 14.0.7155)',
          scout.Device.Browser.INTERNET_EXPLORER, 11.0);

      // Internet Explorer 8
      test('Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.0; WOW64; Trident/4.0; SLCC1; .NET CLR 2.0.50727; Media Center PC 5.0; InfoPath.2; .NET CLR 3.5.30729; .NET CLR 3.0.30729)',
          scout.Device.Browser.INTERNET_EXPLORER, 8.0);

      // Safari (6)
      test('Mozilla/5.0 (iPad; CPU OS 6_0 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/6.0 Mobile/10A5355d Safari/8536.25',
          scout.Device.Browser.SAFARI, 6.0);

      // Firefox (21) from v21 Firefox supports ECMA 5
      test('Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:16.0.1) Gecko/20121011 Firefox/21.0.1',
          scout.Device.Browser.FIREFOX, 21.0);

      // Chrome (23) from v23 Chrome supports ECMA 5
      test('Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.6 Safari/537.11',
          scout.Device.Browser.CHROME, 23.0);

      function test(userAgent, expectedBrowser, expectedVersion) {
        var device = new scout.Device(userAgent);
        expect(device.browser).toBe(expectedBrowser);
        expect(device.browserVersion).toEqual(expectedVersion);
      }
    });

  });

});
