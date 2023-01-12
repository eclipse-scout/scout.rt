/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, Event, InitModelOf, objects, ObjectWithType, ResponsiveHandlerModel, ResponsiveManager, ResponsiveState, SomeRequired, Widget} from '../index';
import $ from 'jquery';

export class ResponsiveHandler implements ResponsiveHandlerModel, ObjectWithType {
  declare model: ResponsiveHandlerModel;
  declare initModel: SomeRequired<this['model'], 'widget'>;

  objectType: string;
  widget: Widget;
  compactThreshold: number;
  condensedThreshold: number;
  oldState: ResponsiveState;
  state: ResponsiveState;
  allowedStates: ResponsiveState[];
  transformations: Record<string, (widget: Widget, apply: boolean) => void>;
  enabledTransformations: Record<ResponsiveState, string[]>;

  protected _transformationsToApply: string[];
  protected _transformationsToReset: string[];
  /** Event handlers */
  protected _destroyHandler: (Event) => void;

  constructor() {
    this.widget = null;
    this.compactThreshold = -1;
    this.condensedThreshold = -1;

    this.oldState = ResponsiveManager.ResponsiveState.NORMAL;
    this.state = ResponsiveManager.ResponsiveState.NORMAL;
    this.allowedStates = [ResponsiveManager.ResponsiveState.NORMAL, ResponsiveManager.ResponsiveState.COMPACT];

    this.transformations = objects.createMap();
    this.enabledTransformations = objects.createMap();

    this._destroyHandler = this._onDestroy.bind(this);
  }

  init(model: InitModelOf<this>) {
    $.extend(this, model);

    this.widget.one('destroy', this._destroyHandler);
  }

  destroy() {
    this.widget.off('destroy', this._destroyHandler);
  }

  getCompactThreshold(): number {
    return this.compactThreshold;
  }

  getCondensedThreshold(): number {
    return this.condensedThreshold;
  }

  active(): boolean {
    return true;
  }

  setAllowedStates(allowedStates: ResponsiveState[]) {
    this.allowedStates = allowedStates;
  }

  acceptState(newState: ResponsiveState): boolean {
    return arrays.contains(this.allowedStates, newState);
  }

  /**
   * Register a transformation with a given transformation id. The transformation id has to be unique.
   */
  protected _registerTransformation(transformationId: string, transformation: (widget: Widget, apply: boolean) => void) {
    this.transformations[transformationId] = transformation.bind(this);
  }

  /**
   * Enable a transformation for a given state. Once the responsive handler changes in to the given state,
   * the transformation will be applied.
   * Before a transformation can be enabled, it has to be registered first.
   */
  protected _enableTransformation(state: ResponsiveState, transformationId: string) {
    let transformationIds = this.enabledTransformations[state];
    if (!transformationIds) {
      transformationIds = [];
      this.enabledTransformations[state] = transformationIds;
    }
    transformationIds.push(transformationId);
  }

  /**
   * Disable a transformation for a given state.
   */
  protected _disableTransformation(state: ResponsiveState, transformationId: string) {
    arrays.remove(this.enabledTransformations[state], transformationId);
  }

  /* --- TRANSFORMATIONS ------------------------------------------------------------- */

  protected _storeFieldProperty(widget: Widget, property: string, value: any) {
    widget._setProperty('responsive-' + property, value);
  }

  protected _hasFieldProperty(widget: Widget, property: string): boolean {
    return widget.hasOwnProperty('responsive-' + property);
  }

  protected _getFieldProperty(widget: Widget, property: string): any {
    return widget['responsive-' + property];
  }

  /**
   * Performs the transformations and computes which transformations have to be applied and which have to be reset.
   * Transformations to be applied are the ones enabled for the new state, but not for the old state.
   * The ones to be reset are those enabled of the old state but not for the new state.
   */
  transform(newState: ResponsiveState, force?: boolean): boolean {
    if (this.state === newState && !force) {
      return false;
    }

    this.oldState = this.state;
    this.state = newState;

    let oldTransformations;
    let newTransformations;
    if (this.oldState !== this.state) {
      oldTransformations = this.enabledTransformations[this.oldState] || [];
      newTransformations = this.enabledTransformations[this.state] || [];
    } else {
      // if the state stays the same, it means we want to enforce the current state. Therefore, the new transformations
      // will contain the transformations of the current state. The old transformations will contain all others.
      oldTransformations = [];
      if (this.state !== ResponsiveManager.ResponsiveState.NORMAL) {
        arrays.pushAll(oldTransformations, this.enabledTransformations[ResponsiveManager.ResponsiveState.NORMAL]);
      }
      if (this.state !== ResponsiveManager.ResponsiveState.CONDENSED) {
        arrays.pushAll(oldTransformations, this.enabledTransformations[ResponsiveManager.ResponsiveState.CONDENSED]);
      }
      if (this.state !== ResponsiveManager.ResponsiveState.COMPACT) {
        arrays.pushAll(oldTransformations, this.enabledTransformations[ResponsiveManager.ResponsiveState.COMPACT]);
      }
      newTransformations = this.enabledTransformations[this.state] || [];
    }

    this._transformationsToApply = arrays.diff(newTransformations, oldTransformations);
    this._transformationsToReset = arrays.diff(oldTransformations, newTransformations);

    this._transform();
    return true;
  }

  /**
   * Performs all the transformations. By default, this method calls _transformWidget() for the own widget.
   * If e.g. child elements need to be transformed as well, override this method and call _transformWidget() for
   * each child as well.
   */
  protected _transform() {
    this._transformWidget(this.widget);
  }

  protected _transformWidget(widget: Widget) {
    this._transformationsToApply.forEach(transformationType => {
      this.transformations[transformationType](widget, true);
    });

    this._transformationsToReset.forEach(transformationType => {
      this.transformations[transformationType](widget, false);
    });
  }

  /* --- HANDLERS ------------------------------------------------------------- */
  protected _onDestroy(event: Event<Widget>) {
    this.destroy();
  }
}
