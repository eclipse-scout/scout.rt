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
scout.ContentEditor = function() {
  scout.ContentEditor.parent.call(this);
};
scout.inherits(scout.ContentEditor, scout.Widget);

scout.ContentEditor.prototype._init = function(model) {
  scout.ContentEditor.parent.prototype._init.call(this, model);
  this.content;
  this.$slots;

  this.iframe = scout.create('IFrame', {
    parent: this,
    sandboxEnabled: false,
    scrollBarEnabled: model.scrollBarEnabled
  });
};

scout.ContentEditor.prototype._render = function() {
  this.$container = this.$parent.appendDiv('ce');
  this._renderButtonGroup();

  this.iframe.render();
  this.iframe.$container.addClass('ce-iframe');
  this.$container.append(this.iframe.$container);

  this.iframe.$iframe.ready(function() {
    this._renderContent();
  }.bind(this));
};

// TODO: remove drag and drop icons --> will be in an other widget
scout.ContentEditor.prototype._renderButtonGroup = function() {
  var $buttonGroup = this.$container.appendDiv('ce-buttongroup');
  var $button1 = $buttonGroup.appendDiv('ce-button').text('Text with Image').attr('draggable', 'true').attr('style', '-khtml-user-drag: element;');

  $button1.on('dragstart', function(event) {
    var textData = '<div data-ce-element="text-with-image"><img src="res/contentEditorRes/img/download.png" class="image" data-ce-element-part="image" alt="Image"><div class="text" data-ce-element-part="text">Text</div></div>';
    event.originalEvent.dataTransfer.setData("text", textData);
  }.bind(this));
  $button1.on('mousedown', function(event) {
    event.stopPropagation(); //firefox
  }.bind(this));

  var $button2 = $buttonGroup.appendDiv('ce-button').text('Text').attr('draggable', 'true');
  $button2.on('dragstart', function(event) {
    var textData = '<div data-ce-element="text" data-ce-element-part="text">Text only</div>';
    event.originalEvent.dataTransfer.setData("text", textData);
  }.bind(this));
  $button2.on('mousedown', function(event) {
    event.stopPropagation(); //firefox
  }.bind(this));
};

scout.ContentEditor.prototype.setContent = function(content) {
  this.setProperty('content', content);
};

// TODO: remove all tags and attributes that were added by the editor
scout.ContentEditor.prototype.getContent = function() {
  return this.iframe.$iframe.get(0).contentWindow.document.outerHtml;
};

scout.ContentEditor.prototype._renderContent = function() {
  var doc = this.iframe.$iframe.get(0).contentWindow.document;

  doc.open();
  doc.write(this.content);
  doc.close();

  // the jquery ready event is attached to the root document and not to the document of the iframe.
  // therefore we must use the DOMContentLoaded event by ourselves
  doc.addEventListener('DOMContentLoaded', this._onIframeContentLoaded.bind(this, doc));
};

scout.ContentEditor.prototype._injectStyleSheet = function($header) {
  $header.append('<link rel="stylesheet" type="text/css" href="res/ContentEditorIFrame.css">');
  this.$slots.addClass('ce-slot');
};

scout.ContentEditor.prototype._onIframeContentLoaded = function(doc) {
  this.$slots = $(doc.body).find('[data-contenteditor-slot]');
  this.$slots.appendDiv('ce-slot-placeholder').text('Add Element');
  this._injectStyleSheet($(doc.head));

  this.$slots
    .on('dragenter dragover', this._onSlotDragOver.bind(this))
    .on('dragleave', this._onSlotDragLeave.bind(this))
    .on('drop', this._onSlotDrop.bind(this));
};

scout.ContentEditor.prototype._onSlotDragOver = function(event) {
  $(event.target).closest('[data-contenteditor-slot]').addClass('ce-accept-drop');
  return false;
};

scout.ContentEditor.prototype._onSlotDragLeave = function(event) {
  $(event.target).closest('[data-contenteditor-slot]').removeClass('ce-accept-drop');
  return false;
};

scout.ContentEditor.prototype._onSlotDrop = function(event) {
  var e = event.originalEvent;
  var $element = $(e.dataTransfer.getData('text'));
  $element.addClass('ce-element');

  var $slot = $(e.target).closest('[data-contenteditor-slot]');
  $slot.find('.ce-slot-placeholder').before($element);
  $slot.removeClass('ce-accept-drop');

  $element
    .on('mouseenter', this._onElementMouseEnter.bind(this))
    .on('mouseleave', this._onElementMouseLeave.bind(this));

  return false;
};

scout.ContentEditor.prototype._onElementMouseEnter = function(event) {
  var $element = $(event.target).closest('.ce-element');

  $element.addClass('ce-element-hover');
  var $removeButton = $element.appendDiv('ce-element-remove-button').text('Remove');
  $removeButton.on('click', function(event) {
    $element.closest('.ce-element').remove();
  });
  var $moveUpButton = $element.appendDiv('ce-element-moveup-button').text('Move Up');
  $moveUpButton.on('click', function(event) {
    $element.prev('.ce-element').insertAfter($element);
  });
  var $moveDownButton = $element.appendDiv('ce-element-movedown-button').text('Move Down');
  $moveDownButton.on('click', function(event) {
    $element.next('.ce-element').insertBefore($element);
  });
};

scout.ContentEditor.prototype._onElementMouseLeave = function(event) {
  var $element = $(event.target).closest('.ce-element');

  $element.removeClass('ce-element-hover');
  $element.find('.ce-element-remove-button').remove();
  $element.find('.ce-element-moveup-button').remove();
  $element.find('.ce-element-movedown-button').remove();
};
