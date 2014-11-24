scout.objects = function() {
};

scout.objects.copyProperties = function(source, dest) {
  var propertyName;
  for (propertyName in source) {
    dest[propertyName] = source[propertyName];
  }
};
