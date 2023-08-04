/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {aria, arrays, FileChooserFieldBrowseKeyStroke, FileChooserFieldDeleteKeyStroke, FileChooserFieldEventMap, FileChooserFieldModel, FileInput, FileInputChangeEvent, InitModelOf, objects, scout, ValueField} from '../../../index';

export class FileChooserField extends ValueField<File> implements FileChooserFieldModel {
  declare model: FileChooserFieldModel;
  declare eventMap: FileChooserFieldEventMap;
  declare self: FileChooserField;

  fileInput: FileInput;
  acceptTypes: string;
  maximumUploadSize: number;

  constructor() {
    super();

    this.fileInput = null;
    this.acceptTypes = null;
    this.maximumUploadSize = FileInput.DEFAULT_MAXIMUM_UPLOAD_SIZE;
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);

    this.fileInput.on('change', this._onFileChange.bind(this));
    this.on('propertyChange', event => {
      if (event.propertyName === 'enabledComputed') {
        // Propagate "enabledComputed" to inner widget
        this.fileInput.setEnabled(event.newValue);
      }
    });
  }

  /**
   * Initializes the file input before calling set value.
   * This cannot be done in _init because the value field would call _setValue first
   */
  protected override _initValue(value: File) {
    this.fileInput = scout.create(FileInput, {
      parent: this,
      acceptTypes: this.acceptTypes,
      text: this.displayText,
      enabled: this.enabledComputed,
      maximumUploadSize: this.maximumUploadSize
    });

    super._initValue(value);
  }

  protected override _initKeyStrokeContext() {
    super._initKeyStrokeContext();
    this.keyStrokeContext.registerKeyStroke(new FileChooserFieldBrowseKeyStroke(this));
    this.keyStrokeContext.registerKeyStroke(new FileChooserFieldDeleteKeyStroke(this));
  }

  protected override _render() {
    this.addContainer(this.$parent, 'file-chooser-field has-icon');
    this.addLabel();
    this.addMandatoryIndicator();
    this._renderFileInput();
    this.addIcon();
    this.addStatus();
    this._addAriaFieldDescription();
  }

  protected _renderFileInput() {
    this.fileInput.render();
    this.addField(this.fileInput.$container);
  }

  override setDisplayText(text: string) {
    super.setDisplayText(text);
    this.fileInput.setText(text);
    if (!text) {
      this.fileInput.clear();
    }
  }

  protected override _readDisplayText(): string {
    return this.fileInput.text;
  }

  setAcceptTypes(acceptTypes: string) {
    this.setProperty('acceptTypes', acceptTypes);
    this.fileInput.setAcceptTypes(acceptTypes);
  }

  protected override _renderEnabled() {
    super._renderEnabled();
    this.$field.setTabbable(this.enabledComputed);
  }

  protected override _renderPlaceholder() {
    let $field = this.fileInput.$text;
    if ($field) {
      $field.placeholder(this.label);
    }
  }

  protected override _removePlaceholder() {
    let $field = this.fileInput.$text;
    if ($field) {
      $field.placeholder('');
    }
  }

  setMaximumUploadSize(maximumUploadSize: number) {
    this.setProperty('maximumUploadSize', maximumUploadSize);
    this.fileInput.setMaximumUploadSize(maximumUploadSize);
  }

  protected override _clear() {
    this.fileInput.clear();
  }

  protected override _onIconMouseDown(event: JQuery.MouseDownEvent) {
    super._onIconMouseDown(event);
    this.activate();
  }

  protected _onFileChange(event: FileInputChangeEvent) {
    let file = arrays.first(event.files);
    if (objects.isNullOrUndefined(file)) {
      this.acceptInput(false);
    }
    this.setValue(file);
  }

  override activate() {
    if (!this.enabledComputed || !this.rendered) {
      return;
    }
    this.$field.focus();
    this.fileInput.browse();
  }

  protected override _validateValue(value: File): File {
    this.fileInput.validateMaximumUploadSize(value);
    return value;
  }

  protected override _formatValue(value: File): string | JQuery.Promise<string> {
    return !value ? '' : value.name;
  }

  protected override _parseValue(displayText: string): File {
    if (!displayText) {
      return null;
    }
    let files = this.fileInput.files;
    return files && files.length ? files[0] : null;
  }

  protected _addAriaFieldDescription() {
    aria.addHiddenDescriptionAndLinkToElement(this.fileInput.$fileInput, this.id + '-func-desc', this.session.text('ui.AriaFileChooserFieldDescription'));
  }
}
