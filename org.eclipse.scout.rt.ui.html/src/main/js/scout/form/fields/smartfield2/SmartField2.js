// FIXME [awe] 7.0 - lookup row als property zwischen server und client hin und her schicken?
// um das problem mit den lazy styles zu lösen

scout.SmartField2 = function() {
  scout.SmartField2.parent.call(this);

  this.popup = null;
  this.lookupCall = null;
  this.codeType = null;
  this._pendingLookup = null;
  this.lookupRow = null;
  this.browseMaxRowCount = scout.SmartField2.DEFAULT_BROWSE_MAX_COUNT;
  this.activeFilterEnabled = false;
  this.activeFilter = null;
  this.activeFilterLabels = [];
  this.columnDescriptors = null;
  this.variant = scout.SmartField2.Variant.DEFAULT; // scout.SmartField2.Variant.DROPDOWN;
};
scout.inherits(scout.SmartField2, scout.ValueField);

// FIXME [awe] 7.0 - SF2: überlegen ob wir das mit flags, mit subklassen oder mit strategies lösen wollen
// zuerst mal flag ansatz ausprobieren und je nach code die eine oder andere methode anwenden.
scout.SmartField2.Variant = {
  DEFAULT: 'smart',
  PROPOSAL: 'proposal',
  DROPDOWN: 'dropdown'
};

scout.SmartField2.DEBOUNCE_DELAY = 200;

scout.SmartField2.DEFAULT_BROWSE_MAX_COUNT = 100;

/**
 * @see IContentAssistField#getActiveFilterLabels() - should have the same order.
 */
scout.SmartField2.ACTIVE_FILTER_VALUES = ['UNDEFINED', 'FALSE', 'TRUE'];

/**
 * @override
 */
scout.SmartField2.prototype._init = function(model) {
  scout.SmartField2.parent.prototype._init.call(this, model);

  this.activeFilterLables = [
    this.session.text('ui.All'),
    this.session.text('ui.Inactive'),
    this.session.text('ui.Active')];

  this._setLookupCall(this.lookupCall);
  this._setCodeType(this.codeType);
};

/**
 * @override Widget.js
 */
scout.SmartField2.prototype._createKeyStrokeContext = function() {
  return new scout.InputFieldKeyStrokeContext();
};

scout.SmartField2.prototype._initKeyStrokeContext = function() {
  scout.SmartField2.parent.prototype._initKeyStrokeContext.call(this);

  this.keyStrokeContext.registerKeyStroke(new scout.SmartField2CancelKeyStroke(this));
  this.keyStrokeContext.registerKeyStroke(new scout.SmartField2ToggleKeyStroke(this));
};

scout.SmartField2.prototype._render = function($parent) {
  var cssClass = this.variant + '-field';
  this.addContainer($parent, cssClass, new scout.SmartFieldLayout(this));
  this.addLabel();

  var $field = scout.fields.makeInputOrDiv(this)
    .on('mousedown', this._onFieldMousedown.bind(this));
  if (!this.touch) {
    $field
      .blur(this._onFieldBlur.bind(this))
      .focus(this._onFieldFocus.bind(this))
      .keyup(this._onFieldKeyup.bind(this))
      .keydown(this._onFieldKeydown.bind(this));
  }
  this.addField($field);

  if (!this.embedded) {
    this.addMandatoryIndicator();
  }
  this.addIcon();
  this.addStatus();
};

/**
 * @override
 */
scout.SmartField2.prototype._renderDisplayText = function() {
  scout.fields.valOrText(this, this.$field, this.displayText);
};

/**
 * Accepts the selected lookup row and sets its id as value.
 * This function is called on blur, by a keystroke or programmatically at any time.
 *
 * @override
 */
scout.SmartField2.prototype.acceptInput = function(whileTyping) {
  var
    searchText = this._readDisplayText(),
    selectedLookupRow = this.popup ? this.popup.getSelectedLookupRow() : null;

  // abort pending lookups
  if (this._pendingLookup) {
    clearTimeout(this._pendingLookup);
  }

  // Do nothing when search text is equals to the text of the current lookup row
  if (this.lookupRow && this.lookupRow.text === searchText && !selectedLookupRow) {
    console.log('unchanged');
    this.closePopup();
    return;
  }

  // 1.) when search text is empty and no lookup-row is selected, simply set the value to null
  if (scout.strings.empty(searchText) && !selectedLookupRow) {
    console.log('empty');
    this.setLookupRow(null);
    this.closePopup();
    return;
  }

  // 2.) proposal chooser is open -> use the selected row as value
  if (selectedLookupRow) {
    console.log('lookupRow selected');
    this.setLookupRow(selectedLookupRow);
    this.closePopup();
    return;
  }

  // 3.) proposal chooser is not open -> try to accept the current display text
  // this causes a lookup which may fail and open a new proposal chooser (property
  // change for 'result'). Or in case the text is empty, just set the value to null
  console.log('getByText');
  this.showLookupInProgress();
  this.lookupCall.getByText(searchText).done(function(result) {

    this.hideLookupInProgress();
    var numLookupRows = result.lookupRows ? result.lookupRows.length : 0;

    // when there's exactly one result, we accept that lookup row
    if (numLookupRows === 1) {
      var lookupRow = result.lookupRows[0];
      if (this._isLookupRowActive(lookupRow)) {
        this.setLookupRow(lookupRow);
      } else {
        this.setErrorStatus(scout.Status.error({
          message: this.session.text('SmartFieldInactiveRow', searchText)
        }));
      }
      return;
    }

    // in any other case something went wrong
    if (numLookupRows === 0) {
      this.setErrorStatus(scout.Status.error({
        message: this.session.text('SmartFieldCannotComplete', searchText)
      }));
      return;
    }

    if (numLookupRows > 1) {
      this.setErrorStatus(scout.Status.error({
        message: this.session.text('SmartFieldNotUnique', searchText)
      }));
      this.openPopup2(result);
      return;
    }

    throw new Error('Unreachable code');
  }.bind(this));
};

/**
 * Validates the given lookup row is enabled and matches the current activeFilter settings.
 *
 * @returns {boolean}
 */
scout.SmartField2.prototype._isLookupRowActive = function(lookupRow) {
  if (!lookupRow.enabled) {
    return false;
  }
  if (!this.activeFilterEnabled) {
    return true;
  }
  if (this.activeFilter === 'TRUE') {
    return lookupRow.active;
  }
  if (this.activeFilter === 'FALSE') {
    return !lookupRow.active;
  }
  return true;
};

scout.SmartField2.prototype._renderEnabled = function() {
  scout.SmartField2.parent.prototype._renderEnabled.call(this);

  this.$field.setTabbable(this.enabledComputed);
};

scout.SmartField2.prototype._setLookupCall = function(lookupCall) {
  if (typeof lookupCall === 'string') {
    lookupCall = scout.create(lookupCall, {
      session: this.session
    });
  }
  this._setProperty('lookupCall', lookupCall);
};

scout.SmartField2.prototype._setCodeType = function(codeType) {
  this._setProperty('codeType', codeType);
  if (!codeType) {
    return;
  }
  var lookupCall = scout.create('CodeLookupCall', {
    session: this.session,
    codeType: codeType
  });
  this.setProperty('lookupCall', lookupCall);
};

scout.SmartField2.prototype._formatValue = function(value) {
  if (!value) {
    return '';
  }

  // we already have a lookup row - Note: in Scout Classic (remote case)
  // we always end here and don't need to perform a getByKey lookup.
  if (this.lookupRow) {
    return this.lookupRow.text;
  }

  // we must do a lookup first to get the display text
  return this.lookupCall.getByKey(value)
    .then(function(lookupRow) {
      return lookupRow ? lookupRow.text : '';
    });
};

/**
 * @param {string} [searchText] optional search text. If set lookupCall#getByText() is called, otherwise lookupCall#getAll()
 */
scout.SmartField2.prototype.openPopup = function() {
  // already open
  if (this.popup) {
    return;
  }

  this.showLookupInProgress();
  this.lookupCall.getAll().done(this.openPopup2.bind(this));
};

scout.SmartField2.prototype.openPopup2 = function(result) { // FIXME [awe] 7.0 - SF2: improve naming openPopup2
  this.hideLookupInProgress();
  this.$container.addClass('popup-open');
  // On touch devices the field does not get the focus.
  // But it should look focused when the popup is open.
  this.$field.addClass('focused');
  this.popup = this._createPopup();
  this.popup.setLookupResult(result);
  this.popup.open();
  this.popup.on('lookupRowSelected', this._onLookupRowSelected.bind(this));
  this.popup.on('activeFilterSelected', this._onActiveFilterSelected.bind(this));
  this.popup.on('remove', function() {
    this.popup = null;
    if (this.rendered) {
      this.$container.removeClass('popup-open');
      this.$field.removeClass('focused');
    }
  }.bind(this));
};

scout.SmartField2.prototype.closePopup = function() {
  if (this.popup) {
    this.popup.close();
  }
};

scout.SmartField2.prototype.togglePopup = function() {
  if (this.popup) {
    this.closePopup();
  } else {
    this.openPopup();
  }
};

scout.SmartField2.prototype._createPopup = function() {
  return scout.create('SmartField2Popup', {
    parent: this,
    $anchor: this.$field,
    boundToAnchor: true,
    closeOnAnchorMousedown: false,
    field: this
  });
};

scout.SmartField2.prototype.showLookupInProgress = function() {
  if (this.popup) {
    this.$field.removeClass('lookup-in-progress');
    this.popup.setStatusLookupInProgress();
  } else {
    this.$field.addClass('lookup-in-progress');
  }
};

scout.SmartField2.prototype.hideLookupInProgress = function() {
  this.$field.removeClass('lookup-in-progress');
  if (this.popup) {
    // this.popup.hideLookupInProgress();
  }
};

/**
 * Calls acceptInput if mouse down happens outside of the field or popup
 * @override
 */
scout.SmartField2.prototype.aboutToBlurByMouseDown = function(target) {
  var eventOnField = this.$field.isOrHas(target);
  var eventOnPopup = this.popup && this.popup.$container.isOrHas(target);
  if (!eventOnField && !eventOnPopup) {
    this.acceptInput(); // event outside this value field
  }
};

scout.SmartField2.prototype._onFieldMousedown = function(event) {
  if (!this.enabledComputed) {
    return;
  }
  this.togglePopup();
};

scout.SmartField2.prototype._onIconMousedown = function(event) {
  if (!this.enabledComputed) {
    return;
  }
  this.$field.focus();
  this.togglePopup();
};

scout.SmartField2.prototype._onFieldFocus = function(event) {
  // FIXME [awe] 7.0 - SF2: im original wird hier mit dem displayText was komisches gemacht. Ich hoffe das ist nicht mehr nötig
};

scout.SmartField2.prototype._onFieldKeyup = function(event) {
  // Escape
  if (event.which === scout.keys.ESCAPE) {
    event.stopPropagation();
    return;
  }

  // Enter
  if (event.which === scout.keys.ENTER) {
    event.stopPropagation();
    return;
  }

  // Pop-ups shouldn't open when one of the following keys is pressed
  var w = event.which;
  if (
    event.ctrlKey || event.altKey ||
    w === scout.keys.TAB ||
    w === scout.keys.SHIFT ||
    w === scout.keys.CTRL ||
    w === scout.keys.HOME ||
    w === scout.keys.END ||
    w === scout.keys.LEFT ||
    w === scout.keys.RIGHT ||
    this._isNavigationKey(event) ||
    this._isFunctionKey(event)) {
    return;
  }

  // The typed character is not available until the keyUp event happens
  // That's why we must deal with that event here (and not in keyDown)
  // We don't use _displayText() here because we always want the text the
  // user has typed.
  if (this.popup) {
    this._proposalTyped();
  } else {
    this.openPopup();
  }
};

/**
 * @override Widget.js
 */
scout.SmartField.prototype._createKeyStrokeContext = function() {
  return new scout.InputFieldKeyStrokeContext();
};

scout.SmartField2.prototype._onFieldKeydown = function(event) {
 if (this._isNavigationKey(event)) {
   if (this.popup) {
     this.popup.delegateKeyEvent(event);
   } else {
     this.openPopup();
   }
 }
};

scout.SmartField2.prototype._isNavigationKey = function(event) {
  var w = event.which;
  return w === scout.keys.PAGE_UP ||
    w === scout.keys.PAGE_DOWN ||
    w === scout.keys.UP ||
    w === scout.keys.DOWN ||
    w === scout.keys.HOME ||
    w === scout.keys.END;
};

scout.SmartField2.prototype._isFunctionKey = function(e) {
  return e.which >= scout.keys.F1 && e.which < scout.keys.F12;
};


scout.SmartField2.prototype._proposalTyped = function() {
  this._startNewLookupByText();
};

scout.SmartField2.prototype._onLookupRowSelected = function(event) {
  this.setLookupRow(event.lookupRow);
  this.closePopup();
};

// FIXME [awe] 7.0 - SF2: discuss usage of activeFilter. With current impl. we cannot
// use the activeFilter in the lookup call because it belongs to the widget state.
scout.SmartField2.prototype._onActiveFilterSelected = function(event) {
  this.setActiveFilter(event.activeFilter);
  this._startNewLookupByText();
};

scout.SmartField2.prototype.setActiveFilter = function(activeFilter) {
  this.setProperty('activeFilter', this.activeFilterEnabled ? activeFilter : null);
};

scout.SmartField2.prototype._startNewLookupByText = function() {
  var searchText = this._readDisplayText();
  $.log.trace('(SmartField2#_startNewLookupByText) searchText=' + searchText);

  // debounce lookup
  if (this._pendingLookup) {
    clearTimeout(this._pendingLookup);
  }

  this._pendingLookup = setTimeout(function() {
    $.log.debug('(SmartField2#_startNewLookupByText) searchText=' + searchText);
    this.showLookupInProgress();
    // this.lookupCall.setActiveFilter(this.activeFilter); // FIXME [awe] 7.0 - SF2: add on LookupCall
    this.lookupCall.getByText(searchText).done(function(result) {
      this.hideLookupInProgress();
      if (this.popup) {
        this.popup.setLookupResult(result);
      }
    }.bind(this));
  }.bind(this), scout.SmartField2.DEBOUNCE_DELAY);
};

/**
 * Returns true if the smart-field lookup returns a lot of rows. In that case
 * the proposal chooser must create a table with virtual scrolling, which means
 * only the rows visible in the UI are rendered in the DOM. By default we render
 * all rows, since this avoids problems with layout-invalidation with rows
 * that have a bitmap-image (PNG) which is loaded asynchronously.
 */
scout.SmartField2.prototype.virtual = function() {
  return this.browseMaxRowCount > scout.SmartField2.DEFAULT_BROWSE_MAX_COUNT;
};

scout.SmartField2.prototype.setLookupRow = function(lookupRow) {
  this.lookupRow = lookupRow;
  var value = lookupRow ? lookupRow.key : null;
  this.setErrorStatus(null);
  this.setValue(value);
};

scout.SmartField2.prototype._setValue = function(value) {
  // set the cached lookup row to null. Keep in mind that the lookup row is set async in a timeout
  // must of the time. Thus we must remove the reference to the old lookup row as early as possible
  if (value) {
    // when a value is set, we only keep the cached lookup row when the key of the lookup row is equals to the value
    if (this.lookupRow && this.lookupRow.key != value) {
      this.lookupRow = null;
    }
  } else {
    // when value is set to null, we must also reset the cached lookup row
    this.lookupRow = null;
  }
  scout.SmartField2.parent.prototype._setValue.call(this, value);
};
