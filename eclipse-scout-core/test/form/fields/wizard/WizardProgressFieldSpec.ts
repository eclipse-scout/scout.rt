/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {scout, WizardProgressField} from '../../../../src/index';

describe('WizardProgressField', () => {

  let session: SandboxSession, field: SpecWizardProgressField;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    field = new SpecWizardProgressField();
    field.session = session;
  });

  class SpecWizardProgressField extends WizardProgressField {
    override _onStepClick(event: JQuery.ClickEvent) {
      super._onStepClick(event);
    }
  }

  // #241222
  describe('rendering', () => {

    it('must evaluate activeStepIndex for action-enabled class', () => {
      field = scout.create(SpecWizardProgressField, {
        parent: session.desktop
      });
      let _onStepClickSpy = spyOn(field, '_onStepClick').and.callThrough();

      field.render();
      field.setProperty('activeStepIndex', 1);
      field.setProperty('steps', [{
        'index': 0,
        'enabled': true,
        'actionEnabled': true
      }, {
        'index': 1,
        'enabled': true,
        'actionEnabled': true
      }]);

      let $steps = field.$container.find('.wizard-step');
      expect($steps.length).toBe(2);
      expect($steps.eq(0).hasClass('action-enabled')).toBe(true);
      expect($steps.eq(1).hasClass('action-enabled')).toBe(false);

      field.setProperty('activeStepIndex', 0);
      expect($steps.eq(0).hasClass('action-enabled')).toBe(false);
      expect($steps.eq(1).hasClass('action-enabled')).toBe(true);

      $steps.eq(0).click();
      $steps.eq(1).click();
      expect(field._onStepClick).toHaveBeenCalledTimes(1);
      expect(_onStepClickSpy.calls.first().args[0].currentTarget).toBe($steps[1]);

      field.setProperty('activeStepIndex', 1);
      expect($steps.eq(0).hasClass('action-enabled')).toBe(true);
      expect($steps.eq(1).hasClass('action-enabled')).toBe(false);

      _onStepClickSpy.calls.reset();
      $steps.eq(0).click();
      $steps.eq(1).click();
      expect(field._onStepClick).toHaveBeenCalledTimes(1);
      expect(_onStepClickSpy.calls.first().args[0].currentTarget).toBe($steps[0]);
    });

  });

  describe('aria properties', () => {

    it('has a non empty status container that represent current progress', () => {
      field = scout.create(SpecWizardProgressField, {
        parent: session.desktop
      });
      field.setProperty('activeStepIndex', 1);
      field.setProperty('steps', [{
        'index': 0,
        'enabled': true,
        'actionEnabled': true
      }, {
        'index': 1,
        'enabled': true,
        'actionEnabled': true
      }]);
      field.render();
      expect(field.$screenReaderStatus).toBeDefined();
      expect(field.$screenReaderStatus).toHaveAttr('role', 'status');
      expect(field.$screenReaderStatus).toHaveClass('sr-only');
      expect(field.$screenReaderStatus.children('.sr-step-description').length).toBe(2);
      expect(field.$screenReaderStatus.children('.sr-step-description').eq(0)).not.toBeEmpty();
      expect(field.$screenReaderStatus.children('.sr-step-description').eq(1)).not.toBeEmpty();
    });
  });
});
