scout.LabelField = function() {
  scout.LabelField.parent.call(this);
};
scout.inherits(scout.LabelField, scout.ValueField);

scout.LabelField.prototype._render = function($parent) {
  this.addContainer($parent, 'label-field');
  this.addLabel();
  this.addField($('<div>'));
  this.addStatus();
};

scout.LabelField.prototype._renderProperties = function() {
  scout.LabelField.parent.prototype._renderProperties.call(this);
  this._renderWrapText(this.wrapText);
  //FIXME render selectable
};

/**
 * @override
 */
scout.LabelField.prototype._renderDisplayText = function() {
  var displayText = this.displayText || '';
  if (this.htmlEnabled) {
    this.$field.html(displayText);
  } else {
    this.$field.html(scout.strings.nl2br(displayText));
  }
};

scout.LabelField.prototype._renderWrapText = function() {
  this.$field.toggleClass('white-space-nowrap', !this.wrapText);
};

scout.LabelField.prototype._renderGridData = function() {
  this.updateInnerAlignment({
    useHorizontalAlignment: true
  });
};
