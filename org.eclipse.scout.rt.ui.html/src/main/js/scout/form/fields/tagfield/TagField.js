/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {keys} from '../../../index';
import {TagFieldNavigationKeyStroke} from '../../../index';
import {TagFieldOpenPopupKeyStroke} from '../../../index';
import {ValueField} from '../../../index';
import {TagFieldDeleteKeyStroke} from '../../../index';
import {TagFieldLayout} from '../../../index';
import {strings} from '../../../index';
import {HtmlComponent} from '../../../index';
import {TagFieldCancelKeyStroke} from '../../../index';
import {TagFieldEnterKeyStroke} from '../../../index';
import {scout} from '../../../index';
import {InputFieldKeyStrokeContext} from '../../../index';
import {LookupCall} from '../../../index';
import {TagFieldContainerLayout} from '../../../index';
import {arrays} from '../../../index';

export default class TagField extends ValueField {

constructor() {
  super();

  this.$field = null;
  this.fieldHtmlComp = null;
  this.chooser = null;
  this.lookupCall = null;
  this._currentLookupCall = null;
  this.tagBar = null;
}


_init(model) {
  super._init( model);

  this.tagBar = scout.create('TagBar', {
    parent: this,
    tags: this.value
  });
  this.tagBar.on('tagRemove', this._onTagRemove.bind(this));
  this.on('propertyChange', this._onValueChange.bind(this));
  this._setLookupCall(this.lookupCall);
}

_onTagRemove(event) {
  this.removeTag(event.tag);
}

_initKeyStrokeContext() {
  super._initKeyStrokeContext();
  this.keyStrokeContext.registerKeyStroke([
    new TagFieldCancelKeyStroke(this),
    new TagFieldEnterKeyStroke(this),
    new TagFieldNavigationKeyStroke(this._createFieldAdapter()),
    new TagFieldDeleteKeyStroke(this._createFieldAdapter()),
    new TagFieldOpenPopupKeyStroke(this)
  ]);
}

_createKeyStrokeContext() {
  return new InputFieldKeyStrokeContext();
}

_render() {
  this.addContainer(this.$parent, 'tag-field', new TagFieldLayout(this));
  this.addLabel();
  this.addMandatoryIndicator();
  var $fieldContainer = this.$container.appendDiv();
  this.fieldHtmlComp = HtmlComponent.install($fieldContainer, this.session);
  this.fieldHtmlComp.setLayout(new TagFieldContainerLayout(this));
  this.tagBar.render($fieldContainer);
  var $field = $fieldContainer.appendElement('<input>', 'field')
    .on('keydown', this._onInputKeydown.bind(this))
    .on('keyup', this._onInputKeyup.bind(this))
    .on('input', this._onFieldInput.bind(this));
  this.addFieldContainer($fieldContainer);
  this.addField($field);
  this.addStatus();
}

_renderProperties() {
  super._renderProperties();
  this._renderValue();
}

_renderValue() {
  this.tagBar.updateTags();
}

_setValue(value) {
  super._setValue( value);
  if (this.tagBar) { // required for _init case
    this.tagBar.setTags(this.value /* do not use the function parameter here. instead use the member variable because the value might have changed in a validator. */ );
  }
}

_setLookupCall(lookupCall) {
  this._setProperty('lookupCall', LookupCall.ensure(lookupCall, this.session));
}

formatValue(value) {
  // Info: value and displayText are not related in the TagField
  return '';
}

/**
 * @override ValueField.js
 */
_validateValue(value) {
  var tags = arrays.ensure(value);
  var result = [];
  tags.forEach(function(tag) {
    if (!strings.empty(tag)) {
      tag = tag.toLowerCase();
      if (result.indexOf(tag) < 0) {
        result.push(tag);
      }
    }
  });
  return result;
}

_parseValue(displayText) {
  var tags = arrays.ensure(this.value);
  tags = tags.slice();
  tags.push(displayText);
  return tags;
}

_renderDisplayText() {
  this.$field.val(this.displayText); // needs to be before super call (otherwise updateHasText fails)
  super._renderDisplayText();
  this._updateInputVisible();
}

_renderEnabled() {
  super._renderEnabled();
  this._updateInputVisible();
}

_renderFieldStyle() {
  super._renderFieldStyle();
  if (this.rendered) {
    this.fieldHtmlComp.invalidateLayoutTree();
  }
}

_updateInputVisible() {
  var visible, oldVisible = !this.$field.isVisible();
  if (this.enabledComputed) {
    visible = true;
  } else {
    visible = strings.hasText(this.displayText);
  }
  this.$field.setVisible(visible);
  // update tag-elements (must remove X when disabled)
  if (visible !== oldVisible) {
    this._renderValue();
  }
}

_readDisplayText() {
  return this.$field.val();
}

_clear() {
  this.$field.val('');
}

/**
 * @override
 */
acceptInput(whileTyping) {
  if (this.chooser) {
    if (this.chooser.selectedRow()) {
      this.chooser.triggerLookupRowSelected();
    } else {
      this.closeChooserPopup();
    }
    return;
  }
  super.acceptInput( false);
}

_triggerAcceptInput() {
  this.trigger('acceptInput', {
    displayText: this.displayText,
    value: this.value
  });
}

/**
 * @override
 */
_onFieldBlur(event) {
  // We cannot call super until chooser popup has been closed (see #acceptInput)
  this.closeChooserPopup();
  super._onFieldBlur( event);
  if (this.rendered && !this.removing) {
    this.tagBar.blur();
  }
}

/**
 * @override
 */
_onFieldFocus(event) {
  super._onFieldFocus( event);
  if (this.rendered && !this.removing) {
    this.tagBar.focus();
  }
}

_onFieldInput() {
  this._updateHasText();
}

addTag(text) {
  var value = this._parseValue(text);
  this.setValue(value);
  this._triggerAcceptInput();
}

removeTag(tag) {
  if (strings.empty(tag)) {
    return;
  }
  tag = tag.toLowerCase();
  var tags = arrays.ensure(this.value);
  if (tags.indexOf(tag) === -1) {
    return;
  }
  tags = tags.slice();
  arrays.remove(tags, tag);
  this.setValue(tags);
  this._triggerAcceptInput();
  // focus was previously on the removed tag, restore focus on the field.
  this.focus();
}

_onInputKeydown(event) {
  if (this._isNavigationKey(event) && this.chooser) {
    this.chooser.delegateKeyEvent(event);
  }
}

_isNavigationKey(event) {
  return scout.isOneOf(event.which, [
    keys.PAGE_UP,
    keys.PAGE_DOWN,
    keys.UP,
    keys.DOWN
  ]);
}

_onInputKeyup(event) {
  // Prevent chooser popup from being opened again, after it has been closed by pressing ESC
  if (event.which === keys.ESC) {
    return;
  }

  if (!this._isNavigationKey(event)) {
    this._lookupByText(this.$field.val());
  }
}

_lookupByText(text) {
  if (!this.lookupCall) {
    return null;
  }
  if (strings.empty(text) || text.length < 2) {
    this.closeChooserPopup();
    return;
  }

  this._currentLookupCall = this.lookupCall.cloneForText(text);
  this.trigger('prepareLookupCall', {
    lookupCall: this._currentLookupCall
  });
  return this._currentLookupCall
    .execute()
    .always(function() {
      this._currentLookupCall = null;
    }.bind(this))
    .done(this._onLookupDone.bind(this));
}

_onLookupDone(result) {
  try {
    if (!this.rendered || !this.isFocused() || result.lookupRows.length === 0) {
      this.closeChooserPopup();
      return;
    }

    this.openChooserPopup();
    this.chooser.setLookupResult(result);
  } finally {
    this.trigger('lookupCallDone', {
      result: result
    });
  }
}

openChooserPopup() {
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
}

closeChooserPopup() {
  if (this.chooser && !this.chooser.destroying) {
    this.chooser.close();
  }
}

_onLookupRowSelected(event) {
  this._clear();
  this._updateHasText();
  this.addTag(event.lookupRow.key);
  this.closeChooserPopup();
}

_onChooserPopupClose(event) {
  this.chooser = null;
}

isInputFocused() {
  var ae = this.$fieldContainer.activeElement();
  return this.$field.is(ae);
}

_onValueChange(event) {
  if ('value' === event.propertyName) {
    this._renderLabel();
  }
}

_renderPlaceholder($field) {
  // only render placeholder when tag field is empty (has no tags)
  var hasTags = !!arrays.ensure(this.value).length;
  $field = scout.nvl($field, this.$field);
  if ($field) {
    $field.placeholder(hasTags ? '' : this.label);
  }
}

_createFieldAdapter() {
  return TagField.createFieldAdapter(this);
}

static createFieldAdapter(field) {
  return {
    $container: function() {
      return field.$fieldContainer;
    },

    enabled: function() {
      return strings.empty(field._readDisplayText());
    },

    focus: function() {
      field.$field.focus();
    },

    one: function(p1, p2) {
      field.one(p1, p2);
    },

    off: function(p1, p2) {
      field.off(p1, p2);
    },

    removeTag: function(tag) {
      field.removeTag(tag);
    }
  };
}
}
