scout.mobileObjectFactories = scout.defaultObjectFactories;
scout.mobileObjectFactories = scout.mobileObjectFactories.concat( [ {
  objectType : 'Table',
  create : function(model, session) {
    return new scout.MobileTable(model, session);
  },
  deviceType: scout.UserAgent.DEVICE_TYPE_MOBILE
},
{
  objectType : 'Desktop',
  create : function(model, session) {
    return new scout.MobileDesktop(model, session);
  },
  deviceType: scout.UserAgent.DEVICE_TYPE_MOBILE
}]);
