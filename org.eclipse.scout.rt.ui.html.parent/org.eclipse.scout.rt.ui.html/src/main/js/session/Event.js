scout.Event = function(type, id, data) {
  $.extend(this, data);
  this.type = type;
  this.id = id;
};
