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
import {arrays, Device, FileInput, HtmlComponent, scout, SingleLayout, strings, ValueField} from '../../../index';

export default class FileChooserButton extends ValueField {

  constructor() {
    super();

    this.button = null;
    this.fileInput = null;

    this.acceptTypes = null;
    this.maximumUploadSize = FileInput.DEFAULT_MAXIMUM_UPLOAD_SIZE;
  }

  _init(model) {
    super._init(model);

    this.button = scout.create('Button', {
      parent: this,
      label: this._buttonLabel(),
      iconId: this.iconId,
      labelHtmlEnabled: this.labelHtmlEnabled
    });
    this.button.on('click', this._onButtonClick.bind(this));

    this.fileInput.on('change', this._onFileChange.bind(this));
    this.on('propertyChange', event => {
      if (event.propertyName === 'enabledComputed') {
        // Propagate "enabledComputed" to inner widgets
        this.button.setEnabled(event.newValue);
        this.fileInput.setEnabled(event.newValue);
      }
    });
  }

  /**
   * Initializes the file input before calling set value.
   * This cannot be done in _init because the value field would call _setValue first
   */
  _initValue(value) {
    this.fileInput = scout.create('FileInput', {
      parent: this,
      acceptTypes: this.acceptTypes,
      text: this.displayText,
      enabled: this.enabledComputed,
      maximumUploadSize: this.maximumUploadSize,
      visible: !Device.get().supportsFile()
    });

    super._initValue(value);
  }

  _buttonLabel() {
    return this.label;
  }

  _render() {
    this.addContainer(this.$parent, 'file-chooser-button has-icon');
    this.addLabel();

    let $field = this.$parent.makeDiv();
    let fieldHtmlComp = HtmlComponent.install($field, this.session);
    this.button.render($field);

    fieldHtmlComp.setLayout(new SingleLayout(this.button.htmlComp));
    this.fileInput.render($field);
    this.addField($field);
    $field.setTabbable(false);

    this.addStatus();
  }

  setDisplayText(text) {
    super.setDisplayText(text);
    this.fileInput.setText(text);
    if (!text) {
      this.fileInput.clear();
    }
  }

  /**
   * @override
   */
  _readDisplayText() {
    return this.fileInput.text;
  }

  /**
   * @override
   */
  _renderLabel() {
    this._renderEmptyLabel();
  }

  _onButtonClick(event) {
    this.fileInput.browse();
  }

  setLabel(label) {
    super.setLabel(label);
    this.button.setLabel(this._buttonLabel());
  }

  setIconId(iconId) {
    this.setProperty('iconId', iconId);
    this.button.setIconId(iconId);
  }

  setAcceptTypes(acceptTypes) {
    this.setProperty('acceptTypes', acceptTypes);
    this.fileInput.setAcceptTypes(acceptTypes);
  }

  setFileExtensions(fileExtensions) {
    this.setProperty('fileExtensions', fileExtensions);
    let acceptTypes = arrays.ensure(fileExtensions);
    acceptTypes = acceptTypes.map(acceptType => {
      return acceptType.indexOf(0) === '.' ? acceptType : '.' + acceptType;
    });
    this.setAcceptTypes(strings.join(',', acceptTypes));
    this.fileInput.acceptTypes = this.acceptTypes;
  }

  setMaximumUploadSize(maximumUploadSize) {
    this.setProperty('maximumUploadSize', maximumUploadSize);
    this.fileInput.setMaximumUploadSize(maximumUploadSize);
  }

  _onFileChange(event) {
    this.setValue(arrays.first(event.files));
  }

  /**
   * @override
   */
  _validateValue(value) {
    this.fileInput.validateMaximumUploadSize(value);
    return value;
  }

  /**
   * @override
   */
  _formatValue(value) {
    return !value ? '' : value.name;
  }

  /**
   * @override
   */
  getFocusableElement() {
    return this.button.getFocusableElement();
  }
}
