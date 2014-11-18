scout.TagCloudField = function() {
  scout.TagCloudField.parent.call(this);
};
scout.inherits(scout.TagCloudField, scout.FormField);

scout.TagCloudField.prototype._renderProperties = function() {
  scout.TagCloudField.parent.prototype._renderProperties.call(this);
};

scout.TagCloudField.prototype._render = function($parent) {
  var $field = $('<div>').text('TagCloud');

  this.addContainer($parent, 'tag-cloud-field');
  this.addField($field);
  this.addLabel();
  this.addStatus();
};
