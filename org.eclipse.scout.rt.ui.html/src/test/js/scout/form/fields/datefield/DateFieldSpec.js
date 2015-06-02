/* global LocaleSpecHelper, FormSpecHelper */
describe("DateField", function() {
  var session;
  var helper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    session.locale = new LocaleSpecHelper().createLocale('de');
    helper = new FormSpecHelper(session);
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(function() {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
    $('.tooltip').remove();
    $('.date-box').remove();
  });

  function createField(model) {
    var field = new scout.DateField();
    field.init(model, session);
    return field;
  }

  function createFieldAndFocusAndOpenPicker(model) {
    var dateField = createField(model);
    dateField.render(session.$entryPoint);

    dateField.$dateField.focus();
    jasmine.clock().tick(101);
    expect(dateField.$dateField).toBeFocused();
    dateField.$dateField.click();
    expect(findPicker().length).toBe(1);

    return dateField;
  }

  function createModel() {
    var model = helper.createFieldModel('scout.DateField');
    model.hasDate=true;
    model.timeFormatPattern = 'HH:mm';
    model.dateFormatPattern = 'dd.MM.yyyy';
    return model;
  }

  function findPicker() {
    return $('.date-box');
  }

  function writeText(dateField, displayText) {
    dateField.$dateField.val(displayText);
    dateField.validateDisplayText(dateField.$dateField.val());
  }

  describe("Clicking the field", function() {

    it("opens the datepicker", function() {
      var model = createModel();
      var dateField = createField(model);
      dateField.render(session.$entryPoint);
      expect(findPicker().length).toBe(0);

      dateField.$dateField.click();

      expect(findPicker().length).toBe(1);
    });

  });

  describe("Leaving the field", function() {

    it("closes the datepicker", function() {
      var model = createModel();
      var dateField = createFieldAndFocusAndOpenPicker(model);
      expect(findPicker().length).toBe(1);

      dateField.$dateField.blur();

      expect(findPicker().length).toBe(0);
    });

    it("accepts the prediction", function() {
      var model = createModel();
      var dateField = createFieldAndFocusAndOpenPicker(model);

      writeText(dateField, '02');
      dateField._$predict.val('02.11.2015');
      dateField._onFieldBlurDate();

      expect(dateField.$dateField.val()).toBe('02.11.2015');
    });

    it("updates the model with the selected value", function() {
      var model = createModel();
      model.timestamp = 1412114400000;
      var dateField = createFieldAndFocusAndOpenPicker(model);
      var dateBefore = new Date(dateField.timestamp);
      expect(dateBefore.getFullYear()).toBe(2014);
      expect(dateBefore.getMonth()).toBe(9);
      expect(dateBefore.getDate()).toBe(1);

      writeText(dateField, '11.02.2015');
      dateBefore = new Date(dateField.timestamp);
      expect(dateBefore.getFullYear()).toBe(2014);
      expect(dateBefore.getMonth()).toBe(9);
      expect(dateBefore.getDate()).toBe(1);

      dateField._onFieldBlurDate();
      var date= new Date(dateField.timestamp);
      expect(date.getFullYear()).toBe(2015);
      expect(date.getMonth()).toBe(1);
      expect(date.getDate()).toBe(11);

    });

  });

  describe("Validation", function() {

    it("invalidates field if value is invalid (not a date)", function() {
      var model = createModel();
      var dateField = createFieldAndFocusAndOpenPicker(model);

      writeText(dateField, '33');
      expect(dateField.$dateField).toHaveClass('has-error');
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

        dateField.$dateField.triggerKeyDown(scout.keys.ESC);

        expect(findPicker().length).toBe(0);
      });

    });

    describe("ENTER", function() {

      it("updates the model with the selected value and closes picker", function() {
        var model = createModel();
        model.timestamp = 1412114400000;
        var dateField = createFieldAndFocusAndOpenPicker(model);
        var dateBefore = new Date(dateField.timestamp);
        expect(dateBefore.getFullYear()).toBe(2014);
        expect(dateBefore.getMonth()).toBe(9);
        expect(dateBefore.getDate()).toBe(1);


        writeText(dateField, '11.02.2015');
        dateBefore=new Date(dateField.timestamp);
        expect(dateBefore.getFullYear()).toBe(2014);
        expect(dateBefore.getMonth()).toBe(9);
        expect(dateBefore.getDate()).toBe(1);


        dateField.$dateField.triggerKeyDown(scout.keys.ENTER);
        var date= new Date(dateField.timestamp);
        expect(date.getFullYear()).toBe(2015);
        expect(date.getMonth()).toBe(1);
        expect(date.getDate()).toBe(11);
        expect(findPicker().length).toBe(0);
      });

    });

    describe("DOWN", function() {

      it("increases day by one", function() {
        var model = createModel();
        model.timestamp = 1412114400000;
        var dateField = createFieldAndFocusAndOpenPicker(model);
        var dateBefore = new Date(dateField.timestamp);
        expect(dateBefore.getFullYear()).toBe(2014);
        expect(dateBefore.getMonth()).toBe(9);
        expect(dateBefore.getDate()).toBe(1);

        dateField.$dateField.triggerKeyDown(scout.keys.DOWN);

        dateBefore = new Date(dateField.timestamp);
        expect(dateBefore.getFullYear()).toBe(2014);
        expect(dateBefore.getMonth()).toBe(9);
        expect(dateBefore.getDate()).toBe(1);
        expect(dateField.$dateField.val()).toBe('02.10.2014');
      });

      it("increases month by one if shift is used as modifier", function() {
        var model = createModel();
        model.timestamp = 1412114400000;
        var dateField = createFieldAndFocusAndOpenPicker(model);
        var dateBefore = new Date(dateField.timestamp);
        expect(dateBefore.getFullYear()).toBe(2014);
        expect(dateBefore.getMonth()).toBe(9);
        expect(dateBefore.getDate()).toBe(1);

        dateField.$dateField.triggerKeyDown(scout.keys.DOWN, 'shift');
        dateBefore = new Date(dateField.timestamp);
        expect(dateBefore.getFullYear()).toBe(2014);
        expect(dateBefore.getMonth()).toBe(9);
        expect(dateBefore.getDate()).toBe(1);

        expect(dateField.$dateField.val()).toBe('01.11.2014');
      });

      it("increases year by one if ctrl is used as modifier", function() {
        var model = createModel();
        model.timestamp = 1412114400000;
        var dateField = createFieldAndFocusAndOpenPicker(model);
        var dateBefore = new Date(dateField.timestamp);
        expect(dateBefore.getFullYear()).toBe(2014);
        expect(dateBefore.getMonth()).toBe(9);
        expect(dateBefore.getDate()).toBe(1);

        dateField.$dateField.triggerKeyDown(scout.keys.DOWN, 'ctrl');

        dateBefore = new Date(dateField.timestamp);
        expect(dateBefore.getFullYear()).toBe(2014);
        expect(dateBefore.getMonth()).toBe(9);
        expect(dateBefore.getDate()).toBe(1);

        expect(dateField.$dateField.val()).toBe('01.10.2015');
      });

    });

    describe("UP", function() {

      it("decreases day by one", function() {
        var model = createModel();
        model.timestamp = 1412114400000;
        var dateField = createFieldAndFocusAndOpenPicker(model);
        var dateBefore = new Date(dateField.timestamp);
        expect(dateBefore.getFullYear()).toBe(2014);
        expect(dateBefore.getMonth()).toBe(9);
        expect(dateBefore.getDate()).toBe(1);

        dateField.$dateField.triggerKeyDown(scout.keys.UP);

        dateBefore = new Date(dateField.timestamp);
        expect(dateBefore.getFullYear()).toBe(2014);
        expect(dateBefore.getMonth()).toBe(9);
        expect(dateBefore.getDate()).toBe(1);
        expect(dateField.$dateField.val()).toBe('30.09.2014');
      });

      it("decreases month by one if shift is used as modifier", function() {
        var model = createModel();
        model.timestamp = 1412114400000;
        var dateField = createFieldAndFocusAndOpenPicker(model);
        var dateBefore = new Date(dateField.timestamp);
        expect(dateBefore.getFullYear()).toBe(2014);
        expect(dateBefore.getMonth()).toBe(9);
        expect(dateBefore.getDate()).toBe(1);

        dateField.$dateField.triggerKeyDown(scout.keys.UP, 'shift');

        dateBefore = new Date(dateField.timestamp);
        expect(dateBefore.getFullYear()).toBe(2014);
        expect(dateBefore.getMonth()).toBe(9);
        expect(dateBefore.getDate()).toBe(1);
        expect(dateField.$dateField.val()).toBe('01.09.2014');
      });

      it("decreases year by one if ctrl is used as modifier", function() {
        var model = createModel();
        model.timestamp = 1412114400000;
        var dateField = createFieldAndFocusAndOpenPicker(model);
        var dateBefore = new Date(dateField.timestamp);
        expect(dateBefore.getFullYear()).toBe(2014);
        expect(dateBefore.getMonth()).toBe(9);
        expect(dateBefore.getDate()).toBe(1);

        dateField.$dateField.triggerKeyDown(scout.keys.UP, 'ctrl');

        dateBefore = new Date(dateField.timestamp);
        expect(dateBefore.getFullYear()).toBe(2014);
        expect(dateBefore.getMonth()).toBe(9);
        expect(dateBefore.getDate()).toBe(1);
        expect(dateField.$dateField.val()).toBe('01.10.2013');
      });

    });

  });

  describe("Date validation and prediction", function() {

    it("can validate inputs", function() {
      var model = createModel();
      var dateField = createField(model);

      dateField._dateFormat = new scout.DateFormat(session.locale, 'dd.MM.yyyy');
      expect(!dateField._validateDisplayText('')).toBe(true);
      expect(!dateField._validateDisplayText(undefined)).toBe(true);
      //expect(!dateField._validateDisplayText('0')).toBe(true); // TODO Sollte das nicht ok sein?
      expect(!dateField._validateDisplayText('1')).toBe(true);
      //expect(!dateField._validateDisplayText('-7')).toBe(true); // TODO Datumsarithmetik sollte erlaubt sein
      expect(!dateField._validateDisplayText('01')).toBe(true);
      expect(!dateField._validateDisplayText('17')).toBe(true);
      expect(!dateField._validateDisplayText('31')).toBe(true);
      expect(!dateField._validateDisplayText('32')).toBe(false);
      expect(!dateField._validateDisplayText('112')).toBe(false);
      expect(!dateField._validateDisplayText('1.')).toBe(true);
      expect(!dateField._validateDisplayText('1.3')).toBe(true);
      expect(!dateField._validateDisplayText('1.3.2')).toBe(true);
      expect(!dateField._validateDisplayText('1.3.2015')).toBe(true);
      expect(!dateField._validateDisplayText('1.3.21015')).toBe(false);
      expect(!dateField._validateDisplayText('01.13.2015')).toBe(false);
      expect(!dateField._validateDisplayText('01.03.2015')).toBe(true);
      //expect(!dateField._validateDisplayText('01032015')).toBe(true); // TODO Sollte das nicht ok sein? Aber was ist mit Locale?
      //expect(!dateField._validateDisplayText('010315')).toBe(true); // TODO Sollte das nicht ok sein? Aber was ist mit Locale?
      expect(!dateField._validateDisplayText('dummy')).toBe(false);
      //expect(!dateField._validateDisplayText('1...2')).toBe(false); // TODO Sollte false sein, ist aber jetzt true
      //expect(!dateField._validateDisplayText('11a')).toBe(false); // TODO Sollte false sein, ist aber jetzt true
      //expect(!dateField._validateDisplayText('31.02.2015')).toBe(false); // TODO Sollte false sein, ist aber jetzt true
    });

    it("can predict dates", function() {
      var model = createModel();
      var dateField = createField(model);
      var now = new Date();
      var nextMonth = scout.dates.shift(now, 0, 1);

      dateField._dateFormat = new scout.DateFormat(session.locale, 'dd.MM.yyyy');
      // _predict is only called for validated values, so we don't have to check invalid values
      // TODO Add tests when logic is defined!
//      expect(dateField._predict('0')).toBe('01.' + ('0' + (nextMonth.getMonth() + 1)).slice(-2) + '.' + nextMonth.getFullYear());
//      expect(dateField._predict('1')).toBe('1.' + ('0' + (nextMonth.getMonth() + 1)).slice(-2) + '.' + nextMonth.getFullYear());
//      expect(dateField._predict('2')).toBe('2.' + ('0' + (nextMonth.getMonth() + 1)).slice(-2) + '.' + nextMonth.getFullYear());
    });

  });

});
