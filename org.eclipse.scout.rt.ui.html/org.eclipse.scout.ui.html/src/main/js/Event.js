Scout.Event = function (type, id, data) {
  $.extend(this, data);
  this.type_ = type;
  this.id = id;
};
