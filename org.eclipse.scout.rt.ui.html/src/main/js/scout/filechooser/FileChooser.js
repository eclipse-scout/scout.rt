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
  this.files = [];
  this._glassPaneRenderer;
};
scout.inherits(scout.FileChooser, scout.Widget);

scout.FileChooser.prototype._init = function(model) {
  scout.FileChooser.parent.prototype._init.call(this, model);
  this._glassPaneRenderer = new scout.GlassPaneRenderer(this.session, this, true);
  this.fileInput = scout.create('FileInput', {
    parent: this,
    acceptTypes: this.acceptTypes,
    multiSelect: this.multiSelect,
    visible: !scout.device.supportsFile()
  });
  this.fileInput.on('change', this._onFileChange.bind(this));
};

/**
 * @override
 */
scout.FileChooser.prototype._createKeyStrokeContext = function() {
  return new scout.KeyStrokeContext();
};

/**
 * @override
 */
scout.FileChooser.prototype._initKeyStrokeContext = function() {
  scout.FileChooser.parent.prototype._initKeyStrokeContext.call(this);

  this.keyStrokeContext.registerKeyStroke([
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
      this.cancel();
    }.bind(this));

  this.$content = this.$container.appendDiv('file-chooser-content');
  this.$title = this.$content.appendDiv('file-chooser-title')
    .text(this.session.text(this.multiSelect ? 'ui.ChooseFiles' : 'ui.ChooseFile'));

  this.fileInput.render(this.$content);

  // DnD and Multiple files are only supported with the new file api
  if (!this.fileInput.legacy) {

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
  }

  // Buttons
  this.$buttons = this.$container.appendDiv('file-chooser-buttons');
  var boxButtons = new scout.BoxButtons(this.$buttons);
  if (!this.fileInput.legacy) {
    this.$addFileButton = boxButtons.addButton({
      text: this.session.text('ui.Browse'),
      onClick: this._onAddFileButtonClicked.bind(this),
      needsClick: true
    });
  }
  this.$uploadButton = boxButtons.addButton({
    text: this.session.text('ui.Upload'),
    onClick: this._onUploadButtonClicked.bind(this),
    enabled: false
  });
  this.$cancelButton = boxButtons.addButton({
    text: this.session.text('Cancel'),
    onClick: this._onCancelButtonClicked.bind(this)
  });

  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);
  this.htmlComp.setLayout(new scout.FormLayout(this));
  this.htmlComp.pixelBasedSizing = false;

  this.$container.addClassForAnimation('animate-open');
  // Prevent resizing when file chooser is dragged off the viewport
  this.$container.addClass('calc-helper');
  this.$container.css('min-width', this.$container.width());
  this.$container.removeClass('calc-helper');
  boxButtons.updateButtonWidths(this.$container.width());
  // Now that all texts, paddings, widths etc. are set, we can calculate the position
  this._position();
};

scout.FileChooser.prototype._renderProperties = function() {
  scout.FileChooser.parent.prototype._renderProperties.call(this);
  if (this.fileInput.legacy) {
    // Files may not be set into native control -> clear list in order to be sync again
    this.setFiles([]);
  }
  this._renderFiles();
};

scout.FileChooser.prototype._postRender = function() {
  scout.FileChooser.parent.prototype._postRender.call(this);
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

scout.FileChooser.prototype.upload = function() {
  if (this.files.length === 0) {
    return;
  }

  if (this.fileInput.legacy) {
    this.fileInput.upload();
  } else {
    this.session.uploadFiles(this, this.files, undefined, this.maximumUploadSize);
  }
};

scout.FileChooser.prototype.cancel = function() {
  // TODO [7.0] cgu offline case?
  this.trigger('cancel');
};

scout.FileChooser.prototype.browse = function() {
  this.fileInput.browse();
};

scout.FileChooser.prototype.setAcceptTypes = function(acceptTypes) {
  this.setProperty('acceptTypes', acceptTypes);
  this.fileInput.setAcceptTypes(acceptTypes);
};

scout.FileChooser.prototype.setMultiSelect = function(multiSelect) {
  this.setProperty('multiSelect', multiSelect);
  this.fileInput.setMultiSelect(multiSelect);
};

scout.FileChooser.prototype.addFiles = function(files) {
  files = scout.arrays.ensure(files);
  if (files.length === 0) {
    return;
  }
  if (!this.multiSelect || this.fileInput.legacy) {
    files = [files[0]];
    this.setFiles([files[0]]);
  } else {
    // copy so that parameter stays untouched
    files = files.slice();
    // append new files to existing ones
    scout.arrays.insertArray(files, this.files, 0);
    this.setFiles(files);
  }
};

scout.FileChooser.prototype.removeFile = function(file) {
  var files = this.files.slice();
  scout.arrays.remove(files, file);
  this.setFiles(files);
  // Clear the input, otherwise user could not choose the file which he has removed previously
  this.fileInput.clear();
};

scout.FileChooser.prototype.setFiles = function(files) {
  files = scout.arrays.ensure(files);
  this.setProperty('files', files);
};

scout.FileChooser.prototype._renderFiles = function() {
  var files = this.files;

  if (!this.fileInput.legacy) {
    this.$files.empty();
    files.forEach(function(file) {
      var $file = this.$files.appendElement('<li>', 'file', file.name);
      // Append a space to allow the browser to break the line here when it gets too long
      $file.append(" ");
      var $remove = $file
        .appendSpan('remove menu-item')
        .on('click', this.removeFile.bind(this, file));
      var $removeLink = $file.makeElement('<a>', 'remove-link', this.session.text('Remove'));
      $remove.appendTextNode('(');
      $remove.append($removeLink);
      $remove.appendTextNode(')');
    }, this);
    scout.scrollbars.update(this.$files);
  }
  this.$uploadButton.setEnabled(files.length > 0);
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

scout.FileChooser.prototype._onUploadButtonClicked = function(event) {
  this.$uploadButton.setEnabled(false);
  this.upload();
  this.session.listen().done(function() {
    this.$uploadButton.setEnabled(true);
  }.bind(this));
};

scout.FileChooser.prototype._onCancelButtonClicked = function(event) {
  this.cancel();
};

scout.FileChooser.prototype._onAddFileButtonClicked = function(event) {
  this.browse();
};

scout.FileChooser.prototype._onFileChange = function(event) {
  this.addFiles(event.files);
};

/**
 * @override Widget.js
 */
scout.FileChooser.prototype._attach = function() {
  this.$parent.append(this.$container);
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
