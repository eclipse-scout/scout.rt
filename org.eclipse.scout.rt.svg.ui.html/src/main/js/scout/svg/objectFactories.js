scout.defaultObjectFactories = scout.defaultObjectFactories.concat([{
  objectType: 'SvgField',
  create: function() {
    return new scout.SvgField();
  }
}]);
