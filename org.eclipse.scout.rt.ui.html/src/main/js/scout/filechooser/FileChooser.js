scout.FileChooser = function() {
  scout.FileChooser.parent.call(this);
  this._files = [];
};
scout.inherits(scout.FileChooser, scout.ModelAdapter);

scout.FileChooser.prototype._render = function($parent) {
  this._$glassPane = scout.fields.new$Glasspane(this.session.uiSessionId).appendTo($parent);
  this.$container = this._$glassPane.appendDiv('file-chooser');

  var $handle = this.$container.appendDiv('drag-handle');
  this.$container.makeDraggable($handle);

  this.$container.appendDiv('closable')
    .on('click', function() {
      this._doCancel();
    }.bind(this));

  this.$fileInputField = $('<input>')
    .attr('type', 'file')
    .prop('multiple', this.multiSelect)
    .attr('accept', this.contentTypes)
    .on('change', this._onFileChange.bind(this))
    .appendTo(this.$container);

  this.$content = this.$container.appendDiv('file-chooser-content');
  this.$title = $.makeDiv('file-chooser-title')
    .text('Choose files') // XXX BSH
    .appendTo(this.$content);
  this.$files = $.makeDiv('file-chooser-files')
    .appendTo(this.$content);
  this.$buttons = $.makeDiv('file-chooser-buttons')
    .appendTo(this.$content);
  this.$addFileButton = $('<button>')
    .text('Add file') // XXX BSH
    .on('click', this._onAddFileButtonClicked.bind(this))
    .appendTo(this.$buttons);
  this.$okButton = $('<button>')
    .text(this.session.text('Ok'))
    .on('click', this._onOkButtonClicked.bind(this))
    .appendTo(this.$buttons);
  this.$cancelButton = $('<button>')
    .text(this.session.text('Cancel'))
    .on('click', this._onCancelButtonClicked.bind(this))
    .appendTo(this.$buttons);

  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(new scout.FormLayout(this));
  this.htmlComp.pixelBasedSizing = false;

  // Class 'shown' is used for css animation
  this.$container.addClass('shown');
  // Prevent resizing when message-box is dragged off the viewport
  this.$container.addClass('calc-helper');
  this.$container.css('min-width', this.$container.width());
  this.$container.removeClass('calc-helper');
  // Now that all texts, paddings, widths etc. are set, we can calculate the position
  this._position();

  this._$glassPane.installFocusContext('auto', this.session.uiSessionId);
};

scout.FileChooser.prototype._remove = function() {
  scout.FileChooser.parent.prototype._remove.call(this);
  this._$glassPane.fadeOutAndRemove();
};

scout.FileChooser.prototype._position = function() {
  this.$container.cssMarginLeft(-this.$container.outerWidth() / 2);
};

scout.FileChooser.prototype._doOk = function() {
  if (this._files.length === 0) {
    this._doCancel();
    return;
  }
  this.session.uploadFiles(this, this._files);
};

scout.FileChooser.prototype._doCancel = function() {
  this.session.send(this.id, 'cancel');
};

scout.FileChooser.prototype._doAddFile = function() {
  // Trigger browser's file chooser
  this.$fileInputField.click();
};

scout.FileChooser.prototype._onOkButtonClicked = function(event) {
  this._doOk();
};

scout.FileChooser.prototype._onCancelButtonClicked = function(event) {
  this._doCancel();
};

scout.FileChooser.prototype._onAddFileButtonClicked = function(event) {
  this._doAddFile();
};

scout.FileChooser.prototype._onFileChange = function(event) {
  var files = this.$fileInputField[0].files;
  for (var i = 0; i < files.length; i++) {
    var file = files[i];
    if (this.multiSelect) {
      this._files.push(file);
    }
    else {
      this._files = [ file ];
      this.$files.empty();
    }
    this.$files.appendDiv('file').text(file.name);
  }
};

scout.FileChooser.prototype.onModelAction = function(event) {
  if (event.type === 'closed') {
    this._onFileChooserClosed();
  }
};

scout.FileChooser.prototype._onFileChooserClosed = function() {
  this.destroy();
  this.session.desktop.onFileChooserClosed(this);
};
