scout.FileChooserField = function() {
  scout.FileChooserField.parent.call(this);
};
scout.inherits(scout.FileChooserField, scout.ValueField);

scout.FileChooserField.prototype._render = function($parent) {
  this.addContainer($parent, 'file-chooser-field');
  this.addLabel();
  this.addMandatoryIndicator();

  this.addField(scout.fields.new$TextField()
      .on('blur', this._onFieldBlur.bind(this))
  );

  this.addIcon();
  this.addStatus();
};

scout.FileChooserField.prototype._remove = function() {
  scout.FileChooserField.parent.prototype._remove.call(this);
  if (this.$fileInputField) {
    this.$fileInputField.remove();
    this.$fileInputField = null;
  }
};

scout.FileChooserField.prototype._onClick = function(event) {
};

scout.FileChooserField.prototype._onIconClick = function(event) {
  scout.FileChooserField.parent.prototype._onIconClick.call(this, event);
  this.session.send(this.id, 'chooseFile');
};
