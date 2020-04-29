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
import {CheckBoxToggleKeyStroke, Device, fields, KeyStrokeContext, styles, tooltips, ValueField} from '../../../index';

export default class CheckBoxField extends ValueField {

  constructor() {
    super();

    this.triStateEnabled = false;
    this.wrapText = false;
    this.keyStroke = null;
    this.checkBoxKeyStroke = new CheckBoxToggleKeyStroke(this);

    this.$checkBox = null;
    this.$checkBoxLabel = null;
  }

  _init(model) {
    super._init(model);
    this._setKeyStroke(this.keyStroke);
  }

  /**
   * @override
   */
  _initKeyStrokeContext() {
    super._initKeyStrokeContext();

    this.keyStrokeContext.registerKeyStroke(new CheckBoxToggleKeyStroke(this));

    // The key stroke configured by this.keyStroke has form scope
    this.formKeyStrokeContext = new KeyStrokeContext();
    this.formKeyStrokeContext.invokeAcceptInputOnActiveValueField = true;
    this.formKeyStrokeContext.registerKeyStroke(this.checkBoxKeyStroke);
    this.formKeyStrokeContext.$bindTarget = function() {
      // use form if available
      let form = this.getForm();
      if (form) {
        return form.$container;
      }
      // use desktop otherwise
      return this.session.desktop.$container;
    }.bind(this);
  }

  _render() {
    this.addContainer(this.$parent, 'check-box-field');
    this.addLabel();
    this.addMandatoryIndicator();
    this.addFieldContainer(this.$parent.makeDiv());

    this.$checkBox = this.$fieldContainer
      .appendDiv('check-box')
      .on('mousedown', this._onMouseDown.bind(this))
      .data('valuefield', this);
    this.addField(this.$checkBox);

    this.$checkBoxLabel = this.$fieldContainer
      .appendDiv('label')
      .on('mousedown', this._onMouseDown.bind(this));

    fields.linkElementWithLabel(this.$checkBox, this.$checkBoxLabel);
    tooltips.installForEllipsis(this.$checkBoxLabel, {
      parent: this
    });
    this.addStatus();
    this.session.keyStrokeManager.installKeyStrokeContext(this.formKeyStrokeContext);
  }

  _renderProperties() {
    super._renderProperties();
    this._renderValue();
    this._renderWrapText();
  }

  _remove() {
    tooltips.uninstall(this.$checkBoxLabel);
    this.session.keyStrokeManager.uninstallKeyStrokeContext(this.formKeyStrokeContext);
    super._remove();
  }

  _renderDisplayText() {
    // NOP
  }

  setValue(value) {
    this.setProperty('value', value);
  }

  /**
   * The value may be false, true (and null in tri-state mode)
   */
  _renderValue() {
    this.$fieldContainer.toggleClass('checked', this.value === true);
    this.$checkBox.toggleClass('checked', this.value === true);
    this.$checkBox.toggleClass('undefined', this.triStateEnabled && this.value !== true && this.value !== false);
  }

  /**
   * @override
   */
  _renderEnabled() {
    super._renderEnabled();
    this.$checkBox
      .setTabbable(this.enabledComputed && !Device.get().supportsOnlyTouch())
      .setEnabled(this.enabledComputed);
  }

  setTriStateEnabled(triStateEnabled) {
    this.setProperty('triStateEnabled', triStateEnabled);
    if (this.rendered) {
      this._renderValue();
    }
  }

  /**
   * @override
   */
  _renderLabel() {
    this.$checkBoxLabel.contentOrNbsp(this.labelHtmlEnabled, this.label, 'empty');
    this._renderEmptyLabel();
  }

  /**
   * @override
   */
  _renderFont() {
    styles.legacyFont(this, this.$fieldContainer);
    // Changing the font may enlarge or shrink the field (e.g. set the style to bold makes the text bigger) -> invalidate layout
    this.invalidateLayoutTree();
  }

  /**
   * @override
   */
  _renderForegroundColor() {
    styles.legacyForegroundColor(this, this.$fieldContainer);
  }

  /**
   * @override
   */
  _renderBackgroundColor() {
    styles.legacyBackgroundColor(this, this.$fieldContainer);
  }

  _renderGridData() {
    super._renderGridData();
    this.updateInnerAlignment({
      useHorizontalAlignment: true
    });
  }

  _renderGridDataHints() {
    super._renderGridDataHints();
    this.updateInnerAlignment({
      useHorizontalAlignment: true
    });
  }

  setKeyStroke(keyStroke) {
    this.setProperty('keyStroke', keyStroke);
  }

  _setKeyStroke(keyStroke) {
    this._setProperty('keyStroke', keyStroke);
    this.checkBoxKeyStroke.parseAndSetKeyStroke(this.keyStroke);
  }

  setWrapText(wrapText) {
    this.setProperty('wrapText', wrapText);
  }

  _renderWrapText() {
    this.$checkBoxLabel.toggleClass('white-space-nowrap', !this.wrapText);
    this.invalidateLayoutTree();
  }

  acceptInput(whileTyping, forceSend) {
    // NOP
  }

  toggleChecked() {
    if (!this.enabledComputed) {
      return;
    }
    if (this.triStateEnabled) {
      if (this.value === false) {
        this.setValue(true);
      } else if (this.value === true) {
        this.setValue(null);
      } else {
        this.setValue(false);
      }
    } else {
      this.setValue(!this.value);
    }
  }

  prepareForCellEdit(opts) {
    super.prepareForCellEdit(opts);
    this.$checkBoxLabel.hide();
  }

  _onMouseDown(event) {
    if (!this.enabledComputed) {
      return;
    }
    this.toggleChecked();
    // Also focus when check box is clicked otherwise firefox would loose the focus (see device.loosesFocusIfPseudoElementIsRemoved)
    let onCheckBox = this.$checkBox.is(event.currentTarget),
      onLabel = this.$checkBoxLabel.isOrHas(event.currentTarget); // isOrHas is required for HTML enabled labels with nested elements
    if (onCheckBox || onLabel) {
      this.focusAndPreventDefault(event);
    }
  }
}
