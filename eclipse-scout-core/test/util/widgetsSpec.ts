/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {scout, StringField, widgets} from '../../src';

describe('widgets', () => {
  let session: SandboxSession;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  describe('preserveAndSetProperty', () => {
    it('preserves and sets the property that can be reset using resetProperty', () => {
      let field = scout.create(StringField, {parent: session.desktop});
      expect(field.updateDisplayTextOnModifyDelay).toBe(250);
      expect(field['__spec.updateDisplayTextOnModifyDelay']).toBeUndefined();

      widgets.preserveAndSetProperty(() => field.setUpdateDisplayTextOnModifyDelay(500), () => field.updateDisplayTextOnModifyDelay, field, '__spec.updateDisplayTextOnModifyDelay');
      expect(field.updateDisplayTextOnModifyDelay).toBe(500);
      expect(field['__spec.updateDisplayTextOnModifyDelay']).toBe(250);

      field.setUpdateDisplayTextOnModifyDelay(800);
      expect(field.updateDisplayTextOnModifyDelay).toBe(800);
      expect(field['__spec.updateDisplayTextOnModifyDelay']).toBe(250);

      widgets.resetProperty((preservedValue: number) => field.setUpdateDisplayTextOnModifyDelay(preservedValue), field, '__spec.updateDisplayTextOnModifyDelay');
      expect(field.updateDisplayTextOnModifyDelay).toBe(250);
      expect(field['__spec.updateDisplayTextOnModifyDelay']).toBeUndefined();
    });

    it('can preserve null', () => {
      let field = scout.create(StringField, {parent: session.desktop});
      expect(field.value).toBe(null);
      expect(field['__spec.value']).toBeUndefined();

      widgets.preserveAndSetProperty(() => field.setValue('abc'), () => field.value, field, '__spec.value');
      expect(field.value).toBe('abc');
      expect(field['__spec.value']).toBe(null);

      field.setValue('zzz');
      expect(field.value).toBe('zzz');
      expect(field['__spec.value']).toBe(null);

      widgets.resetProperty((preservedValue: string) => field.setValue(preservedValue), field, '__spec.value');
      expect(field.value).toBe(null);
      expect(field['__spec.value']).toBeUndefined();
    });
  });
});
