/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Form, GroupBox, PopupWindow, scout} from '../../src/index';
import $ from 'jquery';

describe('PopupWindow', () => {
  let session: SandboxSession, $sandbox: JQuery, myForm: Form, myWindow: Window,
    myErrorHandler = () => {
      // nop
    };

  class SpecPopupWindow extends PopupWindow {

    injectStyleSheetPromises: Promise<JQuery>[] = [];

    protected override _onResize() {
      // Don't execute during spec
    }

    protected override async _injectStyleSheet($linkTag: JQuery<HTMLLinkElement>): Promise<JQuery> {
      const promise = super._injectStyleSheet($linkTag);
      this.injectStyleSheetPromises.push(promise);
      return promise;
    }
  }

  beforeEach(async () => {
    setFixtures(sandbox());
    session = sandboxSession();
    $sandbox = $('#sandbox');

    myForm = scout.create(Form, {
      parent: session.desktop,
      modelClass: 'Foo',
      rootGroupBox: {
        objectType: GroupBox
      }
    });

    // window mock
    myWindow = window.open('_res/popup-window.html', '_blank');
    myWindow.opener = {
      location: {href: null},
      onerror: myErrorHandler
    };
    const deferred = $.Deferred();
    $(myWindow).one('pageshow', () => deferred.resolve());
    await deferred.promise();
  });

  afterEach(() => {
    myWindow.close();
  });

  it('Constructor sets cross references and window-name', () => {
    let popupWindow = new SpecPopupWindow(myWindow, myForm);

    expect(myWindow[PopupWindow.PROP_POPUP_WINDOW]).toBe(popupWindow);
    expect(myWindow.name).toBe('Scout popup-window Foo');
    expect(myForm.popupWindow).toBe(popupWindow);
  });

  describe('_onReady', () => {

    it('initializes PopupWindow', async () => {
      let popupWindow = new SpecPopupWindow(myWindow, myForm),
        called = false;

      popupWindow.one('init', () => {
        called = true;
      });
      expect(popupWindow.initialized).toBe(false);
      await popupWindow._onReady();
      expect(called).toBe(true);
      expect(popupWindow.initialized).toBe(true);
      expect(myWindow.onerror).toBe(myErrorHandler);
    });

    it('injects relevant styleSheets', async () => {
      const popupWindow = new SpecPopupWindow(myWindow, myForm);

      const $red = await $.injectStyleSheet('_res/red.css', {document});
      const $green = await $.injectStyleSheet('_res/green.css', {document, data: {relevantForPopupWindow: true}});

      let windowStyleSheetHrefs = [...document.styleSheets].map(styleSheet => styleSheet.href);
      let popupWindowStyleSheetHrefs = [...myWindow.document.styleSheets].map(styleSheet => styleSheet.href);

      expect(windowStyleSheetHrefs).toContain(`${document.location.origin}/_res/red.css`);
      expect(windowStyleSheetHrefs).toContain(`${document.location.origin}/_res/green.css`);
      expect(popupWindowStyleSheetHrefs).not.toContain(`${document.location.origin}/_res/red.css`);
      expect(popupWindowStyleSheetHrefs).not.toContain(`${document.location.origin}/_res/green.css`);

      await popupWindow._onReady();

      windowStyleSheetHrefs = [...document.styleSheets].map(styleSheet => styleSheet.href);
      popupWindowStyleSheetHrefs = [...myWindow.document.styleSheets].map(styleSheet => styleSheet.href);

      expect(windowStyleSheetHrefs).toContain(`${document.location.origin}/_res/red.css`);
      expect(windowStyleSheetHrefs).toContain(`${document.location.origin}/_res/green.css`);
      expect(popupWindowStyleSheetHrefs).not.toContain(`${document.location.origin}/_res/red.css`);
      expect(popupWindowStyleSheetHrefs).toContain(`${document.location.origin}/_res/green.css`);

      const $blue = await $.injectStyleSheet('_res/blue.css', {document});
      const $yellow = await $.injectStyleSheet('_res/yellow.css', {document, data: {relevantForPopupWindow: true}});

      await Promise.all(popupWindow.injectStyleSheetPromises);

      windowStyleSheetHrefs = [...document.styleSheets].map(styleSheet => styleSheet.href);
      popupWindowStyleSheetHrefs = [...myWindow.document.styleSheets].map(styleSheet => styleSheet.href);

      expect(windowStyleSheetHrefs).toContain(`${document.location.origin}/_res/red.css`);
      expect(windowStyleSheetHrefs).toContain(`${document.location.origin}/_res/green.css`);
      expect(windowStyleSheetHrefs).toContain(`${document.location.origin}/_res/blue.css`);
      expect(windowStyleSheetHrefs).toContain(`${document.location.origin}/_res/yellow.css`);
      expect(popupWindowStyleSheetHrefs).not.toContain(`${document.location.origin}/_res/red.css`);
      expect(popupWindowStyleSheetHrefs).toContain(`${document.location.origin}/_res/green.css`);
      expect(popupWindowStyleSheetHrefs).not.toContain(`${document.location.origin}/_res/blue.css`);
      expect(popupWindowStyleSheetHrefs).toContain(`${document.location.origin}/_res/yellow.css`);

      $yellow.remove();
      $blue.remove();
      $green.remove();
      $red.remove();
    });
  });
});
