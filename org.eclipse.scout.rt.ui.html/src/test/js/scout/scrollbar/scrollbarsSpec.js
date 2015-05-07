describe("scrollbars", function() {
  var $container;

  beforeEach(function() {
    setFixtures(sandbox());
    createScrollable();
  });

  function createScrollable() {
    $container = $('<div>')
      .css('height', '50px')
      .css('width', '200px')
      .css('position', 'absolute')
      .appendTo($('#sandbox'));
  }

  function createContent($parent) {
    return $('<div>')
      .text('element')
      .css('height', '100px')
      .appendTo($parent);
  }

  describe("onScroll", function() {

    it("attaches handler to scrolling parents which execute when scrolling", function() {
      var exec = false;
      var handler = function() {
        exec = true;
      };
      var $content = scout.scrollbars.install($container);
      var $element = createContent($content);

      scout.scrollbars.onScroll($element, handler);
      $container.scroll();
      expect(exec).toBe(true);
    });

  });

  describe("offScroll", function() {

    it("detaches handler from scrolling parents", function() {
      var exec = false;
      var handler = function() {
        exec = true;
      };
      var $content = scout.scrollbars.install($container);
      var $element = createContent($content);

      scout.scrollbars.onScroll($element, handler);
      $container.scroll();
      expect(exec).toBe(true);

      exec = false;
      scout.scrollbars.offScroll(handler);
      $container.scroll();
      expect(exec).toBe(false);
    });

  });

});
