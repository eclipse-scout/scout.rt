describe("ModelAdapter", function() {

  var session;
  var model = {};

  beforeEach(function() {
    setFixtures(sandbox());
    jasmine.Ajax.install();
    jasmine.clock().install();

    session = new scout.Session($('#sandbox'), '1.1');
    session.init();
  });

  afterEach(function() {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });

  it("can handle properties in any order", function() {
    var event;

    // Create a dummy object
    var modelAdapter = new scout.ModelAdapter();
    modelAdapter.id = '2';
    modelAdapter.init(model, session);
    session.registerModelAdapter(modelAdapter);

    // Send a dummy event to this object which contains both a new object and a id-only ref to that new object
    event = new scout.Event('property', '2', {
      'properties': {
        'x1': 'val1',
        'x2': 'val2',
        'o1': {
          'id': '3',
          'objectType': 'GroupBox',
          'visible': true
        },
        'o2': {
          'id': '3'
        }
      }
    });
    session._processEvents([event]);

    expect(modelAdapter.x1).toBe('val1');
    expect(modelAdapter.x2).toBe('val2');
    expect(modelAdapter.o1).toBeDefined();
    expect(modelAdapter.o1.id).toBe('3');
    expect(modelAdapter.o2).toBeDefined();
    expect(modelAdapter.o2.id).toBe('3');

    // Now send a second event, but now send the id-only ref first (in o1).
    event = new scout.Event('property', '2', {
      'properties': {
        'x2': 'val20',
        'x1': 'val10',
        'o1': {
          'id': '4'
        },
        'o2': {
          'id': '4',
          'objectType': 'GroupBox',
          'visible': false
        }
      }
    });
    session._processEvents([event]);

    expect(modelAdapter.x1).toBe('val10');
    expect(modelAdapter.x2).toBe('val20');
    expect(modelAdapter.o1).toBeDefined();
    expect(modelAdapter.o1.id).toBe('4');
    expect(modelAdapter.o2).toBeDefined();
    expect(modelAdapter.o2.id).toBe('4');
  });

  it("_syncPropertiesOnPropertyChange calls _sync* method or sets property", function() {
    var adapter = new scout.ModelAdapter(),
      oldValues = {},
      newValues = {
        foo: 6,
        bar: 7
      };
    adapter.foo = 1;
    adapter.bar = 2;
    adapter._syncFoo = function(value) {
      this.foo = value;
    };
    spyOn(adapter, '_syncFoo').and.callThrough();
    adapter._syncPropertiesOnPropertyChange(oldValues, newValues);
    expect(adapter.foo).toBe(6);
    expect(adapter.bar).toBe(7);
    expect(adapter._syncFoo).toHaveBeenCalled();
    expect(oldValues.foo).toBe(1);
    expect(oldValues.bar).toBe(2);
  });

  describe("_renderPropertiesOnPropertyChange", function() {

    it("for non-adapter property -> expects a _render* method", function() {
      var adapter = new scout.ModelAdapter(),
        $div = $('<div>');
      adapter._renderFoo = function(value) {
        $div.text(value);
      };
      adapter._renderPropertiesOnPropertyChange({}, {foo: 'bar'});
      expect($div.text()).toBe('bar');
    });

    it("for non-adapter property -> throw when _render* method does not exist", function() {
      var adapter = new scout.ModelAdapter();
      expect(adapter._renderPropertiesOnPropertyChange.bind(adapter, {}, {foo: 'bar'})).toThrow();
    });

  });


});
