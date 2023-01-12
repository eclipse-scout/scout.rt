/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {FormField} from '../../../index';

export class PlaceholderField extends FormField {

  protected override _render() {
    this.addContainer(this.$parent, 'placeholder-field');
    this.addLabel();
  }

  protected override _renderLabel() {
    // Field needs a label to ensure correct layout when labelPosition = TOP.
    // The label is always rendered empty, because place holder fields should not have any visible parts.
    this._renderEmptyLabel();
  }
}
