/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  arrays, Device, DragAndDropOptions, EnumObject, Event, FileInput, FileInputChangeEvent, FormField, HtmlComponent, Icon, ImageFieldEventMap, ImageFieldLayout, ImageFieldModel, InitModelOf, scout, scrollbars, SingleLayout
} from '../../../index';
import $ from 'jquery';

export class ImageField extends FormField implements ImageFieldModel {
  declare model: ImageFieldModel;
  declare eventMap: ImageFieldEventMap;
  declare self: ImageField;

  autoFit: boolean;
  imageUrl: string;
  scrollBarEnabled: boolean;
  uploadEnabled: boolean;
  acceptTypes: string;
  maximumUploadSize: number;
  icon: Icon;
  fileInput: FileInput;

  protected _clickHandler: (event: JQuery.ClickEvent) => void;

  constructor() {
    super();

    this.defaultMenuTypes = [...this.defaultMenuTypes, ImageField.MenuTypes.ImageUrl, ImageField.MenuTypes.Null];
    this.autoFit = false;
    this.imageUrl = null;
    this.scrollBarEnabled = false;
    this.uploadEnabled = false;
    this.acceptTypes = null;
    this.maximumUploadSize = FileInput.DEFAULT_MAXIMUM_UPLOAD_SIZE;

    this._clickHandler = null;
  }

  static MenuTypes = {
    Null: 'ImageField.Null',
    ImageUrl: 'ImageField.ImageUrl'
  } as const;

  protected override _init(model: InitModelOf<this>) {
    super._init(model);

    this.resolveIconIds(['imageUrl']);
    this.icon = scout.create(Icon, {
      parent: this,
      iconDesc: this.imageUrl,
      autoFit: this.autoFit,
      prepend: true
    });
    this.icon.on('load', this._onImageLoad.bind(this));
    this.icon.on('error', this._onImageError.bind(this));
  }

  protected override _render() {
    this.addContainer(this.$parent, 'image-field', new ImageFieldLayout(this));
    this.addFieldContainer(this.$parent.makeDiv());

    // Complete the layout hierarchy between the image field and the image
    let htmlComp = HtmlComponent.install(this.$fieldContainer, this.session);
    htmlComp.setLayout(new SingleLayout());
    this.icon.render(this.$fieldContainer);

    this.addLabel();
    this.addField(this.icon.$container);
    this.addStatus();
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderScrollBarEnabled();
    this._renderImageUrl();
    this._renderUploadEnabled();
  }

  protected override _remove() {
    super._remove();
    this._clickHandler = null;
    if (this.fileInput) {
      this.fileInput.destroy();
      this.fileInput = null;
    }
  }

  protected override _getDragAndDropHandlerOptions(): DragAndDropOptions {
    let options = super._getDragAndDropHandlerOptions();
    options.container = () => this.$fieldContainer;
    return options;
  }

  setImageUrl(imageUrl: string) {
    this.setProperty('imageUrl', imageUrl);
  }

  protected _setImageUrl(imageUrl: string) {
    this._setProperty('imageUrl', imageUrl);
    this.icon.setIconDesc(imageUrl);
    this._updateMenus();
  }

  protected _renderImageUrl() {
    let hasImageUrl = !!this.imageUrl;
    this.$fieldContainer.toggleClass('has-image', hasImageUrl);
    this.$container.toggleClass('has-image', hasImageUrl);
    scrollbars.update(this.$fieldContainer);
  }

  setAutoFit(autoFit: boolean) {
    this.setProperty('autoFit', autoFit);
  }

  protected _setAutoFit(autoFit: boolean) {
    this._setProperty('autoFit', autoFit);
    this.icon.setAutoFit(autoFit);
  }

  protected _renderAutoFit() {
    scrollbars.update(this.$fieldContainer);
  }

  setScrollBarEnabled(scrollBarEnabled: boolean) {
    this.setProperty('scrollBarEnabled', scrollBarEnabled);
  }

  protected _renderScrollBarEnabled() {
    // Note: Inner alignment has to be updated _before_ installing the scrollbar, because the inner
    // alignment uses absolute positioning, which confuses the scrollbar calculations.
    this._updateInnerAlignment();

    if (this.scrollBarEnabled) {
      this._installScrollbars();
    } else {
      this._uninstallScrollbars();
    }
  }

  override get$Scrollable(): JQuery {
    return this.$fieldContainer;
  }

  protected override _renderGridData() {
    super._renderGridData();
    this._updateInnerAlignment();
  }

  protected override _renderGridDataHints() {
    super._renderGridDataHints();
    this._updateInnerAlignment();
  }

  protected _updateInnerAlignment() {
    // Enable inner alignment only when scrollbars are disabled
    this.updateInnerAlignment({
      useHorizontalAlignment: !this.scrollBarEnabled,
      useVerticalAlignment: !this.scrollBarEnabled
    });
  }

  protected override _renderEnabled() {
    super._renderEnabled();
    this._updateUploadEnabled();
  }

  setUploadEnabled(uploadEnabled: boolean) {
    this.setProperty('uploadEnabled', uploadEnabled);
  }

  protected _renderUploadEnabled() {
    this._updateUploadEnabled();
  }

  protected _updateUploadEnabled() {
    let enabled = this.enabledComputed && this.uploadEnabled;
    this.$fieldContainer.toggleClass('clickable', enabled);
    if (enabled) {
      if (!this._clickHandler) {
        this._clickHandler = this._onClickUpload.bind(this);
        this.$fieldContainer.on('click', this._clickHandler);
      }
      if (!this.fileInput) {
        this.fileInput = scout.create(FileInput, {
          parent: this,
          acceptTypes: this.acceptTypes,
          text: this.displayText,
          enabled: this.enabledComputed,
          maximumUploadSize: this.maximumUploadSize,
          visible: !Device.get().supportsFile()
        });
        this.fileInput.render(this.$fieldContainer);
        this.fileInput.on('change', this._onFileChange.bind(this));
      }
    } else {
      this.$fieldContainer.off('click', this._clickHandler);
      this._clickHandler = null;
      if (this.fileInput) {
        this.fileInput.destroy();
        this.fileInput = null;
      }
    }
  }

  protected override _getCurrentMenuTypes(): string[] {
    if (this.imageUrl) {
      return [...super._getCurrentMenuTypes(), ImageField.MenuTypes.ImageUrl];
    }
    return [...super._getCurrentMenuTypes(), ImageField.MenuTypes.Null];
  }

  /**
   * The browse() function triggers an artificial click event on the INPUT element,
   * this would trigger our own click handler again. We prevent recursion by
   * checking the click target.
   */
  protected _onClickUpload(event: JQuery.ClickEvent) {
    if ($(event.target).isOrHas(this.$field)) {
      this.fileInput.browse();
    }
  }

  protected _onFileChange(event: FileInputChangeEvent) {
    this.trigger('fileUpload', {
      file: arrays.first(event.files)
    });
  }

  protected _onImageLoad(event: Event<Icon>) {
    this._onIconUpdated();
  }

  protected _onImageError(event: Event<Icon>) {
    this._onIconUpdated();
  }

  /**
   * This function is called whenever the icon has updated its $container. Since the $field
   * variable from ImageField.js references the $container of the icon directly, we must update
   * that variable now.
   * <p>
   * Override this method if a sub-class of ImageField.js needs to update its DOM too.
   */
  protected _onIconUpdated() {
    scrollbars.update(this.$fieldContainer);
    this.$field = this.icon.$container;
  }
}

export type ImageFieldMenuTypes = EnumObject<typeof ImageField.MenuTypes>;
