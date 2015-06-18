/* global FormSpecHelper */
describe("StringField", function() {
  var session;
  var helper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new FormSpecHelper(session);
  });

  function createField(model) {
    var field = new scout.StringField();
    field.init(model, session);
    return field;
  }

  function createModel() {
    return helper.createFieldModel();
  }

  describe("Check if field is switched to password field if inputMasked is true", function() {
    var field;

    beforeEach(function() {
      field = createField(createModel());
    });

      it("set input masked", function() {
        field.inputMasked = true;
        field.render(session.$entryPoint);
        expect(field.$field.attr('type')).toBe('password');
      });

      it("set input not masked", function() {
        field.inputMasked = false;
        field.render(session.$entryPoint);
        expect(field.$field.attr('type')).toBe('text');
      });


  });

  describe("insertText", function() {
    var field;

    beforeEach(function() {
      field = createField(createModel());
    });

      it("expects empty field at the beginning", function() {
        field.render(session.$entryPoint);
        expect(field.$field[0].value).toBe('');
      });

      it("inserts text into an empty field", function() {
        field.render(session.$entryPoint);
        var message = {
            events: [createPropertyChangeEvent(field, {insertText: 'Test1'})]
          };
        session._processSuccessResponse(message);
        expect(field.$field[0].value).toBe('Test1');
      });

      it("appends text to the previous value (if no text is selected)", function() {
        field.render(session.$entryPoint);
        var message = {
            events: [createPropertyChangeEvent(field, {insertText: 'Test1'})]
          };
        session._processSuccessResponse(message);
        message = {
            events: [createPropertyChangeEvent(field, {insertText: 'ABC2'})]
          };
        session._processSuccessResponse(message);
        expect(field.$field[0].value).toBe('Test1ABC2');
      });

      it("replaces selection #1 (if part of the text is selected, selection does not start at the beginning)", function() {
        field.render(session.$entryPoint);
        var message = {
            events: [createPropertyChangeEvent(field, {insertText: 'Test1'})]
          };
        session._processSuccessResponse(message);
        field.$field[0].selectionStart = 2;
        field.$field[0].selectionEnd = 4;
        message = {
            events: [createPropertyChangeEvent(field, {insertText: 'sten2'})]
          };
        session._processSuccessResponse(message);
        expect(field.$field[0].value).toBe('Testen21');
      });

      it("replaces selection #2 (if part of the text is selected, start at the beginning)", function() {
        field.render(session.$entryPoint);
        var message = {
            events: [createPropertyChangeEvent(field, {insertText: 'Test1'})]
          };
        session._processSuccessResponse(message);
        field.$field[0].selectionStart = 0;
        field.$field[0].selectionEnd = 4;
        message = {
            events: [createPropertyChangeEvent(field, {insertText: 'ABC2'})]
          };
        session._processSuccessResponse(message);
        expect(field.$field[0].value).toBe('ABC21');
      });

      it("replaces selection #3 (if whole content is selected)", function() {
        field.render(session.$entryPoint);
        var message = {
            events: [createPropertyChangeEvent(field, {insertText: 'Test1'})]
          };
        session._processSuccessResponse(message);
        field.$field[0].select();
        message = {
            events: [createPropertyChangeEvent(field, {insertText: 'ABC2'})]
          };
        session._processSuccessResponse(message);
        expect(field.$field[0].value).toBe('ABC2');
      });

  });

});
