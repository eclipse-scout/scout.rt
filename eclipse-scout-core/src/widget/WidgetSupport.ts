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
import {scout, Widget} from '../index';


export interface WidgetSupportOptions {
    /**
     * Widget that created the support
     */
    widget: Widget,

    /**
     * jQuery element that will be used for the visualization.
     *  It may be a function to resolve the container later.
     *  If this property is not set the $container of the widget is used by default.
     */
    $container?: JQuery | Function
}

export default class WidgetSupport {
    widget: Widget;
    protected readonly options$Container: JQuery | Function;
    $container: JQuery;

    /**
     * @param {WidgetSupportOptions} options a mandatory options object
     */
    constructor(options: WidgetSupportOptions) {
        scout.assertParameter('widget', options.widget);

        this.widget = options.widget;
        this.options$Container = options.$container;
    }

    _ensure$Container() {
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
