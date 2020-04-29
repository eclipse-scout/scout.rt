/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays, objects, ResponsiveManager} from '../index';
import $ from 'jquery';

export default class ResponsiveHandler {

  constructor() {
    this.widget = null;
    this.compactThreshold = -1;
    this.condensedThreshold = -1;

    this.oldState = ResponsiveManager.ResponsiveState.NORMAL;
    this.state = ResponsiveManager.ResponsiveState.NORMAL;
    this.allowedStates = [ResponsiveManager.ResponsiveState.NORMAL, ResponsiveManager.ResponsiveState.COMPACT];

    this.transformations = objects.createMap();
    this.enabledTransformations = objects.createMap();

    // Event handlers
    this._destroyHandler = this._onDestroy.bind(this);
  }

  init(model) {
    $.extend(this, model);

    this.widget.one('destroy', this._destroyHandler);
  }

  destroy() {
    this.widget.off('destroy', this._destroyHandler);
  }

  getCompactThreshold() {
    return this.compactThreshold;
  }

  getCondensedThreshold() {
    return this.condensedThreshold;
  }

  active() {
    return true;
  }

  setAllowedStates(allowedStates) {
    this.allowedStates = allowedStates;
  }

  acceptState(newState) {
    return arrays.containsAny(this.allowedStates, newState);
  }

  /**
   * Register a transformation with a given transformation id. The transformation id has to be unique.
   */
  _registerTransformation(transformationId, transformation) {
    this.transformations[transformationId] = transformation.bind(this);
  }

  /**
   * Enable a transformation for a given state. Once the responsive handler changes in to the given state,
   * the transformation will be applied.
   * Before a transformation can be enabled, it has to be registered first.
   */
  _enableTransformation(state, transformationId) {
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
  _disableTransformation(state, transformationId) {
    arrays.remove(this.enabledTransformations[state], transformationId);
  }

  /* --- TRANSFORMATIONS ------------------------------------------------------------- */

  _storeFieldProperty(widget, property, value) {
    widget._setProperty('responsive-' + property, value);
  }

  _hasFieldProperty(widget, property) {
    return widget.hasOwnProperty('responsive-' + property);
  }

  _getFieldProperty(widget, property) {
    return widget['responsive-' + property];
  }

  /**
   * Performs the transformations and computes which transformations have to be applied and which have to be reset.
   * Transformations to be applied are the ones enabled for the new state, but not for the old state.
   * The ones to be reset are those enabled of the old state but not for the new state.
   */
  transform(newState, force) {
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
      // if the state stays the same, it means we want to enforce the current state. Therefore the new transformations
      // will contained the transformations of the current state. The old transformations will contain all others.
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
   * Performs all the transformations. By default this method calls _transformWidget() for the own widget.
   * If e.g. child elements need to be transformed as well, override this method and call _transformWidget() for
   * each child as well.
   */
  _transform() {
    this._transformWidget(this.widget);
  }

  _transformWidget(widget) {
    this._transformationsToApply.forEach(transformationType => {
      this.transformations[transformationType](widget, true);
    });

    this._transformationsToReset.forEach(transformationType => {
      this.transformations[transformationType](widget, false);
    });
  }

  /* --- HANDLERS ------------------------------------------------------------- */
  _onDestroy(event) {
    this.destroy();
  }
}
