/**
 *
 * Converts milliseconds to string using java format pattern.<br/>
 * It does not consider timezones.
 *
 * @see http://docs.oracle.com/javase/6/docs/api/java/text/SimpleDateFormat.html
 */
/*jshint sub:true*/
Scout.DateFormat = function(scout, pattern) {
  this._symbols = scout.locale.dateFormatSymbols;
  this.formatFunc = [];
  this.pattern = pattern;

  patterLibrary = {};

  var that = this;
  patterLibrary['year'] = [{
    term: 'yyyy',
    func: function(date) {
      return String(date.getFullYear());
    }
  }, {
    term: 'yy',
    func: function(date) {
      return String(date.getFullYear()).slice(2);
    }
  }];

  patterLibrary['month'] = [{
    term: 'MMMM',
    func: function(date) {
      return that._symbols.months[date.getMonth()];
    }
  }, {
    term: 'MMM',
    func: function(date) {
      return that._symbols.monthsShort[date.getMonth()];
    }
  }, {
    term: 'MM',
    func: function(date) {
      return _padding(date.getMonth() + 1);
    }
  }, {
    term: 'M',
    func: function(date) {
      return date.getMonth() + 1;
    }
  }];

  patterLibrary['week in year'] = [{
    term: 'ww',
    func: function(date) {
      return _padding(_weekInYear(date));
    }
  }, {
    term: 'w',
    func: function(date) {
      return _weekInYear(date);
    }
  }];

  patterLibrary['week in month'] = [{
    term: 'WW',
    func: function(date) {
      return _padding(_weekInMonth(date));
    }
  }, {
    term: 'W',
    func: function(date) {
      return _weekInMonth(date);
    }
  }];

  patterLibrary['day in month'] = [{
    term: 'dd',
    func: function(date) {
      return _padding(date.getDate());
    }
  }, {
    term: 'd',
    func: function(date) {
      return date.getDate();
    }
  }];

  patterLibrary['weekday'] = [{
    term: 'EEEE',
    func: function(date) {
      return that._symbols.weekdays[date.getDay() + 1];
    }
  }, {
    term: 'E',
    func: function(date) {
      return that._symbols.weekdaysShort[date.getDay() + 1];
    }
  }];

  patterLibrary['hour: 0 - 23'] = [{
    term: 'HH',
    func: function(date) {
      return _padding(date.getHours());
    }
  }, {
    term: 'H',
    func: function(date) {
      return date.getHours();
    }
  }];

  patterLibrary['hour: 1 - 12'] = [{
    term: 'KK',
    func: function(date) {
      return _padding((date.getHours() + 11) % 12 + 1);
    }
  }, {
    term: 'K',
    func: function(date) {
      return (date.getHours() + 11) % 12 + 1;
    }
  }];

  patterLibrary['am/pm marker'] = [{
    term: 'a',
    func: function(date) {
      return (date.getHours() < 12) ? that._symbols.am : that._symbols.pm;
    }
  }];

  patterLibrary['minutes'] = [{
    term: 'mm',
    func: function(date) {
      return _padding(date.getMinutes());
    }
  }, {
    term: 'm',
    func: function(date) {
      return date.getMinutes();
    }
  }];

  patterLibrary['seconds'] = [{
    term: 'ss',
    func: function(date) {
      return _padding(date.getSeconds());
    }
  }, {
    term: 's',
    func: function(date) {
      return date.getSeconds();
    }
  }];

  patterLibrary['milliseconds'] = [{
    term: 'SSS',
    func: function(date) {
      return ('000' + date.getMilliseconds()).slice(-3);
    }
  }, {
    term: 'S',
    func: function(date) {
      return date.getMilliseconds();
    }
  }];

  for (var l in patterLibrary) {
    for (var p = 0; p < patterLibrary[l].length; p += 1) {
      var test = patterLibrary[l][p];
      if (pattern.indexOf(test.term) > -1) {
        var closure = function(term, func) {
          return function(string, date) {
            return string.replace(term, func(date));
          };
        }(test.term, test.func);
        this.formatFunc.push(closure);
        break;
      }
    }
  }

  function _padding(number) {
    return (number <= 9 ? '0' + number : number);
  }

  function _weekInYear(date) {
    var onejan = new Date(date.getFullYear(), 0, 1);
    return Math.ceil((((date - onejan) / 86400000) + onejan.getDay() + 1) / 7);
  }

  function _weekInMonth(date) {
    var onemon = new Date(date.getFullYear(), date.getMonth(), 1);
    return Math.ceil((((date - onemon) / 86400000) + onemon.getDay() + 1) / 7);
  }
};

Scout.DateFormat.prototype.format = function format(time) {
  var ret = this.pattern,
    date = new Date(time);

  for (f = this.formatFunc.length - 1; f >= 0; f--) {
    ret = this.formatFunc[f](ret, date);
  }

  return ret;
};
