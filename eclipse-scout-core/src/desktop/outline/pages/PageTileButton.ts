/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
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

    this.label = this.page.text;
    this.iconId = this.page.overviewIconId;
    this.labelHtmlEnabled = this.page.htmlEnabled;

    this.on('click', (event: Event<Button>) => {
      this.outline.selectNode(this.page);
    });
  }

  notifyPageChanged() {
    this.setLabel(this.page.text);
    this.setIconId(this.page.overviewIconId);
    this.setLabelHtmlEnabled(this.page.htmlEnabled);
  }
}
