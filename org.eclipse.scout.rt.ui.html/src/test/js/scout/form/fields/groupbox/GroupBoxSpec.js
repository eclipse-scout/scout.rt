/* global FormSpecHelper */
describe("GroupBox", function() {
  var session;
  var helper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new FormSpecHelper(session);
  });

  function createField(model, parent) {
    var field = new scout.GroupBox();
    field.init(model, session);
    return field;
  }

  describe("_render", function() {
    var groupBox, model = {
        id: '2',
        label: "fooBar",
        gridData: {
          x: 0,
          y: 0
        },
        parent: {
          objectType: 'GroupBox'
        }
      };

    beforeEach(function() {
      groupBox = createField(model);
    });

    it("adds group-box div when label is set", function() {
      groupBox._render($('#sandbox'));
      expect($('#sandbox')).toContainElement('div.group-box');
      expect($('#sandbox')).toContainElement('div.group-box-title');
    });
  });

  describe("test predefined height and width in pixel", function() {
    var form, formAdapter, formController, rootGroupBox, model = $.extend(createSimpleModel('GroupBox'), {
          id: '3',
          label: "fooBar",
          gridData: {
            x: 0,
            y: 0,
            widthInPixel: 97,
            heightInPixel: 123
          },
          mainBox: true
        });

    beforeEach(function() {
      form = helper.createFormModel();
      session.desktop = new scout.Desktop();
      session.rootAdapter.modalityElements = function() { return[]; };
      formController = new scout.FormController(form.parent, session);
      rootGroupBox = model;
      rootGroupBox.fields = [];
      form.rootGroupBox = rootGroupBox.id;
      form.owner = session.rootAdapter.id;
      formAdapter = createAdapter(form, session, [rootGroupBox]);
      session.desktop.$container = $('#sandbox');
    });

    it("adds group-box div when label is set", function() {
      formController._renderDialog(formAdapter);
      expect(formAdapter.rootGroupBox.$container.cssHeight()).toBe(123);
      expect(formAdapter.rootGroupBox.$container.cssWidth()).toBe(97);
    });
  });

});
