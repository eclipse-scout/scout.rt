describe("scrollbars", function() {
  var $scrollableDiv;
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

    $scrollableDiv = $('<div>')
      .css('overflow', 'hidden')
      .css('position', 'absolute')
      .css('height', '100%')
      .css('width', '100%')
      .appendTo($container);
  }

  function createContent($parent) {
    return $('<div>')
      .text('element')
      .css('height', '100px')
      .appendTo($parent);
  }

  describe("attachScrollHandlers", function() {

    it("attaches handler to scrolling parents which execute when scrolling", function() {
      var exec = false;
      var handler = function() {
        exec = true;
      };
      var $content = scout.scrollbars.install($scrollableDiv);
      var $element = createContent($content);

      scout.scrollbars.attachScrollHandlers($element, handler);
      $scrollableDiv.scroll();
      expect(exec).toBe(true);
    });

  });

  describe("detachScrollHandlers", function() {

    it("detaches handler from scrolling parents", function() {
      var exec = false;
      var handler = function() {
        exec = true;
      };
      var $content = scout.scrollbars.install($scrollableDiv);
      var $element = createContent($content);

      scout.scrollbars.attachScrollHandlers($element, handler);
      $scrollableDiv.scroll();
      expect(exec).toBe(true);

      exec = false;
      scout.scrollbars.detachScrollHandlers($element, handler);
      $scrollableDiv.scroll();
      expect(exec).toBe(false);
    });

  });

});
