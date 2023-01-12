/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {App, EnumObject, ResponsiveHandler, scout} from '../index';

export type ResponsiveState = EnumObject<typeof ResponsiveManager.ResponsiveState>;

let instance: ResponsiveManager;

export class ResponsiveManager {
  active: boolean;
  globalState: ResponsiveState;
  protected _responsiveHandlers: ResponsiveHandler[];

  constructor() {
    this.active = true;
    this.globalState = null;
    this._responsiveHandlers = [];
  }

  static ResponsiveState = {
    NORMAL: 'normal',
    CONDENSED: 'condensed',
    COMPACT: 'compact'
  } as const;

  init() {
    // NOP
  }

  destroy() {
    this._responsiveHandlers.forEach(handler => {
      handler.destroy();
    });
  }

  /**
   * Sets the responsive manager to active or inactive globally. Default is active.
   */
  setActive(active: boolean) {
    this.active = active;
  }

  /**
   * Set a global responsive state. This state will always be set. Resizing will no longer result in a different responsive state.
   */
  setGlobalState(globalState: ResponsiveState) {
    this.globalState = globalState;
  }

  /**
   * Checks if the form is smaller than the preferred width of the form. If this is reached, the fields will
   * be transformed to ensure better readability.
   */
  handleResponsive(target: { responsiveHandler?: ResponsiveHandler }, width: number): boolean {
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

  reset(target: { responsiveHandler?: ResponsiveHandler }, force?: boolean) {
    if (!this.active) {
      return;
    }

    if ((!target.responsiveHandler || !target.responsiveHandler.active()) && !force) {
      return;
    }

    target.responsiveHandler.transform(ResponsiveManager.ResponsiveState.NORMAL, force);
  }

  registerHandler(target: { responsiveHandler?: ResponsiveHandler }, handler: ResponsiveHandler) {
    if (target.responsiveHandler) {
      target.responsiveHandler.destroy();
    }
    target.responsiveHandler = handler;
  }

  unregisterHandler(target: { responsiveHandler?: ResponsiveHandler }) {
    if (target.responsiveHandler) {
      target.responsiveHandler.destroy();
      target.responsiveHandler = null;
    }
  }

  static get(): ResponsiveManager {
    return instance;
  }
}

App.addListener('prepare', () => {
  if (instance) {
    // if it was created before the app itself, use it instead of creating a new one
    return;
  }
  instance = scout.create(ResponsiveManager);
});
