/**
 * Controller to paint a glassPane over the given element like Form, message box or file chooser.
 */
scout.ModalityController = function(element) {
  this._element = element;
  this._displayParent = element.parent || element.session.desktop; // the logical parent which the given element is attached to.
  this._session = element.session;
  this._$glassPanes = [];
  this.render = true;
};

/**
 * Adds a glassPane over the DOM elements as specified by the element's 'displayParent'.
 */
scout.ModalityController.prototype.addGlassPane = function() {
  if (!this.render || !this._displayParent) {
    // Nothing to be done if rendering the glasspane is manually disabled by setting
    // "render" to false (see Form.js) or there is no displayParent. The later case
    // may happen while showing a popup before the desktop is available (e.g. fatal
    // message). When no desktop is present, a glasspane is not necessary.
    return;
  }

  this._displayParent.modalityElements().forEach(function($modalityElement) {
    var $glassPane = scout.fields.new$Glasspane(this._session.uiSessionId);
    this._$glassPanes.push($glassPane);
    $glassPane.on('mousedown', this._onMousedown.bind(this))
      .appendTo($modalityElement);
  }, this);
};

/**
 * Removes all added glassPanes.
 */
scout.ModalityController.prototype.removeGlassPane = function() {
  if (!this.render) {
    return;
  }

  this._$glassPanes.forEach(function($glassPane) {
    $glassPane.fadeOutAndRemove();
  }, this);
};

scout.ModalityController.prototype._onMousedown = function(event) {
  var $glassPane = $(event.target);

  this._element.$container.addClassForAnimation('modality-highlight', {
    // remove shown as well, user may click the glasspane before the widget itself was able to remove the shown class
    classesToRemove: 'modality-highlight shown',
    delay: 500
  });
};
