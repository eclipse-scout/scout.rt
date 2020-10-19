/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {BeanField} from '../../../../index';

export default class TestBeanField extends BeanField {
  _render() {
    super._render();
    this.$container.addClass('test-bean-field');
  }

  _renderValue() {
    this.$field.empty();
    if (!this.value) {
      return;
    }

    this.$field.appendDiv('msg-from')
      .text('Message from ' + this.value.sender);

    this.$field.appendDiv('msg-text')
      .textOrNbsp(this.value.message);
  }
}
