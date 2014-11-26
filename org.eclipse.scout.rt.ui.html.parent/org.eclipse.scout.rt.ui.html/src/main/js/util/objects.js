scout.objects = {

    copyProperties: function(source, dest) {
      var propertyName;
      for (propertyName in source) {
        dest[propertyName] = source[propertyName];
      }
    },

    /**
     * Counts and returns the properties of a given object.
     */
    countProperties: function(obj) {
      var count = 0;
      for (var prop in obj) {
        if (obj.hasOwnProperty(prop)) {
          count++;
        }
      }
      return count;
    }

};

