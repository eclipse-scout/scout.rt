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
scout.ContentEditorSlot = function() {
  this.contentEditor = null;

  this.$container = null;
  this.$label = null;

  this.$maxElementsLabel = null;
  this.$minElementsLabel = null;
  this.$elementTypesLabel = null;

  this.maxElements = null;
  this.minElements = null;

  this._$closestPlaceholder = null;
};

scout.ContentEditorSlot.prototype.init = function(model) {
  this.contentEditor = model.contentEditor;
  this.$container = model.$container;

  this.$container.addClass('ce-slot');
  this._renderLabel();
  this._showMandatoryPlaceholders();
};

scout.ContentEditorSlot.prototype.name = function() {
  return this.$container.attr('data-ce-dropzone');
};

scout.ContentEditorSlot.prototype._renderLabel = function() {
  var label = '';
  this.minElements = this.$container.attr('data-ce-dropzone-min-number-of-elements');
  this.maxElements = this.$container.attr('data-ce-dropzone-max-number-of-elements');

  this.$label = this.$container.appendDiv('ce-slot-label').text(label);

  if (this.minElements) {
    this.$minElementsLabel = $('<span class="ce-slot-min-elements-label"> Min: ' + this.minElements + '</span>');
    this.$label.append(this.$minElementsLabel);
  }
  if (this.maxElements) {
    this.$maxElementsLabel = $('<span class="ce-slot-max-elements-label"> Max: ' + this.maxElements + '</span>');
    this.$label.append(this.$maxElementsLabel);
  }
  this.$elementTypesLabel = $('<span class="ce-slot-element-types-label"> Types: ' + this.$container.attr('data-ce-dropzone-allowed-elements') + '</span>');
  this.$label.append(this.$elementTypesLabel);
};

scout.ContentEditorSlot.prototype.show = function(type) {
  this.$container.addClass('ce-slot-visible');
};

scout.ContentEditorSlot.prototype.hide = function(type) {
  this.$container.removeClass('ce-slot-visible ce-slot-invalid ce-slot-accept');
};

scout.ContentEditorSlot.prototype.hasType = function(type) {
  if (type === 'text/plain') {
    type = 'text';
  }
  return this.$container.is('[data-ce-dropzone-allowed-elements~=\'' + type + '\']');
};

scout.ContentEditorSlot.prototype.isFree = function() {
  if (!this.maxElements) {
    return true;
  }
  var numberOfElements = this.$container.children('.ce-element').length;
  return this.maxElements > numberOfElements;
};

scout.ContentEditorSlot.prototype.requestAccepting = function(type) {
  var isOccupied = !this.isFree();
  var isTypeMissing = !this.hasType(type);

  if (isOccupied || isTypeMissing) {
    this.showLabel(isOccupied, isTypeMissing);
  } else {
    this.$container.addClass("ce-slot-accept");
    this.showPlaceholders();
  }
};

scout.ContentEditorSlot.prototype._placeholderWidth = function() {
  return this.$container.width() - 24;
};

scout.ContentEditorSlot.prototype.showPlaceholders = function() {
  // don't show placeholders if there are mandatory placeholders: mandatory placeholders need to be filled first.
  if (this.$container.find('.ce-mandatory-placeholder').length > 0) {
    return;
  }
  this.$container.removeClass('ce-slot-mandatory');
  var width = this._placeholderWidth();

  // add placeholder in the beginning of the slot if not already there.
  if (this.$container.children().first().hasClass('ce-placeholder')) {
    this.$container.children().first().width(width);
  } else {
    this.$container.prependDiv('ce-placeholder').width(width);
  }

  // add placeholders after each element if not already there.
  this.$container.find('.ce-element').each(function() {
    var $element = $(this);
    if ($element.next().hasClass('ce-placeholder')) {
      $element.next().width(width);
    } else {
      $element.afterDiv('ce-placeholder').width(width);
    }
  });
};

scout.ContentEditorSlot.prototype._showMandatoryPlaceholders = function() {
  var numberOfMandatoryPlaceholders = this.$container.attr('data-ce-dropzone-min-number-of-elements') - this.$container.find('.ce-element').length;
  for (var i = 0; i < numberOfMandatoryPlaceholders; i++) {
    this.$container
      .appendDiv('ce-placeholder ce-mandatory-placeholder')
      .text(this.contentEditor.dropzoneLabel);
  }
  if (this._hasMandatoryPlaceholders()) {
    this.$container.addClass('ce-slot-mandatory');
  } else {
    this.$container.removeClass('ce-slot-mandatory');
  }
};

scout.ContentEditorSlot.prototype._hasMandatoryPlaceholders = function() {
  return this.$container.find('.ce-mandatory-placeholder').length > 0;
};

scout.ContentEditorSlot.prototype.showLabel = function(isOccupied, isTypeMissing) {
  this.$container.addClass('ce-slot-invalid');

  if (isOccupied) {
    this.$maxElementsLabel.addClass('ce-slot-label-invalid');
  } else if (this.$maxElementsLabel) {
    this.$maxElementsLabel.removeClass('ce-slot-label-invalid');
  }

  if (isTypeMissing) {
    this.$elementTypesLabel.addClass('ce-slot-label-invalid');
  } else {
    this.$elementTypesLabel.removeClass('ce-slot-label-invalid');
  }
};

scout.ContentEditorSlot.prototype.isAccepting = function() {
  return this.$container.hasClass('ce-slot-accept');
};

scout.ContentEditorSlot.prototype.highlightClosestPlaceholder = function(x, y, type) {
  var $closest = this._getClosestPlaceholder(x, y);
  if (!this.hasType(type)) {
    return null;
  }
  if ($closest && this._$closestPlaceholder !== $closest) {
    this._$closestPlaceholder = $closest;
    this.$container.find('.ce-placeholder').each(function(index, placeholder) {
      if ($(placeholder) !== $closest) {
        $(placeholder).removeClass('ce-placeholder-accept-drop');
      }
    }.bind(this));
    $closest.addClass('ce-placeholder-accept-drop');
  }
  return $closest;
};

scout.ContentEditorSlot.prototype._getClosestPlaceholder = function(x, y) {
  var $closest;
  var closestDistance;

  this.$container.find('.ce-placeholder').each(function(index, placeholder) {
    var $placeholder = $(placeholder);
    var distance = this.contentEditor._getDistance($placeholder, x, y);
    if (!$closest || distance < closestDistance) {
      $closest = $placeholder;
      closestDistance = distance;
    }
  }.bind(this));

  return $closest;
};

scout.ContentEditorSlot.prototype.stopAccepting = function() {
  this.$container.removeClass('ce-slot-accept ce-slot-invalid');

  var $placeholder = this.$container.find('.ce-placeholder');
  $placeholder.each(function(i) {
    scout.arrays.remove(this.contentEditor._iframeDragTargetList, $placeholder[i]);
  }.bind(this));
  this.$container.find('.ce-placeholder:not(.ce-mandatory-placeholder)').fadeOut(200, function() {
    $(this).remove();
  });
  this.$container.find('.ce-mandatory-placeholder').removeClass('ce-placeholder-accept-drop');
};
