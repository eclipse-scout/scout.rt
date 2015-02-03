describe("TableMenuItemsOrder", function() {

  it("createSeparator", function() {
    var separator = scout.TableMenuItemsOrder.createSeparator();
    expect(separator.separator).toBe(true);
    expect(separator.session).toBeTruthy();
    expect(separator.visible).toBe(true);
    expect(separator.enabled).toBe(true);
    expect(separator.selected).toBe(false);
  });

});
