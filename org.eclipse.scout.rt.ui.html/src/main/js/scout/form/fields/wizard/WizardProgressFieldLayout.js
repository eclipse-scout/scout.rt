scout.WizardProgressFieldLayout = function(formField) {
  scout.WizardProgressFieldLayout.parent.call(this, formField);
};
scout.inherits(scout.WizardProgressFieldLayout, scout.FormFieldLayout);

scout.WizardProgressFieldLayout.prototype.layout = function($container) {
  scout.WizardProgressFieldLayout.parent.prototype.layout.call(this, $container);
  this.formField.scrollToActiveStep();
};
