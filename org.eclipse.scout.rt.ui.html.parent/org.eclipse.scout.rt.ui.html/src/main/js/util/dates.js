scout.dates = {
  shift: function(date, years, months, days) {
    return new Date(date.getFullYear() + years, date.getMonth() + months, date.getDate()  + days);
  },
  //FIXME CGU unit test this
  /**
   * @return the difference of the two dates in number of months.
   */
  compareYearAndMonth: function(date1, date2) {
    var d1Month = date1.getMonth(),
      d2Month = date2.getMonth(),
      d1Year = date1.getFullYear(),
      d2Year = date2.getFullYear();

    if (d1Year === d2Year) {
      if (d1Month === d2Month) {
        return 0;
      }
      return  d1Month - d2Month;
    }
    return (d1Year - d2Year) * 12;
  }
};
