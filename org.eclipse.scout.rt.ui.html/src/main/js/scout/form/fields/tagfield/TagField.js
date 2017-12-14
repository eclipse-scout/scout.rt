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
  this.$overflowIcon;
};
scout.inherits(scout.TagField, scout.ValueField);

scout.TagField.prototype._initKeyStrokeContext = function() {
  scout.TagField.parent.prototype._initKeyStrokeContext.call(this);
  this.keyStrokeContext.registerKeyStroke([
    new scout.TagFieldEnterKeyStroke(this)
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

  if (this.gridData.h > 1) {
    this.$fieldContainer.addClass('multiline');
  }
};

scout.TagField.prototype._renderProperties = function() {
  scout.TagField.parent.prototype._renderProperties.call(this);

  this._renderValue();
  this._renderOverflowVisible();
};

scout.TagField.prototype._renderValue = function() {
  var $cont = this.$fieldContainer;
  $cont.find('.tag-element').remove();

  var tags = scout.arrays.ensure(this.value);
  tags.forEach(function(tag) {

    var $element = $cont
      .prependDiv('tag-element')
      .text(tag);

    $element
      .appendSpan('tag-element-remove')
      .data('tag', tag)
      .on('click', this._onTagElementRemove.bind(this))
      .text('x');

  }.bind(this));

  this.fieldHtmlComp.revalidateLayout();
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

  tags.push(tag);
  return tags;
};

scout.TagField.prototype._renderDisplayText = function() {
  this.$field.val(this.displayText);
  scout.TagField.parent.prototype._renderDisplayText.call(this);
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

scout.TagField.prototype._triggerAcceptInput = function() {
  this.trigger('acceptInput', {
    displayText: this.displayText,
    value: this.value
  });
};

scout.TagField.prototype._onTagElementRemove = function(event) {
  if (this.enabledComputed) {
    var $element = $(event.currentTarget);
    this.removeTag($element.data('tag'));
  }
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
      .on('click', this._onOverflowIconClick.bind(this))
      .text('V');
    }
  } else {
    if (this.$overflowIcon) {
     this.$overflowIcon.remove();
     this.$overflowIcon = null;
    }
  }
};

scout.TagField.prototype._onOverflowIconClick = function(event) {
  this._openOverflowPopup();
};

scout.TagField.prototype._openOverflowPopup = function() {
  var popup = scout.create('TagFieldPopup', {
    parent: this,
    closeOnAnchorMouseDown: false,
    $anchor: this.$overflowIcon
  });
  popup.open();
};
