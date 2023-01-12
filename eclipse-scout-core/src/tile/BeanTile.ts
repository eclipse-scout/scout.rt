/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Event, HtmlComponent, Tile, TileEventMap, TileModel} from '../index';
import $ from 'jquery';

export interface BeanTileAppLinkActionEvent<T extends BeanTile = BeanTile> extends Event<T> {
  ref: string;
}

export interface BeanTileModel extends TileModel {
  bean?: object;
}

export interface BeanTileEventMap extends TileEventMap {
  'appLinkAction': BeanTileAppLinkActionEvent;
}

export class BeanTile<TBean extends object = object> extends Tile implements BeanTileModel {
  declare model: BeanTileModel;
  declare eventMap: BeanTileEventMap;
  declare self: BeanTile;

  bean: TBean;

  constructor() {
    super();

    this.bean = null;
  }

  protected override _render() {
    this.$container = this.$parent.appendDiv('bean-tile');
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderBean();
  }

  protected _renderBean() {
    // to be implemented by the subclass
  }

  triggerAppLinkAction(ref: string) {
    this.trigger('appLinkAction', {
      ref: ref
    });
  }

  protected _onAppLinkAction(event: JQuery.TriggeredEvent) {
    let $target = $(event.delegateTarget);
    let ref = $target.data('ref');
    this.triggerAppLinkAction(ref);
  }
}
