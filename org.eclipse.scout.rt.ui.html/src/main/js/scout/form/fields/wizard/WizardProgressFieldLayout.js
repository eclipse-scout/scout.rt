scout.WizardProgressFieldLayout = function(formField) {
  scout.WizardProgressFieldLayout.parent.call(this, formField);
};
scout.inherits(scout.WizardProgressFieldLayout, scout.FormFieldLayout);

scout.WizardProgressFieldLayout.prototype.layout = function($container) {
  scout.WizardProgressFieldLayout.parent.prototype.layout.call(this, $container);

  if (this.formField.activeWizardStepIndex) {
    var $previousStep = this.formField.wizardSteps[this.formField.activeWizardStepIndex - 1].$wizardStep;
    var $currentStep = this.formField.wizardSteps[this.formField.activeWizardStepIndex].$wizardStep;
    if ($currentStep.position().left + $currentStep.width() > $currentStep.parent().width()) {
      $currentStep.parent().scrollLeft($currentStep.position().left - ($previousStep.width() + $currentStep.width() > $currentStep.parent().width()) ? $previousStep.width() : 0);
    }
  }
};
