scout.dates = {
  shift: function(date, years, months, days) {
    var newDate = new Date(date.toISOString());
    years = years !== undefined ? years : 0;
    if (years) {
      newDate.setUTCFullYear(date.getUTCFullYear() + years);
      if (scout.dates.compareMonths(newDate, date) !== years * 12) {
        // Set to last day in month
        // The reason: 2016-02-29 + 1 year -> 2017-03-01 instead of 2017-02-28
        newDate.setUTCDate(0);
      }
    }
    if (months) {
      newDate.setUTCMonth(date.getUTCMonth() + months);
      if (scout.dates.compareMonths(newDate, date) !== months + years * 12) {
        // Set to last day in month
        // The reason: 2010-10-31 + 1 month -> 2010-12-01 instead of 2010-11-30
        newDate.setUTCDate(0);
      }
    }
    if (days) {
      newDate.setUTCDate(date.getUTCDate() + days);
    }
    return newDate;
  },
  isSameDay: function(date, date2) {
    return date.getUTCFullYear() === date2.getUTCFullYear() &&
      date.getUTCMonth() === date2.getUTCMonth() &&
      date.getUTCDate() === date2.getUTCDate();
  },
  /**
   * @return the difference of the two dates in number of months.
   */
  compareMonths: function(date1, date2) {
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
};
