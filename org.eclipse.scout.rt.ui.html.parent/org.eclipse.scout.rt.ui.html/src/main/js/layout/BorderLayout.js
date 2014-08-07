scout.BorderLayout = function(marginTop, marginRight, cssClassPrefix) {
  this.marginTop = marginTop;
  this.cssClassPrefix = cssClassPrefix;
  this.$fixedElements = [];
  this.$dynamicElements = [];
};

scout.BorderLayout.prototype.register = function($element, position) {
  $element.data('position', position);
  $element.addClass(this.cssClassPrefix + '-' + position);
  if (position === 'C') {
    this.$dynamicElements.push($element);
  } else {
    this.$fixedElements.push($element);
  }
};

scout.BorderLayout.prototype.unregister = function($element, position) {
  $element.data('position', null);
  $element.removeClass(this.cssClassPrefix + '-' + position);
  scout.arrays.remove(this.$dynamicElements, $element);
  scout.arrays.remove(this.$fixedElements, $element);
};

scout.BorderLayout.prototype.layout = function() {
  var i;
  for (i = 0; i < this.$fixedElements.length; i++) {
    this.layoutElement(this.$fixedElements[i]);
  }
  for (i = 0; i < this.$dynamicElements.length; i++) {
    this.layoutElement(this.$dynamicElements[i]);
  }
};

scout.BorderLayout.prototype.layoutElement = function($element) {
  var position = $element.data('position');
  $element.css('position', 'absolute');
  if (position === 'W' || position === 'C' || position === 'E') {
    $element.attr('data-row', 1);

    if (this.marginTop > 0) {
      $element.height('calc(100% - ' + this.marginTop + 'px)');
    } else {
      $element.height('100%');
    }
  }

  if (position === 'C') {
    var siblingWidth = 0,
      leftWidth = 0,
      rightWidth = 0;
    $element.prevAll('[data-row="1"]').each(function() {
      leftWidth += $(this).outerWidth();
    });
    $element.nextAll('[data-row="1"]').each(function() {
      rightWidth += $(this).outerWidth();
    });

    $element.css('left', leftWidth);

    siblingWidth = leftWidth + rightWidth;
    if (siblingWidth > 0) {
      $element.width('calc(100% - ' + siblingWidth + 'px)');
    } else {
      $element.width('100%');
    }
  }

  if (position === 'N') {
    $element.data('row', 0);
    this.$top = $element;
  }
};
