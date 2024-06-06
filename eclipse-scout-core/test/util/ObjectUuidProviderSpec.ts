/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {Form, GroupBox, InitModelOf, ObjectUuidProvider, ObjectUuidSource, scout, Widget, WidgetModel} from '../../src';
import {SpecObjectUuidProvider} from '../../src/testing';

describe('ObjectUuidProvider', () => {

  describe('createUiId', () => {
    it('has correct prefix and increases with each call', () => {
      const nextIdSeqNo = SpecObjectUuidProvider.getUniqueIdSeqNo() + 1;
      expect(ObjectUuidProvider.createUiId()).toBe(ObjectUuidProvider.UI_ID_PREFIX + nextIdSeqNo);
      expect(SpecObjectUuidProvider.getUniqueIdSeqNo()).toBe(nextIdSeqNo);
    });
  });

  describe('isUiId', () => {
    it('correctly detects UI IDs', () => {
      expect(ObjectUuidProvider.isUiId(ObjectUuidProvider.UI_ID_PREFIX + '1234')).toBeTrue();
      expect(ObjectUuidProvider.isUiId('_ui_1')).toBeTrue();
      expect(ObjectUuidProvider.isUiId('_ui_0')).toBeTrue();
      expect(ObjectUuidProvider.isUiId('_ui_1234567890')).toBeTrue();

      expect(ObjectUuidProvider.isUiId(ObjectUuidProvider.UI_ID_PREFIX)).toBeFalse();
      expect(ObjectUuidProvider.isUiId('_ui.1234567890')).toBeFalse();
      expect(ObjectUuidProvider.isUiId('ui1234567890')).toBeFalse(); // old style
      expect(ObjectUuidProvider.isUiId('1234567890')).toBeFalse();
      expect(ObjectUuidProvider.isUiId(ObjectUuidProvider.UI_ID_PREFIX + '1234a')).toBeFalse();
      expect(ObjectUuidProvider.isUiId(ObjectUuidProvider.UI_ID_PREFIX + '1234_')).toBeFalse();
      expect(ObjectUuidProvider.isUiId(ObjectUuidProvider.UI_ID_PREFIX + '.1234.')).toBeFalse();
      expect(ObjectUuidProvider.isUiId(ObjectUuidProvider.UI_ID_PREFIX + 'a')).toBeFalse();
      expect(ObjectUuidProvider.isUiId(ObjectUuidProvider.UI_ID_PREFIX + '12.34')).toBeFalse();
    });
  });

  describe('isUuidPathSkipWidget', () => {

    class TestGroupBox extends GroupBox {
    }

    it('only matches exact classes and no instanceof', () => {
      expect(ObjectUuidProvider.isUuidPathSkipWidget(null)).toBeTrue(); // skip null objects
      expect(ObjectUuidProvider.isUuidPathSkipWidget(new GroupBox())).toBeTrue();

      expect(ObjectUuidProvider.isUuidPathSkipWidget(new TestGroupBox())).toBeFalse();
      ObjectUuidProvider.UuidPathSkipWidgets.add(TestGroupBox);
      expect(ObjectUuidProvider.isUuidPathSkipWidget(new TestGroupBox())).toBeTrue();
    });

    afterAll(() => {
      ObjectUuidProvider.UuidPathSkipWidgets.delete(TestGroupBox);
    });
  });

  describe('uuid', () => {

    let session: SandboxSession;
    beforeEach(() => {
      setFixtures(sandbox());
      session = sandboxSession();
    });

    it('uuid uses attributes correctly', () => {
      assertUuid({classId: '1', uuid: '2', id: '3', objectType: '4'}, '1');
      assertUuid({uuid: '2', id: '3', objectType: '4'}, '2');
      assertUuid({id: '3', objectType: '4'}, '3@4');
      assertUuid({id: '3', objectType: '4'}, '3@4', true);
      assertUuid({id: '3'}, '3');
      assertUuid({objectType: '4'}, '4');
      assertUuid({}, null);

      // without fallback:
      assertUuid({id: '3', objectType: '4'}, null, false);
      assertUuid({objectType: '4'}, null, false);
      assertUuid({id: '3'}, null, false);
    });

    it('returns null for UI IDs', () => {
      assertUuid({id: ObjectUuidProvider.createUiId()}, null);
    });

    it('uuid contains static model id for top-level object if dynamic model only has UI id', () => {
      const staticId = 'scout.UuidTestForm';

      class TestForm extends Form {
        override init(model: InitModelOf<this>) {
          expect(ObjectUuidProvider.isUiId(model.id)).toBeTrue(); // ensure dynamic model id is present
          super.init(model);
        }

        protected override _jsonModel(): WidgetModel {
          return {id: staticId}; // also add static model id
        }
      }

      const form = scout.create(TestForm, {parent: session.desktop});
      expect(form.id).toBe(staticId); // ensure static model id wins
      assertUuid(form, staticId);
    });

    it('uuid contains dynamic model id for top-level object if no id is generated', () => {
      const dynamicId = 'scout.UuidTestForm3';

      class TestForm2 extends Form {
        override init(model: InitModelOf<this>) {
          expect(model.id).toBe(dynamicId); // ensure custom dynamic id is present
          super.init(model);
        }

        protected override _jsonModel(): WidgetModel {
          return {id: 'scout.UuidTestForm2'}; // also add static model id
        }
      }

      const form = scout.create(TestForm2, {parent: session.desktop, id: dynamicId});
      expect(form.id).toBe(dynamicId); // ensure static model id wins
      assertUuid(form, dynamicId);
    });

    function assertUuid(object: ObjectUuidSource, expectedUuid: string, useFallback?: boolean) {
      expect(ObjectUuidProvider.get().uuid(object, useFallback)).toBe(expectedUuid);
    }
  });

  describe('uuidPath', () => {

    let session: SandboxSession;
    beforeEach(() => {
      setFixtures(sandbox());
      session = sandboxSession();
    });

    it('uses uuid if no parent present', () => {
      assertUuidPath({uuid: '1'}, '1');
      assertUuidPath({id: '2'}, '2');
      assertUuidPath({classId: '3'}, '3');
    });

    it('returns null if object has no id', () => {
      assertUuidPath({}, null);
    });

    it('uses parent if not ignored', () => {
      session.desktop.id = '1'; // ensure desktop has an id. Should be ignored for uuidPath.
      const parent = scout.create(Widget, {parent: session.desktop, id: '3'});
      const object = {
        uuid: '4',
        parent
      };
      assertUuidPath(object, '4|3@Widget');

      const root = scout.create(Widget, {parent: session.desktop, id: '2'});
      const group = scout.create(GroupBox, {parent: root, uuid: '3' /* must be ignored */});
      const object2 = {
        uuid: '4',
        parent: group
      };
      assertUuidPath(object2, '4|2@Widget');
    });

    it('ignores UI IDs', () => {
      const root = scout.create(Widget, {parent: session.desktop, id: '2'});
      const group = scout.create(Widget, {parent: root, id: ObjectUuidProvider.createUiId() /* is skipped */});
      const object = {
        uuid: '4',
        parent: group
      };
      assertUuidPath(object, '4|2@Widget');
    });

    it('returns null if object has only UI ID', () => {
      const parent = scout.create(Widget, {parent: session.desktop, id: '3'});
      const object = {
        id: ObjectUuidProvider.createUiId(),
        parent
      };
      assertUuidPath(object, null);
    });

    it('works recursively', () => {
      const root = scout.create(Widget, {parent: session.desktop, id: '2'});
      const group = scout.create(Widget, {parent: root, uuid: '3'});
      const object = {
        uuid: '4',
        parent: group
      };
      assertUuidPath(object, '4|3|2@Widget');
    });

    it('stops on classId by default', () => {
      const root = scout.create(Widget, {parent: session.desktop, id: '2' /* ignored because child uses classId which stops the parent visit */});
      const group = scout.create(Widget, {parent: root, classId: '3'});
      const object = {
        classId: '4',
        parent: group
      };
      assertUuidPath(object, '4');
      assertUuidPath(object, '4|3', true, true /* enforce to use parent even if a classId is present */);
    });

    it('skips parent if requested', () => {
      const root = scout.create(Widget, {parent: session.desktop, uuid: '2' /* skipped by request */});
      const group = scout.create(Widget, {parent: root, uuid: '3'});
      assertUuidPath(group, '3', false, false);
    });

    it('ignores parent if classId from remote is used', () => {
      const remoteParent = {
        id: '1',
        classId: '2'
      } as Widget;
      const remoteElement = {
        classId: '3',
        parent: remoteParent // should not be used for classId
      };
      assertUuidPath(remoteElement, '3');
    });

    function assertUuidPath(object: ObjectUuidSource, expectedUuidPath: string, useFallback?: boolean, appendParent?: boolean) {
      expect(ObjectUuidProvider.get().uuidPath(object, {useFallback, appendParent})).toBe(expectedUuidPath);
    }
  });
});
