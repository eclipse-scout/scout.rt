scout.mobileObjectFactories = scout.defaultObjectFactories;
scout.mobileObjectFactories = scout.mobileObjectFactories.concat( [ {
  objectType : 'Table',
  create : function(session, model) {
    return new scout.MobileTable(session, model);
  },
  deviceType: scout.UserAgent.DEVICE_TYPE_MOBILE
},
{
  objectType : 'Desktop',
  create : function(session, model) {
    return new scout.MobileDesktop(session, model);
  },
  deviceType: scout.UserAgent.DEVICE_TYPE_MOBILE
}]);
