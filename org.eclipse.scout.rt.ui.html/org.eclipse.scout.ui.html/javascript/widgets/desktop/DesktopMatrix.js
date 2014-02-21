// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

Scout.DesktopMatrix = function (columns, table) {
  var allData = [],
    allAxis = [];

  // public functions
  this.addData = addData;
  this.addAxis = addAxis;
  this.calculateCube = calculateCube;

  // return (empty) matrix
  return this;

  function addData (data, dataGroup) {
    var dataAxis = [];

    allData.push(dataAxis);
    dataAxis.column = data;

    dataAxis.format = function (n) {return $.numberToString(n, 0); };

    if (dataGroup == -1) {
      dataAxis.norm = function (f) {return 1; };
      dataAxis.group = function (array) {return array.length; };
    } else if (dataGroup == 1) {
      dataAxis.norm = function (f) {return parseFloat(f); };
      dataAxis.group = function (array) {return array.reduce(function(a, b) {return a + b; }); };
    } else if (dataGroup == 2) {
      dataAxis.norm = function (f) {return parseFloat(f); };
      dataAxis.group = function (array) {return array.reduce(function(a, b) {return a + b; }) / array.length; };
    }

    return dataAxis;
  }

  function addAxis (axis, axisGroup) {
    var keyAxis = [];

    allAxis.push(keyAxis);
    keyAxis.column = axis;
    keyAxis.normTable = [];

    keyAxis.add = function (k) { if (keyAxis.indexOf(k) == -1) keyAxis.push(k); };
    keyAxis.reorder = function () { keyAxis.sort(); };

    if (columns[axis].type == 'date') {
      if (axisGroup === 0) {
        keyAxis.norm = function (f) {return $.stringToDate(f).getTime(); };
        keyAxis.format = function (n) {return $.dateToString(new Date(n)); };
      } else if (axisGroup === 1) {
        keyAxis.norm = function (f) {return ($.stringToDate(f).getDay() + 6) % 7; };
        keyAxis.format = function (n) {return $.WEEKDAY_LONG[n]; };
      } else if (axisGroup === 2) {
        keyAxis.norm = function (f) {return $.stringToDate(f).getMonth(); };
        keyAxis.format = function (n) {return $.MONTH_LONG[n]; };
      } else if (axisGroup === 3) {
        keyAxis.norm = function (f) {return $.stringToDate(f).getFullYear(); };
        keyAxis.format = function (n) {return String(n); };
      }
    } else if (columns[axis].type == 'int'){
      keyAxis.norm = function (f) {return parseInt(f, 10); };
      keyAxis.format = function (n) {return $.numberToString(n, 0); };
    } else if (columns[axis].type == 'float'){
      keyAxis.norm = function (f) {return parseFloat(f); };
      keyAxis.format = function (n) {return $.numberToString(n, 0); };
    } else {
      keyAxis.norm = function (f) {var index =  keyAxis.normTable.indexOf(f);
                    if (index == -1) {
                      return  keyAxis.normTable.push(f) - 1;
                    } else {
                      return index;
                    } };
      keyAxis.format = function (n) { return keyAxis.normTable[n]; };
      keyAxis.reorder = function () { log('TODO');};

    }

    return keyAxis;
  }

  function calculateCube () {
    var cube = {},
      r, v, k, data;

    // collect data from table
    for (r = 0; r < table.length; r++) {
      var keys = [];
      for (k = 0; k < allAxis.length; k++) {
        key = table[r][allAxis[k].column];
        normKey = allAxis[k].norm(key);

        allAxis[k].add(normKey);
        keys.push(normKey);
      }
      keys = JSON.stringify(keys);

      var values = [];
      for (v = 0; v < allData.length; v++) {
        data = table[r][allData[v].column];
        normData = allData[v].norm(data);

        values.push(normData);
      }

      if (cube[keys]) {
        cube[keys].push(values);
      } else {
        cube[keys] = [values];
      }
    }

    // group data
    for (v = 0; v < allData.length; v++) {
      data = allData[v];

      data.total = 0;
      data.min = null;
      data.max = null;

      for (k in cube) {
        if (cube.hasOwnProperty(k)) {
          var allCell = cube[k],
            subCell = [];

          for (i = 0; i < allCell.length; i++) {
            subCell.push(allCell[i][v]);
          }

          var newValue = allData[v].group(subCell);
          cube[k][v] = newValue;
          data.total += newValue;

          if (newValue < data.min || data.min === null) data.min = newValue;
          if (newValue > data.max || data.min === null) data.max = newValue;
        }
      }

      var f = Math.ceil(Math.log(data.max) / Math.LN10) - 1;

      data.max = Math.ceil(data.max / Math.pow(10, f)) * Math.pow(10, f);
      data.max = Math.ceil(data.max / 4) * 4;
    }

    // find dimensions and sort
    for (k = 0; k < allAxis.length; k++) {
      key = allAxis[k];

      key.min = Math.min.apply(null, key);
      key.max = Math.max.apply(null, key);

      key.reorder();
    }

    // acces function
    cube.getValue = function (keys) {
      keys = JSON.stringify(keys);

      if (cube.hasOwnProperty(keys)) {
        return cube[keys];
      } else {
        return null;
      }
    };

    return cube;
  }
};
