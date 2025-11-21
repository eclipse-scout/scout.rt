/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Button, Event, InitModelOf, Outline, Page, PageTileButtonModel, SomeRequired, TileButton} from '../../../index';

export class PageTileButton extends TileButton implements PageTileButtonModel {
  declare model: PageTileButtonModel;
  declare initModel: SomeRequired<this['model'], 'parent' | 'page' | 'outline'>;

  page: Page;
  outline: Outline;

  constructor() {
    super();
    this.page = null;
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);

    this.label = this._text;
    this.iconId = this._iconId;
    this.labelHtmlEnabled = this._htmlEnabled;

    this.on('click', (event: Event<Button>) => {
      this.outline.selectNode(this.page);
    });
  }

  notifyPageChanged() {
    this.setLabel(this._text);
    this.setIconId(this._iconId);
    this.setLabelHtmlEnabled(this._htmlEnabled);
  }

  protected get _text(): string {
    if (this.page?.overviewText !== undefined) {
      return this.page.overviewText;
    }
    return this.page?.text;
  }

  protected get _iconId(): string {
    if (this.page?.overviewIconId !== undefined) {
      return this.page.overviewIconId;
    }
    return this.page?.iconId;
  }

  protected get _htmlEnabled(): boolean {
    if (this.page?.overviewHtmlEnabled !== undefined) {
      return !!this.page.overviewHtmlEnabled;
    }
    return this.page?.htmlEnabled;
  }
}
