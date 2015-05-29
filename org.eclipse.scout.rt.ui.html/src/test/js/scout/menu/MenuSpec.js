/* global MenuSpecHelper */
describe("Menu", function() {

  var helper, session, $sandbox, menu1;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    $sandbox = $('#sandbox');
    helper = new MenuSpecHelper(session);
    menu1 = helper.createMenu(helper.createModel('foo'));
  });

  describe('defaults', function() {

    it('should have expected defaults', function() {
      expect(menu1.overflow).toBe(false);
      expect(menu1.defaultMenu).toBe(false);
    });

  });

  describe('render', function() {

    it('render depending on the actionStyle', function() {
      menu1.render($sandbox);
      expect(menu1.$container.hasClass('menu-item')).toBe(true);
      menu1.remove();

      menu1.actionStyle = scout.Action.ActionStyle.BUTTON;
      menu1.render($sandbox);
      expect(menu1.$container.hasClass('menu-button')).toBe(true);
      menu1.remove();

      // when button is used in overflow-menu, style should be back to menu-item
      menu1.overflow = true;
      menu1.render($sandbox);
      expect(menu1.$container.hasClass('menu-item')).toBe(true);
      menu1.remove();

      menu1.overflow = false;
      menu1.defaultMenu = true;
      menu1.render($sandbox);
      expect(menu1.$container.hasClass('menu-item')).toBe(true);
      expect(menu1.$container.hasClass('default-menu')).toBe(true);
    });

    it('render as separator', function() {
      menu1.separator = true;
      menu1.render($sandbox);
      expect(menu1.$container.hasClass('menu-separator')).toBe(true);
    });

  });

  describe('isTabTarget', function() {

    it('should return true when menu can be a target of TAB action', function() {
      menu1.enabled = true;
      menu1.visible = true;
      menu1.actionStyle = scout.Action.ActionStyle.BUTTON;
      expect(menu1.isTabTarget()).toBe(true);

      menu1.actionStyle = scout.Action.ActionStyle.DEFAULT;
      expect(menu1.isTabTarget()).toBe(true);

      menu1.separator = true;
      expect(menu1.isTabTarget()).toBe(false);

      menu1.separator = false;
      menu1.enabled = false;
      expect(menu1.isTabTarget()).toBe(false);
    });

  });

});
