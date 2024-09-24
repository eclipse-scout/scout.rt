/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, BusyIndicator, BusyIndicatorModel, BusySupportModel, Event, EventHandler, InitModelOf, objects, ObjectWithType, scout, SomeRequired} from '../index';
import $ from 'jquery';

export class BusySupport implements ObjectWithType {
  declare model: BusySupportModel;
  declare initModel: SomeRequired<this['model'], 'parent'>;
  declare self: BusySupport;

  objectType: string;
  defaultBusyIndicatorModel: InitModelOf<BusyIndicator>;
  defaultRenderDelay: number;
  busyIndicator: BusyIndicator;

  protected _busyCounter: number;
  protected _busyIndicatorTimeoutId: number;
  protected _cancellationCallbacks: EventHandler<Event<BusyIndicator>>[];

  constructor() {
    this.objectType = null;
    this.defaultBusyIndicatorModel = null;
    this.defaultRenderDelay = 50;
    this.busyIndicator = null;
    this._busyCounter = 0;
    this._busyIndicatorTimeoutId = null;
    this._cancellationCallbacks = [];
  }

  init(model: InitModelOf<this>) {
    scout.assertParameter('model', model);
    const defaultIndicatorParent = scout.assertParameter('parent', model.parent);
    this.defaultBusyIndicatorModel = $.extend({
      cancellable: false,
      parent: defaultIndicatorParent
    }, model.busyIndicatorModel);
    this.defaultRenderDelay = scout.nvl(model.renderDelay, this.defaultRenderDelay);
  }

  /** @see BusySupportModel.renderDelay */
  setDefaultRenderDelay(delay: number) {
    this.defaultRenderDelay = delay;
  }

  /** @see BusySupportModel.busyIndicatorModel */
  setDefaultBusyIndicatorModel(model: InitModelOf<BusyIndicator>) {
    this.defaultBusyIndicatorModel = model;
  }

  isBusy(): boolean {
    return this._busyCounter > 0;
  }

  /**
   * Changes the busy state
   *
   * @param options A boolean indicating if busy or not to show the busy indicator with the default settings (as passed when creating this instance) and without a cancellation callback.
   * Or a {@link BusyIndicatorOptions} object which allows to customize the behavior and style of the indicator and to pass a cancellation callback.
   */
  setBusy(options: boolean | BusyIndicatorOptions) {
    let optionsObj: BusyIndicatorOptions;
    if (objects.isObject(options)) {
      optionsObj = options;
    } else {
      optionsObj = {
        busy: !!options
      };
    }
    optionsObj.renderDelay = scout.nvl(optionsObj.renderDelay, this.defaultRenderDelay);

    if (optionsObj.busy) {
      this._busyCounter++;
      if (optionsObj.onCancel && !arrays.contains(this._cancellationCallbacks, optionsObj.onCancel)) {
        this._cancellationCallbacks.push(optionsObj.onCancel);
      }
      if (this._busyCounter === 1) {
        this._startBusy(optionsObj);
      }
    } else {
      if (this._busyCounter > 0) {
        if (optionsObj.force) {
          this._busyCounter = 0;
        } else {
          this._busyCounter--;
        }
      }
      if (this._busyCounter === 0) {
        this._cancellationCallbacks = []; // reset callback list for next busy round
        this._stopBusy();
      }
    }
  }

  protected _startBusy(options: BusyIndicatorOptions) {
    if (this.busyIndicator) {
      return; // already busy
    }
    const model = $.extend({}, this.defaultBusyIndicatorModel, options.busyIndicatorModel, {parent: options.parent});
    this.busyIndicator = scout.create(BusyIndicator, model);
    this.busyIndicator.one('cancel', this._onBusyIndicatorCancel.bind(this));
    if (options.renderDelay > 0) {
      // Don't show the busy indicator immediately
      this._busyIndicatorTimeoutId = setTimeout(() => {
        this._busyIndicatorTimeoutId = null;
        this._renderBusy();
      }, options.renderDelay);
    } else {
      this._renderBusy();
    }
  }

  protected _renderBusy() {
    if (!this.busyIndicator?.parent?.rendered) {
      return;
    }
    this.busyIndicator.render();
  }

  protected _stopBusy() {
    if (this.busyIndicator) {
      this.busyIndicator.destroy();
      this.busyIndicator = null;
    }
    if (this._busyIndicatorTimeoutId) {
      clearTimeout(this._busyIndicatorTimeoutId);
      this._busyIndicatorTimeoutId = null;
    }
  }

  protected _onBusyIndicatorCancel(event: Event<BusyIndicator>) {
    // Set "canceling" state in busy indicator (after 100ms, would not look good otherwise)
    setTimeout(() => this.busyIndicator?.cancelled(), 100);
    this._cancellationCallbacks.forEach(callback => callback(event));
  }
}

export interface BusyIndicatorOptions extends BusySupportModel {
  /**
   * Specifies if the {@link BusySupport} should be set to busy or not.
   * The default is false.
   */
  busy: boolean;
  /**
   * If set to true and {@link BusyIndicatorOptions.busy} is false, the {@link BusyIndicator} is removed even if there have been fewer calls with busy=false than with busy=true (asymmetric).
   * The default is false.
   */
  force?: boolean;
  /**
   * Adds a callback to be executed when the {@link BusyIndicator} is cancelled. Only executed if {@link BusyIndicatorModel.cancellable} is true.
   * Every callback is only executed once when the {@link BusyIndicator} is removed.
   * Only used if {@link BusyIndicatorOptions.busy} is true.
   */
  onCancel?: EventHandler<Event<BusyIndicator>>;
}
