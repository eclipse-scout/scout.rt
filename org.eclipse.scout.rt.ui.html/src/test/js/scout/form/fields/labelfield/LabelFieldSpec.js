/* global FormSpecHelper */
describe("LabelField", function() {
  var session;
  var helper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new FormSpecHelper(session);
  });

  function createField(model) {
    var field = new scout.LabelField();
    field.init(model);
    return field;
  }

  function createModel() {
    return helper.createFieldModel();
  }

  describe("HtmlEnabled", function() {
    var field;

    beforeEach(function() {
      field = createField(createModel());
    });

    it("if false, encodes html in display text", function() {
      field.htmlEnabled = false;
      field.displayText = '<b>Hello</b>';
      field.render(session.$entryPoint);
      expect(field.$field.html()).toBe('&lt;b&gt;Hello&lt;/b&gt;');
    });

    it("if true, does not encode html in display text", function() {
      field.htmlEnabled = true;
      field.displayText = '<b>Hello</b>';
      field.render(session.$entryPoint);
      expect(field.$field.html()).toBe('<b>Hello</b>');
    });

    it("if false, replaces \n with br tag and encodes other text", function() {
      field.htmlEnabled = false;
      field.displayText = '<b>Hello</b>\nGoodbye';
      field.render(session.$entryPoint);
      expect(field.$field.html()).toBe('&lt;b&gt;Hello&lt;/b&gt;<br>Goodbye');
    });

    it("if true, does not replace \n with br tag and does not encode other text", function() {
      field.htmlEnabled = true;
      field.displayText = '<b>Hello</b>\nGoodbye';
      field.render(session.$entryPoint);
      expect(field.$field.html()).toBe('<b>Hello</b>\nGoodbye');
    });
  });

});
