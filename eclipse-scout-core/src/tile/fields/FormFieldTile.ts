/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {aria, BrowserField, Device, EnumObject, FormField, FormFieldLayout, PropertyChangeEvent, Tile, Widget, WidgetTile, WidgetTileEventMap} from '../../index';

export type FormFieldTileDisplayStyle = EnumObject<typeof FormFieldTile.DisplayStyle>;

export class FormFieldTile extends WidgetTile {
  declare displayStyle: FormFieldTileDisplayStyle;
  declare eventMap: FormFieldTileEventMap;
  declare self: FormFieldTile;
  declare tileWidget: FormField;

  constructor() {
    super();
    this.displayStyle = FormFieldTile.DisplayStyle.DASHBOARD;
  }

  static override DisplayStyle = {
    DEFAULT: Tile.DisplayStyle.DEFAULT,
    PLAIN: Tile.DisplayStyle.PLAIN,
    DASHBOARD: 'dashboard'
  }; // not const, can be extended

  protected override _renderProperties() {
    super._renderProperties();
    this._renderFieldLabelVisible();
    this._renderCompact();
  }

  protected override _renderLoading() {
    // Setting the loading flag to false will show content of the tile immediately and the layout will be validated.
    // However, if the tile is currently being animated, the layout validation will wait until the animation has
    // finished (see HtmlComponent#_checkValidationPossible). To prevent showing non-layouted elements, we therefore
    // also have to postpone the removal of the 'loading' class.
    if (!this.loading && this.$container.hasAnimationClass()) {
      this.$container.oneAnimationEnd(() => this.rendered && this._renderLoading());
    } else {
      super._renderLoading();
    }
  }

  protected override _setTileWidget(tileWidget: Widget) {
    super._setTileWidget(tileWidget);
    this._setDisplayStyle(this.displayStyle);
  }

  protected override _renderTileWidget() {
    super._renderTileWidget();
    if (this.displayStyle !== FormFieldTile.DisplayStyle.DASHBOARD) {
      return;
    }
    if (this.tileWidget && this.tileWidget.htmlComp && this.tileWidget.htmlComp.layout instanceof FormFieldLayout) {
      this.tileWidget.htmlComp.layout.statusWidth = 0;
    }
  }

  protected _setDisplayStyle(displayStyle: FormFieldTileDisplayStyle) {
    this._setProperty('displayStyle', this.displayStyle);
    if (this.tileWidget && this.displayStyle === FormFieldTile.DisplayStyle.DASHBOARD) {
      this.tileWidget.setLabelPosition(FormField.LabelPosition.TOP);
      this.tileWidget.setMandatory(false);
      this.tileWidget.setStatusVisible(false);
    }
  }

  protected override _renderDisplayStyle() {
    super._renderDisplayStyle();
    this.$container.toggleClass('dashboard', this.displayStyle === FormFieldTile.DisplayStyle.DASHBOARD);
  }

  protected _renderFieldLabelVisible() {
    if (this.displayStyle !== FormFieldTile.DisplayStyle.DASHBOARD) {
      return;
    }
    // Special handling for browser field (remove padding when label is invisible)
    if (this.tileWidget instanceof BrowserField) {
      this.tileWidget.$container.toggleClass('no-padding', !this.tileWidget.labelVisible && !this.tileWidget.errorStatus);
    }

    if (this.tileWidget instanceof FormField && this.tileWidget.labelVisible) {
      aria.linkElementWithHeader(this.$container, this.tileWidget.$label);
    }
  }

  protected _renderCompact() {
    this.$container.toggleClass('compact', Device.get().type === Device.Type.MOBILE);
  }

  protected override _onWidgetPropertyChange(event: PropertyChangeEvent) {
    super._onWidgetPropertyChange(event);
    if (event.propertyName === 'labelVisible' || event.propertyName === 'errorStatus') {
      if (this.rendered) {
        this._renderFieldLabelVisible();
      }
    }
  }

  override markAsActiveDescendantFor($container: JQuery) {
    if (this.displayStyle === FormFieldTile.DisplayStyle.DASHBOARD
      && this.tileWidget instanceof FormField) {
      aria.linkElementWithActiveDescendant(this.$container, this.tileWidget?.$field);
    }
  }
}

export interface FormFieldTileEventMap extends WidgetTileEventMap {
  'propertyChange:tileWidget': PropertyChangeEvent<FormField, FormFieldTile>;
}
