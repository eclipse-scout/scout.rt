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
import {
  arrays,
  fields,
  HtmlComponent,
  InputFieldKeyStrokeContext,
  keys,
  LookupCall,
  scout,
  strings,
  TagFieldContainerLayout,
  TagFieldDeleteKeyStroke,
  TagFieldEnterKeyStroke,
  TagFieldLayout,
  TagFieldNavigationKeyStroke,
  TagFieldOpenPopupKeyStroke,
  ValueField
} from '../../../index';

export default class TagField extends ValueField {

  constructor() {
    super();

    this.$field = null;
    this.fieldHtmlComp = null;
    this.popup = null;
    this.lookupCall = null;
    this._currentLookupCall = null;
    this.tagBar = null;
    this.maxLength = 500;
    this.maxLengthHandler = scout.create('MaxLengthHandler', {target: this});
  }

  _init(model) {
    super._init(model);

    this.tagBar = scout.create('TagBar', {
      parent: this,
      tags: this.value,
      clickable: model.clickable
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
    let $fieldContainer = this.$container.appendDiv();
    this.fieldHtmlComp = HtmlComponent.install($fieldContainer, this.session);
    this.fieldHtmlComp.setLayout(new TagFieldContainerLayout(this));
    this.tagBar.render($fieldContainer);
    let $field = $fieldContainer.appendElement('<input>', 'field')
      .attr('type', 'text') // So that css rules from main.less are applied
      .on('keydown', this._onInputKeydown.bind(this))
      .on('keyup', this._onInputKeyup.bind(this))
      .on('input', this._onFieldInput.bind(this));
    this.addFieldContainer($fieldContainer);
    this.addField($field);
    this.maxLengthHandler.install($field);
    this.addStatus();
  }

  _renderProperties() {
    super._renderProperties();
    this._renderValue();
    this._renderMaxLength();
  }

  _renderValue() {
    this.tagBar.updateTags();
  }

  _setValue(value) {
    super._setValue(value);
    if (this.tagBar) { // required for _init case
      this.tagBar.setTags(this.value /* do not use the function parameter here. instead use the member variable because the value might have changed in a validator. */);
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
    let tags = arrays.ensure(value);
    let result = [];
    tags.forEach(tag => {
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
    let tags = arrays.ensure(this.value);
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

  setMaxLength(maxLength) {
    this.setProperty('maxLength', maxLength);
  }

  _renderMaxLength() {
    this.maxLengthHandler.render();
  }

  _updateInputVisible() {
    let visible, oldVisible = !this.$field.isVisible();
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
    if (this.popup) {
      if (this.popup.selectedRow()) {
        this.popup.triggerLookupRowSelected();
      } else {
        this.closePopup();
      }
      return;
    }
    super.acceptInput(false);
  }

  _triggerAcceptInput() {
    this.trigger('acceptInput', {
      displayText: this.displayText,
      value: this.value
    });
  }

  aboutToBlurByMouseDown(target) {
    if (fields.eventOutsideProposalField(this, target)) {
      this.acceptInput(true);
    }
  }

  /**
   * @override
   */
  _onFieldBlur(event) {
    // We cannot call super until chooser popup has been closed (see #acceptInput)
    this.closePopup();
    super._onFieldBlur(event);
    if (this.rendered && !this.removing) {
      this.tagBar.blur();
    }
  }

  /**
   * @override
   */
  _onFieldFocus(event) {
    super._onFieldFocus(event);
    if (this.rendered && !this.removing) {
      this.tagBar.focus();
    }
  }

  _onFieldInput() {
    this._updateHasText();
  }

  addTag(text) {
    let value = this._parseValue(text);
    this.setValue(value);
    this._triggerAcceptInput();
  }

  removeTag(tag) {
    if (strings.empty(tag)) {
      return;
    }
    tag = tag.toLowerCase();
    let tags = arrays.ensure(this.value);
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
    if (this._isNavigationKey(event) && this.popup) {
      this.popup.delegateKeyEvent(event);
    } else if (event.which === keys.ESC) {
      this.closePopup();
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
      this.closePopup();
      return;
    }

    this._currentLookupCall = this.lookupCall.cloneForText(text);
    this.trigger('prepareLookupCall', {
      lookupCall: this._currentLookupCall
    });
    return this._currentLookupCall
      .execute()
      .always(() => {
        this._currentLookupCall = null;
      })
      .done(this._onLookupDone.bind(this));
  }

  _onLookupDone(result) {
    try {
      if (!this.rendered || !this.isFocused() || result.lookupRows.length === 0) {
        this.closePopup();
        return;
      }

      this.openPopup();
      this.popup.setLookupResult(result);
    } finally {
      this.trigger('lookupCallDone', {
        result: result
      });
    }
  }

  openPopup() {
    if (this.popup) {
      return;
    }
    this.popup = scout.create('TagChooserPopup', {
      parent: this,
      $anchor: this.$field,
      boundToAnchor: true,
      closeOnAnchorMouseDown: false,
      field: this
    });
    this.popup.on('lookupRowSelected', this._onLookupRowSelected.bind(this));
    this.popup.one('close', this._onPopupClose.bind(this));
    this.popup.open();
  }

  closePopup() {
    if (this.popup && !this.popup.destroying) {
      this.popup.close();
    }
  }

  _onLookupRowSelected(event) {
    this._clear();
    this._updateHasText();
    this.addTag(event.lookupRow.key);
    this.closePopup();
  }

  _onPopupClose(event) {
    this.popup = null;
  }

  isInputFocused() {
    let ae = this.$fieldContainer.activeElement();
    return this.$field.is(ae);
  }

  _onValueChange(event) {
    if ('value' === event.propertyName) {
      this._renderLabel();
    }
  }

  _renderPlaceholder($field) {
    // only render placeholder when tag field is empty (has no tags)
    let hasTags = !!arrays.ensure(this.value).length;
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
      $container: () => field.$fieldContainer,

      enabled: () => strings.empty(field._readDisplayText()),

      focus: () => {
        field.$field.focus();
      },

      one: (p1, p2) => {
        field.one(p1, p2);
      },

      off: (p1, p2) => {
        field.off(p1, p2);
      },

      removeTag: tag => {
        field.removeTag(tag);
      }
    };
  }
}
