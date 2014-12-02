/**
 * Cached lookup strategy.
 * Options are loaded once from the server and are filtered client-side only.
 */
scout.CachedLookupStrategy = function() {
  this._smartField;
};

scout.CachedLookupStrategy.prototype = {

    filterOptions: function(query) {
      var match, numVisibleOptions = 0,
        showAll = !query || '*' === query,
        regexp = new RegExp(query, 'im'),
        sf = this._smartField;
      sf._get$Options().each(function() {
        if (showAll) {
          $(this).setVisible(true);
        } else {
          match = $(this).html().match(regexp);
          if (match) {
            numVisibleOptions++;
          }
          $.log.debug('regexp=' + regexp + ' html=' + $(this).html());
          $(this).setVisible(match);
        }
      });
      if (showAll) {
        numVisibleOptions = sf.options.length;
      }
      sf._setStatusText(numVisibleOptions);
    },

  onOptionsLoaded: function(options) {
    // NOP
  },

  openPopup: function() {
    var sf = this._smartField,
      numOptions = sf.options.length;
    sf._showPopup(Math.min(10, numOptions), numOptions);
    sf._renderOptions(sf.options);
  }
};


/**
 * Remote lookup strategy.
 * Options are loaded and filtered on the server.
 */
scout.RemoteLookupStrategy = function() {
  this._smartField;
  this._delayed;
};

scout.RemoteLookupStrategy.prototype = {

  filterOptions: function(query) {
    clearTimeout(this._delayed);
    this._delayed = setTimeout(function() {
      this._loadOptions(query);
    }.bind(this), 250);
  },

  onOptionsLoaded: function(options) {
    var sf = this._smartField;
    $.log.debug('options loaded: ' + options.length);
    sf._emptyOptions();

    // adjust size of popup to loaded options (cannot know in advance)
    var oldBounds = scout.graphics.getBounds(sf._$popup),
      optionHeight = scout.HtmlEnvironment.formRowHeight,
      popupHeight = (Math.min(10, options.length) + 1) * optionHeight,
      newBounds = new scout.Rectangle(oldBounds.x, oldBounds.y, oldBounds.width, popupHeight);
    scout.graphics.setBounds(sf._$popup, newBounds);
    scout.graphics.setSize(sf._get$OptionsDiv(), newBounds.width, popupHeight - optionHeight);
    sf._updateScrollbar();
    sf._renderOptions(options);
    sf._setStatusText(options.length);
  },

  openPopup: function() {
    var sf = this._smartField;
    sf._showPopup(1, sf.session.text('LoadOptions'));
    this._loadOptions('*');
  },

  _loadOptions: function(query) {
    var sf = this._smartField;
    $.log.debug('load options from server. query=' + query);
    sf.session.send('loadOptions', sf.id, {'query': query});
  }

};
