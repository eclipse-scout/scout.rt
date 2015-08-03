/* global FormSpecHelper, FocusManagerSpecHelper */
jasmine.DEFAULT_TIMEOUT_INTERVAL = 10000;
describe('scout.Focusmanager', function() {
  var session, formHelper, focusHelper, form, focusContextsForSession;

  beforeEach(function() {
    setFixtures(sandbox());
    formHelper = new FormSpecHelper(session);
    focusHelper = new FocusManagerSpecHelper();
    jasmine.Ajax.install();
    session = sandboxSession();
    session.init();
    jasmine.clock().install();
    focusContextsForSession = scout.focusManager._sessionFocusContexts[session.uiSessionId].focusContexts;
    uninstallUnloadHandlers(session);
  });

  afterEach(function() {
    session = null;
    focusContextsForSession = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });

  describe('_isSelectableText', function() {

    it('must return true for disabled text-fields', function() {
      var $textField = $('<input>')
        .attr('type', 'text')
        .attr('disabled', 'disabled');
      expect(scout.focusManager._isSelectableText($textField)).toBe(true);
    });

  });

  describe('validateFocus', function() {

    it('When nothing else is focusable, focus must be on the Desktop (=sandbox)', function() {
      scout.focusManager.validateFocus(session.uiSessionId);
      var sandbox = $('#sandbox')[0];
      expect(document.activeElement).toBe(sandbox);
    });

    describe('with forms:', function() {

      var form;
      beforeEach(function() {
        form = formHelper.createFormXFields(4, session, false);
        form.render(session.$entryPoint);
      });

      afterEach(function() {
        form.remove();
        form = null;
      });

      /**
       * Because form is not a dialog, it does not install its own focus-context
       * but uses the focus-context of the Desktop (=sandbox) instead.
       */
      it('Focus-context must install listeners on its $container', function() {
        expect(focusHelper.handlersRegistered(session.$entryPoint)).toBe(true);
      });

      it('Focus must be on the 1st form-field when form is rendered', function() {
        var $firstField = form.rootGroupBox.fields[0].$field;
        expect($firstField).toBeFocused();
      });

      it('FocusContext must remember the last focused element', function() {
        var $secondField = form.rootGroupBox.fields[1].$field;
        $secondField.focus();
        expect($secondField).toBeFocused();

        var activeContext = scout.focusManager._activeContext(session.uiSessionId);
        expect(activeContext._lastFocusedElement).toBe($secondField[0]);
      });

      it('A new FocusContext must be created when a form is opened as dialog', function() {
        var $secondField = form.rootGroupBox.fields[1].$field;
        $secondField.focus(); // must be remembered by focus-context

        var sandboxContext = scout.focusManager._activeContext(session.uiSessionId);
        expect(sandboxContext._$container).toBe(session.$entryPoint);

        var dialog = formHelper.createFormXFields(2, session, true);
        dialog.render(session.$entryPoint);

        var allContexts = scout.focusManager._contextsBySession(session.uiSessionId);
        expect(allContexts.length).toBe(2);

        var dialogContext = scout.focusManager._activeContext(session.uiSessionId);
        expect(dialogContext._$container).toBe(dialog.$container);

        // focus-context must install handlers on form $container
        expect(focusHelper.handlersRegistered(dialog.$container)).toBe(true);

        // must remember last focused field of first focus-context
        expect(sandboxContext._lastFocusedElement).toBe($secondField[0]);
      });

      it('Must focus another valid field if the focused field is removed', function() {
        var $firstField = form.rootGroupBox.fields[0].$field,
          $secondField = form.rootGroupBox.fields[1].$field;

        expect($firstField).toBeFocused();
        $firstField.remove();
        expect($secondField).toBeFocused();
      });


      it('Must focus another valid field if the focused field is hidden', function() {
        var $firstField = form.rootGroupBox.fields[0].$field,
        $secondField = form.rootGroupBox.fields[1].$field;

        expect($firstField).toBeFocused();
        $firstField.setVisible(false);
        expect($secondField).toBeFocused();
      });

    });

  });

});
