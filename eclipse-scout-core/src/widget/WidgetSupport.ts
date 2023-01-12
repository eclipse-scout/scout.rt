/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {scout, Widget, WidgetSupportOptions} from '../index';

export class WidgetSupport {
  options$Container: JQuery | (() => JQuery);
  widget: Widget;
  $container: JQuery;

  /**
   * @param options a mandatory options object
   */
  constructor(options: WidgetSupportOptions) {
    scout.assertParameter('widget', options.widget);

    this.widget = options.widget;
    this.options$Container = options.$container;
  }

  protected _ensure$Container() {
    if (typeof this.options$Container === 'function') {
      // resolve function provided by options.$container that returns a jQuery element
      this.$container = this.options$Container();
    } else if (this.options$Container) {
      // use jQuery element provided by options.$container
      this.$container = this.options$Container;
    } else {
      // default: when no options.$container is not set, use jQuery element of widget
      this.$container = this.widget.$container;
    }
  }
}
