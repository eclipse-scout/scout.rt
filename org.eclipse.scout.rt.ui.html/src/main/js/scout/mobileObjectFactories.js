scout.mobileObjectFactories = scout.defaultObjectFactories.concat([{
  objectType: 'Desktop',
  create: function() {
    return new scout.MobileDesktop();
  },
  deviceType: scout.Device.Type.MOBILE
}, {
  objectType: 'Table',
  create: function() {
    return new scout.MobileTable();
  },
  deviceType: scout.Device.Type.MOBILE
}]);
