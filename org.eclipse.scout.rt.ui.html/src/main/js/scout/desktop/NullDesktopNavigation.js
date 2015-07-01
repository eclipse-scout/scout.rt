/**
 * This class is used in place of scout.DesktopNavigation when desktop has no navigation.
 * Avoids a lot of messy if/else code.
 */
scout.NullDesktopNavigation = {
    render: function($parent) {},
    onOutlineChanged: function(outline) {},
    bringToFront: function() {},
    sendToBack: function() {},
    onResize: function() {}
};
