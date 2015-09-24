scout.UserAgent = function(deviceType) {
  if (!deviceType) {
    throw new Error('deviceType needs to be defined');
  }
  this.deviceType = deviceType;
};
