/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
/**
 * Helper to support clone property and event handling between original and clone.
 *
 * OPTION                   DEFAULT VALUE   DESCRIPTION
 * --------------------------------------------------------------------------------------------------------
 * delegateProperties       []              An array of all properties to be delegated from the source
 *                                          to the to the target when changed on the source.
 *
 * excludeProperties        []              An array of all properties to be excluded from delegating to the
 *                                          in all cases.
 *
 * delegateEvents           []              An array of all events to be delegated from the source to
 *                                          the target when fired on the source.
 *
 * delegateAllProperties    false           True to delegate all property changes from the source to
 *                                          the target.
 *
 * delegateAllEvents        false           True to delegate all events from the source to
 *                                          the target.
 *
 */
scout.EventDelegator = function(source, target, options) {
  options = options || {};
  this.source = source;
  this.target = target;
  this.delegateProperties = options.delegateProperties || [];
  this.excludeProperties = options.excludeProperties || [];
  this.delegateEvents = options.delegateEvents || [];
  this.delegateAllProperties = !!options.delegateAllProperties;
  this.delegateAllEvents = !!options.delegateAllEvents;
  this._mirrorListener = null;
  this._destroyHandler;

  this._installSourceListener();
};

scout.EventDelegator.prototype.destroy = function() {
  this._uninstallSourceListener();
};

scout.EventDelegator.prototype._installSourceListener = function() {
  if (this._mirrorListener) {
    throw new Error('source listeners already installed.');
  }
  this._mirrorListener = {
    func: this._onSourceEvent.bind(this)
  };
  this.source.events.addListener(this._mirrorListener);
  this._destroyHandler = this._uninstallSourceListener.bind(this);
  this.source.on('destroy', this._destroyHandler);
  this.target.on('destroy', this._destroyHandler);
};

scout.EventDelegator.prototype._uninstallSourceListener = function() {
  if (this._mirrorListener) {
    this.source.events.removeListener(this._mirrorListener);
    this._mirrorListener = null;
  }
  if (this._destroyHandler) {
    this.source.off('destroy', this._destroyHandler);
    this.target.off('destroy', this._destroyHandler);
    this._destroyHandler = null;
  }
};

scout.EventDelegator.prototype._onSourceEvent = function(event) {
  if (event.type === 'propertyChange') {
    this._onSourcePropertyChange(event);
  } else if (this.delegateAllEvents || this.delegateEvents.indexOf(event.type) > -1) {
    this.target.trigger(event.type, event);
  }
};

scout.EventDelegator.prototype._onSourcePropertyChange = function(event) {
  if (this.excludeProperties.indexOf(event.propertyName) > -1) {
    return;
  }
  if (this.delegateAllProperties || this.delegateProperties.indexOf(event.type) > -1) {
    this.target.callSetter(event.propertyName, event.newValue);
  }
};

scout.EventDelegator.create = function(source, target, options) {
  if ((options.delegateProperties && options.delegateProperties.length > 0) ||
    (options.delegateProperties && options.delegateProperties.length > 0) ||
    options.delegateAllProperties ||
    options.delegateAllEvents) {
    return new scout.EventDelegator(source, target, options);
  }
  return null;
};
