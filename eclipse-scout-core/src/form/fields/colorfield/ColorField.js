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
import {ValueField} from '../../../index';

export default class ColorField extends ValueField {

  constructor() {
    super();
  }

  _render() {
    this.addContainer(this.$parent, 'color-field');
    this.addLabel();
    this.addField(this.$parent.makeDiv('not-implemented').text('not implemented yet'));
    this.addMandatoryIndicator();
    this.addStatus();
  }
}
