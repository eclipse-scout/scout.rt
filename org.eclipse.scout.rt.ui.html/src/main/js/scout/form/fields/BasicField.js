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
 * Common base class for ValueFields having an HTML input field.
 */
scout.BasicField = function() {
  scout.BasicField.parent.call(this);
  this._onDisplayTextModifiedHandler = this._onDisplayTextModified.bind(this);
  this._keyUpListener;
  this.disabledWhenOffline = false;
};
scout.inherits(scout.BasicField, scout.ValueField);

scout.BasicField.prototype._renderProperties = function() {
  scout.BasicField.parent.prototype._renderProperties.call(this);
  this._renderUpdateDisplayTextOnModify();
};

/**
 * @override FormField.js
 */
scout.BasicField.prototype._renderEnabled = function() {
  scout.BasicField.parent.prototype._renderEnabled.call(this);
  this._renderDisabledOverlay();
};

scout.BasicField.prototype._renderUpdateDisplayTextOnModify = function() {
  if (this.updateDisplayTextOnModify) {
    this.$field.on('input', this._onDisplayTextModifiedHandler);
  } else {
    this.$field.off('input', this._onDisplayTextModifiedHandler);
  }
};

/**
 * Called when the property 'updateDisplayTextOnModified' is TRUE and the display text (field's input
 * value) has been modified by a user action, e.g. a key or paste event. If the property is FALSE, this
 * method is _never_ called.
 */
scout.BasicField.prototype._onDisplayTextModified = function() {
  this.acceptInput(true);
};

/**
 * @override ValueField.js
 */
scout.BasicField.prototype._checkDisplayTextChanged = function(displayText, whileTyping) {
  var displayTextChanged = scout.BasicField.parent.prototype._checkDisplayTextChanged.call(this, displayText, whileTyping);

  // OR if updateDisplayTextOnModify is true
  // 2. check is necessary to make sure the value and not only the display text gets written to the model (IBasicFieldUIFacade.parseAndSetValueFromUI vs setDisplayTextFromUI)
  if (displayTextChanged || (this.updateDisplayTextOnModify || this._displayTextChangedWhileTyping) && !whileTyping) {
    // In 'updateDisplayTextOnModify' mode, each change of text is sent to the server with whileTyping=true.
    // On field blur, the text is sent again with whileTyping=false. The following logic prevents sending
    // to many events to the server. When whileTyping is false, the text has only to be send to the server
    // when there have been any whileTyping=true events. When the field looses the focus without any
    // changes, no request should be sent.
    if (this.updateDisplayTextOnModify) {
      if (whileTyping) {
        // Remember that we sent some events to the server with "whileTyping=true".
        this._displayTextChangedWhileTyping = true;
      } else {
        if (!this._displayTextChangedWhileTyping) {
          // If there were no "whileTyping=true" events, don't send anything to the server.
          return false;
        }
        this._displayTextChangedWhileTyping = false; // Reset
      }
    }
    return true;
  }
  return false;
};

/**
 * Add or remove an overlay DIV for browsers that don't support copy from disabled text-fields.
 * The overlay provides a custom 'copy' menu which opens the ClipboardForm.
 */
scout.BasicField.prototype._renderDisabledOverlay = function() {
  if (scout.device.supportsCopyFromDisabledInputFields()) {
    return;
  }

  if (this.enabled) {
    this._removeDisabledOverlay();
  } else if (!this._$disabledOverlay) {
    this._$disabledOverlay = this.$container
      .appendDiv('disabled-overlay')
      .on('contextmenu', this._createCopyContextMenu.bind(this));
  }
};

scout.BasicField.prototype._removeDisabledOverlay = function() {
  if (this._$disabledOverlay) {
    this._$disabledOverlay.remove();
    this._$disabledOverlay = null;
  }
};

scout.BasicField.prototype._createCopyContextMenu = function(event) {
  if (!this.visible ||
    !scout.strings.hasText(this.displayText)) {
    return;
  }

  var field = this;
  var menu = scout.create('Menu', {
    parent: this,
    text: this.session.text('ui.Copy')
  });
  menu.remoteHandler = function(event) {
    if ('doAction' === event.type) {
      field._send('exportToClipboard');
    }
  };

  var popup = scout.create('ContextMenuPopup', {
    parent: this,
    menuItems: [menu],
    cloneMenuItems: false,
    location: {
      x: event.pageX,
      y: event.pageY
    },
    $anchor: this._$disabledOverlay
  });
  popup.open();
};

scout.BasicField.prototype._remove = function() {
  scout.BasicField.parent.prototype._remove.call(this);
  this._removeDisabledOverlay();
};
