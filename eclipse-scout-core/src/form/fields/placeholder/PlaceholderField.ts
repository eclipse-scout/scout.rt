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

export default class PlaceholderField extends FormField {

  constructor() {
    super();
  }

  _render() {
    this.addContainer(this.$parent, 'placeholder-field');
    this.addLabel();
  }

  /**
   * @override
   */
  _renderLabel() {
    // Field needs a label to ensure correct layout when labelPosition = TOP.
    // The label is always rendered empty, because place holder fields should not have any visible parts.
    this._renderEmptyLabel();
  }
}
