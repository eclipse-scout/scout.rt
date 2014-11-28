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

  /**
   * Ensures the given parameter is an array
   */
  ensure: function(array) {
    if (array && !Array.isArray(array)) {
      return [array];
    }
    return array;
  },

  /**
   * Creates an array with the given length and initializes each value with the given initValue.
   */
  init: function(length, initValue) {
    var array = [], i;
    for (i=0; i<length; i++) {
      array[i] = initValue;
    }
    return array;
  },

  remove: function(arr, element) {
    var index = arr.indexOf(element);
    if (index >= 0) {
      arr.splice(index, 1);
    }
  },

  removeAll: function(arr, arr2) {
    for (var i=0; i < arr2.length; i++) {
      scout.arrays.remove(arr, arr2[i]);
    }
  },

  insert: function(arr, element, index) {
    arr.splice(index, 0, element);
  },

  containsAll: function(arr, arr2) {
    for (var i = 0; i < arr2.length; i++) {
      if (arr.indexOf(arr2[i]) < 0) {
        return false;
      }
    }
    return true;
  },

  pushAll: function(arr, arr2) {
    arr.push.apply(arr, arr2);
  },

  equalsIgnoreOrder: function(arr, arr2) {
    if (arr === arr2) {
      return true;
    } else if ((!arr || arr.length === 0) && (!arr2 || arr2.length === 0)) {
      return true;
    } else if (!arr || !arr2) {
      return false;
    } else if (arr.length !== arr2.length) {
      return false;
    }
    return scout.arrays.containsAll(arr, arr2);
  },

  equals: function(arr, arr2) {
    if (arr === arr2) {
      return true;
    } else if ((!arr || arr.length === 0) && (!arr2 || arr2.length === 0)) {
      return true;
    } else if (!arr || !arr2) {
      return false;
    } else if (arr.length !== arr2.length) {
      return false;
    }

    for (var i = 0; i < arr.length; i++) {
      if(arr[i] !== arr2[i]) {
        return false;
      }
    }
    return true;
  },

  greater: function(arr, arr2) {
    var arrLength = 0,
      arr2Length = 0;
    if (arr) {
      arrLength = arr.length;
    }
    if (arr2) {
      arr2Length = arr2.length;
    }
    return arrLength > arr2Length;
  },

  eachSibling: function(arr, element, func) {
    for (var i=0; i< arr.length; i++) {
      var elementAtI = arr[i];
      if (elementAtI !== element) {
        func(elementAtI, i);
      }
    }
  }
};
