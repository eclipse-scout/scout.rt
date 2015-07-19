/**
 * Renders glassPanes over the 'glassPaneTargets' of an element.
 */
scout.GlassPaneRenderer = function(element, enabled) {
  this._element = element;
  this._$glassPanes = [];

  var parent = element.parent || element.session.desktop; // use Desktop if no parent set. However, the Desktop must not be available yet, e.g. fatal errors during startup.

  // Only query glassPaneTargets for a valid parent.
  if (enabled && parent && parent.glassPaneTargets) {
    this._glassPaneTargets = parent.glassPaneTargets();
  } else {
    this._glassPaneTargets = [];
  }
};

scout.GlassPaneRenderer.prototype.renderGlassPanes = function() {
  this._glassPaneTargets.forEach(function($glassPaneTarget) {
    this._$glassPanes.push($.makeDiv('glasspane')
      .on('mousedown', this._onMousedown.bind(this))
      .appendTo($glassPaneTarget));
  }, this);
};

scout.GlassPaneRenderer.prototype.removeGlassPanes = function() {
  this._$glassPanes.forEach(function($glassPane) {
    $glassPane.fadeOutAndRemove();
  });

  this._$glassPanes = [];
};

scout.GlassPaneRenderer.prototype.eachGlassPane = function(func) {
  this._$glassPanes.forEach(function($glassPane) {
    func($glassPane);
  });
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
