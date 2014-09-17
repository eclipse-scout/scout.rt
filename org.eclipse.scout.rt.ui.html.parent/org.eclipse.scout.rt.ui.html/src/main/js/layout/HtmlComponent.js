/**
 * Wrapper for a JQuery selector. Used as replacement for javax.swing.JComponent.
 */
scout.HtmlComponent = function($comp) {
  this.$comp = $comp;
  this.layoutManager;
  this.layoutData;
  // link DOM element with this instance
  $comp.data('htmlComponent', this);
};

/**
 * Static method to get the HtmlComponent associated with the given DOM $comp.
 * Throws an error when data 'htmlComponent' is not set.
 */
scout.HtmlComponent.get = function($comp) {
  var htmlComp = this.optGet($comp);
  if (!htmlComp) {
    throw 'data "htmlComponent" is undefined';
  }
  return htmlComp;
};

scout.HtmlComponent.optGet = function($comp) {
  return $comp.data('htmlComponent');
};

scout.HtmlComponent.prototype.layout = function() {
  if (this.layoutManager) {
    this.layoutManager.layout(this.$comp);
  } else {
    $.log('WARN (HtmlComponent#layout) Called layout() but component ' + this.debug() + ' has no layout manager');
    // throw 'Tried to layout component ' + this.debug() +' but component has no layout manager';
    // TODO AWE: (layout) entscheiden, ob wir dieses throw "scharf machen" wollen oder nicht
  }
};

/**
 * Sets the given layout manager.
 */
scout.HtmlComponent.prototype.setLayout = function(layoutManager) {
  this.layoutManager = layoutManager;
};

// TODO AWE: (layout) konzeptionell ist das noch nicht richtig, preferredLayoutSize braucht keinen parent, nur einen container
scout.HtmlComponent.prototype.getPreferredSize = function() {
  var prefSize;
  if (this.layoutManager) {
    prefSize = this.layoutManager.preferredLayoutSize(this.$comp);
    $.log('(HtmlComponent#getPreferredSize) ' + this.debug() + ' impl. preferredSize=' + prefSize);
  } else {
    // TODO AWE: (layout) hier koennten wir eigentlich einen fehler werfen, weil das nicht passieren sollte
    prefSize = scout.Dimension(this.$comp.width(), this.$comp.height());
    $.log('(HtmlComponent#getPreferredSize) ' + this.debug() + ' size of HTML element=' + prefSize);
  }
  return prefSize;
};

//FIXME AWE: getInsets impl. ist so falsch margin und border separat bestimmen
//schauen ob wir das überhaupt brauchen (width VS outherWidth im vergleich z Swing)
//ggf. andere Stellen refactoren an denen das hier auch gebraucht wird
//scout.HtmlComponent.prototype.getInsets = function() {
//  var hMargin = this.$comp.outerWidth(true) - this.$comp.width();
//  var vMargin = this.$comp.outerHeight(true) - this.$comp.height();
//  return new scout.Insets(vMargin / 2, hMargin / 2, vMargin / 2, hMargin / 2);
//};

scout.HtmlComponent.prototype.getInsets = function() {
  var directions = ['top', 'right', 'bottom', 'left'],
    insets = [0, 0, 0, 0],
    i,
  cssToInt = function($comp, cssProp) {
      return parseInt($comp.css(cssProp), 10);
    };
  for (i=0; i<directions.length; i++) {
  // parseInt will ignore 'px' in string returned from css() method
  insets[i] += cssToInt(this.$comp, 'margin-' + directions[i]);
  insets[i] += cssToInt(this.$comp, 'padding-' + directions[i]);
  insets[i] += cssToInt(this.$comp, 'border-' + directions[i] + '-width');
  }
  return new scout.Insets(insets[0], insets[1], insets[2], insets[3]);
};

// TODO AWE: (layout) merge with Layout. static class, check consequences first

scout.HtmlComponent.prototype.getSize = function() {
  return new scout.Dimension(
      this.$comp.width(),
      this.$comp.height());
};

scout.HtmlComponent.prototype.setSize = function(size) {
  var oldSize = this.getSize();
  if (!oldSize.equals(size)) {
    this.layoutManager.invalidate();
  }
  this.$comp.
    css('width', size.width).
    css('height', size.height);
  this.layout();
};

scout.HtmlComponent.prototype.getBounds = function() {
  return new scout.Rectangle(
      this.$comp.css('left'),
      this.$comp.css('top'),
      this.$comp.css('width'),
      this.$comp.css('height'));
};

scout.HtmlComponent.prototype.setBounds = function(bounds) {
  var oldBounds = this.getBounds();
  if (!oldBounds.equals(bounds)) {
    this.layoutManager.invalidate();
  }
  this.$comp.
    css('left', bounds.x).
    css('width', bounds.width).
    css('top', bounds.y).
    css('height', bounds.height);
  this.layout();
};

scout.HtmlComponent.prototype.debug = function() {
  var attrs = '';
  if (this.$comp.attr('id')) {
    attrs += 'id=' + this.$comp.attr('id');
  }
  if (this.$comp.attr('class')) {
    attrs += ' class=' + this.$comp.attr('class');
  }
  if (attrs.length === 0) {
    attrs = this.$comp.html().substring(0, 30) + '...';
  }
  return 'HtmlComponent[' + attrs.trim() + ']';
};

