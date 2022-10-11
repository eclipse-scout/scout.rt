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
import {FormField, Outline, scout, TileOutlineOverview} from '../../index';

export default class OutlineTileField extends FormField {
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
