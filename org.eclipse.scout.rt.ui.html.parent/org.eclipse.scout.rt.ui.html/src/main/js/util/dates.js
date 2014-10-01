scout.dates = {
  shift: function(date, years, months, days) {
    return new Date(date.getFullYear() + years, date.getMonth() + months, date.getDate()  + days);
  }
};
