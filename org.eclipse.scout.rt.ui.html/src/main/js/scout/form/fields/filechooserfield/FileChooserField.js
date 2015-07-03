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
      .on('dragenter', this._onDragEnterOrOver.bind(this))
      .on('dragover', this._onDragEnterOrOver.bind(this))
      .on('drop', this._onDrop.bind(this))
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

scout.FileChooserField.prototype._onDragEnterOrOver = function(event) {
  scout.dragAndDrop.verifyDataTransferTypesScoutTypes(event, scout.dragAndDrop.SCOUT_TYPES.FILE_TRANSFER);
};

scout.FileChooserField.prototype._onDrop = function(event) {
  if(scout.dragAndDrop.dataTransferTypesContainsScoutTypes(event.originalEvent.dataTransfer, scout.dragAndDrop.SCOUT_TYPES.FILE_TRANSFER)) {
    event.stopPropagation();
    event.preventDefault();

    var files = event.originalEvent.dataTransfer.files;
    if (files.length >= 1) {
      this.session.uploadFiles(this, [files[0]]);
    }
  }
};

scout.FileChooserField.prototype._onClick = function(event) {
};

scout.FileChooserField.prototype._onIconClick = function(event) {
  scout.FileChooserField.parent.prototype._onIconClick.call(this, event);
  this.session.send(this.id, 'chooseFile');
};
