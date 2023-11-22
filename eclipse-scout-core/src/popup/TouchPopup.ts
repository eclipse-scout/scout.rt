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
  AbstractLayout, Action, Event, EventHandler, FormField, HtmlComponent, InitModelOf, Menu, Point, Popup, PopupAlignment, PropertyChangeEvent, RowLayout, scout, SingleLayout, SmartField, SomeRequired, Tooltip, TouchPopupLayout,
  TouchPopupModel, ValueField, ValueFieldModel, Widget
} from '../index';

export class TouchPopup extends Popup {
  declare model: TouchPopupModel;
  declare initModel: SomeRequired<this['model'], 'parent' | 'field'>;

  doneAction: Action;
  htmlBody: HtmlComponent;
  $body: JQuery;

  /**
   * the cloned field from the popup
   * @internal
   */
  _field: TouchPopupField;

  /** the original touch field from the form */
  protected _touchField: TouchPopupField;
  protected _touchFieldTooltip: Tooltip;
  /** the widget placed below the field */
  protected _widget: Widget;
  protected _$widgetContainer: JQuery;
  protected _$header: JQuery;
  protected _widgetContainerHtmlComp: HtmlComponent;
  protected _touchFieldPropertyChangeListener: EventHandler<PropertyChangeEvent<any>>;

  constructor() {
    super();

    this._touchField = null;
    this._touchFieldTooltip = null;
    this._field = null;
    this._widget = null;
    this._$widgetContainer = null;
    this._widgetContainerHtmlComp = null;
    this.windowPaddingX = 0;
    this.windowPaddingY = 0;
    this.withGlassPane = true;
    this._touchFieldPropertyChangeListener = this._onTouchFieldPropertyChange.bind(this);
  }

  protected override _init(options: InitModelOf<this>) {
    super._init(options);
    this._touchField = options.field;
    let touchFieldTooltip = this._touchField.tooltip();
    if (touchFieldTooltip && touchFieldTooltip.rendered) {
      // Hide existing tooltip to not show it twice (it will be shown on the popup too). It may even throw an exception if the tooltip contains a (not cloned) menu
      this._touchFieldTooltip = touchFieldTooltip;
      this._touchFieldTooltip.remove();
    }

    // clone original touch field
    // original and clone both point to the same popup instance
    this._field = this._touchField.clone(this._fieldOverrides(), {
      delegateEventsToOriginal: this._initDelegatedEvents()
    });
    this._touchField.on('propertyChange', this._touchFieldPropertyChangeListener);
    this._initWidget(options);
    this.doneAction = scout.create(Menu, {
      parent: this,
      text: this.session.text('ui.Done')
    });
    this.doneAction.on('action', this._onCloseIconClick.bind(this));
  }

  protected override _destroy() {
    this._touchField.off('propertyChange', this._touchFieldPropertyChangeListener);
    if (this._touchFieldTooltip && !this._touchFieldTooltip.destroyed) {
      // Make tooltip visible again if not destroyed in the meantime
      this._touchFieldTooltip.render(this._touchField._$tooltipParent());
    }
    super._destroy();
  }

  protected _fieldOverrides(): InitModelOf<TouchPopupField> {
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

  protected _initDelegatedEvents(): string[] {
    return [];
  }

  protected _initWidget(options: TouchPopupModel) {
    // NOP
  }

  protected override _createLayout(): AbstractLayout {
    return new TouchPopupLayout(this);
  }

  override prefLocation(verticalAlignment?: PopupAlignment, horizontalAlignment?: PopupAlignment): Point {
    let popupSize = this.htmlComp.prefSize(),
      windowWidth = this.$container.window().width(),
      x = Math.max(this.windowPaddingX, (windowWidth - popupSize.width) / 2);
    return new Point(x, 0);
  }

  protected override _render() {
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

  protected override _handleGlassPanes() {
    super._handleGlassPanes();
    if (this._glassPaneRenderer) {
      this._glassPaneRenderer.eachGlassPane($pane => $pane.addClass('dark'));
    }
  }

  protected _onTouchFieldPropertyChange(event: PropertyChangeEvent<any>) {
    if (event.propertyName === 'errorStatus') {
      this._field.setErrorStatus(event.newValue);
    } else if (event.propertyName === 'lookupRow' && this._field instanceof SmartField) {
      let smartfield = this._field;
      smartfield.setLookupRow(event.newValue);
    }
  }

  /**
   * Calls accept input on the embedded field.
   */
  protected _acceptInput() {
    let promise = this._field.acceptInput();
    if (promise) {
      promise.always(this.close.bind(this));
    } else {
      this.close();
    }
  }

  protected _onCloseIconClick(event: Event<Menu>) {
    this._acceptInput();
  }
}

export type TouchPopupField = ValueField<any> & {
  popup: Popup;
  embedded: boolean;
  touchMode: boolean;
};

export type TouchPopupFieldModel = ValueFieldModel<any, any> & {
  popup?: Popup;
  embedded?: boolean;
  touchMode?: boolean;
};
