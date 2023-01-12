/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BeanField} from '../../../../index';

export class TestBeanField extends BeanField<{ sender: string; message: string }> {
  protected override _render() {
    super._render();
    this.$container.addClass('test-bean-field');
  }

  protected override _renderValue() {
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
