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
import {FileChooserFieldDeleteKeyStroke} from '../../../index';
import {ValueField} from '../../../index';
import {FileInput} from '../../../index';
import {scout} from '../../../index';
import {objects} from '../../../index';
import {arrays} from '../../../index';
import {FileChooserFieldBrowseKeyStroke} from '../../../index';

export default class FileChooserField extends ValueField {

constructor() {
  super();

  this.fileInput = null;

  this.acceptTypes = null;
  this.maximumUploadSize = FileInput.DEFAULT_MAXIMUM_UPLOAD_SIZE;
}


_init(model) {
  super._init( model);

  this.fileInput.on('change', this._onFileChange.bind(this));
  this.on('propertyChange', function(event) {
    if (event.propertyName === 'enabledComputed') {
      // Propagate "enabledComputed" to inner widget
      this.fileInput.setEnabled(event.newValue);
    }
  }.bind(this));
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
    maximumUploadSize: this.maximumUploadSize
  });

  super._initValue( value);
}

_initKeyStrokeContext() {
  super._initKeyStrokeContext();
  if (!this.fileInput.legacy) {
    this.keyStrokeContext.registerKeyStroke(new FileChooserFieldBrowseKeyStroke(this));
    this.keyStrokeContext.registerKeyStroke(new FileChooserFieldDeleteKeyStroke(this));
  }
}

_render() {
  this.addContainer(this.$parent, 'file-chooser-field has-icon');
  this.addLabel();
  this.addMandatoryIndicator();
  this._renderFileInput();
  this.addIcon();
  this.addStatus();
}

_renderFileInput() {
  this.fileInput.render();
  this.addField(this.fileInput.$container);
}

setDisplayText(text) {
  super.setDisplayText( text);
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

setAcceptTypes(acceptTypes) {
  this.setProperty('acceptTypes', acceptTypes);
  this.fileInput.setAcceptTypes(acceptTypes);
}

_renderEnabled() {
  super._renderEnabled();
  this.$field.setTabbable(this.enabledComputed);
}

_renderPlaceholder() {
  var $field = this.fileInput.$text;
  if ($field) {
    $field.placeholder(this.label);
  }
}

_removePlaceholder() {
  var $field = this.fileInput.$text;
  if ($field) {
    $field.placeholder('');
  }
}

setMaximumUploadSize(maximumUploadSize) {
  this.setProperty('maximumUploadSize', maximumUploadSize);
  this.fileInput.setMaximumUploadSize(maximumUploadSize);
}

_clear() {
  this.fileInput.clear();
}

_onIconMouseDown(event) {
  super._onIconMouseDown( event);
  this.activate();
}

_onFileChange(event) {
  var file = arrays.first(event.files);
  if (objects.isNullOrUndefined(file)) {
    this.acceptInput(false);
  }
  this.setValue(file);
}

/**
 * @override
 */
activate() {
  if (!this.enabledComputed || !this.rendered) {
    return;
  }
  this.$field.focus();
  this.fileInput.browse();
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
}
