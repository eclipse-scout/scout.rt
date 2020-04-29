/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {App, scout} from '../index';

let instance;
export default class ResponsiveManager {

  constructor() {
    this.active = true;
    this.globalState = null;

    this._responsiveHandlers = [];
  }

  static ResponsiveState = {
    NORMAL: 'normal',
    CONDENSED: 'condensed',
    COMPACT: 'compact'
  };

  init() {
  }

  destroy() {
    this._responsiveHandlers.forEach(handler => {
      handler.destroy();
    });
  }

  /**
   * Sets the responsive manager to active or inactive globally. Default is active.
   */
  setActive(active) {
    this.active = active;
  }

  /**
   * Set a global responsive state. This state will always be set. Resizing will no longer result in a different responsive state.
   *
   * @param {string} responsive state (ResponsiveManager.ResponsiveState)
   */
  setGlobalState(globalState) {
    this.globalState = globalState;
  }

  /**
   * Checks if the form is smaller than the preferred width of the form. If this is reached, the fields will
   * be transformed to ensure better readability.
   */
  handleResponsive(target, width) {
    if (!this.active) {
      return false;
    }

    if (!target.responsiveHandler || !target.responsiveHandler.active()) {
      return false;
    }

    let newState;
    let state = target.responsiveHandler.state;
    if (this.globalState) {
      newState = this.globalState;
    } else if (width < target.responsiveHandler.getCompactThreshold() && target.responsiveHandler.acceptState(ResponsiveManager.ResponsiveState.COMPACT)) {
      newState = ResponsiveManager.ResponsiveState.COMPACT;
    } else {
      if (state === ResponsiveManager.ResponsiveState.COMPACT) {
        target.responsiveHandler.transform(ResponsiveManager.ResponsiveState.CONDENSED);
      }
      if (width < target.responsiveHandler.getCondensedThreshold() && target.responsiveHandler.acceptState(ResponsiveManager.ResponsiveState.CONDENSED)) {
        newState = ResponsiveManager.ResponsiveState.CONDENSED;
      } else {
        newState = ResponsiveManager.ResponsiveState.NORMAL;
      }
    }

    return target.responsiveHandler.transform(newState);
  }

  reset(target, force) {
    if (!this.active) {
      return;
    }

    if ((!target.responsiveHandler || !target.responsiveHandler.active()) && !force) {
      return false;
    }

    target.responsiveHandler.transform(ResponsiveManager.ResponsiveState.NORMAL, force);
  }

  registerHandler(target, handler) {
    if (target.responsiveHandler) {
      target.responsiveHandler.destroy();
    }
    target.responsiveHandler = handler;
  }

  unregisterHandler(target) {
    if (target.responsiveHandler) {
      target.responsiveHandler.destroy();
      target.responsiveHandler = null;
    }
  }

  static get() {
    return instance;
  }
}

App.addListener('prepare', () => {
  if (instance) {
    // if it was created before the app itself, use it instead of creating a new one
    return;
  }
  instance = scout.create('ResponsiveManager');
  instance.init();
});
