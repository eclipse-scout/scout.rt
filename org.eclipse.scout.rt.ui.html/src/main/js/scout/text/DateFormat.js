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
  this.symbols.weekdaysOrdered = scout.dates.orderWeekdays(this.symbols.weekdays, this.symbols.firstDayOfWeek);
  this.symbols.weekdaysShortOrdered = scout.dates.orderWeekdays(this.symbols.weekdaysShort, this.symbols.firstDayOfWeek);
  this.symbols.monthsToNumber;
  this.symbols.monthsShortToNumber;
  this._createMonthsToNumberMapping;
  this.formatFunc = [];
  this.patternDefinitions = [];
  this.parseFunc = [];
  this.pattern = pattern;
  this.timePart;
  this.datePart;

  // Local helper (constructor) functions
  var DateFormatPatternDefinition = function(term, func, parseFunc, isTimePattern, type) {
    if (Array.isArray(term)) {
      this.terms = term;
    } else {
      this.terms = [term];
    }
    this.func = func;
    this.parseFunc = parseFunc;
    this.isTimePattern = isTimePattern;
    this.patternIndex = -1;
    this.patternTextAfter = ''; //is set by DateFormat
    this.firstPatternDefinition; //is set by DateFormat
    this.selectedPattern;
    this.type = type;
  };

  DateFormatPatternDefinition.prototype.compile = function(pattern) {
    for (var i = 0; i < this.terms.length; i++) {
      var index = pattern.indexOf(this.terms[i]);
      if (index !== -1) {
        this.index = index;
        this.selectedPattern = this.terms[i];
        break;
      }
    }
  };
  DateFormatPatternDefinition.prototype.createFormatFunc = function() {
    var that = this;
    return function(string, date) {
      return string.replace(that.selectedPattern, that.func(date));
    };
  };

  DateFormatPatternDefinition.prototype.createParseFunc = function() {
    var that = this;
    return function(dateString, date) {
      if (that.firstPatternDefinition) {
        dateString = dateString.substring(that.index, dateString.length);
      }
      return that.parseFunc(dateString, date, that.patternTextAfter);
    };
  };

  DateFormatPatternDefinition.prototype.accept = function(pattern) {
    if (!pattern) {
      return false;
    }
    // Check if one of the terms matches
    for (var i = 0; i < this.terms.length; i++) {
      if (pattern.indexOf(this.terms[i]) !== -1) {
        return true;
      }
    }
    return false;
  };
  var that = this;

  // Build the pattern library in the following order:
  // - Sort pattern groups by 'time span', from large (year) to small (milliseconds).
  // - Inside a pattern group, sort the definitions by 'term length', from long (e.g. MMMM)
  //   to short (e.g. M)
  // This order ensures, that the algorithm can pick the best matching pattern function for
  // each term in the pattern.
  var patternLibrary = [
    // Year
    [
      new DateFormatPatternDefinition('yyyy', function(date) {
        return String(date.getFullYear());
      }, function(dateString, date, patternTextAfter) {
        //every time 4 chars
        date.setFullYear(dateString.substr(0, 4));
        return dateString.substring(4 + this.patternTextAfter.length, dateString.length);
      }, false, 'year'),
      new DateFormatPatternDefinition(['yy', 'y'], function(date) {
        return String(date.getFullYear()).slice(2);
      }, function(dateString, date, patternTextAfter) {
        var shortYear = Number(dateString.substr(0, 2));
        var actualYear = Number(String(new Date().getFullYear()).slice(2));
        var rangeMin = actualYear - 50 < 0 ? actualYear + 50 : actualYear - 50;
        var yearprefix = shortYear > rangeMin ? Number(String(new Date().getFullYear()).substr(0, 2)) - 1 : String(new Date().getFullYear()).substr(0, 2);

        date.setFullYear(yearprefix + dateString.substr(0, 2));
        return dateString.substring(2 + this.patternTextAfter.length, dateString.length);
      }, false, 'year')
    ],
    // Month
    [
      new DateFormatPatternDefinition('MMMM', function(date) {
        return that.symbols.months[date.getMonth()];
      }, function(dateString, date, patternTextAfter) {
        //TODO nbu
        return date.setMonth(that.symbols.monthsToNumber[dateString.toUpperCase()]);
      }, false, 'month'),
      new DateFormatPatternDefinition('MMM', function(date) {
        return that.symbols.monthsShort[date.getMonth()];
      }, function(dateString, date, patternTextAfter) {
        //TODO nbu
        return date.setMonth(that.symbols.monthsShortToNumber[dateString.toUpperCase()]);
      }, false, 'month' ),
      new DateFormatPatternDefinition('MM', function(date) {
        return scout.strings.padZeroLeft(date.getMonth() + 1, 2);
      }, function(dateString, date, patternTextAfter) {
        var datePart = dateString.substr(0, 2);
        date.setMonth(Number(datePart.indexOf(0) === 0 ? datePart.substr(1, 1) : datePart) - 1);
        return dateString.substring(2 + this.patternTextAfter.length, dateString.length);
      }, false, 'month'),
      new DateFormatPatternDefinition('M', function(date) {
        return date.getMonth() + 1;
      }, function(dateString, date, patternTextAfter) {
        //TODO nbu
        return date.setMonth(dateString - 1);
      }, false, 'month')
    ],
    // Week in year
    [
      new DateFormatPatternDefinition('ww', function(date) {
        return scout.strings.padZeroLeft(scout.dates.weekInYear(date), 2);
      }, function(dateString, date, patternTextAfter) {
        //not supported
        return date;
      }, false, 'week'),
      new DateFormatPatternDefinition('w', function(date) {
        return scout.dates.weekInYear(date);
      }, function(dateString, date, patternTextAfter) {
        //notSuported
        return date;
      }, false, 'week')
    ],
    // Day in month
    [
      new DateFormatPatternDefinition('dd', function(date) {
        return scout.strings.padZeroLeft(date.getDate(), 2);
      }, function(dateString, date, patternTextAfter) {
        var datePart = dateString.substr(0, 2);
        date.setDate(datePart.indexOf(0) === 0 ? datePart.substr(1, 1) : datePart);
        return dateString.substring(2 + this.patternTextAfter.length, dateString.length);
      }, false, 'day'),
      new DateFormatPatternDefinition('d', function(date) {
        return date.getDate();
      }, function(dateString, date, patternTextAfter) {
        //TODO nbu
        return date.setMonth(dateString);
      }, false, 'day')
    ],
    // Weekday
    [
      new DateFormatPatternDefinition('EEEE', function(date) {
        return that.symbols.weekdays[date.getDay()];
      }, function(dateString, date, patternTextAfter) {
        //not supported
        return date;
      }, false, 'weekday'),
      new DateFormatPatternDefinition(['EEE', 'EE', 'E'], function(date) {
        return that.symbols.weekdaysShort[date.getDay()];
      }, function(dateString, date, patternTextAfter) {
        //not supported
        return date;
      }, false, 'weekday')
    ],
    // Hour (24h)
    [
      new DateFormatPatternDefinition('HH', function(date) {
        return scout.strings.padZeroLeft(date.getHours(), 2);
      }, function(dateString, date, patternTextAfter) {
        var datePart = dateString.substr(0, 2);
        date.setHours(datePart.indexOf(0) === 0 ? datePart.substr(1, 1) : datePart);
        return dateString.substring(2 + this.patternTextAfter.length, dateString.length);
      }, true, 'hour'),
      new DateFormatPatternDefinition(['H', 'h'], function(date) {
        return date.getHours();
      }, function(dateString, date, patternTextAfter) {
        //TODO nbu
        return date.setHours(dateString);
      }, true, 'hour')
    ],
    // Hour (12h)
    [
      new DateFormatPatternDefinition('KK', function(date) {
        return scout.strings.padZeroLeft((date.getHours() + 11) % 12 + 1, 2);
      }, function(dateString, date, patternTextAfter) {
        var datePart = dateString.substr(0, 2);
        date.setHours(datePart.indexOf(0) === 0 ? datePart.substr(1, 1) : datePart);
        return dateString.substring(1 + this.patternTextAfter.length, dateString.length);
      }, true, 'hour'),
      new DateFormatPatternDefinition('K', function(date) {
        return (date.getHours() + 11) % 12 + 1;
      }, function(dateString, date, patternTextAfter) {
        //TODO nbu
        return date.setHours(dateString);
      }, true, 'hour')
    ],

    // AM/PM marker
    [
      new DateFormatPatternDefinition('a', function(date) {
        return (date.getHours() < 12) ? 'am' : 'pm';
      }, function(dateString, date, patternTextAfter) {
        //not supported
        return date;
      }, true, 'ampm')
    ],
    // Minute
    [
      new DateFormatPatternDefinition('mm', function(date) {
        return scout.strings.padZeroLeft(date.getMinutes(), 2);
      }, function(dateString, date, patternTextAfter) {
        var datePart = dateString.substr(0, 2);
        date.setMinutes(datePart.indexOf(0) === 0 ? datePart.substr(1, 1) : datePart);
        return dateString.substring(2 + this.patternTextAfter.length, dateString.length);
      }, true, 'minute'),
      new DateFormatPatternDefinition('m', function(date) {
        return date.getMinutes();
      }, function(dateString, date, patternTextAfter) {
        //TODO nbu
        return date.setMinutes(dateString);
      }, true, 'minute')
    ],
    // Second
    [
      new DateFormatPatternDefinition('ss', function(date) {
        return scout.strings.padZeroLeft(date.getSeconds(), 2);
      }, function(dateString, date, patternTextAfter) {
        var datePart = dateString.substr(0, 2);
        date.setSeconds(datePart.indexOf(0) === 0 ? datePart.substr(1, 1) : datePart);
        return dateString.substring(2 + this.patternTextAfter.length, dateString.length);
      }, true, 'second'),
      new DateFormatPatternDefinition('s', function(date) {
        return date.getSeconds();
      }, function(dateString, date, patternTextAfter) {
        //TODO nbu
        return date.setSeconds(dateString);
      }, true, 'second')
    ],
    // Millisecond
    [
      new DateFormatPatternDefinition('SSS', function(date) {
        return ('000' + date.getMilliseconds()).slice(-3);
      }, function(dateString, date, patternTextAfter) {
        var datePart = dateString.substr(0, 2);
        date.setMilliseconds(datePart.indexOf(0) === 0 ? datePart.indexOf(0) === 1 ? datePart.substr(2, 1) : datePart.substr(1, 1) : datePart);
        return dateString.substring(3 + this.patternTextAfter.length, dateString.length);
      }, true, 'milli'),
      new DateFormatPatternDefinition('S', function(date) {
        return date.getMilliseconds();
      }, function(dateString, date, patternTextAfter) {
        //TODO nbu
        return date.setMilliseconds(dateString);
      }, true, 'milli')
    ]
  ];

  this.compile(patternLibrary);

};

scout.DateFormat.prototype.compile = function(patternLibrary) {
  //Now for each pattern group in the library, pick the best matching definition
  // and retrieve the formatter function from it.
  var lastTimePattern,
    lastDatePattern,
    firstTimePattern,
    firstDatePattern;
  for (var i = 0; i < patternLibrary.length; i++) {
    var patternGroup = patternLibrary[i];
    for (var j = 0; j < patternGroup.length; j++) {
      var patternDefinition = patternGroup[j];
      if (patternDefinition.accept(this.pattern)) {
        patternDefinition.compile(this.pattern);
        this.formatFunc.push(patternDefinition.createFormatFunc());
        this.patternDefinitions[patternDefinition.index] = patternDefinition;
        if (patternDefinition.isTimePattern) {
          lastTimePattern = lastTimePattern ? lastTimePattern.index > patternDefinition.index ? lastTimePattern : patternDefinition : patternDefinition;
          firstTimePattern = firstTimePattern ? firstTimePattern.index > patternDefinition.index ? patternDefinition : firstTimePattern : patternDefinition;
        } else {
          lastDatePattern = lastDatePattern ? lastDatePattern.index > patternDefinition.index ? lastDatePattern : patternDefinition : patternDefinition;
          firstDatePattern = firstDatePattern ? firstDatePattern.index > patternDefinition.index ? patternDefinition : firstDatePattern : patternDefinition;
        }
        break;
      }
    }
  }
  //create date and time part;
  if (firstTimePattern && lastTimePattern) {
    this.timePart = this.pattern.substring(firstTimePattern.index, lastTimePattern.index + lastTimePattern.selectedPattern.length).trim();
  }
  if (firstDatePattern && lastDatePattern) {
    this.datePart = this.pattern.substring(firstDatePattern.index, lastDatePattern.index + lastDatePattern.selectedPattern.length).trim();
  }

  //update Patterndefinitions and create parseFunc in correct order.
  var firstDefinition = true;
  var pattern = this.pattern;
  var lastPatternDef;
  for (i = 0; i < this.patternDefinitions.length; i++) {
    var patternDef = this.patternDefinitions[i];
    if (patternDef) {
      this.parseFunc.push(patternDef.createParseFunc());
      if (firstDefinition) {
        patternDef.firstPatternDefinition = firstDefinition;
        firstDefinition = false;
        lastPatternDef = this.patternDefinitions[i];
        continue;
      }
      if (this.patternDefinitions.length === i + 1) {
        //last element
        var patternAfter = pattern.substring(pattern.indexOf(patternDef.selectedPattern) + patternDef.selectedPattern.length, pattern.length);
        patternDef.patternTextAfter = patternAfter ? patternAfter : '';
      }
      var patternLastAfter = pattern.substring(pattern.indexOf(lastPatternDef.selectedPattern) + lastPatternDef.selectedPattern.length, pattern.indexOf(patternDef.selectedPattern));
      lastPatternDef.patternTextAfter = patternLastAfter ? patternLastAfter : '';
      lastPatternDef = this.patternDefinitions[i];
    }
  }
};

scout.DateFormat.prototype._createMonthsToNumberMapping = function(date) {
  var monthMap = {}, monthShortMap = {};

  for (var i = 0; i < 12; i++) {
    monthMap.set(this.symbols.months[i].toUpperCase(), i);
    monthShortMap.se(this.symbols.monthsShort[i].toUpperCase(), i);
  }

  this.symbols.monthsToNumber = monthMap;
  this.symbols.monthsShortToNumber = monthShortMap;
};

scout.DateFormat.prototype.format = function(date) {
  var ret = this.pattern;
  // Apply all formatter functions for this DateFormat to the pattern to replace the
  // different terms with the corresponding value from the given date.
  for (var i = 0; i < this.formatFunc.length; i++) {
    ret = this.formatFunc[i](ret, date);
  }
  return ret;
};

scout.DateFormat.prototype.patternDefinitionByType = function(type) {
    for(var i = 0; i<this.patternDefinitions.length; i++){
      var patternDefinition =  this.patternDefinitions[i];
      if(patternDefinition && patternDefinition.type===type){
        return patternDefinition;
      }
    }
};

scout.DateFormat.prototype.analyze = function(text, asNumber) {
  var result = {};
  if (text) {
    var patternDefinition, parsedElements = false, firstPatternDefinition;

    for(var i = 0; i<this.patternDefinitions.length; i++){
      patternDefinition = this.patternDefinitions[i];
      if(patternDefinition){
        if(!firstPatternDefinition){
          firstPatternDefinition = patternDefinition;
        }
        //not parsable construct. only last element should have a empty separator
        if(i<this.patternDefinitions.length-1 && patternDefinition.patternTextAfter===''){
          return {};
        }
        var partIndex = patternDefinition.patternTextAfter === ''? -1 : text.indexOf(patternDefinition.patternTextAfter);
        if(text.length>0){
          var endIndex = partIndex>-1? partIndex : text.length;
          var part = text.substring(0,endIndex);
          if(patternDefinition.type ==='day'){
            result.day = part;
          } else if(patternDefinition.type ==='month'){
            result.month = part;
          } else if(patternDefinition.type ==='year'){
            result.year = part;
          }
          text = text.substring(endIndex+1,text.length);
        }
      }
    }

    if (asNumber) {
      result.day = parseInt(result.day, 10);
      result.month = parseInt(result.month, 10);
      result.year = parseInt(result.year, 10);
    }
  }
  return result;
};

scout.DateFormat.prototype.parse = function(text) {
  if (!text) {
    return undefined;
  }

  // FIXME NBU Fix this!
  //  var dateInfo = this.analyze(text, true);
  //  if (isNaN(dateInfo.year) || isNaN(dateInfo.month) || isNaN(dateInfo.day)) {
  //    return undefined;
  //  }
  //
  var date = new Date(0);
  var ret = text;
  for (var i = 0; i < this.parseFunc.length; i++) {
    ret = this.parseFunc[i](ret, date);
  }
  return date;
};
