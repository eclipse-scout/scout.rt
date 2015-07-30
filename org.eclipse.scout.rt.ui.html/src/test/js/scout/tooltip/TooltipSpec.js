//FIXME CGU needs CSS
//describe("Tooltip", function() {
//  var $sandbox;
//
//  beforeEach(function() {
//    setFixtures(sandbox());
//    $sandbox = $('#sandbox');
//  });
//
//  describe("positioning", function() {
//
//    it("shifts to left if origin is near the right window border", function() {
//      var $anchor = $('<div>').appendTo($sandbox)
//        .cssLeft($(window).width - 15)
//        .cssTop(50)
//        .width(20)
//        .height(20);
//
//      var tooltip = new scout.Tooltip(session, "text", $anchor);
//      tooltip.render($sandbox);
//
//      var tooltipOffset = tooltip.$container.offset();
//
//      expect(tooltipOffset.left).toBe($(window).width - tooltip.$container.outerWidth() - tooltip.windowPaddingX);
//      expect(tooltipOffset.top).toBe(50);
//
//      tooltip.remove();
//    });
//  });
//
//});
