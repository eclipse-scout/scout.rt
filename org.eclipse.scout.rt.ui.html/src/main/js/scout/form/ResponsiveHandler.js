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
scout.ResponsiveHandler = function() {
  this.widget = null;
  this.compactThreshold = -1;
  this.condensedThreshold = -1;

  this.oldState = scout.ResponsiveManager.ResponsiveState.NORMAL;
  this.state = scout.ResponsiveManager.ResponsiveState.NORMAL;
  this.allowedStates = [scout.ResponsiveManager.ResponsiveState.NORMAL, scout.ResponsiveManager.ResponsiveState.COMPACT];

  this.transformations = scout.objects.createMap();
  this.enabledTransformations = scout.objects.createMap();

  // Event handlers
  this._destroyHandler = this._onDestroy.bind(this);
};

scout.ResponsiveHandler.prototype.init = function(model) {
  $.extend(this, model);

  this.widget.one('destroy', this._destroyHandler);
};

scout.ResponsiveHandler.prototype.destroy = function() {
  this.widget.off('destroy', this._destroyHandler);
};

scout.ResponsiveHandler.prototype.getCompactThreshold = function() {
  return this.compactThreshold;
};

scout.ResponsiveHandler.prototype.getCondensedThreshold = function() {
  return this.condensedThreshold;
};

scout.ResponsiveHandler.prototype.active = function() {
  return true;
};

scout.ResponsiveHandler.prototype.setAllowedStates = function(allowedStates) {
  this.allowedStates = allowedStates;
};

scout.ResponsiveHandler.prototype.acceptState = function(newState) {
  return scout.arrays.containsAny(this.allowedStates, newState);
};

/**
 * Register a transformation with a given transformation id. The transformation id has to be unique.
 */
scout.ResponsiveHandler.prototype._registerTransformation = function(transformationId, transformation) {
  this.transformations[transformationId] = transformation.bind(this);
};

/**
 * Enable a transformation for a given state. Once the responsive handler changes in to the given state,
 * the transformation will be applied.
 * Before a transformation can be enabled, it has to be registered first.
 */
scout.ResponsiveHandler.prototype._enableTransformation = function(state, transformationId) {
  var transformationIds = this.enabledTransformations[state];
  if (!transformationIds) {
    transformationIds = [];
    this.enabledTransformations[state] = transformationIds;
  }
  transformationIds.push(transformationId);
};

/**
 * Disable a transformation for a given state.
 */
scout.ResponsiveHandler.prototype._disableTransformation = function(state, transformationId) {
  scout.arrays.remove(this.enabledTransformations[state], transformationId);
};

/* --- TRANSFORMATIONS ------------------------------------------------------------- */

scout.ResponsiveHandler.prototype._storeFieldProperty = function(widget, property, value) {
  widget._setProperty('responsive-' + property, value);
};

scout.ResponsiveHandler.prototype._hasFieldProperty = function(widget, property) {
  return widget.hasOwnProperty('responsive-' + property);
};

scout.ResponsiveHandler.prototype._getFieldProperty = function(widget, property) {
  return widget['responsive-' + property];
};

/**
 * Performs the transformations and computes which transformations have to be applied and which have to be reset.
 * Transformations to be applied are the ones enabled for the new state, but not for the old state.
 * The ones to be reset are those enabled of the old state but not for the new state.
 */
scout.ResponsiveHandler.prototype.transform = function(newState, force) {
  if (this.state === newState && !force) {
    return false;
  }

  this.oldState = this.state;
  this.state = newState;

  var oldTransformations;
  var newTransformations;
  if (this.oldState !== this.state) {
    oldTransformations = this.enabledTransformations[this.oldState] || [];
    newTransformations = this.enabledTransformations[this.state] || [];
  } else {
    // if the state stays the same, it means we want to enforce the current state. Therefore the new transformations
    // will contained the transformations of the current state. The old transformations will contain all others.
    oldTransformations = [];
    if (this.state !== scout.ResponsiveManager.ResponsiveState.NORMAL) {
      scout.arrays.pushAll(oldTransformations, this.enabledTransformations[scout.ResponsiveManager.ResponsiveState.NORMAL]);
    }
    if (this.state !== scout.ResponsiveManager.ResponsiveState.CONDENSED) {
      scout.arrays.pushAll(oldTransformations, this.enabledTransformations[scout.ResponsiveManager.ResponsiveState.CONDENSED]);
    }
    if (this.state !== scout.ResponsiveManager.ResponsiveState.COMPACT) {
      scout.arrays.pushAll(oldTransformations, this.enabledTransformations[scout.ResponsiveManager.ResponsiveState.COMPACT]);
    }
    newTransformations = this.enabledTransformations[this.state] || [];
  }

  this._transformationsToApply = scout.arrays.diff(newTransformations, oldTransformations);
  this._transformationsToReset = scout.arrays.diff(oldTransformations, newTransformations);

  this._transform();
  return true;
};

/**
 * Performs all the transformations. By default this method calls _transformWidget() for the own widget.
 * If e.g. child elements need to be transformed as well, override this method and call _transformWidget() for
 * each child as well.
 */
scout.ResponsiveHandler.prototype._transform = function() {
  this._transformWidget(this.widget);
};

scout.ResponsiveHandler.prototype._transformWidget = function(widget) {
  this._transformationsToApply.forEach(function(transformationType) {
    this.transformations[transformationType](widget, true);
  }.bind(this));

  this._transformationsToReset.forEach(function(transformationType) {
    this.transformations[transformationType](widget, false);
  }.bind(this));
};

/* --- HANDLERS ------------------------------------------------------------- */
scout.ResponsiveHandler.prototype._onDestroy = function(event) {
  this.destroy();
};
