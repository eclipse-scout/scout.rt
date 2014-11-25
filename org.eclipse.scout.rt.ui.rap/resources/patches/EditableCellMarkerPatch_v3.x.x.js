/**
 *  This is a workaround to render a visual marker for editable cells by patching RAP 'GridRow.js'. This is necessary because there is no variant-support for individual table cells.
 *
 *  Alternatively, to not patch RAP an additional HTML-element could be introduced to wrap the cell's content. However, this causes some performance overhead and a hack is still required
 *  to reset the cell's padding because being calculated directly into the absolute cell position.
 *
 *  See 'RwtScoutColumnModel.java' where the variant on the 'TableItem' is set.
 */
(function() {

  rwt.qx.Class.__initializeClass(rwt.widgets.base.GridRow);
  var gridRowProto = rwt.widgets.base.GridRow.prototype;
  gridRowProto._renderCellLabelBounds = function(item, cell, config) {
    var element = this.$cellLabels[cell];
    if (element) {
      var padding = this._getCellPadding(config);

      var left = this._getItemTextLeft(item, cell, config);
      var top = padding[0];
      var width = this._getItemTextWidth(item, cell, config);

      // PATCH: If the cell is editable, decorate the cell with a visual marker.
      var elPos = decorateEditableCell(element, item, cell, padding, left, top, width, this.getHeight() - top);

      element.css( { "left" : elPos.left, "top" : elPos.top, "width" : elPos.width, "height" : "auto" } );
    }
  };

  /**
   * Decorates the given cell with a visual marker for editable cells.
   */
  function decorateEditableCell(element, item, cell, padding, left, top, width, height) {
    if (!item) {
      return { left: left, top: top, width: width, height: height };
    }

    if (isCellEditable(item, cell)) {
      var paddingTop = padding[0];
      var paddingRight = padding[1];
      var paddingBottom = padding[2];
      var paddingLeft = padding[3];

      // Install the visual marker as background-image and move the element to the very top-left cell-corner.
      // This is necessary because the padding was already calculated into the absolute position.
      left -= paddingLeft;
      width += paddingLeft;
      top -= paddingTop;
      height += paddingTop;

      element.css({
        "backgroundImage": "url('rwt-resources/editable_tablecell_marker.png')",
        "backgroundOrigin": "border-box",
        "backgroundRepeat": "no-repeat",
        "backgroundPosition": "left top",
        "padding": (paddingTop + "px " + paddingRight + "px " + paddingBottom + "px " + paddingLeft + "px")
      });

      // element.style.padding = (paddingTop + "px " + paddingRight + "px " + paddingBottom + "px " + paddingLeft + "px");
    } else {
      // unset the visual marker because the cell is not editable (anymore).
        element.css( {
            "backgroundImage" : "none",
            "padding" : "0"
         } );
    }

    return {
      left: left,
      top: top,
      width: width,
      height: height
    };
  }

  /**
   * Checks whether the given cell is editable.
   */
  function isCellEditable(item, cell) {
    var variant = item.getVariant(); // the custom variant was set in 'RwtScoutColumnModel.java'.
    var prefix = "variant_EDITABLE_CELL_VARIANT_";
    if (!variant || variant.indexOf(prefix) !== 0) {
      return false;
    }

    variant = variant.substring(prefix.length);
    var cellVariant = variant.split("_")[cell - 1];
    return cellVariant === "EDITABLE";
  }

}());
