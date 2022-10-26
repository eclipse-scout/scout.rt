/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Event, HtmlComponent, Tile} from '../index';
import $ from 'jquery';
import TileModel from './TileModel';
import TileEventMap from './TileEventMap';

export interface BeanTileAppLinkActionEvent<T extends BeanTile = BeanTile> extends Event<T> {
  ref: string;
}

export interface BeanTileModel extends TileModel {
  bean?: object;
}

export interface BeanTileEventMap extends TileEventMap {
  'appLinkAction': BeanTileAppLinkActionEvent;
}

export default class BeanTile extends Tile implements BeanTileModel {
  declare model: BeanTileModel;
  declare eventMap: BeanTileEventMap;

  bean: object;

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
