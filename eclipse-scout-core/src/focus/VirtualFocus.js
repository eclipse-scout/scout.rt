/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */

import * as $ from 'jquery';
import {graphics} from '../index';

// TODO CGU probably issue with popup window, each window needs it s own VirtualFocus.
// TODO CGU think about naming, e.g. Focus, CurrentFocus, ActiveElement, FocusQueue, DeferredFocus...
// TODO CGU focusManager.requestFocus better return promise, remove return value completely or is it ok that it could return deferred focus?
// TODO CGU same as for focusUtils.activeElement, is it confusing that it could return deferred focus?
// TODO CGU suggestion: $.activeElement() should still return the really active element never deferred, maybe need to ajust some calls
// TODO CGU cleanup existing workarounds (postValidate calls, scrollTop hack in Form.js, FocusContext.prepared, etc.)
let instance;
export default class VirtualFocus {

  constructor() {
    this.activeElement = null;
    this.focusScheduled = false;
    // If focus is changed otherwise (e.g. by click or by calling element.focus()), update activeElement
    window.document.addEventListener('focus', function(event) {
      this.activeElement = event.target;
    }.bind(this), true);
  }

  focus(element) {
    this.activeElement = element;
    if (this.focusScheduled) {
      return;
    }
    var widget = scout.widget(element);
    if (widget && widget.htmlComp && !widget.htmlComp.layouted) {
      this.focusScheduled = true;
      $.log.isDebugEnabled() && $.log.debug('Focusing scheduled for ' + graphics.debugOutput(element));
      // TODO CGU It could happen that component is not layouted yet but post validate never executed, e.g. if only validateLayout is called. Imho validateLayout must never be called, only validateLayoutTree. Nevertheless, add safety?
      // TODO CGU can it be that layouted will never be true for a component?
      // TODO CGU listen for element remove? on remove, focus should be changed automatically.
      // TODO CGU listen for widget destroy and reset focusScheduled? But post validate will eventually be executed, sufficient?
      widget.session.layoutValidator.schedulePostValidateFunction(function() {
        this.focusScheduled = false;
        // Focus could have changed in the meantime
        // -> don't focus the originally requested element but the most recently requested one
        // TODO CGU what happens if element is not in dom anymore?
        this.focus(this.activeElement);
      }.bind(this));
      return;
    }
    // TODO CGU check if already focused necessary?
    element.focus();
    $.log.isDebugEnabled() && $.log.debug('Focus set to ' + graphics.debugOutput(element));
  }

  static get() {
    if (!instance) {
      instance = new VirtualFocus();
    }
    return instance;
  }
}
