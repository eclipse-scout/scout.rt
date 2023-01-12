/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {DisplayViewId, HtmlComponent, InitModelOf, Outline, OutlineOverviewModel, scout, Widget} from '../../../index';

export class OutlineOverview extends Widget implements OutlineOverviewModel {
  declare model: OutlineOverviewModel;

  outline: Outline;
  displayViewId: DisplayViewId; // set by DesktopBench
  $content: JQuery;

  constructor() {
    super();
    this.outline = null;
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    if (!this.outline && this.parent instanceof Outline) {
      this.outline = this.parent;
    }
    scout.assertProperty(this, 'outline', Outline);
  }

  protected override _render() {
    this.$container = this.$parent.appendDiv('outline-overview');
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.$content = this.$container.appendDiv('outline-overview-content');
    this.$content.appendDiv('outline-overview-icon').icon(this.outline.iconId);
    this.$content.appendDiv('outline-overview-title').text(this.outline.title);
  }

  protected override _attach() {
    this.$parent.append(this.$container);
    let htmlParent = this.htmlComp.getParent();
    this.htmlComp.setSize(htmlParent.size());
    super._attach();
  }

  protected override _detach() {
    this.$container.detach();
    super._detach();
  }
}
