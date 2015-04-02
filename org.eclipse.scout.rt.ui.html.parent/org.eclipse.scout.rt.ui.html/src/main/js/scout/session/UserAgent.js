scout.UserAgent = function(deviceType) {
  if (!deviceType) {
    throw new Error('deviceType needs to be defined');
  }
  this.deviceType = deviceType;
};

scout.UserAgent.DEVICE_TYPE_DESKTOP = 'DESKTOP';
scout.UserAgent.DEVICE_TYPE_MOBILE = 'MOBILE';
scout.UserAgent.DEVICE_TYPE_TABLET = 'TABLET';
