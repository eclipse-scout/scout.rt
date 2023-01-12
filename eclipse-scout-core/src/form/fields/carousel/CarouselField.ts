/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Carousel, CarouselFieldModel, FormField} from '../../../index';

export class CarouselField extends FormField implements CarouselFieldModel {
  declare model: CarouselFieldModel;

  carousel: Carousel;

  constructor() {
    super();
    this.gridDataHints.weightY = 1.0;

    this._addWidgetProperties(['carousel']);
  }

  protected override _render() {
    this.addContainer(this.$parent, 'carousel-field');
    this.addLabel();
    this.addMandatoryIndicator();
    this.addStatus();
    if (this.carousel) {
      this._renderCarousel();
    }
  }

  protected _renderCarousel() {
    this.carousel.render();
    this.addField(this.carousel.$container);
  }

  protected _removeCarousel() {
    if (this.carousel) {
      this.carousel.remove();
    }
    this._removeField();
  }
}
