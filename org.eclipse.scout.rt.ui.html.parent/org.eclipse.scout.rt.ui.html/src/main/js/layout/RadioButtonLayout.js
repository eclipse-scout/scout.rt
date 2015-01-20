/**
 * RadioButtonLayout. Sets the input behind the displayed Radio-Button ui-elemnt in label:before.
 */
scout.RadioButtonLayout = function(formField) {
  scout.RadioButtonLayout.parent.call(this);
  this.formField = formField;
};
scout.inherits(scout.RadioButtonLayout, scout.FormFieldLayout);

scout.RadioButtonLayout.prototype.layout = function($container) {
  var htmlContainer = scout.HtmlComponent.get($container),
  f = this.formField,
  b;
  scout.RadioButtonLayout.parent.prototype.layout.call(this,$container);

  //move radio button behind radiobutton icon in label:before
  b=scout.graphics.getBounds(f.$fieldContainer);
  b.x=0;
  scout.graphics.setBounds(f.$fieldContainer, b);
};
