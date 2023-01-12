/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Action, EnumObject, HAlign, Key, keys, KeyStrokeFirePolicy, KeyStrokeModel, scout, ScoutKeyboardEvent, Widget} from '../index';
import $ from 'jquery';

export class KeyStroke implements KeyStrokeModel {
  declare model: KeyStrokeModel;

  field?: Widget;

  /** keys which this keystroke is bound to. Typically, this is a single key, but may be multiple keys if handling the same action (e.g. ENTER and SPACE on a button). */
  which: number[];

  ctrl: boolean;
  inheritAccessibility: boolean;
  alt: boolean;
  shift: boolean;
  preventDefault: boolean;
  stopPropagation: boolean;
  stopImmediatePropagation: boolean;
  keyStrokeMode: KeyStrokeMode;

  /** whether the handle method is called multiple times while a key is pressed */
  repeatable: boolean;

  keyStrokeFirePolicy: KeyStrokeFirePolicy;
  enabledByFilter: boolean;

  /** Hints to control rendering of the key(s). */
  renderingHints: KeyStrokeRenderingHints;

  /**
   * Indicates whether to invoke 'acceptInput' on a currently focused value field prior handling the keystroke.
   */
  invokeAcceptInputOnActiveValueField: boolean;

  /**
   * Indicates whether to prevent to invoke 'acceptInput' on a currently focused value field prior handling the keystroke,
   * either triggered by previous property or by KeyStrokeContext
   */
  preventInvokeAcceptInputOnActiveValueField: boolean;

  /** internal flag to remember whether the handle method has been executed (reset on keyup) */
  protected _handleExecuted: boolean;

  constructor() {
    this.field = null;
    this.which = [];
    this.ctrl = false;
    this.inheritAccessibility = true;
    this.alt = false;
    this.shift = false;
    this.preventDefault = true;
    this.stopPropagation = false;
    this.stopImmediatePropagation = false;
    this.keyStrokeMode = KeyStroke.Mode.DOWN;
    this.repeatable = false;
    this._handleExecuted = false;
    this.keyStrokeFirePolicy = Action.KeyStrokeFirePolicy.ACCESSIBLE_ONLY;
    this.enabledByFilter = true;
    this.renderingHints = {
      render: () => {
        if (this.field && this.field.rendered !== undefined) {
          return this.field.rendered; // only render key if associated field is visible.
        }
        return true; // by default, keystrokes are rendered
      },
      gap: 4,
      offset: 4,
      hAlign: HAlign.LEFT,
      text: null,
      $drawingArea: ($drawingArea, event) => $drawingArea
    };
    this.invokeAcceptInputOnActiveValueField = false;
    this.preventInvokeAcceptInputOnActiveValueField = false;
  }

  /**
   * Parses the given keystroke name into the key parts like 'ctrl', 'shift', 'alt' and 'which'.
   */
  parseAndSetKeyStroke(keyStrokeName: string) {
    this.alt = false;
    this.ctrl = false;
    this.shift = false;
    this.which = [];
    if (keyStrokeName) {
      $.extend(this, KeyStroke.parseKeyStroke(keyStrokeName));
    }
  }

  /**
   * Returns true if this event is handled by this keystroke, and if so sets the propagation flags accordingly.
   */
  accept(event: ScoutKeyboardEvent): boolean {
    if (!this._isEnabled()) {
      return false;
    }

    // Check whether this event is accepted for execution.
    if (!this._accept(event)) {
      return false;
    }

    // Apply propagation flags to the event.
    this._applyPropagationFlags(event);
    // only accept on correct event type -> keyup or keydown. But propagation flags should be set to prevent execution of upper keyStrokes.
    return event.type === this.keyStrokeMode;
  }

  /**
   * Method invoked to handle the given keystroke event, and is only called if the event was accepted by 'KeyStroke.accept(event)'.
   */
  handle(event: JQuery.KeyboardEventBase) {
    throw new Error('keystroke event not handled: ' + event);
  }

  invokeHandle(event: JQuery.KeyboardEventBase) {
    // if keystroke is repeatable, handle is called each time the key event occurs
    // which means it is executed multiple times while a key is pressed.
    if (this.repeatable) {
      this.handle(event);
      return;
    }

    // if keystroke is not repeatable it should only call execute once until
    // we receive a key up event for that key
    if (!this._handleExecuted) {
      this.handle(event);

      if (event.type === KeyStroke.Mode.DOWN) {
        this._handleExecuted = true;

        // Reset handleExecuted on the next key up event
        // (use capturing phase to execute even if event.stopPropagation has been called)
        let $target = $(event.target);
        let $window = $target.window();
        let keyStroke = this;
        let keyUpHandler = {
          handleEvent: function(event) {
            keyStroke._handleExecuted = false;
            $window[0].removeEventListener('keyup', this, true);
          }
        };
        $window[0].addEventListener('keyup', keyUpHandler, true);
      }
    }
  }

  /**
   * Method invoked in the context of accepting a keystroke, and returns true if the keystroke is accessible to the user.
   */
  protected _isEnabled(): boolean {
    // Hint: do not check for which.length because there are keystrokes without a which, e.g. RangeKeyStroke.js

    if (!this.field) {
      return true;
    }
    if (this.field.isRemovalPending()) {
      // Prevent possible exceptions or unexpected behavior if a keystroke is executed while a widget is being removed.
      return false;
    }
    if (!this.field.visible) {
      return false;
    }
    if (!this.inheritAccessibility) {
      return true;
    }
    // Check enabled state only if inheritAccessibility is true
    return this.field.enabledComputed;
  }

  /**
   * Method invoked in the context of accepting a keystroke, and returns true if the event matches this keystroke.
   */
  protected _accept(event: ScoutKeyboardEvent): boolean {
    return KeyStroke.acceptEvent(this, event);
  }

  /**
   * Method invoked in the context of accepting a keystroke, and sets the propagation flags accordingly.
   */
  protected _applyPropagationFlags(event: ScoutKeyboardEvent) {
    if (this.stopPropagation) {
      event.stopPropagation();
    }
    if (this.stopImmediatePropagation) {
      event.stopImmediatePropagation();
    }
    if (this.preventDefault) {
      event.preventDefault();
    }
  }

  /**
   * Returns the key(s) associated with this keystroke. Typically, this is a single key, but may be multiple if this keystroke is associated with multiple keys, e.g. ENTER and SPACE on a button.
   */
  keys(): Key[] {
    return this.which.map(which => new Key(this, which));
  }

  /**
   * Renders the visual representation of this keystroke, with the 'which' as given by the event.
   *
   * @returns $drawingArea on which the key was finally rendered.
   */
  renderKeyBox($drawingArea: JQuery, event: ScoutKeyboardEvent): JQuery {
    $drawingArea = this.renderingHints.$drawingArea($drawingArea, event);
    if (!$drawingArea || !$drawingArea.length) {
      return null;
    }

    let $keyBox = this._renderKeyBox($drawingArea, event.which);
    this._postRenderKeyBox($drawingArea, $keyBox);
    return $drawingArea;
  }

  protected _renderKeyBox($parent: JQuery, keyCode: number): JQuery {
    let $existingKeyBoxes = $('.key-box', $parent);
    let text = this.renderingHints.text || keys.codesToKeys[keys.fromBrowser(keyCode)];
    let align = this.renderingHints.hAlign === HAlign.RIGHT ? 'right' : 'left';
    let offset = this.renderingHints.offset;
    $existingKeyBoxes = $existingKeyBoxes.filter(function() {
      if (align === 'right') {
        return $(this).hasClass('right');
      }
      return !$(this).hasClass('right');
    });
    if ($existingKeyBoxes.length > 0) {
      let $boxLastAdded = $existingKeyBoxes.first();
      if (this.renderingHints.hAlign === HAlign.RIGHT) {
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
    let position = $parent.css('position');
    if (position === 'absolute' || position === 'relative' || (position === 'static' && $existingKeyBoxes.length > 0)) {
      return prependKeyBox.call(this, offset);
    }
    let pos = $parent.position();
    if (pos) {
      return prependKeyBox.call(this, pos.left + offset);
    }
    $.log.warn('(keys#drawSingleKeyBoxItem) pos is undefined. $parent=' + $parent);

    function prependKeyBox(alignValue: number): JQuery {
      return $parent.prependDiv('key-box', text)
        .css(align, alignValue + 'px')
        .toggleClass('disabled', !this.enabledByFilter)
        .addClass(align);
    }
  }

  /**
   * Method invoked after this keystroke was rendered, and is typically overwritten to reposition the visual representation.
   */
  protected _postRenderKeyBox($drawingArea: JQuery, $keyBox?: JQuery) {
    // nop
  }

  /**
   * Removes the visual representation of this keystroke.
   */
  removeKeyBox($drawingArea: JQuery) {
    if ($drawingArea) {
      $('.key-box', $drawingArea).remove();
      $('.key-box-additional', $drawingArea).remove();
    }
  }

  static Mode = {
    UP: 'keyup',
    DOWN: 'keydown'
  } as const;

  // --- Static helpers --- //

  /**
   * Parses the given keystroke name into the key parts like 'ctrl', 'shift', 'alt' and 'which'.
   *
   * @see "org.eclipse.scout.rt.client.ui.action.keystroke.KeyStrokeNormalizer"
   */
  static parseKeyStroke(keyStrokeName: string): KeyStrokeModel {
    if (!keyStrokeName) {
      return null;
    }

    let keyStrokeObj: KeyStrokeModel = {
      alt: false,
      ctrl: false,
      shift: false,
      which: []
    };

    keyStrokeName.split('-').forEach(part => {
      if (part === 'alternate' || part === 'alt') {
        keyStrokeObj.alt = true;
      } else if (part === 'control' || part === 'ctrl') {
        keyStrokeObj.ctrl = true;
      } else if (part === 'shift') {
        keyStrokeObj.shift = true;
      } else {
        let key = keys[part.toUpperCase()];
        keyStrokeObj.which = key && [key];
      }
    });

    return keyStrokeObj;
  }

  static acceptEvent(keyStroke: KeyStrokeModel, event: ScoutKeyboardEvent): boolean {
    if (!keyStroke) {
      return false;
    }
    // event.ctrlKey||event.metaKey  --> some keystrokes with ctrl modifier are captured and suppressed by osx use in this cases command key
    return KeyStroke.acceptModifiers(keyStroke, event) &&
      scout.isOneOf(event.which, keyStroke.which);
  }

  static acceptModifiers(keyStroke: KeyStrokeModel, event: ScoutKeyboardEvent): boolean {
    return KeyStroke._acceptModifier(keyStroke.ctrl, (event.ctrlKey || event.metaKey)) &&
      KeyStroke._acceptModifier(keyStroke.alt, event.altKey) &&
      KeyStroke._acceptModifier(keyStroke.shift, event.shiftKey);
  }

  protected static _acceptModifier(modifier: boolean, eventModifier: boolean): boolean {
    return modifier === undefined || eventModifier === undefined || modifier === eventModifier;
  }
}

export type KeyStrokeMode = EnumObject<typeof KeyStroke.Mode>;

export interface KeyStrokeRenderingHints {
  render: boolean | (() => boolean);
  gap?: number;
  offset?: number;
  hAlign?: HAlign;
  text?: string;
  $drawingArea: KeystrokeRenderAreaProvider;
}

export type KeystrokeRenderAreaProvider = ($drawingArea: JQuery, event: ScoutKeyboardEvent) => JQuery;
