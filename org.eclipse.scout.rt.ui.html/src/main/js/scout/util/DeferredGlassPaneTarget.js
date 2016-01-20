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
scout.DeferredGlassPaneTarget = function() {
  this.$glassPaneTarget;
  this.glassPaneRenderer;
};

scout.DeferredGlassPaneTarget.prototype.ready = function($glassPaneTarget) {
  this.$glassPaneTarget = $glassPaneTarget;
  this.renderWhenReady();
};

scout.DeferredGlassPaneTarget.prototype.rendererReady = function(glassPaneRenderer) {
  this.glassPaneRenderer = glassPaneRenderer;
  this.renderWhenReady();
};

scout.DeferredGlassPaneTarget.prototype.renderWhenReady = function() {
  if (this.glassPaneRenderer && this.$glassPaneTarget) {
    this.glassPaneRenderer.renderGlassPane(this.$glassPaneTarget[0]);
  }
};
