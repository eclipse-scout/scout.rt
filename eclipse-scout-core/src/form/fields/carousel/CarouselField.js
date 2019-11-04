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

export default class CarouselField extends FormField {

  constructor() {
    super();
    this.gridDataHints.weightY = 1.0;

    this._addWidgetProperties(['carousel']);
  }

  _render() {
    this.addContainer(this.$parent, 'carousel-field');
    this.addLabel();
    this.addMandatoryIndicator();
    this.addStatus();
    if (this.carousel) {
      this._renderCarousel();
    }
  }

  _renderCarousel() {
    this.carousel.render();
    this.addField(this.carousel.$container);
  }

  _removeCarousel() {
    this.carousel.remove();
    this._removeField();
  }
}
