/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {CalendarsPanelTreeNode, HtmlComponent, InitModelOf, ObjectOrModel, scout, Tree, Widget} from '../index';

export class CalendarsPanel extends Widget {
  tree: CalendarsPanelTree;

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this.tree = scout.create(CalendarsPanelTree, {
      parent: this,
      checkable: true,
      textFilterEnabled: false
    });
  }

  protected override _render() {
    this.$container = this.$parent.appendDiv('calendars-panel-container');
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.tree.render();
  }
}

class CalendarsPanelTree extends Tree {
  declare nodes: CalendarsPanelTreeNode[];

  constructor() {
    super();

    this._scrollDirections = 'y';
  }

  override insertNode(node: ObjectOrModel<CalendarsPanelTreeNode>, parentNode?: CalendarsPanelTreeNode, index?: number) {
    super.insertNode(node, parentNode, index);
  }
}
