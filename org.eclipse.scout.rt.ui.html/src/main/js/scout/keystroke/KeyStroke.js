/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
scout.KeyStroke = function() {
  this.field = null; // optional model field

  this.which = []; // keys which this keystroke is bound to. Typically, this is a single key, but may be multiple keys if handling the same action (e.g. ENTER and SPACE on a button).
  this.ctrl = false;
  this.alt = false;
  this.shift = false;
  this.preventDefault = true;
  this.stopPropagation = false;
  this.stopImmediatePropagation = false;
  this.keyStrokeMode = scout.KeyStroke.Mode.DOWN;
  this.repeatable = false; // whether or not the handle method is called multiple times while a key is pressed
  this._handleExecuted = false; // internal flag to remember whether or not the handle method has been executed (reset on keyup)
  this.keyStrokeFirePolicy = scout.Action.KeyStrokeFirePolicy.ACCESSIBLE_ONLY;
  this.enabledByFilter = true;

  // Hints to control rendering of the key(s).
  this.renderingHints = {
    render: function() {
      if (this.field && this.field.rendered !== undefined) {
        return this.field.rendered; // only render key if associated field is visible.
      } else {
        return true; // by default, keystrokes are rendered
      }
    }.bind(this),
    gap: 4,
    offset: 4,
    hAlign: scout.HAlign.LEFT,
    text: null,
    $drawingArea: function($drawingArea, event) {
      return $drawingArea;
    }
  };

  /**
   * Indicates whether to invoke 'acceptInput' on a currently focused value field prior handling the keystroke.
   */
  this.invokeAcceptInputOnActiveValueField = false;

  /**
   * Indicates whether to prevent the invoke of 'acceptInput' on a currently focused value field prior handling the keystroke,
   * either triggered by previous property or by KeyStrokeContext
   */
  this.preventInvokeAcceptInputOnActiveValueField = false;
};

/**
 * Parses the given keystroke name into the key parts like 'ctrl', 'shift', 'alt' and 'which'.
 */
scout.KeyStroke.prototype.parseAndSetKeyStroke = function(keyStrokeName) {
  this.alt = this.ctrl = this.shift = false;
  this.which = [];
  if (keyStrokeName) {
    $.extend(this, scout.KeyStroke.parseKeyStroke(keyStrokeName));
  }
};

/**
 * Returns true if this event is handled by this keystroke, and if so sets the propagation flags accordingly.
 */
scout.KeyStroke.prototype.accept = function(event) {
  if (!this._isEnabled()) {
    return false;
  }

  // Check whether this event is accepted for execution.
  if (!this._accept(event)) {
    return false;
  }

  // Apply propagation flags to the event.
  this._applyPropagationFlags(event);
  //only accept on correct event type -> keyup or keydown. But propagation flags should be set to prevent execution of upper keyStrokes.
  return event.type === this.keyStrokeMode;
};

/**
 * Method invoked to handle the given keystroke event, and is only called if the event was accepted by 'KeyStroke.accept(event)'.
 */
scout.KeyStroke.prototype.handle = function(event) {
  throw new Error('keystroke event not handled: ' + event);
};

scout.KeyStroke.prototype.invokeHandle = function(event) {
  // if key stroke is repeatable, handle is called each time the key event occurs
  // which means it is executed multiple times while a key is pressed.
  if (this.repeatable) {
    this.handle(event);
    return;
  }

  // if key stroke is not repeatable it should only call execute once until
  // we receive a key up event for that key
  if (!this._handleExecuted) {
    this.handle(event);

    if (event.type === scout.KeyStroke.Mode.DOWN) {
      this._handleExecuted = true;

      // Reset handleExecuted on the next key up event
      // (use capturing phase to execute even if event.stopPropagation has been called)
      var $target = $(event.target);
      var $window = $target.window();
      var keyStroke = this;
      var keyUpHandler = {
        handleEvent: function(event) {
          keyStroke._handleExecuted = false;
          $window[0].removeEventListener('keyup', this, true);
        }
      };
      $window[0].addEventListener('keyup', keyUpHandler, true);
    }
  }
};

/**
 * Method invoked in the context of accepting a keystroke, and returns true if the keystroke is accessible to the user.
 */
scout.KeyStroke.prototype._isEnabled = function() {
  // Hint: do not check for which.length because there are keystrokes without a which, e.g. RangeKeyStroke.js

  if (this.field) {
    // Check visibility
    if (this.field.visible !== undefined && !this.field.visible) {
      return false;
    }
    // Check enabled state
    if (this.field.enabled !== undefined && !this.field.enabled) {
      return false;
    }
  }
  return true;
};

/**
 * Method invoked in the context of accepting a keystroke, and returns true if the event matches this keystroke.
 */
scout.KeyStroke.prototype._accept = function(event) {
  return scout.KeyStroke.acceptEvent(this, event);
};

/**
 * Method invoked in the context of accepting a keystroke, and sets the propagation flags accordingly.
 */
scout.KeyStroke.prototype._applyPropagationFlags = function(event) {
  if (this.stopPropagation) {
    event.stopPropagation();
  }
  if (this.stopImmediatePropagation) {
    event.stopImmediatePropagation();
  }
  if (this.preventDefault) {
    event.preventDefault();
  }
};

/**
 * Returns the key(s) associated with this keystroke. Typically, this is a single key, but may be multiple if this keystroke is associated with multiple keys, e.g. ENTER and SPACE on a button.
 */
scout.KeyStroke.prototype.keys = function() {
  return this.which.map(function(which) {
    return new scout.Key(this, which);
  }, this);
};

/**
 * Renders the visual representation of this keystroke, with the 'which' as given by the event.
 *
 * @return $drawingArea on which the key was finally rendered.
 */
scout.KeyStroke.prototype.renderKeyBox = function($drawingArea, event) {
  $drawingArea = this.renderingHints.$drawingArea($drawingArea, event);
  if (!$drawingArea || !$drawingArea.length) {
    return null;
  }

  var $keyBox = this._renderKeyBox($drawingArea, event.which);
  this._postRenderKeyBox($drawingArea, $keyBox);
  return $drawingArea;
};

scout.KeyStroke.prototype._renderKeyBox = function($parent, keyCode) {
  var $existingKeyBoxes = $('.key-box', $parent);
  var text = this.renderingHints.text || scout.keys.codesToKeys[keyCode];
  var align = this.renderingHints.hAlign === scout.HAlign.RIGHT ? 'right' : 'left';
  var offset = this.renderingHints.offset;
  $existingKeyBoxes = $existingKeyBoxes.filter(function() {
    if (align === 'right') {
      return $(this).hasClass('right');
    }
    return !$(this).hasClass('right');
  });
  if ($existingKeyBoxes.length > 0) {
    var $boxLastAdded = $existingKeyBoxes.first();
    if (this.renderingHints.hAlign === scout.HAlign.RIGHT) {
      offset = $parent.outerWidth() - $boxLastAdded.position().left + this.renderingHints.gap;
    } else {
      offset = $boxLastAdded.position().left + this.renderingHints.gap + $boxLastAdded.outerWidth();
    }
  }
  if (this.shift) {
    text = 'Shift ' + text;
  }
  if (this.alt) {
    text = 'Alt ' + text;
  }
  if (this.ctrl) {
    text = 'Ctrl ' + text;
  }
  var position = $parent.css('position');
  if (position === 'absolute' || position === 'relative' || (position === 'static' && $existingKeyBoxes.length > 0)) {
    return prependKeyBox.call(this, offset);
  }
  var pos = $parent.position();
  if (pos) {
    return prependKeyBox.call(this, pos.left + offset);
  }
  $.log.warn('(keys#drawSingleKeyBoxItem) pos is undefined. $parent=' + $parent);

  function prependKeyBox(alignValue) {
    return $parent.prependDiv('key-box', text)
      .css(align, alignValue + 'px')
      .toggleClass('disabled', !this.enabledByFilter)
      .addClass(align);
  }
};

/**
 * Method invoked after this keystroke was rendered, and is typically overwritten to reposition the visual representation.
 */
scout.KeyStroke.prototype._postRenderKeyBox = function($drawingArea) {};

/**
 * Removes the visual representation of this keystroke.
 */
scout.KeyStroke.prototype.removeKeyBox = function($drawingArea) {
  if ($drawingArea) {
    $('.key-box', $drawingArea).remove();
    $('.key-box-additional', $drawingArea).remove();
  }
};

scout.KeyStroke.Mode = {
  UP: 'keyup',
  DOWN: 'keydown'
};

// --- Static helpers --- //

/**
 * Parses the given keystroke name into the key parts like 'ctrl', 'shift', 'alt' and 'which'.
 *
 * @returns a plain object with properties ctrl, shift, alt and which - may be used as input
 *     for Key.js and KeyStroke.js
 * @see org.eclipse.scout.rt.client.ui.action.keystroke.KeyStrokeNormalizer
 */
scout.KeyStroke.parseKeyStroke = function(keyStrokeName) {
  if (!keyStrokeName) {
    return null;
  }

  var keyStrokeObj = {
    alt: false,
    ctrl: false,
    shift: false,
    which: []
  };

  keyStrokeName.split('-').forEach(function(part) {
    if (part === 'alternate' || part === 'alt') {
      keyStrokeObj.alt = true;
    } else if (part === 'control' || part === 'ctrl') {
      keyStrokeObj.ctrl = true;
    } else if (part === 'shift') {
      keyStrokeObj.shift = true;
    } else {
      var key = scout.keys[part.toUpperCase()];
      keyStrokeObj.which = key && [key];
    }
  });

  return keyStrokeObj;
};

scout.KeyStroke.acceptEvent = function(keyStroke, event) {
  if (!keyStroke) {
    return false;
  }
  //event.ctrlKey||event.metaKey  --> some keystrokes with ctrl modifier are captured and suppressed by osx use in this cases command key
  return scout.KeyStroke._acceptModifier(keyStroke.ctrl, (event.ctrlKey || event.metaKey)) &&
    scout.KeyStroke._acceptModifier(keyStroke.alt, event.altKey) &&
    scout.KeyStroke._acceptModifier(keyStroke.shift, event.shiftKey) &&
    scout.isOneOf(event.which, keyStroke.which);
};

scout.KeyStroke._acceptModifier = function(modifier, eventModifier) {
  return modifier === undefined || modifier === eventModifier;
};
