scout.CalendarLayout = function(calendar) {
  scout.CalendarLayout.parent.call(this);
  this.calendar = calendar;
};
scout.inherits(scout.CalendarLayout, scout.AbstractLayout);

scout.CalendarLayout.prototype.layout = function($container) {
  var height = 0;

  height += $container.cssMarginTop() + $container.cssMarginBottom();
  $container.css('height', 'calc(100% - '+ height + 'px)');
};

scout.CalendarLayout.prototype.preferredLayoutSize = function($comp) {
  return scout.graphics.getSize($comp);
};
