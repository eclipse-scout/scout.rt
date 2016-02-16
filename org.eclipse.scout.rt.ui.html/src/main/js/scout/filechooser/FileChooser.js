/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.FileChooser = function() {
  scout.FileChooser.parent.call(this);
  this._files = [];
  this._glassPaneRenderer;
};
scout.inherits(scout.FileChooser, scout.ModelAdapter);

scout.FileChooser.prototype._init = function(model) {
  scout.FileChooser.parent.prototype._init.call(this, model);
  this._glassPaneRenderer = new scout.GlassPaneRenderer(this.session, this, true);
};

/**
 * @override ModelAdapter
 */
scout.FileChooser.prototype._initKeyStrokeContext = function(keyStrokeContext) {
  scout.FileChooser.parent.prototype._initKeyStrokeContext.call(this, keyStrokeContext);

  keyStrokeContext.registerKeyStroke([
    new scout.FocusAdjacentElementKeyStroke(this.session, this),
    new scout.ClickActiveElementKeyStroke(this, [
      scout.keys.SPACE, scout.keys.ENTER
    ]),
    new scout.CloseKeyStroke(this, function() {
      return this.$cancelButton;
    }.bind(this))
  ]);
};

scout.FileChooser.prototype._render = function($parent) {
  // Render modality glasspanes (must precede adding the file chooser to the DOM)
  this._glassPaneRenderer.renderGlassPanes();
  this.$container = $parent.appendDiv('file-chooser');
  var $handle = this.$container.appendDiv('drag-handle');
  this.$container.makeDraggable($handle);

  this.$container.appendDiv('closable')
    .on('click', function() {
      this._doCancel();
    }.bind(this));

  this.$content = this.$container.appendDiv('file-chooser-content');
  this.$title = this.$content.appendDiv('file-chooser-title')
    .text(this.session.text(this.multiSelect ? 'ui.ChooseFiles' : 'ui.ChooseFile'));

  this.$fileInputField = $parent.makeElement('<input>')
    .attr('type', 'file')
    .prop('multiple', this.multiSelect)
    .attr('accept', this.contentTypes)
    .on('change', this._onFileChange.bind(this));

  if (scout.device.supportsFile()) {
    this.$fileInputField.appendTo(this.$container);

    // Install DnD support
    this.$container.on('dragenter', this._onDragEnterOrOver.bind(this))
      .on('dragover', this._onDragEnterOrOver.bind(this))
      .on('drop', this._onDrop.bind(this));

    // explanation for file chooser
    this.$content.appendDiv('file-chooser-label')
      .text(this.session.text('ui.FileChooserHint'));

    // List of files
    this.$files = this.$content.appendElement('<ul>', 'file-chooser-files');
    scout.scrollbars.install(this.$files, {
      parent: this
    });

  } else {
    // legacy iframe code
    this.$legacyFormTarget = this.$fileInputField.appendElement('<iframe>')
      .attr('name', 'legacyFileUpload' + this.id)
      .on('load', function() {
        // Manually handle JSON response from iframe
        try {
          // "onAjaxDone"
          var text = this.$legacyFormTarget.contents().text();
          if (scout.strings.hasText(text)) {
            // Manually handle JSON response
            var json = $.parseJSON(text);
            this.session.responseQueue.process(json);
          }
        } finally {
          // "onAjaxAlways"
          this.session.setBusy(false);
          this.session.layoutValidator.validate();
        }
      }.bind(this));
    this.$fileInputField
      .attr('name', 'file')
      .addClass('legacy-upload-file-input');
    this.$legacyForm = this.$content.appendElement('<form>', 'legacy-upload-form')
      .attr('action', 'upload/' + this.session.uiSessionId + '/' + this.id)
      .attr('enctype', 'multipart/form-data')
      .attr('method', 'post')
      .attr('target', 'legacyFileUpload' + this.id)
      .append(this.$fileInputField);
    this.$legacyForm.appendElement('<input>')
      .attr('name', 'legacyFormTextPlainAnswer')
      .attr('type', 'hidden');
  }

  // Buttons
  this.$buttons = this.$container.appendDiv('file-chooser-buttons');
  var boxButons = new scout.BoxButtons(this.$buttons);
  if (scout.device.supportsFile()) {
    this.$addFileButton = boxButons.addButton({
      text: this.session.text('ui.Browse'),
      onClick: this._onAddFileButtonClicked.bind(this)
    });
  }
  this.$uploadButton = boxButons.addButton({
    text: this.session.text('ui.Upload'),
    onClick: this._onUploadButtonClicked.bind(this),
    enabled: false
  });
  this.$cancelButton = boxButons.addButton({
    text: this.session.text('Cancel'),
    onClick: this._onCancelButtonClicked.bind(this)
  });

  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(new scout.FormLayout(this));
  this.htmlComp.pixelBasedSizing = false;

  this.$container.addClassForAnimation('shown');
  // Prevent resizing when file chooser is dragged off the viewport
  this.$container.addClass('calc-helper');
  this.$container.css('min-width', this.$container.width());
  this.$container.removeClass('calc-helper');
  boxButons.updateButtonWidths(this.$container.width());
  // Now that all texts, paddings, widths etc. are set, we can calculate the position
  this._position();
};

scout.FileChooser.prototype._postRender = function() {
  this.session.focusManager.installFocusContext(this.$container, scout.focusRule.AUTO);
};

scout.FileChooser.prototype._remove = function() {
  this._glassPaneRenderer.removeGlassPanes();
  this.session.focusManager.uninstallFocusContext(this.$container);
  scout.FileChooser.parent.prototype._remove.call(this);
};

scout.FileChooser.prototype._position = function() {
  this.$container.cssMarginLeft(-this.$container.outerWidth() / 2);
};

scout.FileChooser.prototype._doUpload = function() {
  if (scout.device.supportsFile()) {
    if (this._files.length === 0) {
      this._doCancel();
      return;
    }
    this.session.uploadFiles(this, this._files, undefined, this.maximumUploadSize);
  } else if (this.$fileInputField[0].value) {
    // legacy iframe code
    this.session.setBusy(true);
    this.$legacyForm[0].submit();
  }
};

scout.FileChooser.prototype._doCancel = function() {
  this._send('cancel');
};

scout.FileChooser.prototype._doAddFile = function() {
  // Trigger browser's file chooser
  this.$fileInputField.click();
};

scout.FileChooser.prototype._onUploadButtonClicked = function(event) {
  this.$uploadButton.setEnabled(false);
  this._doUpload();
  this.session.listen().done(function() {
    this.$uploadButton.setEnabled(true);
  }.bind(this));

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
  } else {
    this.$uploadButton.setEnabled(this.$fileInputField[0].value);
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
    } else {
      this._files = [file];
      this.$files.empty();
    }
    var $file = this.$files.appendElement('<li>', 'file', file.name);
    // Append a space to allow the browser to break the line here when it gets too long
    $file.append(" ");
    var $remove = $file
      .appendSpan('remove menu-item')
      .on('click', this.removeFile.bind(this, file, $file));
    var $removeLink = $file.makeElement('<a>', 'remove-link', this.session.text('Remove'));
    $remove.appendTextNode('(');
    $remove.append($removeLink);
    $remove.appendTextNode(')');
  }
  this.$uploadButton.setEnabled(this._files.length > 0);
  scout.scrollbars.update(this.$files);
};

scout.FileChooser.prototype.removeFile = function(file, $file) {
  var index = this._files.indexOf(file);
  if (index > -1) {
    this._files.splice(index, 1);
  }
  if ($file) {
    $file.remove();
  }
  this.$uploadButton.setEnabled(this._files.length > 0);
  scout.scrollbars.update(this.$files);
};

scout.FileChooser.prototype._onDragEnterOrOver = function(event) {
  scout.dragAndDrop.verifyDataTransferTypesScoutTypes(event, scout.dragAndDrop.SCOUT_TYPES.FILE_TRANSFER);
};

scout.FileChooser.prototype._onDrop = function(event) {
  if (scout.dragAndDrop.dataTransferTypesContainsScoutTypes(event.originalEvent.dataTransfer, scout.dragAndDrop.SCOUT_TYPES.FILE_TRANSFER)) {
    $.suppressEvent(event);
    this.addFiles(event.originalEvent.dataTransfer.files);
  }
};

/**
 * @override Widget.js
 */
scout.FileChooser.prototype._attach = function() {
  this._$parent.append(this.$container);
  this.session.detachHelper.afterAttach(this.$container);
  scout.FileChooser.parent.prototype._attach.call(this);
};

/**
 * @override Widget.js
 */
scout.FileChooser.prototype._detach = function() {
  this.session.detachHelper.beforeDetach(this.$container);
  this.$container.detach();
  scout.FileChooser.parent.prototype._detach.call(this);
};

/**
 * Used by CloseKeyStroke.js
 */
scout.FileChooser.prototype.close = function() {
  if (this.$cancelButton && this.session.focusManager.requestFocus(this.$cancelButton)) {
    this.$cancelButton.click();
  }
};
