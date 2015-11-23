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
  this.previousActiveWizardStepIndex;
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
  if (this.parent instanceof scout.GroupBox &&
      this.parent.controls[0] === this &&
      this.parent.parent instanceof scout.Form) {
    var form = this.parent.parent;
    form.$container.addClass('wizard-container-form');
  }
};

scout.WizardProgressField.prototype._renderProperties = function() {
  scout.WizardProgressField.parent.prototype._renderProperties.call(this);
  this._renderWizardSteps();
  this._renderActiveWizardStepIndex();
};

scout.WizardProgressField.prototype._renderWizardSteps = function() {
  this.$wizardStepsBody.empty();

  this.wizardSteps.forEach(function(wizardStep, index) {
    var $wizardStep, $content, $title, $subTitle, $separator;

    // Step
    $wizardStep = this.$wizardStepsBody
      .appendDiv('wizard-step')
      .data('wizard-step', wizardStep);
    wizardStep.$wizardStep = $wizardStep;
    if (wizardStep.enabled && wizardStep.actionEnabled) {
      $wizardStep.addClass('action-enabled');
      $wizardStep.on('click', this._onWizardStepClick.bind(this));
    } else if (!wizardStep.enabled) {
      $wizardStep.addClass('disabled');
    }
    if (scout.strings.hasText(wizardStep.tooltipText)) {
      scout.tooltips.install($wizardStep, {
        parent: this,
        text: wizardStep.tooltipText,
        position: 'bottom'
      });
    }
    if (index === 0) {
      $wizardStep.addClass('first');
    }
    if (index === this.wizardSteps.length - 1) {
      $wizardStep.addClass('last');
    }
    this._updateWizardStepActiveClasses($wizardStep);

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

  this.invalidateLayoutTree(false);
};

scout.WizardProgressField.prototype._syncActiveWizardStepIndex = function(activeWizardStepIndex) {
  this.previousActiveWizardStepIndex = this.activeWizardStepIndex;
  // Ensure this.activeWizardStepIndex always has a value. If the server has no active step set (may
  // happen during transition between steps), we use -1 as dummy value
  this.activeWizardStepIndex = scout.helpers.nvl(activeWizardStepIndex, -1);
};

scout.WizardProgressField.prototype._renderActiveWizardStepIndex = function() {
  var $wizardSteps = this.$wizardStepsBody.children('.wizard-step');
  this.wizardSteps.forEach(function(wizardStep, index) {
    this._updateWizardStepActiveClasses($wizardSteps.eq(index));
  }.bind(this));

  // update background color for this.$wizardStepsBody, use same as for last step (otherwise there might be white space after last step)
  this.$wizardStepsBody.css('background-color', $wizardSteps.eq(this.wizardSteps.length - 1).css('background-color'));

  this.invalidateLayoutTree(false);
};

scout.WizardProgressField.prototype._updateWizardStepActiveClasses = function($wizardStep) {
  $wizardStep.removeClass('current before-current after-current left-of-current right-of-current');
  var wizardStepIndex = this._wizardStepIndex($wizardStep);
  if (wizardStepIndex >= 0 && this.activeWizardStepIndex >= 0) {
    if (wizardStepIndex < this.activeWizardStepIndex) {
      $wizardStep.addClass('before-current');
      if (wizardStepIndex === this.activeWizardStepIndex - 1) {
        $wizardStep.addClass('left-of-current');
      }
    } else if (wizardStepIndex > this.activeWizardStepIndex) {
      $wizardStep.addClass('after-current');
      if (wizardStepIndex === this.activeWizardStepIndex + 1) {
        $wizardStep.addClass('right-of-current');
      }
    } else {
      $wizardStep.addClass('current');
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
    this._send('doWizardStepAction', {
      stepIndex: targetStepIndex
    });
  }
};

scout.WizardProgressField.prototype.scrollToActiveStep = function() {
  var currentStep = this.wizardSteps[this.activeWizardStepIndex];
  if (currentStep) {
    var $currentStep = currentStep.$wizardStep;
    var scrollLeft = this.$field.scrollLeft();
    var currentStepLeft = $currentStep.position().left;
    var currentStepWidth = $currentStep.width();
    var fieldWidth = this.$field.width();

    // If going forward, try to scroll the steps such that the center of active step is not after 75% of the available space.
    // If going backward, try to scroll the steps such that the center of the active step is not before 25% of the available space.
    var goingBack = (this.previousActiveWizardStepIndex > this.activeWizardStepIndex);
    var p1 = scrollLeft + Math.floor(fieldWidth * (goingBack ? 0.25 : 0.75));
    var p2 = currentStepLeft + Math.floor(currentStepWidth / 2);
    if ((goingBack && p2 < p1) || (!goingBack && p2 > p1)) {
      scout.scrollbars.scrollLeft(this.$field, scrollLeft + (p2 - p1));
    }
  }
};
