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
import {FormField, HtmlComponent, Point, Popup, scout, SingleLayout, TouchPopupLayout, ValueField} from '../index';
import RowLayout from '../layout/RowLayout';

export default class TouchPopup extends Popup {

  constructor() {
    super();

    // the original touch field from the form
    this._touchField = null;
    this._touchFieldTooltip = null;
    // the cloned field from the popup
    this._field = null;
    // the widget placed below the field
    this._widget = null;
    this._$widgetContainer = null;
    this._widgetContainerHtmlComp = null;
    this.windowPaddingX = 0;
    this.windowPaddingY = 0;
    this.withGlassPane = true;
    this._touchFieldPropertyChangeListener = this._onTouchFieldPropertyChange.bind(this);
  }

  _init(options) {
    super._init(options);
    this._touchField = options.field;
    if (this._touchField._tooltip() && this._touchField._tooltip().rendered) {
      // Hide existing tooltip to not show it twice (it will be shown on the popup too). It may even throw an exception if the tooltip contains a (not cloned) menu
      this._touchFieldTooltip = this._touchField._tooltip();
      this._touchFieldTooltip.remove();
    }

    // clone original touch field
    // original and clone both point to the same popup instance
    this._field = this._touchField.clone(this._fieldOverrides());
    this._touchField.on('propertyChange', this._touchFieldPropertyChangeListener);
    this._initWidget(options);
    this.doneAction = scout.create('Menu', {
      parent: this,
      text: this.session.text('ui.Done')
    });
    this.doneAction.on('action', this._onCloseIconClick.bind(this));
  }

  _destroy() {
    this._touchField.off('propertyChange', this._touchFieldPropertyChangeListener);
    if (this._touchFieldTooltip && !this._touchFieldTooltip.destroyed) {
      // Make tooltip visible again if not destroyed in the meantime
      this._touchFieldTooltip.render(this._touchField._$tooltipParent());
    }
    super._destroy();
  }

  _fieldOverrides() {
    return {
      parent: this,
      labelVisible: false,
      fieldStyle: FormField.FieldStyle.CLASSIC,
      popup: this,
      statusVisible: false,
      menusVisible: false, // menus don't work (action on clone is not propagated to original, currentMenuTypes is not updated correctly) -> don't show it on popup
      embedded: true,
      touchMode: false,
      clearable: ValueField.Clearable.ALWAYS
    };
  }

  _initWidget(options) {
    // NOP
  }

  _createLayout() {
    return new TouchPopupLayout(this);
  }

  /**
   * @override Popup.js
   */
  prefLocation(verticalAlignment, horizontalAlignment) {
    let popupSize = this.htmlComp.prefSize(),
      windowWidth = this.$container.window().width(),
      x = Math.max(this.windowPaddingX, (windowWidth - popupSize.width) / 2);
    return new Point(x, 0);
  }

  _render() {
    this.$container = this.$parent.appendDiv('popup touch-popup');
    this.$body = this.$container.appendDiv('body');
    this.htmlBody = HtmlComponent.install(this.$body, this.session);
    this.htmlBody.setLayout(new RowLayout());

    this._$header = this.$body.appendDiv('touch-popup-header');
    HtmlComponent.install(this._$header, this.session);
    this._$header.appendDiv('touch-popup-title').textOrNbsp(this._touchField.label, 'empty');
    this.doneAction.render(this._$header);

    this._$widgetContainer = this.$body.appendDiv('touch-popup-widget-container');
    this._widgetContainerHtmlComp = HtmlComponent.install(this._$widgetContainer, this.session);
    this._widgetContainerHtmlComp.setLayout(new SingleLayout());

    // field may render something into the widget container -> render after widget container and move to correct place
    this._field.render();

    // Move to top
    this._field.$container.insertBefore(this._$widgetContainer);
    this._field.$container.addClass('touch-popup-field');

    if (this._widget) {
      this._widget.render(this._$widgetContainer);
    }

    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.validateRoot = true;
    this.htmlComp.setLayout(this._createLayout());
  }

  _handleGlassPanes() {
    super._handleGlassPanes();
    if (this._glassPaneRenderer) {
      this._glassPaneRenderer.eachGlassPane($pane => $pane.addClass('dark'));
    }
  }

  _onTouchFieldPropertyChange(event) {
    if (event.propertyName === 'errorStatus') {
      this._field.setErrorStatus(event.newValue);
    } else if (event.propertyName === 'lookupRow') {
      this._field.setLookupRow(event.newValue);
    }
  }

  /**
   * Calls accept input on the embedded field.
   */
  _acceptInput() {
    let promise = this._field.acceptInput();
    if (promise) {
      promise.always(this.close.bind(this));
    } else {
      this.close();
    }
  }

  _onCloseIconClick(event) {
    this._acceptInput();
  }
}
