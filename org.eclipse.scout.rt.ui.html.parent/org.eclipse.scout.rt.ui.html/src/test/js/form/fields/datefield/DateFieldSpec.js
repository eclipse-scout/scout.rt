/* global LocaleSpecHelper */
describe("DateField", function() {
  var session;
  var helper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = new scout.Session($('#sandbox'), '1.1');
    session.locale = new LocaleSpecHelper().createLocale('de');
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(function() {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
    $('.tooltip').remove();
  });

  function createField(model) {
    var field = new scout.DateField();
    field.init(model, session);
    return field;
  }

  function createFieldAndFocusAndOpenPicker(model) {
    var dateField = createField(model);
    dateField.render(session.$entryPoint);

    dateField.$field.focus();
    expect(dateField.$field).toBeFocused();

    dateField.$field.click();
    expect(findPicker().length).toBe(1);

    return dateField;
  }

  function createModel(id) {
    if (id === undefined) {
      id = createUniqueAdapterId();
    }
    var model =  {
      "id": id,
      "enabled": true,
      "visible": true
    };
    return model;
  }

  function findPicker() {
    return $('.date-box');
  }

  function writeText(dateField, displayText) {
    dateField.$field.val(displayText);
    dateField.validateDisplayText(dateField.$field.val());
  }

  describe("Clicking the field", function() {

    it("opens the datepicker", function() {
      var model = createModel();
      var dateField = createField(model);
      dateField.render(session.$entryPoint);
      expect(findPicker().length).toBe(0);

      dateField.$field.click();

      expect(findPicker().length).toBe(1);
    });
  });

  describe("Leaving the field", function() {

    it("closes the datepicker", function() {
      var model = createModel();
      var dateField = createFieldAndFocusAndOpenPicker(model);
      expect(findPicker().length).toBe(1);

      dateField.$field.blur();

      expect(findPicker().length).toBe(0);
    });

    it("accepts the prediction", function() {
      var model = createModel();
      var dateField = createFieldAndFocusAndOpenPicker(model);

      writeText(dateField, '02');
      dateField._$predict.val('02.11.2015');
      dateField._onFieldBlur();

      expect(dateField.$field.val()).toBe('02.11.2015');
    });

    it("updates the model with the selected value", function() {
      var model = createModel();
      model.displayText = '01.10.2014';
      var dateField = createFieldAndFocusAndOpenPicker(model);
      expect(dateField.displayText).toBe('01.10.2014');

      writeText(dateField, '02.11.2015');
      expect(dateField.displayText).toBe('01.10.2014');
      dateField._onFieldBlur();
      expect(dateField.displayText).toBe('02.11.2015');
    });
  });

  describe("Validation", function() {
    it("invalidates field if value is invalid (not a date)", function() {
      var model = createModel();
      var dateField = createFieldAndFocusAndOpenPicker(model);

      writeText(dateField, '33');
      expect(dateField.$field).toHaveClass('has-error');
    });

    it("prevents model update if value is invalid", function() {
      var model = createModel();
      var dateField = createFieldAndFocusAndOpenPicker(model);

      writeText(dateField, '33');
      expect(dateField.displayText).toBeFalsy();

      expect(mostRecentJsonRequest()).toBeUndefined();
    });
  });

  describe("Key handling", function() {

    describe("ESC", function() {

      it("closes the datepicker", function() {
        var model = createModel();
        var dateField = createFieldAndFocusAndOpenPicker(model);
        expect(findPicker().length).toBe(1);

        dateField.$field.triggerKeyDown(scout.keys.ESC);

        expect(findPicker().length).toBe(0);
      });

    });

    describe("ENTER", function() {

      it("updates the model with the selected value and closes picker", function() {
        var model = createModel();
        model.displayText = '01.10.2014';
        var dateField = createFieldAndFocusAndOpenPicker(model);
        expect(dateField.displayText).toBe('01.10.2014');

        writeText(dateField, '02.11.2015');
        expect(dateField.displayText).toBe('01.10.2014');
        dateField.$field.triggerKeyDown(scout.keys.ENTER);
        expect(dateField.displayText).toBe('02.11.2015');
        expect(findPicker().length).toBe(0);
      });

    });

    describe("DOWN", function() {

      it("increases day by one", function() {
        var model = createModel();
        model.displayText = '01.10.2014';
        var dateField = createFieldAndFocusAndOpenPicker(model);
        expect(dateField.displayText).toBe('01.10.2014');

        dateField.$field.triggerKeyDown(scout.keys.DOWN);

        expect(dateField.displayText).toBe('01.10.2014');
        expect(dateField.$field.val()).toBe('02.10.2014');
      });

      it("increases month by one if shift is used as modifier", function() {
        var model = createModel();
        model.displayText = '01.10.2014';
        var dateField = createFieldAndFocusAndOpenPicker(model);
        expect(dateField.displayText).toBe('01.10.2014');

        dateField.$field.triggerKeyDown(scout.keys.DOWN, 'shift');

        expect(dateField.displayText).toBe('01.10.2014');
        expect(dateField.$field.val()).toBe('01.11.2014');
      });

      it("increases year by one if ctrl is used as modifier", function() {
        var model = createModel();
        model.displayText = '01.10.2014';
        var dateField = createFieldAndFocusAndOpenPicker(model);
        expect(dateField.displayText).toBe('01.10.2014');

        dateField.$field.triggerKeyDown(scout.keys.DOWN, 'ctrl');

        expect(dateField.displayText).toBe('01.10.2014');
        expect(dateField.$field.val()).toBe('01.10.2015');
      });
    });

    describe("UP", function() {
      it("decreases day by one", function() {
        var model = createModel();
        model.displayText = '01.10.2014';
        var dateField = createFieldAndFocusAndOpenPicker(model);
        expect(dateField.displayText).toBe('01.10.2014');

        dateField.$field.triggerKeyDown(scout.keys.UP);

        expect(dateField.displayText).toBe('01.10.2014');
        expect(dateField.$field.val()).toBe('30.09.2014');
      });

      it("decreases month by one if shift is used as modifier", function() {
        var model = createModel();
        model.displayText = '01.10.2014';
        var dateField = createFieldAndFocusAndOpenPicker(model);
        expect(dateField.displayText).toBe('01.10.2014');

        dateField.$field.triggerKeyDown(scout.keys.UP, 'shift');

        expect(dateField.displayText).toBe('01.10.2014');
        expect(dateField.$field.val()).toBe('01.09.2014');
      });

      it("decreases year by one if ctrl is used as modifier", function() {
        var model = createModel();
        model.displayText = '01.10.2014';
        var dateField = createFieldAndFocusAndOpenPicker(model);
        expect(dateField.displayText).toBe('01.10.2014');

        dateField.$field.triggerKeyDown(scout.keys.UP, 'ctrl');

        expect(dateField.displayText).toBe('01.10.2014');
        expect(dateField.$field.val()).toBe('01.10.2013');
      });
    });
  });
});
