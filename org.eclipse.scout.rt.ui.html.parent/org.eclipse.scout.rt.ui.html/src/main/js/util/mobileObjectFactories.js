scout.mobileObjectFactories = scout.defaultObjectFactories;
scout.mobileObjectFactories = scout.mobileObjectFactories.concat( [ {
  objectType : 'Table',
  create : function() {
    return new scout.MobileTable();
  },
  deviceType: scout.UserAgent.DEVICE_TYPE_MOBILE
},
{
  objectType : 'Desktop',
  create : function() {
    return new scout.MobileDesktop();
  },
  deviceType: scout.UserAgent.DEVICE_TYPE_MOBILE
}]);
