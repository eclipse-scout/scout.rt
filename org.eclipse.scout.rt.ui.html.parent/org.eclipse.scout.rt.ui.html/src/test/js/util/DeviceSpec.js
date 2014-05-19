describe("Device", function() {

  function verifyDevices(actual, expected) {
    expect(actual.getSystem()).toBe(expected.getSystem());
  }

  function createDevice(system) {
    var device = new scout.Device();
    device._userAgentParsed = true;
    device.system=system;
    return device;
  }

  describe("parseUserAgent", function() {

    it("recognizes ios devices", function() {
      var userAgent, actualDevice , expectedDevice;

      //iPhone 4S
      userAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 6_0 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/6.0 Mobile/10A403 Safari/8536.25";
      actualDevice = new scout.Device(userAgent);
      expectedDevice = createDevice(scout.Device.SYSTEM_IOS);
      verifyDevices(actualDevice, expectedDevice);

      //iPad 3
      userAgent = "Mozilla/5.0 (iPad; CPU OS 5_1 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9B176 Safari/7534.48.3";
      actualDevice = new scout.Device(userAgent);
      expectedDevice = createDevice(scout.Device.SYSTEM_IOS);
      verifyDevices(actualDevice, expectedDevice);

      //iPad 3 (home screen icon mode)
      userAgent = "Mozilla/5.0 (iPad; CPU OS 5_1 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Mobile/9B176";
      actualDevice = new scout.Device(userAgent);
      expectedDevice = createDevice(scout.Device.SYSTEM_IOS);
      verifyDevices(actualDevice, expectedDevice);
    });

  });

});
