/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, Button, Device, Event, FileChooserButtonEventMap, FileChooserButtonModel, FileInput, FileInputChangeEvent, HtmlComponent, InitModelOf, scout, SingleLayout, strings, ValueField} from '../../../index';

export class FileChooserButton extends ValueField<File> implements FileChooserButtonModel {
  declare model: FileChooserButtonModel;
  declare eventMap: FileChooserButtonEventMap;
  declare self: FileChooserButton;

  button: Button;
  fileInput: FileInput;
  acceptTypes: string;
  maximumUploadSize: number;
  iconId: string;
  fileExtensions: string | string[];

  constructor() {
    super();

    this.button = null;
    this.fileInput = null;
    this.acceptTypes = null;
    this.maximumUploadSize = FileInput.DEFAULT_MAXIMUM_UPLOAD_SIZE;
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);

    this.button = scout.create(Button, {
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
  protected override _initValue(value: File) {
    this.fileInput = scout.create(FileInput, {
      parent: this,
      acceptTypes: this.acceptTypes,
      text: this.displayText,
      enabled: this.enabledComputed,
      maximumUploadSize: this.maximumUploadSize,
      visible: !Device.get().supportsFile()
    });

    super._initValue(value);
  }

  protected _buttonLabel(): string {
    return this.label;
  }

  protected override _render() {
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

  protected override _renderLabel() {
    this._renderEmptyLabel();
  }

  protected _onButtonClick(event: Event<Button>) {
    this.fileInput.browse();
  }

  override setLabel(label: string) {
    super.setLabel(label);
    this.button.setLabel(this._buttonLabel());
  }

  setIconId(iconId: string) {
    this.setProperty('iconId', iconId);
    this.button.setIconId(iconId);
  }

  setAcceptTypes(acceptTypes: string) {
    this.setProperty('acceptTypes', acceptTypes);
    this.fileInput.setAcceptTypes(acceptTypes);
  }

  setFileExtensions(fileExtensions: string | string[]) {
    this.setProperty('fileExtensions', fileExtensions);
    let acceptTypes = arrays.ensure(fileExtensions);
    acceptTypes = acceptTypes.map(acceptType => acceptType.indexOf('.') === 0 ? acceptType : '.' + acceptType);
    this.setAcceptTypes(strings.join(',', ...acceptTypes));
    this.fileInput.acceptTypes = this.acceptTypes;
  }

  setMaximumUploadSize(maximumUploadSize: number) {
    this.setProperty('maximumUploadSize', maximumUploadSize);
    this.fileInput.setMaximumUploadSize(maximumUploadSize);
  }

  protected _onFileChange(event: FileInputChangeEvent) {
    this.setValue(arrays.first(event.files));
  }

  protected override _validateValue(value: File): File {
    this.fileInput.validateMaximumUploadSize(value);
    return value;
  }

  protected override _formatValue(value: File): string | JQuery.Promise<string> {
    return !value ? '' : value.name;
  }

  override getFocusableElement(): HTMLElement | JQuery {
    return this.button.getFocusableElement();
  }
}
