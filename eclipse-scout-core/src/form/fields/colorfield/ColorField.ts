/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {ValueField} from '../../../index';

export class ColorField extends ValueField<string> {

  protected override _render() {
    this.addContainer(this.$parent, 'color-field');
    this.addLabel();
    this.addField(this.$parent.makeDiv('not-implemented').text('not implemented yet'));
    this.addMandatoryIndicator();
    this.addStatus();
  }
}
