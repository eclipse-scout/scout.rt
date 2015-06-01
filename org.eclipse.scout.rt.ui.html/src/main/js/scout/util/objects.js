scout.objects = {

  /**
   * @memberOf scout.objects
   */
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
  },

  valueCopy: function(obj) {
    // Nothing to be done for immutable things
    if (obj === undefined || obj === null || typeof obj !== 'object') {
      return obj;
    }
    var copy;
    // Arrays
    if (Array.isArray(obj)) {
      copy = [];
      for (var i = 0; i < obj.length; i++) {
        copy[i] = scout.objects.valueCopy(obj[i]);
      }
      return copy;
    }
    // All other objects
    copy = {};
    for (var prop in obj) {
      if (obj.hasOwnProperty(prop)) {
        copy[prop] = scout.objects.valueCopy(obj[prop]);
      }
    }
    return copy;
  },

  /**
   * Returns true if the given object is an object but _not_ an array.
   */
  isPlainObject: function(obj) {
    return (typeof obj === 'object' && !Array.isArray(obj));
  }
};
