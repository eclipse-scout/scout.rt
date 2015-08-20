describe('Device', function() {

  function verifyDevices(actual, expected) {
    expect(actual.system).toBe(expected.system);
  }

  function createDevice(system) {
    var device = new scout.Device();
    device._userAgentParsed = true;
    device.system = system;
    return device;
  }

  describe('scout.device', function() {

    it('is initialized automatically', function() {
      expect(scout.device).toBeDefined();
      expect(scout.device.browser).toBeDefined();
      expect(scout.device.unselectableAttribute).toBeDefined();
    });

  });

  describe('parseUserAgent', function() {

    it('recognizes iOS devices', function() {
      var userAgent, actualDevice , expectedDevice;

      // iPhone 4S
      userAgent = 'Mozilla/5.0 (iPhone; CPU iPhone OS 6_0 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/6.0 Mobile/10A403 Safari/8536.25';
      actualDevice = new scout.Device(userAgent);
      expectedDevice = createDevice(scout.Device.SYSTEM_IOS);
      verifyDevices(actualDevice, expectedDevice);

      // iPad 3
      userAgent = 'Mozilla/5.0 (iPad; CPU OS 5_1 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9B176 Safari/7534.48.3';
      actualDevice = new scout.Device(userAgent);
      expectedDevice = createDevice(scout.Device.SYSTEM_IOS);
      verifyDevices(actualDevice, expectedDevice);

      // iPad 3 (home screen icon mode)
      userAgent = 'Mozilla/5.0 (iPad; CPU OS 5_1 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Mobile/9B176';
      actualDevice = new scout.Device(userAgent);
      expectedDevice = createDevice(scout.Device.SYSTEM_IOS);
      verifyDevices(actualDevice, expectedDevice);
    });

    it('recognizes supported browsers', function() {
      var userAgent, device;

      // Internet Explorer
      userAgent = 'Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.0; WOW64; Trident/4.0; SLCC1; .NET CLR 2.0.50727; Media Center PC 5.0; InfoPath.2; .NET CLR 3.5.30729; .NET CLR 3.0.30729)';
      device = new scout.Device(userAgent);
      expect(device.browser).toBe(scout.Device.SupportedBrowsers.INTERNET_EXPLORER);
      expect(device.browserVersion).toEqual(8.0);

      // Safari (6)
      userAgent = 'Mozilla/5.0 (iPad; CPU OS 6_0 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/6.0 Mobile/10A5355d Safari/8536.25';
      device = new scout.Device(userAgent);
      expect(device.browser).toBe(scout.Device.SupportedBrowsers.SAFARI);
      expect(device.browserVersion).toEqual(6.0);

      // Firefox (21) from v21 Firefox supports ECMA 5
      userAgent = 'Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:16.0.1) Gecko/20121011 Firefox/21.0.1';
      device = new scout.Device(userAgent);
      expect(device.browser).toBe(scout.Device.SupportedBrowsers.FIREFOX);
      expect(device.browserVersion).toEqual(21.0);

      // Chrome (23) from v23 Chrome supports ECMA 5
      userAgent = 'Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.6 Safari/537.11';
      device = new scout.Device(userAgent);
      expect(device.browser).toBe(scout.Device.SupportedBrowsers.CHROME);
      expect(device.browserVersion).toEqual(23.0);
    });

  });

});
