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
scout.TagField = function() {
  scout.TagField.parent.call(this);

  this.$field = null;
  this.fieldHtmlComp = null;
  this.overflowVisible = false;
  this.$overflowIcon = null;
};
scout.inherits(scout.TagField, scout.ValueField);

scout.TagField.prototype._initKeyStrokeContext = function() {
  scout.TagField.parent.prototype._initKeyStrokeContext.call(this);
  this.keyStrokeContext.registerKeyStroke([
    new scout.TagFieldEnterKeyStroke(this),
    new scout.TagFieldNavigationKeyStroke(this),
    new scout.TagFieldDeleteKeyStroke(this),
    new scout.TagFieldOpenPopupKeyStroke(this)
  ]);
};

scout.TagField.prototype._createKeyStrokeContext = function() {
  return new scout.InputFieldKeyStrokeContext();
};

scout.TagField.prototype._render = function() {
  this.addContainer(this.$parent, 'tag-field');
  this.addLabel();
  this.addMandatoryIndicator();

  this.addFieldContainer(this.$parent.makeDiv());
  this.fieldHtmlComp = scout.HtmlComponent.install(this.$fieldContainer, this.session);
  this.fieldHtmlComp.setLayout(new scout.TagFieldLayout(this));
  var $field = this.$fieldContainer.appendElement('<input>', 'field');

  this.addField($field);
  this.addStatus();
  this._installTooltipSupport();
};

scout.TagField.prototype._renderProperties = function() {
  scout.TagField.parent.prototype._renderProperties.call(this);

  this._renderValue();
  this._renderOverflowVisible();
};

scout.TagField.prototype._remove = function() {
  this._uninstallTooltipSupport();
  scout.TagField.parent.prototype._remove.call(this);
};

scout.TagField.prototype._renderValue = function() {
  this.$fieldContainer.find('.tag-element').remove();

  var tags = scout.arrays.ensure(this.value);
  tags.forEach(function(tagText) {
    this._makeTagElement(this.$fieldContainer, tagText, this._onTagRemoveClick.bind(this))
      .insertBefore(this.$field);
  }.bind(this));

  if (!this.rendering) {
    this.fieldHtmlComp.revalidateLayout();
  }
};

scout.TagField.prototype._makeTagElement = function($parent, tagText, clickHandler) {
  var $element = this.$fieldContainer
      .makeDiv('tag-element')
      .data('tag', tagText);
  $element.appendSpan('tag-text', tagText);
  if (this.enabledComputed) {
    $element.appendSpan('tag-remove-icon')
      .on('click', clickHandler);
  } else {
    $element.addClass('disabled');
  }
  return $element;
};

scout.TagField.prototype.formatValue = function(value) {
  // Info: value and displayText are not related in the TagField
  return '';
};

scout.TagField.prototype._parseValue = function(displayText) {
  var tags = scout.arrays.ensure(this.value);

  if (scout.strings.empty(displayText)) {
    return tags;
  }
  var tag = displayText.toLowerCase();
  if (tags.indexOf(tag) > -1) {
    return tags;
  }

  tags = tags.slice();
  tags.push(tag);
  return tags;
};

scout.TagField.prototype._renderDisplayText = function() {
  this.$field.val(this.displayText); // needs to be before super call (otherwise updateHasText fails)
  scout.TagField.parent.prototype._renderDisplayText.call(this);
  this._updateInputVisible();
};

scout.TagField.prototype._renderEnabled = function() {
  scout.TagField.parent.prototype._renderEnabled.call(this);
  this._updateInputVisible();
};

scout.TagField.prototype._updateInputVisible = function() {
  var visible, oldVisible = !this.$field.isVisible();
  if (this.enabledComputed) {
    visible = true;
  } else {
    visible = scout.strings.hasText(this.displayText);
  }
  this.$field.setVisible(visible);
  // update tag-elements (must remove X when disabled)
  if (visible !== oldVisible) {
    this._renderValue();
  }
};

scout.TagField.prototype._readDisplayText = function() {
  return this.$field.val();
};

scout.TagField.prototype.addField = function($field) { // FIXME [awe] copy/paste from BasicField. check if it is better to inherit from that class
  scout.TagField.parent.prototype.addField.call(this, $field);
  if ($field) {
    $field.on('blur', this._onFieldBlur.bind(this))
      .on('focus', this._onFieldFocus.bind(this))
      .on('input', this._onFieldInput.bind(this));
  }
};

scout.TagField.prototype._clear = function() {
  this.$field.val('');
};

scout.TagField.prototype._triggerAcceptInput = function() {
  this.trigger('acceptInput', {
    displayText: this.displayText,
    value: this.value
  });
};

scout.TagField.prototype._onFieldFocus = function(event) {
  scout.TagField.parent.prototype._onFieldFocus.call(this, event);
  this._removeFocusFromTagElements();
};

scout.TagField.prototype._onTagRemoveClick = function(event) {
  if (this.enabledComputed) {
    this.removeTagByElement($(event.currentTarget));
  }
};

scout.TagField.prototype.getTagData = function($tag) {
  var tagData = $tag.data('tag');
  if (tagData) {
    return tagData;
  }
  return $tag.parent().data('tag');
};

scout.TagField.prototype.removeTagByElement = function($tag) {
  this.removeTag(this.getTagData($tag));
};

scout.TagField.prototype.removeTag = function(tag) {
  if (scout.strings.empty(tag)) {
    return;
  }
  tag = tag.toLowerCase();
  var tags = scout.arrays.ensure(this.value);
  if (tags.indexOf(tag) === -1) {
    return;
  }
  tags = tags.slice();
  scout.arrays.remove(tags, tag);
  this.setValue(tags);
  this._triggerAcceptInput();
};

scout.TagField.prototype.setOverflowVisible = function(overflowVisible) {
  this.setProperty('overflowVisible', overflowVisible);
};

scout.TagField.prototype._renderOverflowVisible = function() {
  if (this.overflowVisible) {
    if (!this.$overflowIcon) {
      this.$overflowIcon = this.$fieldContainer
        .prependDiv('overflow-icon')
        .on('click', this._onOverflowIconClick.bind(this));
    }
  } else {
    if (this.$overflowIcon) {
     this.$overflowIcon.remove();
     this.$overflowIcon = null;
    }
  }
};

scout.TagField.prototype._onOverflowIconClick = function(event) {
  this.openOverflowPopup();
};

scout.TagField.prototype.openOverflowPopup = function() {
  var popup = scout.create('TagFieldPopup', {
    parent: this,
    closeOnAnchorMouseDown: false,
    $anchor: this.$fieldContainer
  });
  popup.open();
};

scout.TagField.prototype._installTooltipSupport = function() {
  scout.tooltips.install(this.$fieldContainer, {
    parent: this,
    selector: '.tag-text',
    text: this._tagTooltipText.bind(this),
    arrowPosition: 50,
    arrowPositionUnit: '%',
    nativeTooltip: !scout.device.isCustomEllipsisTooltipPossible()
  });
};

scout.TagField.prototype._uninstallTooltipSupport = function() {
  scout.tooltips.uninstall(this.$fieldContainer);
};

scout.TagField.prototype._tagTooltipText = function($tag) {
  return $tag.isContentTruncated() ? $tag.text() : null;
};

scout.TagField.prototype._removeFocusFromTagElements = function() {
  this.$fieldContainer.find('.tag-element:not(.hidden),.overflow-icon')
    .removeClass('focused')
    .setTabbable(false);
};

scout.TagField.prototype.isInputFocused = function() {
  var ae = this.$fieldContainer.activeElement();
  return this.$field.is(ae);
};

scout.TagField.prototype.isOverflowIconFocused = function() {
  if (!this.$overflowIcon) {
    return false;
  }
  var ae = this.$fieldContainer.activeElement();
  return this.$overflowIcon.is(ae);
};

/**
 * Returns the tag-texts of the tag-elements currently visible in the UI (=not hidden).
 */
scout.TagField.prototype.visibleTags = function() {
  if (!this.rendered) {
    return [];
  }
  var tags = [], that = this;
  this.$fieldContainer
    .find('.tag-element:not(.hidden)')
    .each(function() {
      tags.push(that.getTagData($(this)));
     });
  return tags;
};
