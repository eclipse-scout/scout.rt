// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.WizardProgressField = function() {
  scout.WizardProgressField.parent.call(this);

  this._$wizardSteps;
};
scout.inherits(scout.WizardProgressField, scout.FormField);

scout.WizardProgressField.prototype._render = function($parent) {
  this.addContainer($parent, 'wizard-progress-field');

  this._$wizardSteps = $.makeDiv('wizard-steps');
  this.addField(this._$wizardSteps);

  // TODO BSH Layout? Scrolling?
};

scout.WizardProgressField.prototype._renderProperties = function() {
  scout.WizardProgressField.parent.prototype._renderProperties.call(this);
  this._renderWizardSteps();
  this._renderActiveWizardStepIndex();
};

scout.WizardProgressField.prototype._renderWizardSteps = function() {
  this._$wizardSteps.children('.wizard-step').remove();

  this.wizardSteps.forEach(function(wizardStep, index) {
    var $wizardStep, $content, $title, $subTitle, $separator;

    // Step
    $wizardStep = $.makeDiv('wizard-step')
      .data('wizard-step', wizardStep)
      .appendTo(this._$wizardSteps);
    wizardStep.$wizardStep = $wizardStep;
    if (wizardStep.enabled && wizardStep.actionEnabled) {
      $wizardStep.addClass('action-enabled');
      $wizardStep.on('click', this._onWizardStepClick.bind(this));
    } else if (!wizardStep.enabled) {
      $wizardStep.addClass('disabled');
    }
    if (scout.strings.hasText(wizardStep.tooltipText)) {
      scout.tooltips.install($wizardStep, this.session, {
        text: wizardStep.tooltipText,
        position: 'bottom'
      });
    }

    // Content
    $content = $wizardStep.appendDiv('wizard-step-content');
    $title = $content.appendDiv('wizard-step-title').textOrNbsp(wizardStep.title);
    if (wizardStep.subTitle) {
      $subTitle = $content.appendDiv('wizard-step-sub-title').textOrNbsp(wizardStep.subTitle);
    }

    // Separator
    if (index < this.wizardSteps.length - 1) {
      $separator = $wizardStep.appendDiv('wizard-step-separator');
    }

    // TODO BSH Wizard | Add icon
  }.bind(this));
};

scout.WizardProgressField.prototype._renderActiveWizardStepIndex = function() {
  var $wizardSteps = this._$wizardSteps.children('.wizard-step');
  this.wizardSteps.forEach(function(wizardStep, index) {
    this._updateWizardStepActiveClasses($wizardSteps.eq(index));
  }.bind(this));
};

scout.WizardProgressField.prototype._updateWizardStepActiveClasses = function($wizardStep){
  $wizardStep.removeClass('all-before-active before-active active all-after-active after-active');
  var wizardStepIndex = this._wizardStepIndex($wizardStep);
  if (wizardStepIndex >= 0 && this.activeWizardStepIndex >= 0) {
    if (wizardStepIndex < this.activeWizardStepIndex) {
      $wizardStep.addClass('all-before-active');
      if (wizardStepIndex === this.activeWizardStepIndex - 1) {
        $wizardStep.addClass('before-active');
      }
    } else if (wizardStepIndex > this.activeWizardStepIndex) {
      $wizardStep.addClass('all-after-active');
      if (wizardStepIndex === this.activeWizardStepIndex + 1) {
        $wizardStep.addClass('after-active');
      }
    } else {
      $wizardStep.addClass('active');
    }
  }
};


scout.WizardProgressField.prototype._wizardStepIndex = function($wizardStep) {
  if ($wizardStep) {
    var wizardStep = $wizardStep.data('wizard-step');
    if (wizardStep) {
      return wizardStep.index;
    }
  }
  return -1;
};

scout.WizardProgressField.prototype._onWizardStepClick = function(event) {
  var $wizardStep = $(event.currentTarget); // currentTarget instead of target to support event bubbling from inner divs
  var targetStepIndex = this._wizardStepIndex($wizardStep);
  if (targetStepIndex >= 0 && targetStepIndex !== this.activeWizardStepIndex) {
    this.session.send(this.id, 'doWizardStepAction', {
      stepIndex: targetStepIndex
    });
  }
};
