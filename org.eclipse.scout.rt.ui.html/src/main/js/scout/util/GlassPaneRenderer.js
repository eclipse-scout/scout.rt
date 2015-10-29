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
};

scout.GlassPaneRenderer.prototype.renderGlassPanes = function() {
  this.findGlassPaneTargets().forEach(function($glassPaneTarget) {
    // Render glasspanes onto glasspane targets.
    this._$glassPanes.push($.makeDiv('glasspane')
      .on('mousedown', this._onMousedown.bind(this))
      .appendTo($glassPaneTarget));
    this._$glassPaneTargets.push($glassPaneTarget);

    // Register 'glassPaneTarget' in focus manager.
    this.session.focusManager.registerGlassPaneTarget($glassPaneTarget);

  }, this);
};

scout.GlassPaneRenderer.prototype.removeGlassPanes = function() {
  // Remove glasspanes
  this._$glassPanes.forEach(function($glassPane) {
    $glassPane.remove();
  });

  // Unregister glasspane targets from focus manager
  this._$glassPaneTargets.forEach(function($glassPaneTarget) {
    this.session.focusManager.unregisterGlassPaneTarget($glassPaneTarget);
  }, this);

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

  var parent = this._element.parent || this.session.desktop; // use Desktop if no parent set.
  if (!parent) {
    return []; // No parent, e.g. during startup to display fatal errors.
  }

  if (!parent.glassPaneTargets) {
    return []; // Parent is not a valid display parent.
  }

  return parent.glassPaneTargets();
};

scout.GlassPaneRenderer.prototype._onMousedown = function(event) {
  var $glassPane = $(event.target);

  if (this._element.$container) {
    this._element.$container.addClassForAnimation('modality-highlight', {
      // remove shown as well, user may click the glasspane before the widget itself was able to remove the shown class
      classesToRemove: 'modality-highlight shown',
      delay: 500
    });
  }
};
