// SCOUT GUI 0.2
// (c) Copyright 2013-2014, BSI Business Systems Integration AG
//
// scrollbar namespace and element
//

Scout.Scrollbar = function (scout, $container, axis, trackResize) {
  var dir = (axis === "x" ? "left" : "top"),
    dim = (axis === "x" ? "Width" : "Height"),
    begin = 0, beginDefault = 0,
    setThumb;

  // create scrollbar
  var $scrollbar = $container.beforeDiv('', 'scrollbar'),
    $thumb = $scrollbar.appendDiv('', 'scrollbar-thumb');

  //event handling
  $container.parent().on('DOMMouseScroll mousewheel', '', scrollWheel);
  $thumb.on('mousedown', '', scrollStart);
  $scrollbar.on('mousedown', scrollEnd);
  if (trackResize) $(window).on('load resize', initThumb);

  // set this for later usage
  this.initThumb = initThumb;

  // use this function (from outside) if size of tree content changes
  function initThumb () {
    var offset = $container[0]["offset" + dim],
      scroll = $container[0]["scroll" + dim],
      margin = parseInt($scrollbar.css('margin-top'), 0),
      topContainer = parseInt($container.css(dir), 0);

    // when needed: move container to right position
    if (offset - topContainer >= scroll){
      topContainer = Math.min(0, - scroll + offset);
      $container.stop().animateAVCSD(dir, topContainer);
    }

    // calc size and range of thumb
    var thumbSize = Math.max(offset * offset / scroll - margin * 2, 30),
      thumbRange = offset - thumbSize - margin * 2;

    // set size of thumb
    $thumb.css(dim.toLowerCase(), thumbSize);
    beginDefault = thumbSize / 2;

    // set location of thumb
    $thumb.css(dir, topContainer / (offset - scroll) * thumbRange);

    // show scrollbar
    if (offset >= scroll) {
      $scrollbar.css('visibility', 'hidden');
    }
    else {
      $scrollbar.css('visibility', 'visible');
    }

    // prepare function (with colsure) for later usage
    setThumb = function (posDiff) {
      var posOld = $thumb.offset()[dir] - $scrollbar.offset()[dir],
        posNew = Math.min(thumbRange, Math.max(0, posOld + posDiff));

      $container.css(dir, (offset - scroll) / thumbRange * posNew );
      $thumb.css(dir, posNew);
    };
  }

  function scrollWheel (event) {
    event = event.originalEvent || window.event.originalEvent;
    var w = event.wheelDelta ? - event.wheelDelta / 4 : event.detail * 30;
    setThumb(w);
    return false;
  }

  function scrollStart (event) {
    begin = (axis === "x" ? event.pageX : event.pageY) - $thumb.offset()[dir];
    $thumb.addClass('scrollbar-thumb-move');
    $(document).on('mousemove', scrollEnd)
      .one('mouseup', scrollExit);
    return false;
  }

  function scrollEnd (event) {
    begin = begin === 0 ? beginDefault : begin;
    var end = (axis === "x" ? event.pageX : event.pageY) - $thumb.offset()[dir];
    setThumb(end - begin);
  }

  function scrollExit() {
    $thumb.removeClass('scrollbar-thumb-move');
    $(document).off("mousemove");
    return false;
  }
};
