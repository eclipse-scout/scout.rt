/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {FormField, Outline, scout, TileOutlineOverview} from '../../index';

export class OutlineTileField extends FormField {
  /** outline is not a widget, because at the time where _init runs, outline is not yet available */
  outline: string;
  tileOutlineOverview: TileOutlineOverview;

  constructor() {
    super();
    this.outline = null;
    this.tileOutlineOverview = null;
  }

  protected override _render() {
    this._ensureTileOutlineOverview();
    this.addContainer(this.$parent, 'outline-tile-field');
    this.tileOutlineOverview.render(this.$container);
    this.addField(this.tileOutlineOverview.$container);
  }

  /**
   * We cannot create the TileOutlineOverview instance in the _init function, because at the time where _init runs, outlines are not yet available.
   */
  protected _ensureTileOutlineOverview() {
    if (this.tileOutlineOverview) {
      return;
    }
    let outline = this.outline ? this.session.getWidget(this.outline) as Outline : null;
    this.tileOutlineOverview = scout.create(TileOutlineOverview, {
      parent: this,
      outline: outline
    });
  }
}
