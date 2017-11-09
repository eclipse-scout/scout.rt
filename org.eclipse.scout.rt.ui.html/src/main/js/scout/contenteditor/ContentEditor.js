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
  this._slots = [];
  this._closestSlot = null;

  this.dropzoneLabel = '';
  this.doc = null;
  this.content = '';
  this._$closestPlaceholder = null;
};
scout.inherits(scout.ContentEditor, scout.Widget);

scout.ContentEditor.prototype._init = function(model) {
  scout.ContentEditor.parent.prototype._init.call(this, model);

  this._resetDragging();

  this.iframe = scout.create('IFrame', {
    parent: this,
    sandboxEnabled: false,
    scrollBarEnabled: model.scrollBarEnabled
  });
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

scout.ContentEditor.prototype.setDropzoneLabel = function(dropzoneLabel) {
  this.setProperty('dropzoneLabel', dropzoneLabel);
};

scout.ContentEditor.prototype.setContent = function(content) {
  this.setProperty('content', content);
};

scout.ContentEditor.prototype.updateContentProperty = function() {
  this._setProperty('content', this.getContent());
};

scout.ContentEditor.prototype.getContent = function() {
  var $html = $(this.iframe.$iframe.get(0).contentWindow.document.documentElement).clone();
  $html.find('#ce-iframe-style').remove();
  $html.find('.ce-dropping-element').remove();
  $html.find('.ce-slot-label').remove();
  $html.find('.ce-slot').removeClass('ce-slot ce-slot-visible ce-slot-invalid ce-slot-accept ce-slot-mandatory');
  $html.find('.ce-placeholder').remove();
  $html.find('.ce-element').removeClass('ce-element ce-element-hover');
  $html.find('.ce-element-edit-indicator').remove();
  $html.find('.ce-element-button-group').remove();
  $html.find('.ce-glasspane').remove();
  return $html.html();
};

scout.ContentEditor.prototype.addElement = function(element) {
  this._contentElements.push(element);
  this.updateContentProperty();
};

scout.ContentEditor.prototype.removeElement = function(element) {
  scout.arrays.remove(this._contentElements, element);
  this.updateContentProperty();
};

scout.ContentEditor.prototype.updateElement = function(elementContent, slot, elementId) {
  var element = scout.arrays.find(this._contentElements, function(element) {
    return element.elementId === elementId;
  });
  element.updateContent(elementContent);
  this.updateContentProperty();
};

scout.ContentEditor.prototype._renderContent = function() {
  this.doc = this.iframe.$iframe.get(0).contentWindow.document;

  this.doc.open();
  this.doc.write(this.content);

  // the jquery ready event is attached to the root document and not to the document of the iframe.
  // therefore we must use the DOMContentLoaded event by ourselves
  this.doc.addEventListener('DOMContentLoaded', this._onIframeContentLoaded.bind(this));
  this.doc.close();
};

scout.ContentEditor.prototype._onIframeContentLoaded = function() {
  this._injectStyleSheet($(this.doc.head), function() {
    this._createSlots();
  }.bind(this));
  this._disableLinks();

  $(this.doc)
    .on('dragenter', this._onIframeDragEnter.bind(this))
    .on('dragleave', this._onIframeDragLeave.bind(this))
    .on('dragover', this._onIframeDragOver.bind(this))
    .on('drop', this._onIframeDrop.bind(this));
};

scout.ContentEditor.prototype._onIframeDragEnter = function(event) {
  this._iframeDragTargetList.push(event.target);
  this._showSlots(this._getTypeOfDragEvent(event));

  event.preventDefault();
};

scout.ContentEditor.prototype._onIframeDragLeave = function(event) {
  scout.arrays.remove(this._iframeDragTargetList, event.target);
  if (this._iframeDragTargetList.length === 0) {
    this._hideSlots();
    this._stopAccepting();
    this._resetDragging();
  }
};

scout.ContentEditor.prototype._onIframeDragOver = function(event) {
  event.preventDefault();

  if (event.clientX === this._dragPositionX && event.clientY === this._dragPositionY) {
    return;
  }
  this._dragPositionX = event.clientX;
  this._dragPositionY = event.clientY;

  if (!this._dragAnimationFrame) {
    var type = this._getTypeOfDragEvent(event);
    this._dragAnimationFrame = window.requestAnimationFrame(this._highlightClosestSlot.bind(this, type, event.clientX, event.clientY));
  }
};

scout.ContentEditor.prototype._onIframeDrop = function(event) {
  event.preventDefault();

  if (!this._$closestPlaceholder) {
    this._stopAccepting();
    this._hideSlots();
    this._resetDragging();
    return false;
  }

  var e = event.originalEvent;
  if (e.dataTransfer.types) {
    var type = e.dataTransfer.types[0];
    var $elementContent = $(e.dataTransfer.getData(type));
    var contentElement = scout.create('ContentElement', {
      contentEditor: this,
      $container: $elementContent
    });
    this._animateDrop(event, this._$closestPlaceholder, function() {
      contentElement.dropInto(this._closestSlot, this._$closestPlaceholder);
      this._stopAccepting();
    }.bind(this));
  }
  this._hideSlots();
  this._resetDragging();
  return false;
};

scout.ContentEditor.prototype._injectStyleSheet = function($header, callback) {
  var $styleLink = $('<link rel="stylesheet" type="text/css" id="ce-iframe-style">');
  $header.append($styleLink);
  // use the baseurl here to prevent loading the contenteditor.css file from the resource server.
  $styleLink.on('load', callback.bind(this)).attr("href", this.session.url.baseUrlRaw + "res/contenteditor.css");
};

scout.ContentEditor.prototype._disableLinks = function() {
  $(this.doc.body).find('a').on('click', function() {
    return false;
  });
};

scout.ContentEditor.prototype._createSlots = function() {
  $(this.doc.body).find('[data-ce-dropzone]').each(function(index, slot) {
    this._slots.push(scout.create('ContentEditorSlot', {
      contentEditor: this,
      $container: $(slot)
    }));
  }.bind(this));
};

scout.ContentEditor.prototype._resetDragging = function() {
  this._iframeDragTargetList = [];

  this._dragPositionX = null;
  this._dragPositionY = null;
  this._dragAnimationFrame = null;

  if (this._dragAnimationFrame) {
    window.cancelAnimationFrame(this._dragAnimationFrame);
  }
};

scout.ContentEditor.prototype._getTypeOfDragEvent = function(event) {
  var e = event.originalEvent;
  if (e.dataTransfer.types) {
    return e.dataTransfer.types[0];
  }
  return '';
};

scout.ContentEditor.prototype._animateDrop = function(event, $placeholder, complete) {
  var $element = $(this.doc.body).appendDiv('ce-dropping-element');
  $element.offset({
    left: event.clientX,
    top: event.clientY
  });
  $element.animate({
    'left': $placeholder.offset().left + 'px',
    'top': $placeholder.offset().top,
    'height': '10px',
    'width': $placeholder.width()
  }, 200, function() {
    $element.remove();
    if (complete) {
      complete();
    }
  }.bind(this));
};

scout.ContentEditor.prototype._highlightClosestSlot = function(type, x, y) {
  this._dragAnimationFrame = null;

  if (!this._iframeDragTargetList || this._iframeDragTargetList.length === 0) {
    // dragging already stopped but animation frame was requested before. Canceling this function.
    return;
  }

  this._closestSlot = this._getClosestSlot(x, y);
  if (!this._closestSlot) {
    this._stopAccepting();
    return;
  }

  if (!this._closestSlot.isAccepting()) {
    this._slots.forEach(function(slot) {
      if (slot !== this._closestSlot) {
        slot.stopAccepting();
      }
    }.bind(this));
    this._closestSlot.requestAccepting(type);
  }
  this._$closestPlaceholder = this._closestSlot.highlightClosestPlaceholder(x, y, type);
};

scout.ContentEditor.prototype._showSlots = function(type) {
  this._slots.forEach(function(slot) {
    slot.show(type);
  });
};

scout.ContentEditor.prototype._stopAccepting = function() {
  this._slots.forEach(function(slot) {
    slot.stopAccepting();
  });
};

scout.ContentEditor.prototype._hideSlots = function() {
  this._slots.forEach(function(slot) {
    slot.hide();
  });
};

scout.ContentEditor.prototype._getClosestSlot = function(x, y) {
  var closest = null;
  var closestDistance;

  for (var i = 0; i < this._slots.length; i++) {
    var distance = this._getDistance(this._slots[i].$container, x, y);
    if (distance > 50) {
      continue;
    }

    if (!closest || distance < closestDistance) {
      closest = this._slots[i];
      closestDistance = distance;
    }
  }
  return closest;
};

scout.ContentEditor.prototype._getDistance = function($element, x, y) {
  var xOffset = 0;
  var yOffset = 0;

  var elementTop = $element.offset().top - $(this.doc).scrollTop();
  var elementLeft = $element.offset().left - $(this.doc).scrollLeft();
  var elementBottom = elementTop + $element.height();
  var elementRight = elementLeft + $element.width();

  if (elementTop > y) {
    yOffset = elementTop - y;
  } else if (elementBottom < y) {
    yOffset = y - elementTop - $element.height();
  }

  if (elementLeft > x) {
    xOffset = elementLeft - x;
  } else if (elementRight < x) {
    xOffset = x - elementLeft - $element.width();
  }
  return Math.sqrt(xOffset * xOffset + yOffset * yOffset);
};
