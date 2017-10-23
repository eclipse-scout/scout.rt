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
  this.dropzoneLabel = '';
  this.doc = null;
  this.content = '';
  this.$slots = null;
  this.$placeholders = null;
  this._$currentClosestSlot = null;
  this._$currentClosestPlaceholder = null;
};
scout.inherits(scout.ContentEditor, scout.Widget);

scout.ContentEditor.prototype._init = function(model) {
  scout.ContentEditor.parent.prototype._init.call(this, model);
  this._iframeDragTargetList = [];

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

scout.ContentEditor.prototype.setDropzoneLabel = function(dropzoneLabel) {
  this.setProperty('dropzoneLabel', dropzoneLabel);
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
  this.$slots.each(function() {
    $(this).appendDiv('ce-slot-content-list').text($(this).attr('data-ce-allowed-elements'));
  });

  this._injectStyleSheet($(this.doc.head));
  this._disableLinks();

  $(this.doc)
    .on('dragenter', this._onIframeDragEnter.bind(this))
    .on('dragover', this._onIframeDragOver.bind(this))
    .on('dragleave', this._onIframeDragLeave.bind(this))
    .on('drop', this._onIframeDrop.bind(this));
};

scout.ContentEditor.prototype._disableLinks = function() {
  $(this.doc.body).find('a').on('click', function() {
    return false;
  });
};

scout.ContentEditor.prototype._onIframeDragEnter = function(event) {
  this._iframeDragTargetList.push(event.target);
  this._showSlots(this._getTypeOfDragEvent(event));
  event.preventDefault();
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
  var $closestSlot = this._getClosestSlot(event.clientX, event.clientY, type);

  if ($closestSlot && this._$currentClosestSlot !== $closestSlot) {
    this._$currentClosestSlot = $closestSlot;
    this._getSlotsForType(type).each(function(index, slot) {
      if ($(slot) !== this._$currentClosestSlot) {
        $(slot).removeClass('ce-accept-drop');
        this._hidePlaceholders($(slot));
      }
    }.bind(this));
    this._$currentClosestSlot.addClass('ce-accept-drop');
    this._showPlaceholders();
  }
  this._highlightClosestPlaceholder(event.clientX, event.clientY);
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
    this._animateDrop(event, this._$currentClosestPlaceholder, function() {
      contentElement.dropInto(this._$currentClosestPlaceholder);
      this._hidePlaceholders();
    }.bind(this));
  }
  this._hideSlots();
  this._iframeDragTargetList = [];
  return false;
};

scout.ContentEditor.prototype._animateDrop = function(event, $placeholder, complete) {
  var $element = $(this.doc.body).appendDiv('ce-dropping-element');
  $element.offset({left: event.clientX, top: event.clientY});
  $element.animate({'left': $placeholder.offset().left + 'px', 'top': $placeholder.offset().top, 'height': '10px', 'width': $placeholder.width() }, 200, function() {
    $element.remove();
    if(complete) {
      complete();
    }
  }.bind(this));
};

scout.ContentEditor.prototype._onIframeDragLeave = function(event) {
  scout.arrays.remove(this._iframeDragTargetList, event.target);
  if (this._iframeDragTargetList.length === 0) {
    this._hideSlots();
    this._hidePlaceholders();
  }
};

scout.ContentEditor.prototype._showSlots = function(type) {
  var $slotsForType = this._getSlotsForType(type);
  $slotsForType.addClass('ce-slot-accept');
};

scout.ContentEditor.prototype._showPlaceholders = function() {
  this._$currentClosestSlot.prependDiv('ce-placeholder');
  this._$currentClosestSlot
    .find('.ce-element')
    .afterDiv('ce-placeholder');
};

scout.ContentEditor.prototype._highlightClosestPlaceholder = function(x, y) {
  var $closest = this._getClosestPlaceholder(x, y);
  if ($closest && this._$currentClosestPlaceholder !== $closest) {
    this._$currentClosestPlaceholder = $closest;
    $(this.doc.body).find('.ce-placeholder').each(function(index, placeholder) {
      if ($(placeholder) !== $closest) {
        $(placeholder).removeClass('ce-placeholder-accept-drop');
      }
    }.bind(this));
    $closest.addClass('ce-placeholder-accept-drop');
  }
};

scout.ContentEditor.prototype._hidePlaceholders = function($parent) {
  if (!$parent) {
    $parent = $(this.doc.body);
  }
  var $placeholder = $parent.find('.ce-placeholder');
  $placeholder.each(function(i) {
    scout.arrays.remove(this._iframeDragTargetList, $placeholder[i]);
  }.bind(this));
  $parent.find('.ce-placeholder').remove();
};

scout.ContentEditor.prototype._hideSlots = function() {
  $(this.doc.body).find('[data-ce-slot]').removeClass('ce-slot-accept ce-accept-drop');
};

scout.ContentEditor.prototype._getPlaceholdersForType = function(type) {
  return this._getSlotsForType(type).find('.ce-slot-placeholder');
};

scout.ContentEditor.prototype._getSlotsForType = function(type) {
  if (type === 'text/plain') {
    type = 'text';
  }
  return $(this.doc.body).find('[data-ce-allowed-elements~=\'' + type + '\']');
};

scout.ContentEditor.prototype._getClosestPlaceholder = function(x, y) {
  var $closest;
  var closestDistance;
  $(this.doc.body).find('.ce-placeholder').each(function(index, placeholder) {
    var $placeholder = $(placeholder);
    var distance = this._getDistance($placeholder, x, y);
    if (!$closest || distance < closestDistance) {
      $closest = $placeholder;
      closestDistance = distance;
    }
  }.bind(this));
  return $closest;
};

scout.ContentEditor.prototype._getClosestSlot = function(x, y, type) {
  var $closest;
  var closestDistance;
  this._getSlotsForType(type).each(function(index, slot) {
    var $slot = $(slot);
    var distance = this._getDistance($slot, x, y);
    if (!$closest || distance < closestDistance) {
      $closest = $slot;
      closestDistance = distance;
    }
  }.bind(this));
  return $closest;
};

scout.ContentEditor.prototype._getDistance = function($element, x, y) {
  var xOffset = 0;
  var yOffset = 0;

  if ($element.offset().top > y) {
    yOffset = $element.offset().top - y;
  } else if ($element.offset().top + $element.height() < y) {
    yOffset = y - $element.offset().top - $element.height();
  }

  if ($element.offset().left > x) {
    xOffset = $element.offset().left - x;
  } else if ($element.offset().left + $element.width() < x) {
    xOffset = x - $element.offset().left - $element.width();
  }
  return Math.sqrt(xOffset * xOffset + yOffset * yOffset);
};
