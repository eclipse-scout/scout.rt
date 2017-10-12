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
 * Is used to render glasspane after the glasspane targets are set. This case occurs when a child is rendered before a parent is rendered-> on reload page.
 */
scout.DeferredGlassPaneTarget = function() {
  this.$glassPaneTargets;
  this.glassPaneRenderer;
};

scout.DeferredGlassPaneTarget.prototype.ready = function($glassPaneTargets) {
  this.$glassPaneTargets = $glassPaneTargets;
  this.renderWhenReady();
};

scout.DeferredGlassPaneTarget.prototype.rendererReady = function(glassPaneRenderer) {
  this.glassPaneRenderer = glassPaneRenderer;
  this.renderWhenReady();
};

scout.DeferredGlassPaneTarget.prototype.removeGlassPaneRenderer = function(glassPaneRenderer) {
  if (this.glassPaneRenderer === glassPaneRenderer) {
    this.glassPaneRenderer = null;
  }
};

scout.DeferredGlassPaneTarget.prototype.renderWhenReady = function() {
  if (this.glassPaneRenderer && this.$glassPaneTargets && this.$glassPaneTargets.length > 0) {
    this.$glassPaneTargets.forEach(function($glassPaneTarget) {
      this.glassPaneRenderer.renderGlassPane($glassPaneTarget);
    }.bind(this));
  }
};

/* --- STATIC HELPERS ------------------------------------------------------------- */

/**
 * @param widget a not rendered scout.Widget
 * @findGlassPaneTargets function which returns the targets
 */
scout.DeferredGlassPaneTarget.createFor = function(widget, findGlassPaneTargets) {
  if (widget.rendered) {
    throw new Error('Don\'t call this function if widget is already rendered.');
  }

  var deferred = new scout.DeferredGlassPaneTarget();
  var renderedHandler = function(event) {
    var elements = findGlassPaneTargets();
    deferred.ready(elements);
  };

  widget.one('render', renderedHandler);
  widget.one('destroy', function() {
    widget.off('render', renderedHandler);
  }.bind(widget));
  return [deferred];
};
