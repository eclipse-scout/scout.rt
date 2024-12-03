/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Constructor, InitModelOf, ObjectModel, ObjectWithType, scout, Widget} from '../../index';
import $ from 'jquery';

/**
 * Represents a widget and optionally a non-{@link Widget} element owned by it.
 */
export class HybridActionContextElement implements ObjectWithType {
  declare model: HybridActionContextElementModel;

  objectType: string;

  widget: Widget = null;
  element: any = null; // optional

  /**
   * @param widget may not be `null`
   * @param element may be `null`
   * @throws Error if widget is `null`
   */
  static of(widget: Widget, element?: any): HybridActionContextElement {
    return scout.create(HybridActionContextElement, {
      widget,
      element
    });
  }

  init(model: InitModelOf<HybridActionContextElement>) {
    scout.assertProperty(model, 'widget', Widget);
    $.extend(this, model);
  }

  /**
   * @returns the typed {@link widget}, never `null`
   * @throws Error if the widget is not an instance of the given type
   */
  getWidget(): Widget;
  getWidget<W extends Widget>(widgetType: Constructor<W>): W;
  getWidget<W extends Widget>(widgetType?: Constructor<W>): W {
    let widget = scout.assertValue(this.widget);
    return widgetType ? scout.assertInstance(widget, widgetType) : scout.assertInstance(widget, Widget) as W;
  }

  /**
   * @returns the typed or untyped {@link element}, never `null`
   * @throws Error if the element is `null` or not an instance of the given type. Use {@link optElement} to return `null` instead.
   */
  getElement<E>(elementType?: Constructor<E>): E {
    return scout.assertValue(this.optElement(elementType));
  }

  /**
   * @returns the typed or untyped {@link element} or `null`
   * @throws Error if the element is not an instance of the given type.
   */
  optElement<E>(elementType?: Constructor<E>): E {
    if (!this.element) {
      return null;
    }
    let element = this.element;
    return elementType ? scout.assertInstance(element, elementType) : element;
  }
}

export interface HybridActionContextElementModel extends ObjectModel {
  widget: Widget;
  element?: any;
}
