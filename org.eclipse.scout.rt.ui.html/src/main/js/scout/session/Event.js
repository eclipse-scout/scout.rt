scout.Event = function(target, type, data) {
  $.extend(this, data);
  this.target = target;
  this.type = type;
};
