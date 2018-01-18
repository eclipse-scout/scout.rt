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
  this.overflow = null;
  this.chooser = null;
};
scout.inherits(scout.TagField, scout.ValueField);

scout.TagField.prototype._init = function(model) {
  scout.TagField.parent.prototype._init.call(this, model);

  this.on('propertyChange', this._onValueChange.bind(this));
  this._setLookupCall(this.lookupCall);
};

scout.TagField.prototype._initKeyStrokeContext = function() {
  scout.TagField.parent.prototype._initKeyStrokeContext.call(this);
  this.keyStrokeContext.registerKeyStroke([
    new scout.TagFieldCancelKeyStroke(this),
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
  var $field = this.$fieldContainer.appendElement('<input>', 'field')
    .on('keydown', this._onInputKeydown.bind(this))
    .on('keyup', this._onInputKeyup.bind(this));

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

scout.TagField.prototype._setLookupCall = function(lookupCall) {
  this._setProperty('lookupCall', scout.LookupCall.ensure(lookupCall, this.session));
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

scout.TagField.prototype.acceptInput = function() {
  if (this.chooser) {
    this.chooser.triggerLookupRowSelected();
    return;
  }
  scout.TagField.parent.prototype.acceptInput.call(this);
};

scout.TagField.prototype._triggerAcceptInput = function() {
  this.trigger('acceptInput', {
    displayText: this.displayText,
    value: this.value
  });
};

scout.TagField.prototype._onFieldBlur = function(event) {
  this.$fieldContainer.removeClass('focused');
  this.closeChooserPopup();

  // We cannot call super until chooser popup has been closed (see #acceptInput)
  scout.TagField.parent.prototype._onFieldBlur.call(this, event);

  // when overflow popup opens it sets focus to the first tag element, this means:
  // the input field loses focus. In that case we must prevent that the overflow popup is closed.
  var popupRequestsFocus = this.overflow && this.overflow.$container.has(event.relatedTarget);
  if (popupRequestsFocus) {
    return;
  }
  this.closeOverflowPopup();
};

scout.TagField.prototype._onFieldFocus = function(event) {
  scout.TagField.parent.prototype._onFieldFocus.call(this, event);
  this.$fieldContainer.addClass('focused');
  this._removeFocusFromTagElements();
  this.closeOverflowPopup();
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

scout.TagField.prototype.addTag = function(text) {
  var value = this._parseValue(text);
  this.setValue(value);
  this._triggerAcceptInput();
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
        .on('mousedown', this._onOverflowIconMousedown.bind(this));
    }
  } else {
    if (this.$overflowIcon) {
     this.$overflowIcon.remove();
     this.$overflowIcon = null;
    }
  }
};

scout.TagField.prototype._onInputKeydown = function(event) {
  if (event.which === scout.keys.ENTER) {
    this._handleEnterKey(event);
    return;
  }

  if (this._isNavigationKey(event) && this.chooser) {
    this.chooser.delegateKeyEvent(event);
  }
};

scout.TagField.prototype._isNavigationKey = function(event) {
  return scout.isOneOf(event.which, [
    scout.keys.PAGE_UP,
    scout.keys.PAGE_DOWN,
    scout.keys.UP,
    scout.keys.DOWN
  ]);
};

scout.TagField.prototype._onInputKeyup = function(event) {
  // Prevent chooser popup from being opened again, after it has been closed by pressing ESC
  if (event.which === scout.keys.ESC) {
    return;
  }

  if (!this._isNavigationKey(event)) {
    this._lookupByText(this.$field.val());
  }
};

scout.TagField.prototype._handleEnterKey = function(event) {
  if (this.chooser) {
    this.chooser.triggerLookupRowSelected();
    event.stopPropagation();
  }
};

scout.TagField.prototype._lookupByText = function(text) {
  if (!this.lookupCall) {
    return null;
  }
  if (scout.strings.empty(text) || text.length < 2) {
    return;
  }
  return this.lookupCall.getByText(text)
    .done(this._onLookupDone.bind(this));
};

scout.TagField.prototype._onLookupDone = function(result) {
  if (!this.rendered || !this.isFocused() || result.lookupRows.length === 0) {
    this.closeChooserPopup();
    return;
  }

  this.openChooserPopup();
  this.chooser.setLookupResult(result);
};

scout.TagField.prototype.openChooserPopup = function() {
  if (this.chooser) {
    return;
  }
  this.chooser = scout.create('TagChooserPopup', {
    parent: this,
    $anchor: this.$field,
    closeOnAnchorMouseDown: false,
    field: this
  });
  this.chooser.on('lookupRowSelected', this._onLookupRowSelected.bind(this));
  this.chooser.one('close', this._onChooserPopupClose.bind(this));
  this.chooser.open();
};

scout.TagField.prototype.closeChooserPopup = function() {
  if (this.chooser && !this.chooser.destroying) {
    this.chooser.close();
  }
};

scout.TagField.prototype._onLookupRowSelected = function(event) {
  this._clear();
  this.addTag(event.lookupRow.key);
  this.closeChooserPopup();
};

scout.TagField.prototype._onChooserPopupClose = function(event) {
  this.chooser = null;
};

scout.TagField.prototype._onOverflowIconMousedown = function(event) {
  this.openOverflowPopup();
};

scout.TagField.prototype.openOverflowPopup = function() {
  if (this.overflow) {
    return;
  }
  this.overflow = scout.create('TagOverflowPopup', {
    parent: this,
    closeOnAnchorMouseDown: false,
    focusableContainer: true,
    $anchor: this.$fieldContainer,
    $headBlueprint: this.$overflowIcon
  });
  this.overflow.on('close', this._onOverflowPopupClose.bind(this));
  this.overflow.open();
};

scout.TagField.prototype.closeOverflowPopup = function() {
  if (this.overflow && !this.overflow.destroying) {
    this.overflow.close();
  }
};

scout.TagField.prototype._onOverflowPopupClose = function() {
  this.overflow = null;
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
  scout.TagField.findFocusableTagElements(this.$fieldContainer)
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

scout.TagField.prototype._onValueChange = function(event) {
  if ('value' === event.propertyName) {
    this._renderLabel();
  }
};

scout.TagField.prototype._renderPlaceholder = function($field) {
  // only render placeholder when tag field is empty (has no tags)
  var hasTags = !!scout.arrays.ensure(this.value).length;
  $field = scout.nvl($field, this.$field);
  if ($field) {
    $field.placeholder(hasTags ? '' : this.label);
  }
};

// --- static helpers ---

scout.TagField.focusFirstTagElement = function($container) {
  this.focusTagElement(this.firstTagElement($container));
};

scout.TagField.firstTagElement = function($container) {
  return $container.find('.tag-element').first();
};

scout.TagField.focusTagElement = function($tagElement) {
  $tagElement
    .setTabbable(true)
    .addClass('focused')
    .focus();
};

scout.TagField.unfocusTagElement = function($tagElement) {
  $tagElement
    .setTabbable(false)
    .removeClass('focused');
};

scout.TagField.findFocusedTagElement = function($container) {
  return $container.find('.tag-element.focused');
};

scout.TagField.findFocusableTagElements = function($container) {
  return $container.find('.tag-element:not(.hidden),.overflow-icon');
};

