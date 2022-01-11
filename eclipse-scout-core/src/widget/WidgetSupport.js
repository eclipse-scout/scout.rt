/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {objects, scout} from '../index';

export default class WidgetSupport {

  /**
   * @typedef WidgetSupportOptions
   * @property {Widget} widget Widget that created the support
   * @property {$|function} [$container] jQuery element that will be used for the visualization.
   *  It may be a function to resolve the container later.
   *  If this property is not set the $container of the widget is used by default.
   */

  /**
   * @param {WidgetSupportOptions} options a mandatory options object
   */
  constructor(options) {
    scout.assertParameter('widget', options.widget);

    this.widget = options.widget;
    this.options$Container = options.$container;
  }

  _ensure$Container() {
    if (objects.isFunction(this.options$Container)) {
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
