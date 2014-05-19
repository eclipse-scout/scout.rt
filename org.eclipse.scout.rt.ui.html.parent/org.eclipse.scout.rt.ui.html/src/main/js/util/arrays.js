scout.arrays = {
  create: function(args) {
    if (!Array.isArray(args)) {
      args = Array.prototype.slice.call(arguments);
    }
    var length = args[0] || 0;
    var array = new Array(length);

    if (args.length > 1) {
      var lengths = args.slice(1);
      for (var j = 0; j < array.length; j++) {
        array[j] = scout.arrays.create(lengths);
      }
    }

    return array;
  },
  remove: function(arr, element) {
    var index = arr.indexOf(element);
    if (index >= 0) {
      arr.splice(index, 1);
    }
  },
  containsAll: function(arr, arr2) {
    for (var i = 0; i < arr2.length; i++) {
      if (arr.indexOf(arr2[i]) < 0) {
        return false;
      }
    }
    return true;
  },
  equalsIgnoreOrder: function(arr, arr2) {
    if (arr === arr2) {
      return true;
    } else if (!arr || !arr2) {
      return false;
    } else if (arr.length != arr2.length) {
      return false;
    }
    return scout.arrays.containsAll(arr, arr2);
  }

};
