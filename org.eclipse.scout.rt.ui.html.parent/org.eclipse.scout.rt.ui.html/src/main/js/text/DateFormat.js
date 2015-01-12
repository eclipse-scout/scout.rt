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
  this.formatFunc = [];
  this.pattern = pattern;

  // Local helper (constructor) functions
  var DateFormatPatternDefinition = function(term, func) {
    if (Array.isArray(term)) {
      this.terms = term;
    }
    else {
      this.terms = [term];
    }
    this.func = func;
  };
  DateFormatPatternDefinition.prototype.createFormatFunc = function() {
    var that = this;
    return function(string, date) {
      // Find the first term that matches (we can assume that one does, because
      // accept() should have been called earlier), then replace the term by
      // the value provided by the definition's function.
      for (var i = 0; i < that.terms.length; i++) {
        if (string.indexOf(that.terms[i]) !== -1) {
          return string.replace(that.terms[i], that.func(date));
        }
      }
      return string;
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
      }),
      new DateFormatPatternDefinition(['yy', 'y'], function(date) {
        return String(date.getFullYear()).slice(2);
      })
    ],
    // Month
    [
      new DateFormatPatternDefinition('MMMM', function(date) {
        return that.symbols.months[date.getMonth()];
      }),
      new DateFormatPatternDefinition('MMM', function(date) {
        return that.symbols.monthsShort[date.getMonth()];
      }),
      new DateFormatPatternDefinition('MM', function(date) {
        return scout.strings.padZeroLeft(date.getMonth() + 1, 2);
      }),
      new DateFormatPatternDefinition('M', function(date) {
        return date.getMonth() + 1;
      })
    ],
    // Week in year
    [
      new DateFormatPatternDefinition('ww', function(date) {
        return scout.strings.padZeroLeft(scout.dates.weekInYear(date), 2);
      }),
      new DateFormatPatternDefinition('w', function(date) {
        return scout.dates.weekInYear(date);
      })
    ],
    // Day in month
    [
      new DateFormatPatternDefinition('dd', function(date) {
        return scout.strings.padZeroLeft(date.getDate(), 2);
      }),
      new DateFormatPatternDefinition('d', function(date) {
        return date.getDate();
      })
    ],
    // Weekday
    [
      new DateFormatPatternDefinition('EEEE', function(date) {
        return that.symbols.weekdays[date.getDay()];
      }),
      new DateFormatPatternDefinition(['EEE', 'EE', 'E'], function(date) {
        return that.symbols.weekdaysShort[date.getDay()];
      })
    ],
    // Hour (24h)
    [
      new DateFormatPatternDefinition('HH', function(date) {
        return scout.strings.padZeroLeft(date.getHours(), 2);
      }),
      new DateFormatPatternDefinition('H', function(date) {
        return date.getHours();
      })
    ],
    // Hour (12h)
    [
      new DateFormatPatternDefinition('KK', function(date) {
        return scout.strings.padZeroLeft((date.getHours() + 11) % 12 + 1, 2);
      }),
      new DateFormatPatternDefinition('K', function(date) {
        return (date.getHours() + 11) % 12 + 1;
      })
    ],

    // AM/PM marker
    [
      new DateFormatPatternDefinition('a', function(date) {
        return (date.getHours() < 12) ? that.symbols.am : that.symbols.pm;
      })
    ],
    // Minute
    [
      new DateFormatPatternDefinition('mm', function(date) {
        return scout.strings.padZeroLeft(date.getMinutes(), 2);
      }),
      new DateFormatPatternDefinition('m', function(date) {
        return date.getMinutes();
      })
    ],
    // Second
    [
      new DateFormatPatternDefinition('ss', function(date) {
        return scout.strings.padZeroLeft(date.getSeconds(), 2);
      }),
      new DateFormatPatternDefinition('s', function(date) {
        return date.getSeconds();
      })
    ],
    // Millisecond
    [
      new DateFormatPatternDefinition('SSS', function(date) {
        return ('000' + date.getMilliseconds()).slice(-3);
      }),
      new DateFormatPatternDefinition('S', function(date) {
        return date.getMilliseconds();
      })
    ]
  ];

  // Now for each pattern group in the library, pick the best matching definition
  // and retrieve the formatter function from it.
  for (var i = 0; i < patternLibrary.length; i++) {
    var patternGroup = patternLibrary[i];
    for (var j = 0; j < patternGroup.length; j++) {
      var patternDefinition = patternGroup[j];
      if (patternDefinition.accept(pattern)) {
        this.formatFunc.push(patternDefinition.createFormatFunc());
        break;
      }
    }
  }
};

scout.DateFormat.prototype.weekInYear = function(date) {
  if (!date) {
    return undefined;
  }

  // If the given date is not a thursday, set it to the thursday of that week
   var thursday = new Date(date.valueOf());
   if (thursday.getDay() !== 4) { // 0 = Sun, 1 = Mon, 2 = Thu, 3 = Wed, 4 = Thu, 5 = Fri, 6 = Sat
     // Calculate week day index if week would start with this.symbols.firstDayOfWeek instead of Sunday
     var normalizedWeekday = (date.getDay() + 7 - this.symbols.firstDayOfWeek) % 7;
     thursday.setDate(thursday.getDate() - normalizedWeekday + 3);
   }
   // ISO format: week #1 is the week with January 4th
   var jan4 = new Date(thursday.getFullYear(), 0, 4);

   var diffInDays = (thursday - jan4) / 86400000;
   return 1 + Math.ceil(diffInDays / 7);
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

scout.DateFormat.prototype.parse = function(text) {
  if (!text) {
    return;
  }

  var dateInfo = this.analyze(text, true);
  if (isNaN(dateInfo.year) || isNaN(dateInfo.month) || isNaN(dateInfo.day)) {
    return null;
  }

  var year = dateInfo.year;
  // TODO BSH Date | Try to move this logic to analyze()
  if (year < 100) {
    year += 2000;
  }

  var date = new Date();
  date.setFullYear(year);
  date.setMonth(dateInfo.month - 1);
  date.setDate(dateInfo.day);
  date.setHours(0, 0, 0, 0);

  return date;
};
