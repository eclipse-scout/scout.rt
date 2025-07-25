/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {FocusManagerSpecHelper, FormSpecHelper, JQueryTesting} from '../../src/testing/index';
import {FocusManager, FocusRule, GlassPane, scout} from '../../src/index';

describe('FocusManager', () => {
  let session: SandboxSession, formHelper: FormSpecHelper, focusHelper: FocusManagerSpecHelper, focusManager: FocusManager;

  beforeEach(() => {
    setFixtures(sandbox());
    jasmine.Ajax.install();
    session = sandboxSession({
      desktop: {
        headerVisible: true,
        benchVisible: true
      }
    });
    focusManager = session.focusManager;
    formHelper = new FormSpecHelper(session);
    focusHelper = new FocusManagerSpecHelper();
    jasmine.clock().install();
    uninstallUnloadHandlers(session);
  });

  afterEach(() => {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });

  function createDivWithTwoInputs() {
    let $container = session.$entryPoint.makeDiv();
    $container.appendElement('<input type="text" value="input1" class="input1">');
    $container.appendElement('<input type="text" value="input2" class="input2">');
    return $container;
  }

  function expectMouseDownToPreventDefault(prevented: boolean) {
    session.$entryPoint.one('mousedown', e => expect(e.isDefaultPrevented()).toBe(prevented));
  }

  describe('validateFocus', () => {

    it('When nothing else is focusable, focus must be on the Desktop (=sandbox)', () => {
      focusManager.validateFocus();
      let sandbox = $('#sandbox')[0];
      expect(document.activeElement).toBe(sandbox);
    });

    describe('with forms:', () => {

      let form;
      beforeEach(() => {
        form = formHelper.createFormWithFields(session.desktop, false, 4);
        form.render();
      });

      afterEach(() => {
        form.destroy();
        form = null;
      });

      /**
       * Because form is not a dialog, it does not install its own focus-context
       * but uses the focus-context of the Desktop (=sandbox) instead.
       */
      it('Focus-context must install listeners on its $container', () => {
        expect(focusHelper.handlersRegistered(session.$entryPoint)).toBe(true);
      });

      it('Focus must be on the 1st form-field when form is rendered', () => {
        let $firstField = form.rootGroupBox.fields[0].$field;
        expect($firstField).toBeFocused();
      });

      it('FocusContext must remember the last focused element', () => {
        let $secondField = form.rootGroupBox.fields[1].$field;
        $secondField.focus();
        expect($secondField).toBeFocused();

        expect(focusManager._findActiveContext().lastValidFocusedElement).toBe($secondField[0]);
      });

      it('A new FocusContext must be created when a form is opened as dialog', () => {
        let $secondField = form.rootGroupBox.fields[1].$field;
        $secondField.focus(); // must be remembered by focus-context

        let sandboxContext = focusManager._findActiveContext();
        expect(sandboxContext.$container).toBe(session.$entryPoint);

        let dialog = formHelper.createFormWithFields(session.desktop, true, 2);
        dialog.render();

        // @ts-expect-error
        expect(focusManager._focusContexts.length).toBe(2);

        let dialogContext = focusManager._findActiveContext();
        expect(dialogContext.$container).toBe(dialog.$container);

        // focus-context must install handlers on form $container
        expect(focusHelper.handlersRegistered(dialog.$container)).toBe(true);

        // must remember last focused field of first focus-context
        expect(sandboxContext.lastValidFocusedElement).toBe($secondField[0]);
      });

      it('Must focus another valid field if the focused field is removed', () => {
        let $firstField = form.rootGroupBox.fields[0].$field,
          $secondField = form.rootGroupBox.fields[1].$field;

        expect($firstField).toBeFocused();
        $firstField.remove();
        expect($secondField).toBeFocused();
      });

      it('Must focus another valid field if the focused field is hidden', () => {
        let $firstField = form.rootGroupBox.fields[0].$field,
          $secondField = form.rootGroupBox.fields[1].$field;

        expect($firstField).toBeFocused();
        $firstField.setVisible(false);
        expect($secondField).toBeFocused();
      });

    });

  });

  describe('activateFocusContext', () => {

    it('activates the context of the given $container and restores its focus', () => {
      let $container1 = createDivWithTwoInputs().appendTo(session.$entryPoint);
      let $container2 = createDivWithTwoInputs().appendTo(session.$entryPoint);
      focusManager.installFocusContext($container1);
      expect(document.activeElement).toBe($container1.children('.input1')[0]);

      focusManager.installFocusContext($container2);
      focusManager.requestFocus($container2.children('.input2'));
      expect(document.activeElement).toBe($container2.children('.input2')[0]);

      focusManager.activateFocusContext($container1);
      expect(document.activeElement).toBe($container1.children('.input1')[0]);

      focusManager.activateFocusContext($container2);
      expect(document.activeElement).toBe($container2.children('.input2')[0]);

      focusManager.uninstallFocusContext($container1);
      focusManager.uninstallFocusContext($container2);
    });

  });

  describe('requestFocus', () => {
    it('focuses the given element', () => {
      let $container1 = createDivWithTwoInputs().appendTo(session.$entryPoint);
      focusManager.installFocusContext($container1);

      focusManager.requestFocus($container1.children('.input1'));
      expect(document.activeElement).toBe($container1.children('.input1')[0]);

      focusManager.uninstallFocusContext($container1);
    });

    it('activates the context of the element if the element to focus is not in the active context', () => {
      let $container1 = createDivWithTwoInputs().appendTo(session.$entryPoint);
      let $container2 = createDivWithTwoInputs().appendTo(session.$entryPoint);
      focusManager.installFocusContext($container1);
      focusManager.installFocusContext($container2);

      focusManager.requestFocus($container1.children('.input1'));
      expect(document.activeElement).toBe($container1.children('.input1')[0]);
      expect(focusManager._findActiveContext().$container[0]).toBe($container1[0]);

      focusManager.requestFocus($container2.children('.input2'));
      expect(document.activeElement).toBe($container2.children('.input2')[0]);
      expect(focusManager._findActiveContext().$container[0]).toBe($container2[0]);

      focusManager.uninstallFocusContext($container1);
      focusManager.uninstallFocusContext($container2);
    });

    it('does nothing if the element cannot be focused', () => {
      let $container1 = createDivWithTwoInputs().appendTo(session.$entryPoint);
      let $container2 = createDivWithTwoInputs().appendTo(session.$entryPoint);
      focusManager.installFocusContext($container1);
      focusManager.installFocusContext($container2);

      focusManager.requestFocus($container1.children('.input1'));
      expect(document.activeElement).toBe($container1.children('.input1')[0]);
      expect(focusManager._findActiveContext().$container[0]).toBe($container1[0]);

      // Container2 is covered by a glass pane -> requesting focus on a covered element should do nothing
      let glassPane = scout.create(GlassPane, {
        parent: session.desktop
      });
      glassPane.render($container2);
      focusManager.requestFocus($container2.children('.input2'));
      expect(document.activeElement).toBe($container1.children('.input1')[0]);
      expect(focusManager._findActiveContext().$container[0]).toBe($container1[0]);

      focusManager.uninstallFocusContext($container1);
      focusManager.uninstallFocusContext($container2);
    });

    it('activates the correct context', () => {
      let $input0 = session.$entryPoint.appendElement('<input type="text" class="input0">');
      let $container1 = createDivWithTwoInputs().appendTo(session.$entryPoint);
      let $container2 = createDivWithTwoInputs().appendTo(session.$entryPoint);
      focusManager.installFocusContext(session.$entryPoint);
      focusManager.installFocusContext($container1);
      focusManager.installFocusContext($container2);

      focusManager.requestFocus($input0);
      expect(document.activeElement).toBe($input0[0]);
      expect(focusManager._findActiveContext().$container[0]).toBe(session.$entryPoint[0]);

      focusManager.requestFocus($container1.children('.input1'));
      expect(document.activeElement).toBe($container1.children('.input1')[0]);
      expect(focusManager._findActiveContext().$container[0]).toBe($container1[0]);

      focusManager.requestFocus($container2.children('.input2'));
      expect(document.activeElement).toBe($container2.children('.input2')[0]);
      expect(focusManager._findActiveContext().$container[0]).toBe($container2[0]);

      focusManager.uninstallFocusContext(session.$entryPoint);
      focusManager.uninstallFocusContext($container1);
      focusManager.uninstallFocusContext($container2);
    });
  });

  describe('registerGlassPaneTarget', () => {
    it('removes the focus if the active element will be covered by the glass pane', () => {
      let $container1 = createDivWithTwoInputs().appendTo(session.$entryPoint);
      focusManager.installFocusContext($container1);

      focusManager.requestFocus($container1.children('.input1'));
      expect(document.activeElement).toBe($container1.children('.input1')[0]);
      expect(focusManager._findActiveContext().$container[0]).toBe($container1[0]);

      // GlassPane will cover the active element -> blur it and focus desktop
      let glassPane = scout.create(GlassPane, {
        parent: session.desktop
      });
      glassPane.render($container1);
      expect(document.activeElement).toBe(session.$entryPoint[0]);

      // Destroy glass pane -> restore focus
      glassPane.destroy();
      expect(document.activeElement).toBe($container1.children('.input1')[0]);

      focusManager.uninstallFocusContext($container1);
    });
  });

  describe('evaluateFocusRule', () => {
    it('should find first focusable element', () => {
      let $container = createDivWithTwoInputs().appendTo(session.$entryPoint);
      focusManager.installFocusContext($container);
      let input1 = $container.children('.input1')[0];
      let input2 = $container.children('.input2')[0];

      expect(focusManager.evaluateFocusRule($container, FocusRule.NONE)).toBe(null);
      expect(focusManager.evaluateFocusRule($container, FocusRule.AUTO)).toBe(input1);
      expect(focusManager.evaluateFocusRule($container, FocusRule.PREPARE)).toBe(input1);
      expect(focusManager.evaluateFocusRule($container, null)).toBe(input1);
      // @ts-expect-error
      expect(focusManager.evaluateFocusRule($container, 'invalid-rule')).toBe('invalid-rule');
      expect(focusManager.evaluateFocusRule($container, input2)).toBe(input2);
    });
  });

  describe('focusNextTabbable', () => {

    it('selects text content on focus', () => {
      let $container = session.$entryPoint.appendDiv();
      let input1 = $container.appendElement('<input type="text" value="lorem">')[0] as HTMLInputElement;
      let input2 = $container.appendElement('<input type="text" value="ipsum">')[0] as HTMLInputElement;
      let input3 = $container.appendElement('<textarea>dolor</textarea>')[0] as HTMLInputElement;
      let input4 = $container.appendElement('<div contenteditable="true" tabindex="0">magna</div>')[0] as HTMLElement;

      focusManager.installFocusContext($container);
      focusManager.requestFocus(input1);
      expect(document.activeElement).toBe(input1);
      // Text not selected initially
      expect(input1.selectionStart).toBe(0);
      expect(input1.selectionEnd).toBe(0);

      focusManager.focusNextTabbable($container.activeElement());

      expect(document.activeElement).toBe(input2);
      // Text selected (because of selectText=true in #focusNextTabbable)
      expect(input2.selectionStart).toBe(0);
      expect(input2.selectionEnd).toBe(5);

      focusManager.focusNextTabbable($container.activeElement());

      expect(document.activeElement).toBe(input3);
      // No text selected (element is not a simple <input> field)
      expect(input3.selectionStart).toBe(0);
      expect(input3.selectionEnd).toBe(0);

      focusManager.focusNextTabbable($container.activeElement());

      expect(document.activeElement).toBe(input4);
      // No text selected (not a HTMLInputField, therefore no 'selectionStart' property)
      expect(document.getSelection().getRangeAt(0).startOffset).toBe(0);
      expect(document.getSelection().getRangeAt(0).endOffset).toBe(0);

      focusManager.focusNextTabbable($container.activeElement());

      expect(document.activeElement).toBe(input1);
      // Text selected (because of selectText=true in #focusNextTabbable)
      expect(input1.selectionStart).toBe(0);
      expect(input1.selectionEnd).toBe(5);
    });

    it('does not focus elements covered by a glasspane', () => {
      let $container = session.$entryPoint.appendDiv();
      let input1 = $container.appendElement('<input type="text">')[0];
      let input2 = $container.appendElement('<input type="text">')[0];
      let input3 = $container.appendElement('<input type="text">')[0];
      focusManager.installFocusContext($container);

      // Cover middle element
      let glassPane = scout.create(GlassPane, {parent: session.desktop});
      glassPane.render($(input2));
      focusManager.requestFocus(input1);
      expect(document.activeElement).toBe(input1);
      focusManager.focusNextTabbable($container.activeElement());
      expect(document.activeElement).toBe(input3);
      focusManager.focusNextTabbable($container.activeElement(), false);
      expect(document.activeElement).toBe(input1);

      // Cover last element
      glassPane.remove();
      glassPane.render($(input3));
      focusManager.focusNextTabbable($container.activeElement());
      expect(document.activeElement).toBe(input2);
      focusManager.focusNextTabbable($container.activeElement());
      expect(document.activeElement).toBe(input1);
      focusManager.focusNextTabbable($container.activeElement(), false);
      expect(document.activeElement).toBe(input2);

      // Cover first element
      glassPane.remove();
      glassPane.render($(input1));
      focusManager.focusNextTabbable($container.activeElement(), false);
      expect(document.activeElement).toBe(input3);
      focusManager.focusNextTabbable($container.activeElement());
      expect(document.activeElement).toBe(input2);

      // Cover every element
      let glassPane2 = scout.create(GlassPane, {parent: session.desktop});
      glassPane2.render($(input2));
      let glassPane3 = scout.create(GlassPane, {parent: session.desktop});
      glassPane3.render($(input3));
      focusManager.focusNextTabbable(input1);
      expect(document.activeElement).toBe(session.$entryPoint[0]);
    });
  });

  describe('findFirstFocusableElement', () => {
    it('ignores disabled form fields', async () => {
      jasmine.clock().uninstall();
      let form = formHelper.createFormWithFields(session.desktop, false, 2);
      form.rootGroupBox.fields[0].setEnabled(false);
      await form.open();

      expect(focusManager.findFirstFocusableElement(form.$container)).toBe(form.rootGroupBox.fields[1].$field[0]);
    });

    it('ignores readonly and disabled inputs', async () => {
      let $inputContainer = session.$entryPoint.appendDiv();
      $inputContainer.appendElement('<input type="text">').setEnabled(false);
      $inputContainer.appendElement('<input type="text" readonly="readonly" tabindex="-1">'); // should be the same as the result of setEnabled(false)
      $inputContainer.appendElement('<input type="text" disabled="disabled">');
      let $input = $inputContainer.appendElement('<input type="text">');
      expect(focusManager.findFirstFocusableElement($inputContainer)).toBe($input[0]);

      let $buttonContainer = session.$entryPoint.appendDiv();
      $buttonContainer.appendElement('<button>button</button>>').setEnabled(false);
      $buttonContainer.appendElement('<button disabled="disabled">button</button>>'); // should be the same as the result of setEnabled(false)
      let $button = $buttonContainer.appendElement('<button>button</button>>');
      expect(focusManager.findFirstFocusableElement($buttonContainer)).toBe($button[0]);
    });

    it('does not ignore readonly inputs without tabindex or tabindex => 0', () => {
      // A readonly input normally is tabbable. In Scout, when an input is disabled, we don't want it to be tabbable so we set tabindex to -1.
      // But maybe there are use cases where a readonly input should be tabbable. To do so, just don't use jquery-scout#setEnabled(boolean) and set the readonly attribute explicitly.
      // In that case, it should get the initial focus because the user can tab to it.
      let $container = session.$entryPoint.appendDiv();
      $container.appendElement('<input type="text" readonly="readonly" tabindex="-1">');
      let $input = $container.appendElement('<input type="text" readonly="readonly">');
      expect(focusManager.findFirstFocusableElement($container)).toBe($input[0]);

      let $container2 = session.$entryPoint.appendDiv();
      $container2.appendElement('<input type="text" readonly="readonly" tabindex="-1">');
      let $input2 = $container2.appendElement('<input type="text" readonly="readonly" tabindex="0">');
      expect(focusManager.findFirstFocusableElement($container2)).toBe($input2[0]);
    });
  });

  describe('_acceptFocusChangeOnMouseDown', () => {
    it('accepts the focus if a readonly input was clicked', () => {
      let $container = session.$entryPoint.appendDiv();
      let $input1 = $container.appendElement('<input type="text">').setEnabled(false);
      let $input2 = $container.appendElement('<input type="text" readonly="readonly" tabindex="-1">'); // should be the same as the result of setEnabled(false)
      let $input3 = $container.appendElement('<input type="text" readonly="readonly">');
      // Browsers nowadays completely ignore mouse events on disabled inputs, so it is not necessary to handle this case

      expectMouseDownToPreventDefault(false);
      JQueryTesting.triggerMouseDown($input1);

      expectMouseDownToPreventDefault(false);
      JQueryTesting.triggerMouseDown($input2);

      expectMouseDownToPreventDefault(false);
      JQueryTesting.triggerMouseDown($input3);
    });
  });
});
