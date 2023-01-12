/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {DateField, scout, SequenceBox, SequenceBoxAdapter, StringField} from '../../../../src/index';
import {CloneSpecHelper, FormSpecHelper, MenuSpecHelper} from '../../../../src/testing/index';

describe('SequenceBoxAdapter', () => {
  let session: SandboxSession, helper: FormSpecHelper, menuHelper: MenuSpecHelper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new FormSpecHelper(session);
    menuHelper = new MenuSpecHelper(session);
  });

  describe('clone', () => {
    it('does not accidentally create a logical grid', () => {
      let cloneHelper = new CloneSpecHelper();
      let model = {
        id: 'seq01',
        parent: session.desktop,
        session: session,
        objectType: 'SequenceBox',
        modelClass: 'asdf',
        classId: 'bbb',
        fields: [{
          objectType: StringField,
          gridData: {
            useUiWidth: true
          }
        }, {
          objectType: DateField
        }]
      };
      let adapter = scout.create(SequenceBoxAdapter, $.extend({}, model));
      let seqBox = adapter.createWidget(model, session.desktop) as SequenceBox;
      linkWidgetAndAdapter(seqBox.fields[0], 'StringFieldAdapter');
      linkWidgetAndAdapter(seqBox.fields[1], 'DateFieldAdapter');
      let clone = seqBox.clone({
        parent: seqBox.parent
      });

      cloneHelper.validateClone(seqBox, clone);

      // Assert that clone does not have a logical grid, otherwise gridDataHints would overwrite gridData sent by server
      expect(clone.logicalGrid).toBe(null);
      expect(clone.fields[0].gridDataHints.useUiWidth).toBe(false);
      expect(clone.fields[0].gridData.useUiWidth).toBe(true);
    });
  });

});
