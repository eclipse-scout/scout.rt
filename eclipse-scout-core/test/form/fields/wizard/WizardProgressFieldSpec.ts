/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {scout, WizardProgressField} from '../../../../src/index';

describe('WizardProgressField', () => {

  let session, field;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    field = new WizardProgressField();
    field.session = session;
  });

  // #241222
  describe('rendering', () => {

    it('must evaluate activeStepIndex for action-enabled class', () => {
      field = scout.create(WizardProgressField, {
        parent: session.desktop
      });
      spyOn(field, '_onStepClick').and.callThrough();

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
      expect(field._onStepClick.calls.first().args[0].currentTarget).toBe($steps[1]);

      field.setProperty('activeStepIndex', 1);
      expect($steps.eq(0).hasClass('action-enabled')).toBe(true);
      expect($steps.eq(1).hasClass('action-enabled')).toBe(false);

      field._onStepClick.calls.reset();
      $steps.eq(0).click();
      $steps.eq(1).click();
      expect(field._onStepClick).toHaveBeenCalledTimes(1);
      expect(field._onStepClick.calls.first().args[0].currentTarget).toBe($steps[0]);
    });

  });

});
