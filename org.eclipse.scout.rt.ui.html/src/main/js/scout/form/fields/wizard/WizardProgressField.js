/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.WizardProgressField = function() {
  scout.WizardProgressField.parent.call(this);

  this.steps = [];

  // Used to determine direction of transition ("going backward" or "going forward")
  this.previousActiveStepIndex;

  // Helper map to find a step by step index. The step index does not necessarily correspond to the
  // array index, because invisible model steps can produce "holes" in the sequence of indices.
  this.stepsMap = {};
};
scout.inherits(scout.WizardProgressField, scout.FormField);


scout.WizardProgressField.prototype._init = function(model) {
  scout.WizardProgressField.parent.prototype._init.call(this, model);
  this._updateStepsMap();
};

scout.WizardProgressField.prototype._render = function($parent) {
  this.addContainer($parent, 'wizard-progress-field', new scout.WizardProgressFieldLayout(this));
  this.addField($parent.makeDiv('wizard-steps'));

  this.$wizardStepsBody = this.$field.appendDiv('wizard-steps-body');

  scout.scrollbars.install(this.$field, {
    parent: this,
    axis: 'x'
  });

  // If this field is the first field in a form's main box, mark the form as "wizard-container-form"
  if (this.parent instanceof scout.GroupBox && this.parent.controls[0] === this && this.parent.parent instanceof scout.Form) {
    var form = this.parent.parent;
    form.$container.addClass('wizard-container-form');
  }
};

scout.WizardProgressField.prototype._renderProperties = function() {
  scout.WizardProgressField.parent.prototype._renderProperties.call(this);
  this._renderSteps();
  this._renderActiveStepIndex();
};

scout.WizardProgressField.prototype._syncSteps = function(steps) {
  this.steps = steps;
  this._updateStepsMap();
};

scout.WizardProgressField.prototype._renderSteps = function() {
  this.$wizardStepsBody.empty();

  this.steps.forEach(function(step, index) {
    // Step
    var $step = this.$wizardStepsBody
      .appendDiv('wizard-step')
      .addClass(step.cssClass)
      .data('wizard-step', step);
    step.$step = $step;
    if (this.enabled && step.enabled && step.actionEnabled) {
      $step.addClass('action-enabled');
      $step.on('click', this._onStepClick.bind(this));
    } else if (!this.enabled || !step.enabled) {
      $step.addClass('disabled');
    }
    if (scout.strings.hasText(step.tooltipText)) {
      scout.tooltips.install($step, {
        parent: this,
        text: step.tooltipText,
        position: 'bottom'
      });
    }
    this._updateStepClasses(step);

    // Inspector info
    scout.inspector.applyInfo(step, $step);
    if (step.classId) { // true if inspector hints are enabled
      $step.attr('data-step-index', step.index);
    }

    // Content
    var $content = $step.appendDiv('wizard-step-content');
    // Icon
    if (step.iconId) {
      var $icon = $content.appendDiv('wizard-step-content-icon');
      $icon.icon(step.iconId);
    }
    // Text
    var $text = $content.appendDiv('wizard-step-content-text');
    $text.appendDiv('wizard-step-title').textOrNbsp(step.title);
    if (step.subTitle) {
      $text.appendDiv('wizard-step-sub-title').textOrNbsp(step.subTitle);
    }

    // Separator
    if (index < this.steps.length - 1) {
      $step.appendDiv('wizard-step-separator');
    }
  }.bind(this));

  this.invalidateLayoutTree(false);
};

scout.WizardProgressField.prototype._syncActiveStepIndex = function(activeStepIndex) {
  this.previousActiveStepIndex = this.activeStepIndex;
  // Ensure this.activeStepIndex always has a value. If the server has no active step set (may
  // happen during transition between steps), we use -1 as dummy value
  this.activeStepIndex = scout.nvl(activeStepIndex, -1);
};

scout.WizardProgressField.prototype._renderActiveStepIndex = function() {
  this.steps.forEach(function(step) {
    this._updateStepClasses(step);
  }.bind(this));

  // update background color for this.$wizardStepsBody, use same as for last step (otherwise there might be white space after last step)
  if (this.steps.length > 0) {
    this.$wizardStepsBody.css('background-color', this.steps[this.steps.length - 1].$step.css('background-color'));
  }

  this.invalidateLayoutTree(false);
};

scout.WizardProgressField.prototype._updateStepClasses = function(step) {
  var $step = step.$step;
  $step.removeClass('active before-active after-active left-of-active right-of-active first last');

  // Important: those indices correspond to the UI's data structures (this.steps) and are not necessarily
  // consistent with the server indices (because the server does not send invisible steps).
  var stepIndex = this.steps.indexOf(step);
  var activeStepIndex = this.steps.indexOf(this.stepsMap[this.activeStepIndex]);

  if (stepIndex >= 0 && activeStepIndex >= 0) {
    // Active
    if (stepIndex < activeStepIndex) {
      $step.addClass('before-active');
      if (stepIndex === activeStepIndex - 1) {
        $step.addClass('left-of-active');
      }
    } else if (stepIndex > activeStepIndex) {
      $step.addClass('after-active');
      if (stepIndex === activeStepIndex + 1) {
        $step.addClass('right-of-active');
      }
    } else {
      $step.addClass('active');
    }
    // First / last
    if (stepIndex === 0) {
      $step.addClass('first');
    }
    if (stepIndex === this.steps.length - 1) {
      $step.addClass('last');
    }
  }
};

scout.WizardProgressField.prototype._stepIndex = function($step) {
  if ($step) {
    var step = $step.data('wizard-step');
    if (step) {
      return step.index;
    }
  }
  return -1;
};

scout.WizardProgressField.prototype._updateStepsMap = function() {
  this.stepsMap = {};
  this.steps.forEach(function(step) {
    this.stepsMap[step.index] = step;
  }.bind(this));
};

scout.WizardProgressField.prototype._resolveStep = function(stepIndex) {
  // Because "step index" does not necessarily correspond to the array indices
  // (invisible model steps produce "holes"), we have to loop over the array.
  for (var i = 0; i < this.steps.length; i++) {
    var step = this.steps[i];
    if (step.index === stepIndex) {
      return step;
    }
  }
  return null;
};

scout.WizardProgressField.prototype._onStepClick = function(event) {
  var $step = $(event.currentTarget); // currentTarget instead of target to support event bubbling from inner divs
  var targetStepIndex = this._stepIndex($step);
  if (targetStepIndex >= 0 && targetStepIndex !== this.activeStepIndex) {
    this._send('doStepAction', {
      stepIndex: targetStepIndex
    });
  }
};

scout.WizardProgressField.prototype.scrollToActiveStep = function() {
  var currentStep = this.stepsMap[this.activeStepIndex];
  if (currentStep) {
    var $currentStep = currentStep.$step;
    var scrollLeft = this.$field.scrollLeft();
    var currentStepLeft = $currentStep.position().left;
    var currentStepWidth = $currentStep.width();
    var fieldWidth = this.$field.width();

    // If going forward, try to scroll the steps such that the center of active step is not after 75% of the available space.
    // If going backward, try to scroll the steps such that the center of the active step is not before 25% of the available space.
    var goingBack = (this.previousActiveStepIndex > this.activeStepIndex);
    var p1 = scrollLeft + Math.floor(fieldWidth * (goingBack ? 0.25 : 0.75));
    var p2 = currentStepLeft + Math.floor(currentStepWidth / 2);
    if ((goingBack && p2 < p1) || (!goingBack && p2 > p1)) {
      scout.scrollbars.scrollLeft(this.$field, scrollLeft + (p2 - p1));
    }
  }
};
