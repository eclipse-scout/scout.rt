// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.ChartTableControlMatrix = function(table, session) {
  this.locale = session.locale;
  this._allData = [];
  this._allAxis = [];
  this._columns = table.columns;
  this._rows = table.rows;
  this._table = table;
};

/**
 * add data axis
 */
scout.ChartTableControlMatrix.prototype.addData = function(data, dataGroup) {
  var dataAxis = [],
    locale = this.locale;

  // collect all axis
  this._allData.push(dataAxis);

  // copy column for later access
  dataAxis.column = data;

  // data always is number
  dataAxis.format = function(n) {
    return locale.decimalFormat.format(n);
  };

  // count, sum, avg
  if (dataGroup == -1) {
    dataAxis.norm = function(f) {
      return 1;
    };
    dataAxis.group = function(array) {
      return array.length;
    };
  } else if (dataGroup == 1) {
    dataAxis.norm = function(f) {
      return parseFloat(f);
    };
    dataAxis.group = function(array) {
      return array.reduce(function(a, b) {
        return a + b;
      });
    };
  } else if (dataGroup == 2) {
    dataAxis.norm = function(f) {
      return parseFloat(f);
    };
    dataAxis.group = function(array) {
      return array.reduce(function(a, b) {
        return a + b;
      }) / array.length;
    };
  }

  return dataAxis;
};

//add x or y Axis
scout.ChartTableControlMatrix.prototype.addAxis = function(axis, axisGroup) {
  var keyAxis = [],
    locale = this.locale;

  // collect all axis
  this._allAxis.push(keyAxis);
  keyAxis.column = axis;

  // normalized string data
  keyAxis.normTable = [];

  // add a key to the axis
  keyAxis.add = function(k) {
    if (keyAxis.indexOf(k) == -1) keyAxis.push(k);
  };

  // default sorts function
  keyAxis.reorder = function() {
    keyAxis.sort();
  };

  // norm and format depends of datatype and group functionality
  if (axis.type == 'date') {
    if (axisGroup === 0) {
      keyAxis.norm = function(f) {
        if (f) {
          return new Date(f).getTime();
        }
      };
      keyAxis.format = function(n) {
        return locale.dateFormat.format(n);
      };
    } else if (axisGroup === 1) {
      keyAxis.norm = function(f) {
        if (f) {
          var b = (new Date(f).getDay() + 7 - locale.dateFormatSymbols.firstDayOfWeek) % 7;
          return b;
        }
      };
      keyAxis.format = function(n) {
        return locale.dateFormatSymbols.weekdaysOrdered[n];
      };
    } else if (axisGroup === 2) {
      keyAxis.norm = function(f) {
        if (f) {
          return new Date(f).getMonth();
        }
      };
      keyAxis.format = function(n) {
        return locale.dateFormatSymbols.months[n];
      };
    } else if (axisGroup === 3) {
      keyAxis.norm = function(f) {
        if (f) {
          return new Date(f).getFullYear();
        }
      };
      keyAxis.format = function(n) {
        return String(n);
      };
    }
  } else if (axis.type == 'number') {
    keyAxis.norm = function(f) {
      return parseFloat(f);
    };
    keyAxis.format = function(n) {
      return locale.decimalFormat.format(n);
    };
  } else {
    keyAxis.norm = function(f) {
      var index = keyAxis.normTable.indexOf(f);
      if (index == -1) {
        return keyAxis.normTable.push(f) - 1;
      } else {
        return index;
      }
    };
    keyAxis.format = function(n) {
      return keyAxis.normTable[n];
    };
    keyAxis.reorder = function() {
      //FIXME implement
    };

  }

  return keyAxis;
};

scout.ChartTableControlMatrix.prototype.calculateCube = function() {
  var cube = {},
    r, v, k, data, key;

  // collect data from table
  for (r = 0; r < this._rows.length; r++) {
    // collect keys of x, y axis from row
    var keys = [];
    for (k = 0; k < this._allAxis.length; k++) {
      key = this._table.getCellValue(this._allAxis[k].column, this._rows[r]);
      var normKey = this._allAxis[k].norm(key);

      if (normKey !== undefined) {
        this._allAxis[k].add(normKey);
        keys.push(normKey);
      }
    }
    keys = JSON.stringify(keys);

    // collect values of data axis from row
    var values = [];
    for (v = 0; v < this._allData.length; v++) {
      data = this._table.getCellValue(this._allData[v].column, this._rows[r]);
      normData = this._allData[v].norm(data);
      var normData = this._allData[v].norm(data);
      if (normData !== undefined) {
        values.push(normData);
      }
    }

    // build cube
    if (cube[keys]) {
      cube[keys].push(values);
    } else {
      cube[keys] = [values];
    }
  }

  // group values and find sum, min and max of data axis
  for (v = 0; v < this._allData.length; v++) {
    data = this._allData[v];

    data.total = 0;
    data.min = null;
    data.max = null;

    for (k in cube) {
      if (cube.hasOwnProperty(k)) {
        var allCell = cube[k],
          subCell = [];

        for (var i = 0; i < allCell.length; i++) {
          subCell.push(allCell[i][v]);
        }

        var newValue = this._allData[v].group(subCell);
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

  // find dimensions and sort for x, y axis
  for (k = 0; k < this._allAxis.length; k++) {
    key = this._allAxis[k];

    key.min = Math.min.apply(null, key);
    key.max = Math.max.apply(null, key);

    key.reorder();
  }

  // acces function used by chart
  cube.getValue = function(keys) {
    keys = JSON.stringify(keys);

    if (cube.hasOwnProperty(keys)) {
      return cube[keys];
    } else {
      return null;
    }
  };

  return cube;
};

scout.ChartTableControlMatrix.prototype.columnCount = function() {
  var colCount = [];

  for (var c = 0; c < this._columns.length; c++) {
    var column = this._columns[c];
    colCount.push([column, []]);

    for (var r = 0; r < this._rows.length; r++) {
      var v = this._table.getCellValue(column, this._rows[r]);
      if (colCount[c][1].indexOf(v) == -1) colCount[c][1].push(v);
    }

    colCount[c][1] = colCount[c][1].length;
  }
  return colCount;
};
