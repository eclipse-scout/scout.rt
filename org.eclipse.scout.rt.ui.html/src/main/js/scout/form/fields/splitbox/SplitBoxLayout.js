scout.SplitBoxLayout = function(splitBox) {
  scout.SplitBoxLayout.parent.call(this);
  this._splitBox = splitBox;
  this.invalidateOnResize = true;
};
scout.inherits(scout.SplitBoxLayout, scout.AbstractLayout);

scout.SplitBoxLayout.prototype.layout = function($container) {
  // Extract components
  var htmlContainer = scout.HtmlComponent.get($container), // = split-area
    $splitter = $container.children('.splitter'),
    $fields = $container.children('.form-field'),
    htmlFirstField = scout.HtmlComponent.optGet($fields.eq(0)),
    htmlSecondField = scout.HtmlComponent.optGet($fields.eq(1));

  // Calculate available size for split area
  var splitXAxis = this._splitBox.splitHorizontal;
  var splitterSize = scout.graphics.getVisibleSize($splitter, true);
  var availableSize = htmlContainer.getAvailableSize()
    .subtract(htmlContainer.getInsets());
  if (splitXAxis) { // "|"
    availableSize.width -= splitterSize.width;
  } else { // "--"
    availableSize.height -= splitterSize.height;
  }

  if (htmlFirstField) {
    // Default case: two fields
    if (htmlSecondField) {
      // Distribute available size to the two fields according to the splitter position ratio
      var firstFieldSize = new scout.Dimension(availableSize);
      var secondFieldSize = new scout.Dimension(availableSize);
      if (splitXAxis) { // "|"
        firstFieldSize.width *= this._splitBox.splitterPosition;
        secondFieldSize.width *= (1 - this._splitBox.splitterPosition);
      } else { // "--"
        firstFieldSize.height *= this._splitBox.splitterPosition;
        secondFieldSize.height *= (1 - this._splitBox.splitterPosition);
      }
      firstFieldSize = firstFieldSize.subtract(htmlFirstField.getMargins());
      secondFieldSize = secondFieldSize.subtract(htmlSecondField.getMargins());

      // Calculate and set bounds (splitter and second field have to be moved)
      var firstFieldBounds = new scout.Rectangle(0, 0, firstFieldSize.width, firstFieldSize.height);
      var secondFieldBounds = new scout.Rectangle(0, 0, secondFieldSize.width, secondFieldSize.height);
      if (splitXAxis) { // "|"
        $splitter.cssLeft(firstFieldBounds.width);
        secondFieldBounds.x = firstFieldBounds.width + splitterSize.width;
      } else { // "--"
        $splitter.cssTop(firstFieldBounds.height);
        secondFieldBounds.y = firstFieldBounds.height + splitterSize.height;
      }
      htmlFirstField.setBounds(firstFieldBounds);
      htmlSecondField.setBounds(secondFieldBounds);
    }
    // Special case: only first field
    else {
      var firstFieldOnlySize = availableSize.subtract(htmlFirstField.getMargins());
      htmlFirstField.setSize(firstFieldOnlySize);
    }
  }
};

scout.SplitBoxLayout.prototype.preferredLayoutSize = function($container) {
  // Extract components
  var htmlContainer = scout.HtmlComponent.get($container), // = split-area
    $splitter = $container.children('.splitter'),
    $fields = $container.children('.form-field'),
    htmlFirstField = scout.HtmlComponent.optGet($fields.eq(0)),
    htmlSecondField = scout.HtmlComponent.optGet($fields.eq(1));

  var splitXAxis = this._splitBox.splitHorizontal;
  var splitterSize = scout.graphics.getVisibleSize($splitter, true);

  // Get preferred size of fields
  var firstFieldSize = new scout.Dimension(0, 0);
  if (htmlFirstField) {
    firstFieldSize = htmlFirstField.getPreferredSize()
      .add(htmlFirstField.getMargins());
  }
  var secondFieldSize = new scout.Dimension(0, 0);
  if (htmlSecondField) {
    secondFieldSize = htmlSecondField.getPreferredSize()
      .add(htmlSecondField.getMargins());
  }

  // Calculate prefSize
  var prefSize;
  if (splitXAxis) { // "|"
    prefSize = new scout.Dimension(
        firstFieldSize.width + secondFieldSize.width + splitterSize.width,
        Math.max(firstFieldSize.height, secondFieldSize.height)
    );
  } else { // "--"
    prefSize = new scout.Dimension(
        Math.max(firstFieldSize.width, secondFieldSize.width),
        firstFieldSize.height + secondFieldSize.height + splitterSize.height
    );
  }
  prefSize = prefSize.add(htmlContainer.getInsets());

  return prefSize;
};
