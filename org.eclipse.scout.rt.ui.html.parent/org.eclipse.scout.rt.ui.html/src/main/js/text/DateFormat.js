/**
 *
 * Provides formatting of dates using java format pattern.<br/>
 * It does not consider timezones.
 * <p>
 * locale.dateFormatSymbols contains:
 * <ul>
 * <li>weekdays start with Sunday (starts at 0 and not 1 as it does in java)</li>
 * <li>weekdaysShort start with Sunday (starts at 0 and not 1 as it does in java)</li>
 * <li>months start with January</li>
 * <li>monthsShort start with January<7li>
 * <li>am</li>
 * <li>pm</li>
 *</ul>
 *
 * @see http://docs.oracle.com/javase/6/docs/api/java/text/SimpleDateFormat.html
 */
scout.DateFormat = function(locale, pattern) {
  /*jshint sub:true*/
  if (!pattern) {
    pattern = locale.dateFormatPatternDefault;
  }

  this.symbols = locale.dateFormatSymbols;
  this.symbols.firstDayOfWeek = 1; // monday //FIXME deliver from server
  this.symbols.weekdaysOrdered = scout.DateFormat.orderWeekdays(this.symbols.weekdays, this.symbols.firstDayOfWeek);
  this.symbols.weekdaysShortOrdered = scout.DateFormat.orderWeekdays(this.symbols.weekdaysShort, this.symbols.firstDayOfWeek);
  this.formatFunc = [];
  this.pattern = pattern;

  var patternLibrary = {};

  var that = this;
  patternLibrary['year'] = [{
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

  patternLibrary['month'] = [{
    term: 'MMMM',
    func: function(date) {
      return that.symbols.months[date.getMonth()];
    }
  }, {
    term: 'MMM',
    func: function(date) {
      return that.symbols.monthsShort[date.getMonth()];
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

  patternLibrary['week in year'] = [{
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

  patternLibrary['week in month'] = [{
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

  patternLibrary['day in month'] = [{
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

  patternLibrary['weekday'] = [{
    term: 'EEEE',
    func: function(date) {
      return that.symbols.weekdays[date.getDay()];
    }
  }, {
    term: 'E',
    func: function(date) {
      return that.symbols.weekdaysShort[date.getDay()];
    }
  }];

  patternLibrary['hour: 0 - 23'] = [{
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

  patternLibrary['hour: 1 - 12'] = [{
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

  patternLibrary['am/pm marker'] = [{
    term: 'a',
    func: function(date) {
      return (date.getHours() < 12) ? that.symbols.am : that.symbols.pm;
    }
  }];

  patternLibrary['minutes'] = [{
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

  patternLibrary['seconds'] = [{
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

  patternLibrary['milliseconds'] = [{
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

  var createHandler = function(term, func) {
    return function(string, date) {
      return string.replace(term, func(date));
    };
  };

  for (var l in patternLibrary) {
    for (var p = 0; p < patternLibrary[l].length; p += 1) {
      var test = patternLibrary[l][p];
      if (pattern.indexOf(test.term) > -1) {
        this.formatFunc.push(createHandler(test.term, test.func));
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

scout.DateFormat.prototype.format = function(date) {
  var ret = this.pattern;

  for (var f = this.formatFunc.length - 1; f >= 0; f--) {
    ret = this.formatFunc[f](ret, date);
  }

  return ret;
};

scout.DateFormat.prototype.analyze = function(text, asNumber) {
  var result = {};
  var sep = this.pattern.replace('dd', '').replace('MM', '').replace('yyyy', '')[0];
  var pattern = this.pattern.split(sep);
  text = text.split(sep);

  result.day = text[pattern.indexOf('dd')];
  result.month = text[pattern.indexOf('MM')];
  result.year = text[pattern.indexOf('yyyy')];

  if (asNumber) {
    result.day = parseInt(result.day, 10);
    result.month = parseInt(result.month, 10);
    result.year = parseInt(result.year, 10);
  }

  return result;
};


scout.DateFormat.prototype.parse = function (text) {
  if (!text) {
    return;
  }

  var dateInfo = this.analyze(text, true);
  if (isNaN(dateInfo.year) || isNaN(dateInfo.year) || isNaN(dateInfo.day)) {
    return null;
  }

  var year = dateInfo.year;
  if (year < 100) {
    year += 2000;
  }

  return new Date(year, dateInfo.month - 1, dateInfo.day);
};

scout.DateFormat.orderWeekdays = function(weekdays, firstDayOfWeek) {
  var weekdaysOrdered = [];
  for (var i=0; i < 7; i++) {
    weekdaysOrdered[i] = weekdays[(i + firstDayOfWeek) % 7];
  }
  return weekdaysOrdered;
};
