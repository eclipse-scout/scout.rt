/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {App, arrays, scout, Session, UuidPoolEventMap, UuidPoolModel, Widget} from '../index';

export class UuidPool extends Widget implements UuidPoolModel {
  declare model: UuidPoolModel;
  declare eventMap: UuidPoolEventMap;

  static readonly ZERO_UUID: string = '00000000-0000-0000-0000-000000000000';

  uuids: string[];
  size: number;
  refillThreshold: number;
  failOnStarvation: boolean;
  refillInProgress: boolean;

  constructor() {
    super();

    this.uuids = [];
    this.size = 100;
    this.refillThreshold = 25;
    this.failOnStarvation = false;

    this.refillInProgress = false;
  }

  take(): string {
    if (!this.uuids.length) {
      if (this.failOnStarvation) {
        throw new Error('UUID pool exhausted');
      }
      // Fallback: instead of failing, use an algorithm to generate a random V4 UUID. Due to
      // browser limitations, there is a higher risk of collisions.
      return this.generateUuid();
    }

    let result = this.uuids.shift();
    if (this.uuids.length <= this.refillThreshold) {
      this.refill();
    }
    return result;
  }

  refill() {
    let count = this.size - this.uuids.length;
    if (count > 0) {
      this.fill(count);
    }
  }

  fill(count: number) {
    this.trigger('refill', {
      count: count
    });
  }

  generateUuid(): string {
    // Source: https://stackoverflow.com/a/8809472/7188380 (Public Domain/MIT)
    let d = Date.now() + (performance ? performance.now() : 0); // use high-precision timer if available
    return 'zzzzzzzz-zzzz-4zzz-yzzz-zzzzzzzzzzzz'.replace(/[zy]/g, c => {
      let r = (d + Math.random() * 16) % 16 | 0;
      d = Math.floor(d / 16);
      return (c === 'z' ? r : (r & 0x3 | 0x8)).toString(16);
    });
  }

  // ----- Static functions -----

  /**
   * Creates a new UuidPool widget, i.e. without model adapter attached.
   * @param parent mandatory parent widget
   */
  protected static _createInstance(parent: Widget): UuidPool {
    return scout.create(UuidPool, {
      parent: parent
    });
  }

  /**
   * Installs a new UuidPool instanceof as an add-on to any desktop of this Scout app. It can then be
   * accessed conveniently by all other widgets by using the helper function <code>UuidPool.get()</code>.
   * <p>
   * Note: In the case of a RemoteApp, the add-on is provided by the UI server (see UuidPoolAddOnDesktopExtension.java).
   */
  static installDesktopAddOn() {
    App.addListener('desktopReady', event => {
      let desktop = event.desktop;
      if (desktop.addOns.some(addOn => addOn instanceof UuidPool)) {
        // pool might already have been created as part of the desktop received from the server
        return;
      }
      desktop.addOns.push(UuidPool._createInstance(desktop));
    });
  }

  /**
   * Returns an instanceof of UuidPool. If a desktop add-on of this type is available, this instance is returned.
   * Otherwise, a new instance is created.
   *
   * @param session Session object providing a desktop. If this is omitted, the first session of the app is used.
   * If the app does not have any active sessions (e.g. during unit testing), this argument is mandatory.
   */
  static get(session?: Session): UuidPool {
    session = session || App.get().sessions[0];
    scout.assertParameter('session', session);
    let uuidPool = arrays.find(session.desktop.addOns, addOn => addOn instanceof UuidPool) as UuidPool;
    return uuidPool || UuidPool._createInstance(session.desktop);
  }

  /**
   * Convenience function for <code>UuidPool.get(session).take()</code>.
   *
   * @returns new UUID
   */
  static take(session?: Session): string {
    return UuidPool.get(session).take();
  }
}

UuidPool.installDesktopAddOn();
