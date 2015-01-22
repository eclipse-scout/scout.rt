scout.ActivityMapLayout = function(activityMap) {
  scout.ActivityMapLayout.parent.call(this);
  this.activityMap = activityMap;
};
scout.inherits(scout.ActivityMapLayout, scout.AbstractLayout);

scout.ActivityMapLayout.prototype.layout = function($container) {
  var $data = this.activityMap.$data;
  var height = 0;

  height += $data.cssMarginTop() + $data.cssMarginBottom();
  $data.css('height', 'calc(100% - '+ height + 'px)');

  scout.scrollbars.update(this.activityMap._$scrollable);
};

scout.ActivityMapLayout.prototype.preferredLayoutSize = function($comp) {
  return scout.graphics.getSize($comp);
};
