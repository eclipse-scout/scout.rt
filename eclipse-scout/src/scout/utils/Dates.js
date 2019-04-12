import Scout from '../Scout';

export default class Dates {

  static orderWeekdays(weekdays, firstDayOfWeek) {
    var weekdaysOrdered = [];
    for (var i = 0; i < 7; i++) {
      weekdaysOrdered[i] = weekdays[(i + firstDayOfWeek) % 7];
    }
    return weekdaysOrdered;
  }

  static shiftToNextDayOfType(date, day) {
    var diff = day - date.getDay();

    if (diff <= 0) {
      diff += 7;
    }
    return Dates.shift(date, 0, 0, diff);
  }

  static weekInYear(date, option) {
    if (!date) {
      return undefined;
    }
    var firstDayOfWeek = 1;
    if (typeof option === 'object') {
      // scout.DateFormat
      if (option.symbols !== undefined && option.symbols.firstDayOfWeek !== undefined) {
        firstDayOfWeek = option.symbols.firstDayOfWeek;
      }
      // scout.Locale
      else if (option.decimalFormatSymbols !== undefined && option.decimalFormatSymbols.firstDayOfWeek !== undefined) {
        firstDayOfWeek = option.decimalFormatSymbols.firstDayOfWeek;
      }
    } else if (typeof option === 'number') {
      firstDayOfWeek = option;
    }

    // Thursday of current week decides the year
    var thursday = this._thursdayOfWeek(date, firstDayOfWeek);

    // In ISO format, the week with January 4th is the first week
    var jan4 = new Date(thursday.getFullYear(), 0, 4);

    // If the date is before the beginning of the year, it belongs to the year before
    var startJan4 = this._firstDayOfWeek(jan4, firstDayOfWeek);
    if (date.getTime() < startJan4.getTime()) {
      jan4 = new Date(thursday.getFullYear() - 1, 0, 4);
    }

    // Get the Thursday of the first week, to be able to compare it to 'thursday'
    var thursdayFirstWeek = this._thursdayOfWeek(jan4, firstDayOfWeek);

    var diffInDays = (thursday.getTime() - thursdayFirstWeek.getTime()) / 86400000;

    return 1 + Math.round(diffInDays / 7);
  }

  static trunc(date, createCopy) {
    if (date) {
      if (Scout.nvl(createCopy, true)) {
        date = new Date(date.getTime());
      }
      date.setHours(0, 0, 0, 0); // clear time
    }
    return date;
  }

  static isLeapYear(year) {
    if (year === undefined || year === null) {
      return false;
    }
    var date = new Date(year, 1, 29);
    return (date.getDate() === 29);
  }

  static shift(date, years, months, days) {
    var newDate = new Date(date.getTime());
    if (years) {
      newDate.setFullYear(date.getFullYear() + years);
      if (Dates.compareMonths(newDate, date) !== years * 12) {
        // Set to last day of the previous month
        // The reason: 2016-02-29 + 1 year -> 2017-03-01 instead of 2017-02-28
        newDate.setDate(0);
      }
    }
    if (months) {
      newDate.setMonth(date.getMonth() + months);
      if (Dates.compareMonths(newDate, date) !== months + years * 12) {
        // Set to last day of the previous month
        // The reason: 2010-10-31 + 1 month -> 2010-12-01 instead of 2010-11-30
        newDate.setDate(0);
      }
    }
    if (days) {
      newDate.setDate(date.getDate() + days);
    }
    return newDate;
  }

  static compareMonths(date1, date2) {
    var d1Month = date1.getMonth(),
      d2Month = date2.getMonth(),
      d1Year = date1.getFullYear(),
      d2Year = date2.getFullYear(),
      monthDiff = d1Month - d2Month;
    if (d1Year === d2Year) {
      return monthDiff;
    }
    return (d1Year - d2Year) * 12 + monthDiff;
  }
}
