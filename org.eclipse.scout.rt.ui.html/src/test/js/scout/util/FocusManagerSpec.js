/* global FormSpecHelper, FocusManagerSpecHelper */
jasmine.DEFAULT_TIMEOUT_INTERVAL = 10000;
describe("scout.Focusmanager", function() {
  var session, formHelper, focusHelper, form, focusContextsForSession;

  beforeEach(function() {
    $.log.trace('before test');
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

  describe("install focuscontext and let focusmanager set the first element", function() {
    it("No form added so focus is set on sandbox", function() {
      $.log.trace('first test');
      var sandbox = $('#sandbox')[0];
      expect(document.activeElement).toBe(sandbox);
    });
    describe("create form", function() {
      var form;
      beforeEach(function() {
        form = formHelper.createFormXFields(4, session, false);
        $.log.trace('before form');
        form.render(session.$entryPoint);
        $.log.trace('before validate focus');
        scout.focusManager.validateFocus(session.uiSessionId);
      });
      afterEach(function() {
        form.remove();
        form = null;
      });
      it("focus first input field over focusManager", function() {
        $.log.trace('second test');
          expect(document.activeElement).toBeTruthy();
          expect(document.activeElement).toBe(form.rootGroupBox.fields[0].$field[0]);
          focusHelper.checkListenersRegistered();
          focusHelper.checkListeners(sandbox, false);

      });

      describe('select second field in first form', function() {
        beforeEach(function() {
          //open dialog with glass pane
          $.log.trace('before test select second');
          form.rootGroupBox.fields[1].$field.focus();
        });

        it("check selection", function() {
          $.log.trace('third test');
          //focus second field
          expect(form.rootGroupBox.fields[1].$field).toBeFocused();
          //old context should have second field as last selected
          expect(focusContextsForSession[0]._$focusedElement[0]).toBe(form.rootGroupBox.fields[1].$field[0]);
          focusHelper.checkListeners(form.rootGroupBox.fields[1].$field[0], true);
        });
        describe('Additional focus context(open dialog)', function() {
          var dialogform;
          beforeEach(function() {
            //open dialog with glass pane
              dialogform = formHelper.createFormXFields(4, session, true);
              dialogform.render(session.$entryPoint);
              jasmine.clock().tick(100);
          });

          it("New focusContext is installed for dialog and first element on dialog is selected", function() {
            $.log.trace('fifth test');
            expect(dialogform.rootGroupBox).toBeTruthy();
            expect(dialogform.rootGroupBox.fields[0].$field).toBeFocused();
            expect(document.activeElement).toBe(dialogform.rootGroupBox.fields[0].$field[0]);
            focusHelper.checkListenersRegistered();

            //two installed focus contexts.
            expect(focusContextsForSession.length).toBe(2);
            //old context should have second field on form as selected(no change to state before installing focuscontext from dialogForm)
            expect(focusContextsForSession[0]._$focusedElement[0]).toBe(form.rootGroupBox.fields[1].$field[0]);
              focusHelper.checkListeners(form.rootGroupBox.fields[1].$field[0], false);


          });
          describe('dialog focus functions', function() {
            it("remove focused field->focusContext has to set the focus on a valid field", function() {
              $.log.trace('sixth test');
              var $focusedField = focusContextsForSession[focusContextsForSession.length - 1]._$focusedElement;
              expect($focusedField).toBeTruthy();
              $focusedField.remove();
                var $newFocusedField = focusContextsForSession[focusContextsForSession.length - 1]._$focusedElement,
                  isInDialog = false;
                for (var i = 0; i < dialogform.rootGroupBox.fields.length; i++) {
                  if (dialogform.rootGroupBox.fields[i].$field[0] === $newFocusedField[0]) {
                    isInDialog = true;
                  }
                }
                expect(isInDialog).toBeTruthy();

            });

            it("hide focused field->focusContext has to set the focus on a valid field", function() {
              $.log.trace('seventh test');
              var $focusedField = focusContextsForSession[focusContextsForSession.length - 1]._$focusedElement;
              expect($focusedField).toBeTruthy();
              $focusedField.hide();
                var $newFocusedField = focusContextsForSession[focusContextsForSession.length - 1]._$focusedElement,
                  isInDialog = false;
                for (var i = 0; i < dialogform.rootGroupBox.fields.length; i++) {
                  if (dialogform.rootGroupBox.fields[i].$field[0] === $newFocusedField[0]) {
                    isInDialog = true;
                  }
                }
                expect(isInDialog).toBeTruthy();
            });
          });
        });

      });
    });
  });
});
