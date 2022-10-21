/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {FormField} from '../../../index';

export default class PlannerField extends FormField {

  constructor() {
    super();
    this._addWidgetProperties(['planner']);

    this.gridDataHints.weightY = 1.0;
  }

  _render() {
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
  _renderPlanner() {
    this.planner.render();
    this.addField(this.planner.$container);
  }

  _removePlanner() {
    this.planner.remove();
    this._removeField();
  }
}
