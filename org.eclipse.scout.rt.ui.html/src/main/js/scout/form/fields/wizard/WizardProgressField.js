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

  // Used to determine direction of transition ("going backward" or "going forward")
  this.previousActiveStepIndex;
};
scout.inherits(scout.WizardProgressField, scout.FormField);

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

scout.WizardProgressField.prototype._renderSteps = function() {
  this.$wizardStepsBody.empty();

  this.steps.forEach(function(step, index) {
    var $step, $content;

    // Step
    $step = this.$wizardStepsBody
      .appendDiv('wizard-step')
      .data('wizard-step', step);
    step.$step = $step;
    if (step.enabled && step.actionEnabled) {
      $step.addClass('action-enabled');
      $step.on('click', this._onStepClick.bind(this));
    } else if (!step.enabled) {
      $step.addClass('disabled');
    }
    if (scout.strings.hasText(step.tooltipText)) {
      scout.tooltips.install($step, {
        parent: this,
        text: step.tooltipText,
        position: 'bottom'
      });
    }
    if (index === 0) {
      $step.addClass('first');
    }
    if (index === this.steps.length - 1) {
      $step.addClass('last');
    }
    this._updateStepClasses($step);

    scout.inspector.applyInfo(step, $step);

    // Content
    $content = $step.appendDiv('wizard-step-content');
    $content.appendDiv('wizard-step-title').textOrNbsp(step.title);
    if (step.subTitle) {
      $content.appendDiv('wizard-step-sub-title').textOrNbsp(step.subTitle);
    }

    // Separator
    if (index < this.steps.length - 1) {
      $step.appendDiv('wizard-step-separator');
    }

    // TODO BSH Wizard | Add icon
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
    this._updateStepClasses(step.$step);
  }.bind(this));

  // update background color for this.$wizardStepsBody, use same as for last step (otherwise there might be white space after last step)
  if (this.steps.length > 0) {
    this.$wizardStepsBody.css('background-color', this.steps[this.steps.length - 1].$step.css('background-color'));
  }

  this.invalidateLayoutTree(false);
};

scout.WizardProgressField.prototype._updateStepClasses = function($step) {
  $step.removeClass('active before-active after-active left-of-active right-of-active');
  var stepIndex = this._stepIndex($step);
  if (stepIndex >= 0 && this.activeStepIndex >= 0) {
    if (stepIndex < this.activeStepIndex) {
      $step.addClass('before-active');
      if (stepIndex === this.activeStepIndex - 1) {
        $step.addClass('left-of-active');
      }
    } else if (stepIndex > this.activeStepIndex) {
      $step.addClass('after-active');
      if (stepIndex === this.activeStepIndex + 1) {
        $step.addClass('right-of-active');
      }
    } else {
      $step.addClass('active');
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
  var currentStep = this.steps[this.activeStepIndex];
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
