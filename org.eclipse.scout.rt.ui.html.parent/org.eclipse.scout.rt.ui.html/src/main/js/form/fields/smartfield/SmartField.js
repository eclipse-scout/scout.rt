scout.SmartField = function() {
  scout.SmartField.parent.call(this);
  this._$popup;
};
scout.inherits(scout.SmartField, scout.ValueField);

scout.SmartField.prototype._render = function($parent) {
  this.addContainer($parent, 'smart-field');
  this.addLabel();
  this.addMandatoryIndicator();

  this.$field = $('<input>').
    attr('type', 'text').
    addClass('field').
    disableSpellcheck().
    blur(this._onFieldBlur.bind(this)).
    appendTo(this.$container).
    // TODO AWE: (smartfield) event-handling mit C.GU besprechen, siehe auch ValueField.js
    // --> 1. auf ValueField eine attachXyzEvent() Methode machen
    //     2. _onXyzEvent überschreiben
    on('keyup', this._onKeyup.bind(this)).
    on('blur', this._onBlur.bind(this));

  this.addIcon();
  this.addStatus();
};

scout.SmartField.prototype._onKeyup = function() {
  $.log.debug("_onKeyup");
  if (!this._$popup) {
    var fieldBounds = scout.HtmlComponent.getBounds(this.$field);
    var popupBounds = new scout.Rectangle(fieldBounds.x, fieldBounds.y + fieldBounds.height, fieldBounds.width, 100);
    this._$popup = $('<div>').
      addClass('smart-field-popup').
      appendTo(this.$container);
    scout.HtmlComponent.setBounds(this._$popup, popupBounds);
    this.session.send('proposal', this.id, {'text':this.$field.val()});
  }
};

scout.SmartField.prototype._onBlur = function() {
  $.log.debug("_onBlur");
  if (this._$popup) {
    this._$popup.remove();
    this._$popup = null;
  }
};

scout.SmartField.prototype.onModelAction = function(event) {
  if (event.type == 'searchResultsUpdated') {
    this._onSearchResultsUpdated(event.searchResults);
  } else {
    scout.SmartField.parent.prototype.onModelAction(event).bind(this);
  }
};

scout.SmartField.prototype._onSearchResultsUpdated = function(searchResults) {
  $.log.info('on searchResultsUpdated: ' + searchResults);
  this._$popup.children().remove();
  var i, sr;
  for (i=0; i<searchResults.length; i++) {
    sr = searchResults[i];
    $('<div>').
      on('click', this._proposalSelected.bind(this)).
      appendTo(this._$popup).
      html(sr);
  }
};

scout.SmartField.prototype._proposalSelected = function(event) {
  var source = event.target;
  $.log.info('proposal selected ' + $(source).html());
};
