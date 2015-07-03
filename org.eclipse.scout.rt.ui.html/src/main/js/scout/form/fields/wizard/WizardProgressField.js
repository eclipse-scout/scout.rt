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
};

scout.WizardProgressField.prototype._renderProperties = function() {
  scout.WizardProgressField.parent.prototype._renderProperties.call(this);
  this._renderWizardSteps();
  this._renderActiveWizardStepIndex();
};

scout.WizardProgressField.prototype._renderWizardSteps = function() {
  this._$wizardSteps.empty();
  this.wizardSteps.forEach(function(wizardStep, index) {
    var $wizardStep, $content, $title, $subTitle, $separator;

    // Step
    $wizardStep = $.makeDiv('wizard-step')
      .attr('data-index', wizardStep.index)
      .appendTo(this._$wizardSteps);
    if (wizardStep.enabled) {
      $wizardStep.on('click', this._onWizardStepClick.bind(this));
    } else {
      $wizardStep.addClass('disabled');
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
  if (this.activeWizardStepIndex >= 0 && wizardStepIndex >= 0) {
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

scout.WizardProgressField.prototype._onWizardStepClick = function(event) {
  var $wizardStep = $(event.currentTarget); // currentTarget instead of target to support event bubbling from inner divs
  var targetStepIndex = this._wizardStepIndex($wizardStep);
  if (targetStepIndex !== this.activeWizardStepIndex) {
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
