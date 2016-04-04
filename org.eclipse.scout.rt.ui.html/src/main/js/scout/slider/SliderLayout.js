scout.SliderLayout = function(slider) {
  scout.SliderLayout.parent.call(this);
  this.slider = slider;
};
scout.inherits(scout.SliderLayout, scout.AbstractLayout);

scout.SliderLayout.prototype.layout = function($container) {
  var size = scout.graphics.getSize($container);
  this.slider.$sliderInput.css('height', size.height);
  this.slider.$sliderValue.css('height', size.height);
};
