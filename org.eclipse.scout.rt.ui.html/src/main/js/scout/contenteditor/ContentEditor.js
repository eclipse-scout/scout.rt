/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
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
  this._contentElements = [];
  this.placeholderText = '';
  this.doc = null;
  this.content = '';
  this.$slots = null;
  this.$placeholders = null;
  this._$currentClosestPlaceholder = null;
};
scout.inherits(scout.ContentEditor, scout.Widget);

scout.ContentEditor.prototype._init = function(model) {
  scout.ContentEditor.parent.prototype._init.call(this, model);
  this._iframeDragCounter = 0;

  this.iframe = scout.create('IFrame', {
    parent: this,
    sandboxEnabled: false,
    scrollBarEnabled: model.scrollBarEnabled
  });
};

scout.ContentEditor.prototype.addElement = function(element) {
  this._contentElements.push(element);
};

scout.ContentEditor.prototype.removeElement = function(element) {
  scout.arrays.remove(this._contentElements, element);
};

scout.ContentEditor.prototype.updateElement = function(elementContent, slot, elementId) {
  var element = scout.arrays.find(this._contentElements, function(element) {
    return element.elementId === elementId;
  });
  element.updateContent(elementContent);
};

scout.ContentEditor.prototype._render = function() {
  this.$container = this.$parent.appendDiv('ce');

  this.iframe.render();
  this.iframe.$container.addClass('ce-iframe');
  this.$container.append(this.iframe.$container);

  this.iframe.$iframe.ready(function() {
    this._renderContent();
  }.bind(this));
};

scout.ContentEditor.prototype.setContent = function(content) {
  this.setProperty('content', content);
  this._renderContent();
};

// TODO: get content of content editor and all elements
scout.ContentEditor.prototype.getContent = function() {
  return this.iframe.$iframe.get(0).contentWindow.document.outerHtml;
};

scout.ContentEditor.prototype.setPlaceholderText = function(placeholderText) {
  this.setProperty('placeholderText', placeholderText);
};

scout.ContentEditor.prototype._renderContent = function() {
  this.doc = this.iframe.$iframe.get(0).contentWindow.document;

  this.doc.open();
  this.doc.write(this.content);
  this.doc.close();

  // the jquery ready event is attached to the root document and not to the document of the iframe.
  // therefore we must use the DOMContentLoaded event by ourselves
  this.doc.addEventListener('DOMContentLoaded', this._onIframeContentLoaded.bind(this));
};

scout.ContentEditor.prototype._injectStyleSheet = function($header) {
  $header.append('<link rel="stylesheet" type="text/css" href="' + this.session.url.baseUrlRaw + 'res/contenteditor.css">');
  this.$slots.addClass('ce-slot');
};

scout.ContentEditor.prototype._onIframeContentLoaded = function() {
  this.$slots = $(this.doc.body).find('[data-ce-slot]');
  this.$placeholders = this.$slots.appendDiv('ce-slot-placeholder').text(this.placeholderText);
  this._injectStyleSheet($(this.doc.head));

  $(this.doc)
    .on('dragenter', this._onIframeDragEnter.bind(this))
    .on('dragover', this._onIframeDragOver.bind(this))
    .on('dragleave', this._onIframeDragLeave.bind(this))
    .on('drop', this._onIframeDrop.bind(this));
};

scout.ContentEditor.prototype._onIframeDragEnter = function(event) {
  event.preventDefault();
  this._iframeDragCounter++;
  this._showPlaceHolders(this._getTypeOfDragEvent(event));
};

scout.ContentEditor.prototype._getTypeOfDragEvent = function(event) {
  var e = event.originalEvent;
  if (e.dataTransfer.types) {
    return e.dataTransfer.types[0];
  }
  return '';
};

scout.ContentEditor.prototype._onIframeDragOver = function(event) {
  event.preventDefault();
  var type = this._getTypeOfDragEvent(event);
  var $closestPlaceholder = this._getClosestPlaceholder(event.clientX, event.clientY, type);

  if ($closestPlaceholder && this._$currentClosestPlaceholder !== $closestPlaceholder) {
    this._$currentClosestPlaceholder = $closestPlaceholder;
    this._getPlaceholdersForType(type).each(function(index, slot) {
      if ($(slot) !== this._$currentClosestPlaceholder) {
        $(slot).removeClass('ce-accept-drop');
      }
    }.bind(this));
    this._$currentClosestPlaceholder.addClass('ce-accept-drop');
  }
};

scout.ContentEditor.prototype._onIframeDragLeave = function(event) {
  if (--this._iframeDragCounter === 0) {
    this._hidePlaceHolders();
  }
};

scout.ContentEditor.prototype._showPlaceHolders = function(type) {
  this._getPlaceholdersForType(type)
    .addClass('ce-slot-placeholder-accept')
    .css('opacity', '1')
    .css('height', 'auto');
};

scout.ContentEditor.prototype._getPlaceholdersForType = function(type) {
  if (type === 'text/plain') {
    type = 'text';
  }
  return $(this.doc.body).find('[data-ce-allowed-elements~=\'' + type + '\'] .ce-slot-placeholder');
};

scout.ContentEditor.prototype._hidePlaceHolders = function() {
  $(this.doc.body).find('[data-ce-slot] .ce-slot-placeholder').animate({
    height: '0',
    opacity: 0
  }, 'fast', 'linear', function() {
    $(this.doc.body).find('[data-ce-slot] .ce-slot-placeholder')
      .removeClass('ce-slot-placeholder-accept');
  }.bind(this));
};

scout.ContentEditor.prototype._onIframeDrop = function(event) {
  event.preventDefault();
  var e = event.originalEvent;
  if (e.dataTransfer.types) {
    var type = e.dataTransfer.types[0];
    var $elementContent = $(e.dataTransfer.getData(type));
    var contentElement = scout.create('ContentElement', {
      contentEditor: this,
      $container: $elementContent
    });
    contentElement.dropInto(this._$currentClosestPlaceholder);
  }
  this._hidePlaceHolders();
  this._iframeDragCounter = 0;
  return false;
};

scout.ContentEditor.prototype._getClosestPlaceholder = function(x, y, type) {
  var $closest;
  var closestDistance;
  this._getPlaceholdersForType(type).each(function(index, placeholder) {
    var $placeholder = $(placeholder);
    var distance = this._getDistance($placeholder, x, y);
    if (!$closest || distance < closestDistance) {
      $closest = $placeholder;
      closestDistance = distance;
    }
  }.bind(this));
  return $closest;
};

scout.ContentEditor.prototype._getDistance = function($element, x, y) {
  var xOffset = 0;
  var yOffset = 0;

  if ($element.position().top > y) {
    yOffset = $element.position().top - y;
  } else if ($element.position().top + $element.height() < y) {
    yOffset = y - $element.position().top - $element.height();
  }

  if ($element.position().left > x) {
    xOffset = $element.position().left - x;
  } else if ($element.position().left + $element.width() < x) {
    xOffset = x - $element.position().left - $element.width();
  }
  return Math.sqrt(xOffset * xOffset + yOffset * yOffset);
};
