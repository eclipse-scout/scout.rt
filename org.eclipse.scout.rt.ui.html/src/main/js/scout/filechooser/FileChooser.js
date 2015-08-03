scout.FileChooser = function() {
  scout.FileChooser.parent.call(this);
  this._files = [];
  this._glassPaneRenderer;
  this.attached = false; // Indicates whether this file chooser is currently visible to the user.
};
scout.inherits(scout.FileChooser, scout.ModelAdapter);

scout.FileChooser.prototype._init = function(model, session) {
  scout.FileChooser.parent.prototype._init.call(this, model, session);

  this._glassPaneRenderer = new scout.GlassPaneRenderer(this, true, session.uiSessionId);
};

scout.FileChooser.prototype._render = function($parent) {
  this._$parent = $parent;

  // Render modality glasspanes (must precede adding the file chooser to the DOM)
  this._glassPaneRenderer.renderGlassPanes();

  this.$container = $parent.appendDiv('file-chooser');

  var $handle = this.$container.appendDiv('drag-handle');
  this.$container.makeDraggable($handle);

  this.$container.on('dragenter', this._onDragEnterOrOver.bind(this))
    .on('dragover', this._onDragEnterOrOver.bind(this))
    .on('drop', this._onDrop.bind(this));

  this.$container.appendDiv('closable')
    .on('click', function() {
      this._doCancel();
    }.bind(this));

  this.$fileInputField = $('<input>')
    .attr('type', 'file')
    .prop('multiple', this.multiSelect)
    .attr('accept', this.contentTypes)
    .on('change', this._onFileChange.bind(this));

  if (scout.device.supportsFile()) {
    this.$fileInputField.appendTo(this.$container);
  }

  this.$content = this.$container.appendDiv('file-chooser-content');
  this.$title = $.makeDiv('file-chooser-title')
    .text(this.session.text(this.multiSelect ? 'ui.ChooseFiles' : 'ui.ChooseFile'))
    .appendTo(this.$content);

  if (scout.device.supportsFile()) {
    this.$files = $.makeDiv('file-chooser-files')
      .appendTo(this.$content);
  } else {
    // legacy iframe code
    this.$legacyFormTarget = $('<iframe>')
      .attr('name', 'legacyFileUpload' + this.id)
      .on('load', function() {
          var text = this.$legacyFormTarget.contents().text();
          if (scout.strings.hasText(text)) {
            var json = $.parseJSON(text);
            this.session.onAjaxDone({}, json);
            this.session.onAjaxAlways({}, json);
          }
      }.bind(this))
      .appendTo(this.$container);
    this.$fileInputField
      .attr('name', 'file');
    this.$legacyForm = $('<form>')
      .attr('action', 'upload/' + this.session.uiSessionId + '/' + this.id)
      .attr('enctype', 'multipart/form-data')
      .attr('method', 'post')
      .attr('target', 'legacyFileUpload' + this.id)
      .append(this.$fileInputField)
      .appendTo(this.$content);
    $('<input>')
      .attr('name', 'legacyFormTextPlainAnswer')
      .attr('type', 'hidden')
      .appendTo(this.$legacyForm);
  }

  this.$buttons = $.makeDiv('file-chooser-buttons')
    .appendTo(this.$content);
  if (scout.device.supportsFile()) {
    this.$addFileButton = $('<button>')
      .unfocusable()
      .text(this.session.text('ui.Browse')) // XXX BSH
      .on('click', this._onAddFileButtonClicked.bind(this))
      .appendTo(this.$buttons);
  }
  this.$okButton = $('<button>')
    .unfocusable()
    .text(this.session.text('ui.Upload'))
    .on('click', this._onOkButtonClicked.bind(this))
    .appendTo(this.$buttons);
  this.$cancelButton = $('<button>')
    .unfocusable()
    .text(this.session.text('Cancel'))
    .on('click', this._onCancelButtonClicked.bind(this))
    .appendTo(this.$buttons);

  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(new scout.FormLayout(this));
  this.htmlComp.pixelBasedSizing = false;

  this.$container.addClassForAnimation('shown');
  // Prevent resizing when file chooser is dragged off the viewport
  this.$container.addClass('calc-helper');
  this.$container.css('min-width', this.$container.width());
  this.$container.removeClass('calc-helper');
  // Now that all texts, paddings, widths etc. are set, we can calculate the position
  this._position();

  this.attached = true;
};

scout.FileChooser.prototype._postRender = function() {
  this.$container.installFocusContext(this.session, scout.FocusRule.AUTO);
};

scout.FileChooser.prototype._remove = function() {
  this._glassPaneRenderer.removeGlassPanes();
  this.$container.uninstallFocusContext(this.session);
  this.attached = false;

  scout.FileChooser.parent.prototype._remove.call(this);
};

scout.FileChooser.prototype._position = function() {
  this.$container.cssMarginLeft(-this.$container.outerWidth() / 2);
};

scout.FileChooser.prototype._doOk = function() {
  if (scout.device.supportsFile()) {
    if (this._files.length === 0) {
      this._doCancel();
      return;
    }
    this.session.uploadFiles(this, this._files, undefined, this.maximumUploadSize);
  } else if (this.$fileInputField[0].value) {
    // legacy iframe code
    this.$legacyForm[0].submit();
  }
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
  if (scout.device.supportsFile()) {
    this.addFiles(this.$fileInputField[0].files);
  }
};

/**
 * Add files using java script files api.
 */
scout.FileChooser.prototype.addFiles = function(files) {
  for (var i = 0; i < files.length; i++) {
    var file = files[i];
    if (this.multiSelect) {
      this._files.push(file);
    }
    else {
      this._files = [file];
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
};

scout.FileChooser.prototype._onDragEnterOrOver = function(event) {
  scout.dragAndDrop.verifyDataTransferTypesScoutTypes(event, scout.dragAndDrop.SCOUT_TYPES.FILE_TRANSFER);
};

scout.FileChooser.prototype._onDrop = function(event) {
  if (scout.dragAndDrop.dataTransferTypesContainsScoutTypes(event.originalEvent.dataTransfer, scout.dragAndDrop.SCOUT_TYPES.FILE_TRANSFER)) {
    event.stopPropagation();
    event.preventDefault();

    this.addFiles(event.originalEvent.dataTransfer.files);
  }
};

/**
 * === Method required for objects attached to a 'displayParent' ===
 *
 * Method invoked once the 'displayParent' is detached;
 *
 *  In contrast to 'render/remove', this method uses 'JQuery attach/detach mechanism' to retain CSS properties, so that the model must not be interpreted anew.
 *  This method has no effect if already attached.
 */
scout.FileChooser.prototype.attach = function() {
  if (this.attached || !this.rendered) {
    return;
  }

  this._$parent.append(this.$container);
  this.session.detachHelper.afterAttach(this.$container);

  if (this.keyStrokeAdapter) {
    scout.keyStrokeManager.installAdapter(this.session, this.$container, this.keyStrokeAdapter);
  }

  this.attached = true;
};

/**
 * === Method required for objects attached to a 'displayParent' ===
 *
 * Method invoked once the 'displayParent' is attached;
 *
 *  In contrast to 'render/remove', this method uses 'JQuery attach/detach mechanism' to retain CSS properties, so that the model must not be interpreted anew.
 *  This method has no effect if already detached.
 */
scout.FileChooser.prototype.detach = function() {
  if (!this.attached || !this.rendered) {
    return;
  }

  if (scout.keyStrokeManager.isAdapterInstalled(this.session, this.keyStrokeAdapter)) {
    scout.keyStrokeManager.uninstallAdapter(this.session, this.keyStrokeAdapter);
  }

  this.session.detachHelper.beforeDetach(this.$container);
  this.$container.detach();

  this.attached = false;
};
