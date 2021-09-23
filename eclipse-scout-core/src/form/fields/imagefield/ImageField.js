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
import {arrays, Device, FileInput, FormField, HtmlComponent, ImageFieldLayout, scout, scrollbars, SingleLayout} from '../../../index';
import $ from 'jquery';

export default class ImageField extends FormField {

  constructor() {
    super();

    this.autoFit = false;
    this.imageUrl = null;
    this.scrollBarEnabled = false;
    this.uploadEnabled = false;
    this.acceptTypes = null;
    this.maximumUploadSize = FileInput.DEFAULT_MAXIMUM_UPLOAD_SIZE;

    this._clickHandler = null;
  }

  _init(model) {
    super._init(model);

    this.resolveIconIds(['imageUrl']);
    this.icon = scout.create('Icon', {
      parent: this,
      iconDesc: this.imageUrl,
      autoFit: this.autoFit,
      prepend: true
    });
    this.icon.on('load', this._onImageLoad.bind(this));
    this.icon.on('error', this._onImageError.bind(this));
  }

  _render() {
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

  _renderProperties() {
    super._renderProperties();
    this._renderScrollBarEnabled();
    this._renderImageUrl();
    this._renderUploadEnabled();
  }

  _remove() {
    super._remove();
    this._clickHandler = null;
    if (this.fileInput) {
      this.fileInput.destroy();
      this.fileInput = null;
    }
  }

  _getDragAndDropHandlerOptions() {
    let options = super._getDragAndDropHandlerOptions();
    options.container = () => this.$fieldContainer;
    return options;
  }

  setImageUrl(imageUrl) {
    this.setProperty('imageUrl', imageUrl);
  }

  _setImageUrl(imageUrl) {
    this._setProperty('imageUrl', imageUrl);
    this.icon.setIconDesc(imageUrl);
  }

  _renderImageUrl() {
    let hasImageUrl = !!this.imageUrl;
    this.$fieldContainer.toggleClass('has-image', hasImageUrl);
    this.$container.toggleClass('has-image', hasImageUrl);
    scrollbars.update(this.$fieldContainer);
  }

  setAutoFit(autoFit) {
    this.setProperty('autoFit', autoFit);
  }

  _setAutoFit(autoFit) {
    this._setProperty('autoFit', autoFit);
    this.icon.setAutoFit(autoFit);
  }

  _renderAutoFit() {
    scrollbars.update(this.$fieldContainer);
  }

  setScrollBarEnabled(scrollBarEnabled) {
    this.setProperty('scrollBarEnabled', scrollBarEnabled);
  }

  _renderScrollBarEnabled() {
    // Note: Inner alignment has to be updated _before_ installing the scrollbar, because the inner
    // alignment uses absolute positioning, which confuses the scrollbar calculations.
    this._updateInnerAlignment();

    if (this.scrollBarEnabled) {
      this._installScrollbars();
    } else {
      this._uninstallScrollbars();
    }
  }

  /**
   * @override
   */
  get$Scrollable() {
    return this.$fieldContainer;
  }

  _renderGridData() {
    super._renderGridData();
    this._updateInnerAlignment();
  }

  _renderGridDataHints() {
    super._renderGridDataHints();
    this._updateInnerAlignment();
  }

  _updateInnerAlignment() {
    // Enable inner alignment only when scrollbars are disabled
    this.updateInnerAlignment({
      useHorizontalAlignment: (!this.scrollBarEnabled),
      useVerticalAlignment: (!this.scrollBarEnabled)
    });
  }

  _renderEnabled() {
    super._renderEnabled();
    this._updateUploadEnabled();
  }

  setUploadEnabled(uploadEnabled) {
    this.setProperty('uploadEnabled', uploadEnabled);
  }

  _renderUploadEnabled() {
    this._updateUploadEnabled();
  }

  _updateUploadEnabled() {
    let enabled = this.enabledComputed && this.uploadEnabled;
    this.$fieldContainer.toggleClass('clickable', enabled);
    if (enabled) {
      if (!this._clickHandler) {
        this._clickHandler = this._onClickUpload.bind(this);
        this.$fieldContainer.on('click', this._clickHandler);
      }
      if (!this.fileInput) {
        this.fileInput = scout.create('FileInput', {
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

  /**
   * The browse() function triggers an artificial click event on the INPUT element,
   * this would trigger our own click handler again. We prevent recursion by
   * checking the click target.
   */
  _onClickUpload(event) {
    if ($(event.target).isOrHas(this.$field)) {
      this.fileInput.browse();
    }
  }

  _onFileChange(event) {
    this.trigger('fileUpload', {
      file: arrays.first(event.files)
    });
  }

  _onImageLoad(event) {
    this._onIconUpdated();
  }

  _onImageError(event) {
    this._onIconUpdated();
  }

  /**
   * This function is called whenever the icon has updated its $container. Since the $field
   * variable from ImageField.js references the $container of the icon directly, we must update
   * that variable now.
   * <p>
   * Override this method if a sub-class of ImageField.js needs to update its DOM too.
   */
  _onIconUpdated() {
    scrollbars.update(this.$fieldContainer);
    this.$field = this.icon.$container;
  }
}
