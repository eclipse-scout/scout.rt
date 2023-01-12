/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {FormField, Planner, PlannerFieldModel} from '../../../index';

export class PlannerField extends FormField implements PlannerFieldModel {
  declare model: PlannerFieldModel;

  planner: Planner;

  constructor() {
    super();
    this._addWidgetProperties(['planner']);
    this.gridDataHints.weightY = 1.0;
  }

  protected override _render() {
    this.addContainer(this.$parent, 'planner-field');
    this.addLabel();
    this.addStatus();
    if (this.planner) {
      this._renderPlanner();
    }
  }

  /**
   * Will also be called by model adapter on property change event
   */
  protected _renderPlanner() {
    this.planner.render();
    this.addField(this.planner.$container);
  }

  protected _removePlanner() {
    this.planner.remove();
    this._removeField();
  }
}
