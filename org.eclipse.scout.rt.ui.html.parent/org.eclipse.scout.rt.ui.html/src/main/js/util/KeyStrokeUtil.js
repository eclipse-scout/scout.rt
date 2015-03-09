scout.KeyStrokeUtil = {
  additionalMarginLeft: 22,
  marginLeft: 28,
  drawSingleKeyBoxItem: function(offset, keyBoxText, $container, ctrl, alt, shift) {
    var additionalKeyBox;
    var existingKeyBoxes = $('.key-box', $container);
    var existingSeparators = $('.key-box-additional', $container);
    offset = offset + this.marginLeft*existingKeyBoxes.length + this.additionalMarginLeft * existingSeparators.length;
    if (existingKeyBoxes.length>0) {
      additionalKeyBox = $container.prependDiv('key-box-additional', ';');
      additionalKeyBox.css('left', '' + offset + 'px');
      offset = offset + this.additionalMarginLeft;
    }
    if (ctrl) {
      $container.prependDiv('key-box ', 'ctrl').css('left', '' + offset + 'px');
      offset = offset + this.marginLeft;
      $container.prependDiv('key-box-additional', '+').css('left', '' + offset + 'px');
      offset = offset + this.additionalMarginLeft;
    }
    if (alt) {
      $container.prependDiv('key-box ', 'alt').css('left', '' + offset + 'px');
      offset = offset + this.marginLeft;
      $container.prependDiv('key-box-additional ', '+').css('left', '' + offset + 'px');
      offset = offset + this.additionalMarginLeft;
    }
    if (shift) {
      $container.prependDiv('key-box ', 'shift').css('left', '' + offset + 'px');
      offset = offset + this.marginLeft;
      $container.prependDiv('key-box-additional ', '+').css('left', '' + offset + 'px');
      offset = offset + this.additionalMarginLeft;
    }
    $container.prependDiv('key-box ', keyBoxText).css('left', '' + offset + 'px');
    offset = offset + this.marginLeft;
    return offset;
  }
};
