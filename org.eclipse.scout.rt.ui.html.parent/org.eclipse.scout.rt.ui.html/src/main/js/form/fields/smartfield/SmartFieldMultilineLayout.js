scout.SmartFieldMultilineLayout = function() {
  scout.SmartFieldMultilineLayout.parent.call(this);
};
scout.inherits(scout.SmartFieldMultilineLayout, scout.AbstractLayout);

scout.SmartFieldMultilineLayout.prototype.preferredLayoutSize = function($container) {
  var htmlContainer = scout.HtmlComponent.get($container);

  // TODO BSH/AWE/CGU Layout | Is there a better way to get the layoutData? Calling parent() does not seem to be very elegant
  var htmlParent = scout.HtmlComponent.get($container.parent());
  var layoutData = htmlParent.layoutData;

  // use preferred size of container as default TODO BSH Layout | Check if this default value is really good
  var preferredSize = new scout.Dimension(htmlContainer.getSize());

  // TODO BSH/AWE/CGU Layout | Handle all properties of LogicalGridData: gridw, gridh, weightx, weighty, useUiWidth, useUiHeight, widthHint, heightHint, horizontalAlignment, verticalAlignment, fillHorizontal, fillVertical, topInset
  // TODO BSH/AWE/CGU Layout | Extract this logic to a base class (FormFieldLayout.js?)
  if (layoutData.useUiWidth) {
    // TODO BSH/AWE/CGU Layout | Calculate UI width
  }
  else {
    if (layoutData.gridw === 1) {
      preferredSize.width = scout.HtmlEnvironment.formColumnWidth;
    }
    else if (layoutData.gridw > 1) {
      preferredSize.width = (scout.HtmlEnvironment.formColumnWidth * layoutData.gridw) +
        (scout.HtmlEnvironment.formColumnGap * (layoutData.gridw - 1));
    }
  }

  if (layoutData.useUiHeight) {
    // TODO BSH/AWE/CGU Layout | Calculate UI height
  }
  else {
    if (layoutData.gridh === 1) {
      preferredSize.height = scout.HtmlEnvironment.formRowHeight;
    }
    else if (layoutData.gridh > 1) {
      preferredSize.height = (scout.HtmlEnvironment.formRowHeight * layoutData.gridh) +
        (scout.HtmlEnvironment.formRowGap * (layoutData.gridh - 1));
    }
  }

  // Add container insets TODO BSH Layout | Really?
  preferredSize = preferredSize.add(htmlContainer.getInsets());

  return preferredSize;
};

scout.SmartFieldMultilineLayout.prototype.layout = function($container) {
  var htmlContainer = scout.HtmlComponent.get($container);

  var $inputField = $container.children('.multiline');
  var $multilines = $container.children('.multiline-field');
  var innerSize = htmlContainer.getSize()
    .subtract(htmlContainer.getInsets());

  $inputField.cssHeight(scout.HtmlEnvironment.formRowHeight);
  $multilines.cssHeight(innerSize.height - scout.HtmlEnvironment.formRowHeight);
};
