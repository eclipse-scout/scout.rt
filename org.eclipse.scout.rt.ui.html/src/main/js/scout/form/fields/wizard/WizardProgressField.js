// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.WizardProgressField = function() {
  scout.WizardProgressField.parent.call(this);

  this._$stepList;
};
scout.inherits(scout.WizardProgressField, scout.FormField);

scout.WizardProgressField.prototype._render = function($parent) {
  this.addContainer($parent, 'wizard-progress-field');

  this._$stepList = $.makeDiv('wizard-steps');
  this.addField(this._$stepList);
};

scout.WizardProgressField.prototype._renderProperties = function() {
  scout.WizardProgressField.parent.prototype._renderProperties.call(this);
  this._renderWizardSteps(this.wizardSteps);
  this._renderActiveWizardStep(this.activeWizardStep);
};

scout.WizardProgressField.prototype._renderWizardSteps = function(wizardSteps) {
  this._$stepList.empty();
  if (wizardSteps) {
    for (var i = 0; i < wizardSteps.length; i++) {
      var wizardStep = wizardSteps[i];

      var $wizardStep = $.makeDiv('wizard-step')
        .attr('data-index', wizardStep.index)
        .appendTo(this._$stepList);
      var $wizardStepLabel = $.makeDiv('wizard-step-label')
        .text(wizardStep.title)
        .appendTo($wizardStep);
      // TODO BSH Wizard | Add icon

      if (wizardStep.enabled) {
        $wizardStep.on('click', this._onWizardStepClick.bind(this));
      } else {
        $wizardStep.addClass('disabled');
      }
      this._updateWizardStepActiveClasses($wizardStep, this.activeWizardStep);
    }
  }
};

scout.WizardProgressField.prototype._renderActiveWizardStep = function(activeWizardStep) {
  if (this.wizardSteps) {
    var $wizardSteps = this._$stepList.children('.wizard-step');
    for (var i = 0; i < this.wizardSteps.length; i++) {
      var wizardStep = this.wizardSteps[i];
      var $wizardStep = $wizardSteps.eq(i);
      this._updateWizardStepActiveClasses($wizardStep, activeWizardStep);
    }
  }
};

scout.WizardProgressField.prototype._updateWizardStepActiveClasses = function($wizardStep, activeWizardStep){
  $wizardStep.removeClass('before-active active after-active');
  var wizardStepIndex = this._wizardStepIndex($wizardStep);
  if (activeWizardStep >= 0 && wizardStepIndex >= 0) {
    if (wizardStepIndex < activeWizardStep) {
      $wizardStep.addClass('before-active');
    } else if (wizardStepIndex > activeWizardStep) {
      $wizardStep.addClass('after-active');
    } else {
      $wizardStep.addClass('active');
    }
  }
};

scout.WizardProgressField.prototype._onWizardStepClick = function(event) {
  var $wizardStep = $(event.currentTarget); // currentTarget instead of target to support event bubbling from inner divs
  var targetStepIndex = this._wizardStepIndex($wizardStep);
  if (targetStepIndex !== this.activeWizardStep) {
    this.session.send(this.id, 'stepClicked', {
      stepIndex: targetStepIndex
    });
  }
};

scout.WizardProgressField.prototype._wizardStepIndex = function($wizardStep) {
  if ($wizardStep) {
    var index = parseInt($wizardStep.attr('data-index'), 10);
    if (!isNaN(index)) {
      return index;
    }
  }
};
