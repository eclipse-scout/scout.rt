describe("MenuItemsOrder", function() {

  var session, menuItemsOrder;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    menuItemsOrder = new scout.MenuItemsOrder(session, 'Table');
  });

  it("_createSeparator", function() {
    var separator = menuItemsOrder._createSeparator();
    expect(separator.separator).toBe(true);
    expect(separator.session).toBeTruthy();
    expect(separator.visible).toBe(true);
    expect(separator.enabled).toBe(true);
    expect(separator.selected).toBe(false);
  });

  it("_menuTypes", function() {
    var menuTypes = menuItemsOrder._menuTypes();
    expect(menuTypes.length).toBe(0);
    menuTypes = menuItemsOrder._menuTypes('Foo');
    expect(menuTypes.length).toBe(1);
    expect(menuTypes[0]).toBe('Table.Foo');
    menuTypes = menuItemsOrder._menuTypes('Foo', 'Bar');
    expect(menuTypes.length).toBe(2);
    expect(menuTypes[1]).toBe('Table.Bar');
  });

});
