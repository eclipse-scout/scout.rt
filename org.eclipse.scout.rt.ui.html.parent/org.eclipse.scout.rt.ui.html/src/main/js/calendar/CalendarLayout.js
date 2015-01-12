scout.CalendarLayout = function(calendar) {
  scout.CalendarLayout.parent.call(this);
  this.calendar = calendar;
  this.invalidateOnResize = false;
};
scout.inherits(scout.CalendarLayout, scout.AbstractLayout);

scout.CalendarLayout.prototype.layout = function($container) {
  var $data = this.calendar.$data;
  var height = 0;

  height += $data.cssMarginTop() + $data.cssMarginBottom();
  $data.css('height', 'calc(100% - '+ height + 'px)');
};

scout.CalendarLayout.prototype.preferredLayoutSize = function($comp) {
  return scout.graphics.getSize($comp);
};
