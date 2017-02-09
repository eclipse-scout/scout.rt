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
 * Renders glassPanes over the 'glassPaneTargets' of an element.
 */
scout.GlassPaneRenderer = function(session, element, enabled) {
  this._element = element;
  this._enabled = enabled;
  this.session = session;
  this._$glassPanes = [];
  this._$glassPaneTargets = [];
  this._deferredGlassPanes = [];
  this._resolvedDisplayParent = null;
  this._registeredDisplayParent = null;
};

scout.GlassPaneRenderer.prototype.renderGlassPanes = function() {
  this.findGlassPaneTargets().forEach(function(glassPaneTarget) {
    if (glassPaneTarget instanceof scout.DeferredGlassPaneTarget) {
      glassPaneTarget.rendererReady(this);
      this._deferredGlassPanes.push(glassPaneTarget);
    } else {
      this.renderGlassPane(glassPaneTarget);
    }
  }, this);
};

scout.GlassPaneRenderer.prototype.renderGlassPane = function(glassPaneTarget) {
  var $glassPane;

  // Render glasspanes onto glasspane targets.
  $glassPane = $(glassPaneTarget)
    .appendDiv('glasspane')
    .on('mousedown', this._onMousedown.bind(this));

  // Glasspanes in popup-windows must be visible, otherwise the user cannot recognize that the popup
  // is blocked, since the element that blocks (e.g a message-box) may be opened in the main-window.
  if ($glassPane.window(true).popupWindow) {
    $glassPane.addClass('dark');
  }

  this._$glassPanes.push($glassPane);
  this._$glassPaneTargets.push(glassPaneTarget);

  // Register 'glassPaneTarget' in focus manager.
  this.session.focusManager.registerGlassPaneTarget(glassPaneTarget);
  this._registerDisplayParent();

};

scout.GlassPaneRenderer.prototype.removeGlassPanes = function() {
  // Remove glass-panes
  this._$glassPanes.forEach(function($glassPane) {
    $glassPane.remove();
  });

  //Unregister all deferedGlassPaneTargets
  this._deferredGlassPanes.forEach(function(glassPaneTarget) {
    glassPaneTarget.removeGlassPaneRenderer(this);
  }, this);

  this._deferredGlassPanes = [];

  // Unregister glasspane targets from focus manager
  this._$glassPaneTargets.forEach(function($glassPaneTarget) {
    this.session.focusManager.unregisterGlassPaneTarget($glassPaneTarget);
  }, this);
  this._unregisterDisplayParent();


  this._$glassPanes = [];
  this._$glassPaneTargets = [];
};

scout.GlassPaneRenderer.prototype.eachGlassPane = function(func) {
  this._$glassPanes.forEach(function($glassPane) {
    func($glassPane);
  });
};

scout.GlassPaneRenderer.prototype.findGlassPaneTargets = function() {
  if (!this._enabled) {
    return []; // No glasspanes to be rendered, e.g. for none-modal dialogs.
  }

  var displayParent = this._resolveDisplayParent();
  if (!displayParent || !displayParent.glassPaneTargets) {
    return []; // Parent is not a valid display parent.
  }

  return displayParent.glassPaneTargets();
};

scout.GlassPaneRenderer.prototype._resolveDisplayParent = function() {
  // Note: This has to be done after rendering, because otherwise session.desktop could be undefined!
  if (!this._resolvedDisplayParent) {
    this._resolvedDisplayParent = this._element.displayParent || this.session.desktop;
  }
  return this._resolvedDisplayParent;
};

scout.GlassPaneRenderer.prototype._registerDisplayParent = function() {
  // if this._resolvedDisplayParent is not yet resolved, do it now
  if (!this._resolvedDisplayParent) {
    this._resolveDisplayParent();
  }
  // if this._resolvedDisplayParent is resolved, but not yet registered
  if (this._resolvedDisplayParent) {
    if (!this._registeredDisplayParent) {
      // register this._resolvedDisplayParent and remember it as this._registeredDisplayParent
      this.session.focusManager.registerGlassPaneDisplayParent(this._resolvedDisplayParent);
      this._registeredDisplayParent = this._resolvedDisplayParent;
    }
  }
};

scout.GlassPaneRenderer.prototype._unregisterDisplayParent = function() {
  // if this._registeredDisplayParent is defined, unregister it
  if (this._registeredDisplayParent) {
    this.session.focusManager.unregisterGlassPaneDisplayParent(this._registeredDisplayParent);
    this._registeredDisplayParent = null;
  }
};

scout.GlassPaneRenderer.prototype._onMousedown = function(event) {
  var $glassPane = $(event.target);
  var $animationTarget = null;

  if (this._element instanceof scout.Form && this._element.isView()) {
    // If the blocking element is a view, the $container cannot be animated (this only works for dialogs). Instead,
    // highlight the view tab (or the overflow item, if the view tab is not visible).

    $animationTarget = this.session.desktop.bench.getViewTab(this._element).$container;
    if (!$animationTarget.isVisible()) {
      $animationTarget = $animationTarget.siblings('.overflow-tab-item');
    }
  } else if (this._element.$container) {
    $animationTarget = this._element.$container;
  }

  if ($animationTarget) {
    $animationTarget.addClassForAnimation('animate-modality-highlight', {
      // remove animate-open as well, user may click the glasspane before the widget itself was able to remove the animate-open class
      classesToRemove: 'animate-modality-highlight animate-open'
    });
  }
};
