/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Button, ButtonAdapterMenu} from '../../src/index';

describe('ButtonAdapterMenu', () => {

  let session: SandboxSession, $sandbox: JQuery, button: Button, adapterMenu: ButtonAdapterMenu;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    $sandbox = $('#sandbox');
    button = new Button();
    button.init({id: '123', parent: session.desktop});
    adapterMenu = new ButtonAdapterMenu();
    adapterMenu.init({id: '234', button: button, parent: session.desktop});
  });

  describe('maps defaultButton setting', () => {

    describe('from not set to null', () => {

      it('to defaultMenu = true', () => {
        expect(adapterMenu.defaultMenu).toBe(null);
      });

    });

    describe('from true', () => {
      beforeEach(() => {
        button.setProperty('defaultButton', false); // set other value first to trigger actual property change with following line
        button.setProperty('defaultButton', true);
      });

      it('to defaultMenu = true', () => {
        expect(adapterMenu.defaultMenu).toBe(true);
      });

    });

    describe('from false (w/o other previous values) to null', () => {
      beforeEach(() => {
        button.setProperty('defaultButton', false);
      });

      it('to defaultMenu = null', () => {
        expect(adapterMenu.defaultMenu).toBe(null);
      });

    });

    describe('from false (with other previous values) to false', () => {
      beforeEach(() => {
        button.setProperty('defaultButton', true); // set other value first to trigger actual property change with following line
        button.setProperty('defaultButton', false);
      });

      it('to defaultMenu = false', () => {
        expect(adapterMenu.defaultMenu).toBe(false);
      });

    });

  });

  describe('initialization / destroy', () => {

    it('should set/delete adaptedBy property on original button instance', () => {
      // init
      expect(button.adaptedBy).toBe(adapterMenu);
      // destroy
      adapterMenu.destroy();
      expect(button.adaptedBy).toBe(undefined);
    });

  });

  describe('focusable element', () => {

    it('button should delegate to adapter menu', () => {
      expect(button.getFocusableElement()).toBe(null);
      expect(adapterMenu.getFocusableElement()).toBe(null);
      adapterMenu.render($sandbox);
      let adapterMenuContainer = adapterMenu.$container[0];
      expect(button.getFocusableElement()).toBe(adapterMenuContainer);
      expect(adapterMenu.getFocusableElement()).toBe(adapterMenuContainer);
    });

  });

});
