/* global MenuSpecHelper */
describe("menus", function() {
  var helper;

  beforeEach(function() {
    helper = new MenuSpecHelper();
  });

  describe("filter", function() {

    it("does nothing if no menus are given", function() {
      var menus;
      menus = scout.menus.filter(menus);
      expect(menus).toBeUndefined();
    });

    it("only returns visible menus", function() {
      var menus, menu1, menu2, menu3;
      menu1 = helper.createModel(1);
      menu2 = helper.createModel(2);
      menu3 = helper.createModel(3);
      menu3.visible = false;

      menus = scout.menus.filter([menu1, menu2, menu3]);

      expect(menus).toEqual([menu1, menu2]);
    });

    it("only returns menus with given type", function() {
      var menus, menu1, menu2, menu3;
      menu1 = helper.createModel(1);
      menu1.menuTypes = ['MultiSelection', 'SingleSelection'];
      menu2 = helper.createModel(2);
      menu2.menuTypes = ['MultiSelection'];
      menu3 = helper.createModel(3);
      menu3.menuTypes = ['SingleSelection'];

      menus = scout.menus.filter([menu1, menu2, menu3], 'SingleSelection');

      expect(menus).toEqual([menu1, menu3]);
    });

    it("only returns menus with given type which are visible", function() {
      var menus, menu1, menu2, menu3;
      menu1 = helper.createModel(1);
      menu1.menuTypes = ['MultiSelection', 'SingleSelection'];
      menu1.visible = false;
      menu2 = helper.createModel(2);
      menu2.menuTypes = ['MultiSelection'];
      menu3 = helper.createModel(3);
      menu3.menuTypes = ['SingleSelection'];

      menus = scout.menus.filter([menu1, menu2, menu3], 'SingleSelection');

      expect(menus).toEqual([menu3]);
    });

    it("only returns parent menus if child menus should be displayed", function() {
      var menus, menu1, menu2, menu3;
      menu1 = helper.createModel(1);
      menu1.menuTypes = ['MultiSelection', 'SingleSelection'];
      menu2 = helper.createModel(2);
      menu2.menuTypes = ['SingleSelection'];
      menu3 = helper.createModel(3);
      menu3.menuTypes = ['MultiSelection'];
      menu2.childMenus = [menu3];

      menus = scout.menus.filter([menu1, menu2], 'SingleSelection');
      expect(menus).toEqual([menu1]);

      menu3.menuTypes = ['SingleSelection'];
      menus = scout.menus.filter([menu1, menu2], 'SingleSelection');
      expect(menus).toEqual([menu1, menu2]);
    });

  });

});
