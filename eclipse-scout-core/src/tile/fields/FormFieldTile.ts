/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {BrowserField, Device, EnumObject, FormFieldLayout, PropertyChangeEvent, Tile, WidgetTile} from '../../index';

export type FormFieldTileDisplayStyle = EnumObject<typeof FormFieldTile.DisplayStyle>;

export default class FormFieldTile extends WidgetTile {
  declare displayStyle: FormFieldTileDisplayStyle;

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

  protected override _renderTileWidget() {
    super._renderTileWidget();
    if (this.displayStyle !== FormFieldTile.DisplayStyle.DASHBOARD) {
      return;
    }
    if (this.tileWidget && this.tileWidget.htmlComp && this.tileWidget.htmlComp.layout instanceof FormFieldLayout) {
      this.tileWidget.htmlComp.layout.statusWidth = 0;
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
  }

  protected _renderCompact() {
    this.$container.toggleClass('compact', Device.get().type === Device.Type.MOBILE);
  }

  // FIXME TS is this code used? Maybe override onWidgetPropertyChange instead and call super?
  protected _onFieldPropertyChange(event: PropertyChangeEvent) {
    if (event.propertyName === 'labelVisible' || event.propertyName === 'errorStatus') {
      if (this.rendered) {
        this._renderFieldLabelVisible();
      }
    }
  }
}
