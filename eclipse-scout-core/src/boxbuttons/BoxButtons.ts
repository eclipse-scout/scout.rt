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
import {Action, ActionModel, BoxButtonsEventMap, BoxButtonsModel, scout, Widget} from '../index';
import {ObjectFactoryOptions} from '../ObjectFactory';

/**
 * Widget to render a set of Actions that look like Buttons.
 */
export default class BoxButtons extends Widget implements BoxButtonsModel {
  declare model: BoxButtonsModel;
  declare eventMap: BoxButtonsEventMap;

  buttons: Action[];
  defaultButtonIndex: number;

  constructor() {
    super();
    this._addWidgetProperties(['buttons']);
    this._addPreserveOnPropertyChangeProperties(['defaultButtonIndex']);

    this.$container = null;
    this.buttons = [];
    this.defaultButtonIndex = 0;
  }

  protected override _render() {
    this.$container = this.$parent.appendDiv('box-buttons');
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderButtons();
    this._renderDefaultButtonIndex();
  }

  protected _renderButtons() {
    this.buttons
      .filter(button => !button.rendered)
      .forEach(button => {
        button.render();
        button.$container.unfocusable().addClass('button box-button');
      });
  }

  protected _renderDefaultButtonIndex() {
    for (let i = 0; i < this.buttons.length; i++) {
      let button = this.buttons[i];
      button.$container.toggleClass('default', button.isVisible() && button.enabledComputed && this.isDefaultButtonIndex(i));
    }
  }

  isDefaultButtonIndex(index: number): boolean {
    return index === this.defaultButtonIndex;
  }

  addButton(model: Omit<ActionModel, 'parent'>, options?: ObjectFactoryOptions): Action {
    model = model || {};
    model.parent = this;
    model.tabbable = true;
    model.actionStyle = Action.ActionStyle.BUTTON;
    model.preventDoubleClick = true;
    let button = scout.create(Action, model as ActionModel, options);
    this.buttons.push(button);
    return button;
  }

  setDefaultButtonIndex(index: number) {
    this.setProperty('defaultButtonIndex', index);
  }

  buttonCount(): number {
    return this.buttons.length;
  }
}
