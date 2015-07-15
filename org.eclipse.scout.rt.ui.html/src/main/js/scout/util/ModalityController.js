/**
 * Controller to paint a glassPane over the given element like Form, message box or file chooser.
 */
scout.ModalityController = function(element) {
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

    // FIXME [dwi] Fix keystrokes on modal elements.
    // $glassPane.installFocusContext('auto', this._session.uiSessionId);
    $glassPane.appendTo($modalityElement);
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
