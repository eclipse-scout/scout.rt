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
  },

  keyStrokeName: function(ctrl, alt, shift, keyStroke){
    var name =ctrl ? 'ctrl+':'';
    name +=alt ? 'alt+':'' ;
    name += shift ? 'shift+':'';
    name += keyStroke;
    return name;
  },

  keyStrokesAlreadyDrawn: function(paintedKeyStrokes,ctrl, alt, shift, start, end){
    var name =ctrl ? 'ctrl+':'';
    name +=alt ? 'alt+':'' ;
    name += shift ? 'shift+':'';
    for(var i = start ; i<=end; i++ ){
      if(paintedKeyStrokes[name+i]){
        return true;
      }
    }
    return false;
  },
  /**
   * check if key stroke is already drawn. if its already drawn it returns true. if not add it and return false
   */
  keyStrokeAlreadyDrawnAndDraw: function(paintedKeyStrokes,ctrl, alt, shift, keyStrokeNr){
    var name =ctrl ? 'ctrl+':'';
    name +=alt ? 'alt+':'' ;
    name += shift ? 'shift+':'';
    name += keyStrokeNr;
      if(paintedKeyStrokes[name]){
        return true;
      }
    paintedKeyStrokes[name]=true;
    return false;
  },

  keyStrokeRangeDrawn: function(paintedKeyStrokes,ctrl, alt, shift, start, end){
    var name =ctrl ? 'ctrl+':'';
    name +=alt ? 'alt+':'' ;
    name += shift ? 'shift+':'';
    for(var i = start ; i<=end; i++ ){
      paintedKeyStrokes[name+i]=true;
    }
  }
};
