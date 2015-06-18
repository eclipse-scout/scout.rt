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
  this._renderActiveWizardStepIndex(this.activeWizardStepIndex);
};

scout.WizardProgressField.prototype._renderWizardSteps = function(wizardSteps) {
  this._$stepList.empty();
  wizardSteps.forEach(function(wizardStep, index) {
    var $wizardStep, $content, $title, $subTitle, $separator;

    // Step
    $wizardStep = $.makeDiv('wizard-step')
      .attr('data-index', wizardStep.index)
      .appendTo(this._$stepList);
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
    if (index < wizardSteps.length - 1) {
      $separator = $wizardStep.appendDiv('wizard-step-separator');
    }

    // TODO BSH Wizard | Add icon

    this._updateWizardStepActiveClasses($wizardStep, this.activeWizardStep);
  }.bind(this));
};

scout.WizardProgressField.prototype._renderActiveWizardStepIndex = function(activeWizardStepIndex) {
  var $wizardSteps = this._$stepList.children('.wizard-step');
  this.wizardSteps.forEach(function(wizardStep, index) {
    this._updateWizardStepActiveClasses($wizardSteps.eq(index), activeWizardStepIndex);
  }.bind(this));
};

scout.WizardProgressField.prototype._updateWizardStepActiveClasses = function($wizardStep, activeWizardStepIndex){
  $wizardStep.removeClass('all-before-active before-active active all-after-active after-active');
  var wizardStepIndex = this._wizardStepIndex($wizardStep);
  if (activeWizardStepIndex >= 0 && wizardStepIndex >= 0) {
    if (wizardStepIndex < activeWizardStepIndex) {
      $wizardStep.addClass('all-before-active');
      if (wizardStepIndex === activeWizardStepIndex - 1) {
        $wizardStep.addClass('before-active');
      }
    } else if (wizardStepIndex > activeWizardStepIndex) {
      $wizardStep.addClass('all-after-active');
      if (wizardStepIndex === activeWizardStepIndex + 1) {
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
