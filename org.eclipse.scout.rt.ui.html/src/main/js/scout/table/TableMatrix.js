/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.TableMatrix = function(table, session) {
  this.session = session;
  this.locale = session.locale;
  this._allData = [];
  this._allAxis = [];
  this._columns = table.columns;
  this._rows = table.rows;
  this._table = table;
};

scout.TableMatrix.DateGroup = {
  NONE: 0,
  YEAR: 1,
  MONTH: 2,
  WEEKDAY: 3
};

scout.TableMatrix.NumberGroup = {
  COUNT: -1,
  SUM: 1,
  AVG: 2
};

/**
 * add data axis
 */
scout.TableMatrix.prototype.addData = function(data, dataGroup) {
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
  if (dataGroup === scout.TableMatrix.NumberGroup.COUNT) {
    dataAxis.norm = function(f) {
      return 1;
    };
    dataAxis.group = function(array) {
      return array.length;
    };
  } else if (dataGroup === scout.TableMatrix.NumberGroup.SUM) {
    dataAxis.norm = function(f) {
      if (isNaN(f) || f === null || f === '') {
        return null;
      } else {
        return parseFloat(f);
      }
    };
    dataAxis.group = function(array) {
      return array.reduce(function(a, b) {
        return a + b;
      });
    };
  } else if (dataGroup === scout.TableMatrix.NumberGroup.AVG) {
    dataAxis.norm = function(f) {
      if (isNaN(f) || f === null || f === '') {
        return null;
      } else {
        return parseFloat(f);
      }
    };
    dataAxis.group = function(array) {
      var sum = array.reduce(function(a, b) {
          return a + b;
        }),
        count = array.reduce(function(a, b) {
          return (b === null ? a : a + 1);
        }, 0);

      if (count === 0) {
        return null;
      } else {
        return sum / count;
      }
    };
  }

  return dataAxis;
};

//add x or y Axis
scout.TableMatrix.prototype.addAxis = function(axis, axisGroup) {
  var keyAxis = [],
    locale = this.locale,
    session = this.session,
    getText = this.session.text.bind(this.session),
    emptyCell = getText('ui.EmptyCell');

  // collect all axis
  this._allAxis.push(keyAxis);
  keyAxis.column = axis;

  // normalized string data
  keyAxis.normTable = [];

  // add a key to the axis
  keyAxis.add = function(k) {
    if (keyAxis.indexOf(k) === -1) {
      keyAxis.push(k);
    }
  };

  // default functions
  keyAxis.reorder = function() {
    keyAxis.sort(function(a, b) {
      // make sure -empty- is at the bottom
      if (a === null) {
        return 1;
      }
      if (b === null) {
        return -1;
      }
      // sort others
      return (a - b);
    });
  };
  keyAxis.norm = function(f) {
    if (f === null || f === '') {
      return null;
    } else {
      var index = keyAxis.normTable.indexOf(f);
      if (index === -1) {
        return keyAxis.normTable.push(f) - 1;
      } else {
        return index;
      }
    }
  };
  keyAxis.format = function(n) {
    if (n === null) {
      return emptyCell;
    } else {
      return keyAxis.normTable[n];
    }
  };

  // norm and format depends of datatype and group functionality
  if (axis instanceof scout.DateColumn) {
    if (axisGroup === scout.TableMatrix.DateGroup.NONE) {
      keyAxis.norm = function(f) {
        if (f === null || f === '') {
          return null;
        } else {
          return f.getTime();
        }
      };
      keyAxis.format = function(n) {
        if (n === null) {
          return null;
        } else {
          var format = axis.format;
          if (format) {
            format = new scout.DateFormat(locale, format);
          } else {
            format = locale.dateFormat;
          }
          return format.format(new Date(n));
        }
      };
    } else if (axisGroup === scout.TableMatrix.DateGroup.YEAR) {
      keyAxis.norm = function(f) {
        if (f === null || f === '') {
          return null;
        } else {
          return f.getFullYear();
        }
      };
      keyAxis.format = function(n) {
        if (n === null) {
          return emptyCell;
        } else {
          return String(n);
        }
      };
    } else if (axisGroup === scout.TableMatrix.DateGroup.MONTH) {
      keyAxis.norm = function(f) {
        if (f === null || f === '') {
          return null;
        } else {
          return f.getMonth();
        }
      };
      keyAxis.format = function(n) {
        if (n === null) {
          return emptyCell;
        } else {
          return locale.dateFormatSymbols.months[n];
        }
      };
    } else if (axisGroup === scout.TableMatrix.DateGroup.WEEKDAY) {
      keyAxis.norm = function(f) {
        if (f === null || f === '') {
          return null;
        } else {
          var b = (f.getDay() + 7 - locale.dateFormatSymbols.firstDayOfWeek) % 7;
          return b;
        }
      };
      keyAxis.format = function(n) {
        if (n === null) {
          return emptyCell;
        } else {
          return locale.dateFormatSymbols.weekdaysOrdered[n];
        }
      };
    }
  } else if (axis instanceof scout.NumberColumn) {
    keyAxis.norm = function(f) {
      if (isNaN(f) || f === null || f === '') {
        return null;
      } else {
        return parseFloat(f);
      }
    };
    keyAxis.format = function(n) {
      if (isNaN(n) || n === null) {
        return emptyCell;
      } else {
        return axis.decimalFormat.format(n);
      }
    };
  } else if (axis instanceof scout.BooleanColumn) {
    keyAxis.norm = function(f) {
      if (!f) {
        return 0;
      } else {
        return 1;
      }
    };
    keyAxis.format = function(n) {
      if (n === 0) {
        return getText('ui.BooleanColumnGroupingFalse');
      } else {
        return getText('ui.BooleanColumnGroupingTrue');
      }
    };
  } else if (axis instanceof scout.IconColumn) {
    keyAxis.textIsIcon = true;
  } else {
    keyAxis.reorder = function() {
      var comparator = scout.comparators.TEXT;
      comparator.install(session);

      keyAxis.sort(function(a, b) {
        // make sure -empty- is at the bottom
        if (a === null) {
          return 1;
        }
        if (b === null) {
          return -1;
        }
        // sort others
        return comparator.compare(keyAxis.format(a), keyAxis.format(b));
      });
    };
  }

  return keyAxis;
};

/**
 * @returns a cube containing the results
 */
scout.TableMatrix.prototype.calculate = function() {
  var cube = {},
    r, v, k, data, key, normData, normKey;

  // collect data from table
  for (r = 0; r < this._rows.length; r++) {
    var row = this._rows[r];
    // collect keys of x, y axis from row
    var keys = [];
    for (k = 0; k < this._allAxis.length; k++) {
      var column = this._allAxis[k].column;
      key = column.cellValueForGrouping(row);
      normKey = this._allAxis[k].norm(key);

      if (normKey !== undefined) {
        this._allAxis[k].add(normKey);
        keys.push(normKey);
      }
    }
    keys = JSON.stringify(keys);

    // collect values of data axis from row
    var values = [];
    for (v = 0; v < this._allData.length; v++) {
      data = this._table.cellValue(this._allData[v].column, row);
      normData = this._allData[v].norm(data);
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

        if (newValue === null) {
          continue;
        }

        if (newValue < data.min || data.min === null) {
          data.min = newValue;
        }
        if (newValue > data.max || data.min === null) {
          data.max = newValue;
        }
      }
    }

    //To calculate correct y axis scale data.max must not be 0. If data.max===0-> log(data.max)=-infinity
    if(scout.nvl(data.max, 0)===0){
      data.max = 0.1;
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

    // null value should be handeld as first value (in charts)
    if (key.indexOf(null) > -1) {
      key.max = key.max + 1;
    }

    key.reorder();
  }

  // access function used by chart
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

scout.TableMatrix.prototype.columnCount = function() {
  var colCount = [];

  var count = 0;
  for (var c = 0; c < this._columns.length; c++) {
    var column = this._columns[c];
    if (column instanceof scout.NumberColumn){
      continue;
    }
    colCount.push([column, []]);

    for (var r = 0; r < this._rows.length; r++) {
      var row = this._rows[r];
      var v = this._table.cellValue(column, row);
      if (colCount[count][1].indexOf(v) === -1) {
        colCount[count][1].push(v);
      }
    }

    colCount[count][1] = colCount[count][1].length;
    count++;
  }
  return colCount;
};

scout.TableMatrix.prototype.isEmpty = function() {
  return this._rows.length === 0 || this._columns.length === 0;
};
