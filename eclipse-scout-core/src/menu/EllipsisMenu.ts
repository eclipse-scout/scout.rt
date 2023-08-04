/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {aria, EllipsisMenuEventMap, EllipsisMenuModel, icons, Menu, Widget} from '../index';

export class EllipsisMenu extends Menu implements EllipsisMenuModel {
  declare model: EllipsisMenuModel;
  declare eventMap: EllipsisMenuEventMap;
  declare self: EllipsisMenu;

  hidden: boolean;

  constructor() {
    super();
    this.hidden = true;
    this.ellipsis = true;
    this.stackable = false;
    this.horizontalAlignment = 1;
    this.iconId = icons.ELLIPSIS_V;
    this.tabbable = false;
    this._addPreserveOnPropertyChangeProperties(['childActions']);
  }

  protected override _render() {
    super._render();
    this.$container.addClass('ellipsis');
  }

  override setChildActions(childActions: Menu[]) {
    super.setChildActions(childActions);

    if (childActions) {
      // close all actions that have been added to the ellipsis
      childActions.forEach(ca => ca.setSelected(false));
    }
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderHidden();
  }

  protected override _renderSelected() {
    super._renderSelected();
    // has to be done here because hasPopup is false for ellipsis menu
    aria.hasPopup(this.$container, 'menu');
    aria.expanded(this.$container, this.selected);
  }

  // add the set hidden function to the ellipsis
  setHidden(hidden: boolean) {
    this.setProperty('hidden', hidden);
  }

  protected _renderHidden() {
    this.$container.setVisible(!this.hidden);
  }

  override isTabTarget(): boolean {
    return super.isTabTarget() && !this.hidden;
  }

  protected override _childrenForEnabledComputed(): Widget[] {
    return this.childActions;
  }
}
