/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {InitModelOf, ObjectModel, ObjectWithType, scout, Widget} from '../../index';
import $ from 'jquery';

export class HybridActionContextElement implements ObjectWithType {
  declare model: HybridActionContextElementModel;

  objectType: string;

  widget: Widget;
  element: any; // optional

  static of(widget: Widget, element: any): HybridActionContextElement {
    return scout.create(HybridActionContextElement, {
      widget,
      element
    });
  }

  constructor() {
    this.widget = null;
    this.element = null;
  }

  init(model: InitModelOf<HybridActionContextElement>) {
    $.extend(this, model);
  }
}

export interface HybridActionContextElementModel extends ObjectModel {
  widget: Widget;
  element?: any;
}

export interface JsonHybridActionContextElement {
  /** adapter id */
  widget: string;
  /** widget-specific element representation */
  element?: any;
}
