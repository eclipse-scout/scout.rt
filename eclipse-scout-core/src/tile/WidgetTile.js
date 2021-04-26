/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Tile, Widget} from '../index';

/**
 * A tile containing a widget.
 */
export default class WidgetTile extends Tile {

  constructor() {
    super();
    // The referenced widget which will be rendered (it is not possible to just call it 'widget' due to the naming conflict with the widget function)
    this.tileWidget = null;
    this._addWidgetProperties(['tileWidget']);
    this._widgetPropertyChangeHandler = this._onWidgetPropertyChange.bind(this);
  }

  _init(model) {
    super._init(model);
    this._setTileWidget(this.tileWidget);
  }

  _destroy() {
    if (this.tileWidget) {
      this.tileWidget.off('propertyChange', this._widgetPropertyChangeHandler);
    }
    super._destroy();
  }

  _renderProperties() {
    super._renderProperties();
    this._renderTileWidget();
  }

  _renderTileWidget() {
    if (this.tileWidget) {
      // render the tileWidget into the container of this tile.
      this.tileWidget.render();
    }
  }

  _removeTileWidget() {
    if (this.tileWidget) {
      this.tileWidget.remove();
    }
  }

  _onWidgetPropertyChange(event) {
    if (event.propertyName === 'visible') {
      this.setVisible(event.newValue);
    } else if (event.propertyName === 'enabled') {
      this.setEnabled(event.newValue);
    } else if (event.propertyName === 'disabledStyle') {
      this.setDisabledStyle(event.newValue);
    }
  }

  _setTileWidget(tileWidget) {
    if (this.tileWidget) {
      this.tileWidget.off('propertyChange', this._widgetPropertyChangeHandler);
    }
    this._setProperty('tileWidget', tileWidget);
    if (tileWidget) {
      // Hide tile if tileWidget is made invisible (don't do it if visible is true to not accidentally override the visibility state)
      if (!this.tileWidget.visible) {
        this.setVisible(false);
      }
      if (!this.tileWidget.enabled) {
        this.setEnabled(false);
      }
      if (this.tileWidget.disabledStyle !== Widget.DisabledStyle.DEFAULT) {
        this.setDisabledStyle(this.tileWidget.disabledStyle);
      }
      this.tileWidget.on('propertyChange', this._widgetPropertyChangeHandler);
    }
    this.invalidateLayoutTree();
  }
}
