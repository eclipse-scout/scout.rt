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
    });

  });

});
