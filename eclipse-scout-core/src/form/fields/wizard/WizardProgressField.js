/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Device, Form, FormField, GroupBox, icons, inspector, scout, scrollbars, strings, tooltips, WizardProgressFieldLayout} from '../../../index';
import $ from 'jquery';

export default class WizardProgressField extends FormField {

  constructor() {
    super();

    this.activeStepIndex = -1;
    this.steps = [];

    // Used to determine direction of transition ("going backward" or "going forward")
    this.previousActiveStepIndex = -1;

    // Helper map to find a step by step index. The step index does not necessarily correspond to the
    // array index, because invisible model steps can produce "holes" in the sequence of indices.
    this.stepsMap = {};
    this.$wizardStepsBody = null;
    this.keepActiveStepAtLeftBorder = Device.get().type === Device.Type.MOBILE;
    this.animateScrolling = Device.get().type === Device.Type.MOBILE;
  }

  _init(model) {
    super._init(model);
    this._updateStepsMap();
  }

  _render() {
    this.addContainer(this.$parent, 'wizard-progress-field', new WizardProgressFieldLayout(this));
    this.addField(this.$parent.makeDiv('wizard-steps'));
    this.addStatus();
    this.addLabel();

    // Add compact class on mobile. It is not based on width because height will be smaller too which is not desired on desktop or tablet
    // (field would not be correctly aligned with other components anymore)
    this.$field.toggleClass('compact', Device.get().type === Device.Type.MOBILE);
    this.htmlComp.layout.compactFieldWidth = -1; // disable compact toggling

    this.$wizardStepsBody = this.$field.appendDiv('wizard-steps-body');

    this._installScrollbars({
      axis: 'x',
      scrollShadow: 'none'
    });

    // If this field is the first field in a form's main box, mark the form as "wizard-container-form"
    if (this.parent instanceof GroupBox && this.parent.controls[0] === this && this.parent.parent instanceof Form) {
      let form = this.parent.parent;
      form.$container.addClass('wizard-container-form');
    }
  }

  _renderProperties() {
    super._renderProperties();
    this._renderSteps();
    this._renderActiveStepIndex();
  }

  _setSteps(steps) {
    this._setProperty('steps', steps);
    this._updateStepsMap();
  }

  _renderSteps() {
    this.$wizardStepsBody.children('.wizard-step').each(function() {
      // Tooltips are only uninstalled if user clicked outside container. However, the steps
      // may be updated by clicking inside the container. Therefore, manually make sure all
      // tooltips are uninstalled before destroying the DOM elements.
      tooltips.uninstall($(this));
    });
    this.$wizardStepsBody.empty();

    this.steps.forEach((step, index) => {
      // Step
      let $step = this.$wizardStepsBody
        .appendDiv('wizard-step')
        .addClass(step.cssClass)
        .data('wizard-step', step);
      step.$step = $step;
      this._updateStepClasses(step);

      // Inspector info
      if (this.session.inspector) {
        inspector.applyInfo(step, $step);
        $step.attr('data-step-index', step.index);
      }

      if (strings.hasText(step.tooltipText)) {
        tooltips.install($step, {
          parent: this,
          text: step.tooltipText,
          tooltipPosition: 'bottom'
        });
      }

      // Icon
      let $icon = $step.appendDiv('icon');
      if (step.iconId) {
        $icon.icon(step.iconId);
      } else if (step.finished) {
        $icon.icon(icons.CHECKED_BOLD);
      } else {
        $icon.text(index + 1);
      }
      // Text
      let $text = $step.appendDiv('text');
      $text.appendDiv('title').textOrNbsp(step.title).attr('data-text', step.title);
      if (step.subTitle) {
        $text.appendDiv('sub-title').textOrNbsp(step.subTitle);
      }

      // Separator
      if (index < this.steps.length - 1) {
        this.$wizardStepsBody
          .appendDiv('wizard-step-separator');
      }
    });

    this.invalidateLayoutTree(false);
  }

  _setActiveStepIndex(activeStepIndex) {
    this.previousActiveStepIndex = this.activeStepIndex;
    // Ensure this.activeStepIndex always has a value. If the server has no active step set (may
    // happen during transition between steps), we use -1 as dummy value
    this._setProperty('activeStepIndex', scout.nvl(activeStepIndex, -1));
  }

  _renderActiveStepIndex() {
    this.steps.forEach(this._updateStepClasses.bind(this));
    this.invalidateLayoutTree(false);
  }

  _updateStepClasses(step) {
    let $step = step.$step;
    $step.removeClass('selected first last action-enabled disabled');
    $step.off('click.selected');

    // Important: those indices correspond to the UI's data structures (this.steps) and are not necessarily
    // consistent with the server indices (because the server does not send invisible steps).
    let stepIndex = this.steps.indexOf(step);
    let activeStepIndex = this.steps.indexOf(this.stepsMap[this.activeStepIndex]);

    if (this.enabledComputed && step.enabled && step.actionEnabled && stepIndex !== this.activeStepIndex) {
      $step.addClass('action-enabled');
      $step.on('click.selected', this._onStepClick.bind(this));
    } else if (!this.enabledComputed || !step.enabled) {
      $step.addClass('disabled');
    }
    $step.toggleClass('finished', step.finished);

    if (stepIndex >= 0 && activeStepIndex >= 0) {
      // Active
      if (stepIndex === activeStepIndex) {
        $step.addClass('selected');
      }
      // First / last
      if (stepIndex === 0) {
        $step.addClass('first');
      }
      if (stepIndex === this.steps.length - 1) {
        $step.addClass('last');
      }
    }

    // update background color for this.$wizardStepsBody, use same as for last step (otherwise there might be white space after last step)
    if (stepIndex === this.steps.length - 1) {
      this.$wizardStepsBody.css('background-color', $step.css('background-color'));
    }
  }

  _stepIndex($step) {
    if ($step) {
      let step = $step.data('wizard-step');
      if (step) {
        return step.index;
      }
    }
    return -1;
  }

  _updateStepsMap() {
    this.stepsMap = {};
    this.steps.forEach(step => {
      this.stepsMap[step.index] = step;
    });
  }

  _resolveStep(stepIndex) {
    // Because "step index" does not necessarily correspond to the array indices
    // (invisible model steps produce "holes"), we have to loop over the array.
    for (let i = 0; i < this.steps.length; i++) {
      let step = this.steps[i];
      if (step.index === stepIndex) {
        return step;
      }
    }
    return null;
  }

  _onStepClick(event) {
    let $step = $(event.currentTarget); // currentTarget instead of target to support event bubbling from inner divs
    let targetStepIndex = this._stepIndex($step);
    if (targetStepIndex >= 0 && targetStepIndex !== this.activeStepIndex) {
      this.trigger('stepAction', {
        stepIndex: targetStepIndex
      });
    }
  }

  scrollToActiveStep() {
    let currentStep = this.stepsMap[this.activeStepIndex];
    if (!currentStep) {
      return;
    }
    let $currentStep = currentStep.$step;
    let currentStepLeft = $currentStep.position().left;
    let animate = this.animateScrolling && this.htmlComp.layouted;
    if (this.keepActiveStepAtLeftBorder) {
      let $firstStep = this.steps[0].$step;
      let scrollLeft = currentStepLeft - $firstStep.cssPaddingLeft() + $currentStep.cssPaddingLeft();
      scrollbars.scrollLeft(this.$field, scrollLeft, {animate: animate});
    } else {
      let scrollLeft = this.$field.scrollLeft();
      let currentStepWidth = $currentStep.width();
      let fieldWidth = this.$field.width();

      // If going forward, try to scroll the steps such that the center of active step is not after 75% of the available space.
      // If going backward, try to scroll the steps such that the center of the active step is not before 25% of the available space.
      let goingBack = (this.previousActiveStepIndex > this.activeStepIndex);
      let p1 = scrollLeft + Math.floor(fieldWidth * (goingBack ? 0.25 : 0.75));
      let p2 = currentStepLeft + Math.floor(currentStepWidth / 2);
      if ((goingBack && p2 < p1) || (!goingBack && p2 > p1)) {
        scrollbars.scrollLeft(this.$field, scrollLeft + (p2 - p1), {animate: animate});
      }
    }
  }
}
