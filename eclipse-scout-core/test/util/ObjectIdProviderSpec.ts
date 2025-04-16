/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {arrays, Button, GroupBox, ObjectIdProvider, ObjectUuidSource, Outline, scout, Widget, WidgetModel} from '../../src';

describe('ObjectIdProvider', () => {
  let session: SandboxSession;
  let uuidProvider: SpecObjectIdProvider;

  class SpecObjectIdProvider extends ObjectIdProvider {
    override _skipParent(obj: Widget, considerSkipWidgets = true): boolean {
      return super._skipParent(obj, considerSkipWidgets);
    }
  }

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    uuidProvider = new SpecObjectIdProvider();
  });

  describe('createUiId', () => {
    it('has correct prefix and increases with each call', () => {
      const nextIdSeqNo = uuidProvider.uiSeqNo + 1;
      expect(uuidProvider.createUiSeqId()).toBe(ObjectIdProvider.UI_SEQ_ID_PREFIX + nextIdSeqNo);
      expect(uuidProvider.uiSeqNo).toBe(nextIdSeqNo);
    });
  });

  describe('isUiId', () => {
    it('correctly detects UI IDs', () => {
      expect(uuidProvider.isUiSeqId(ObjectIdProvider.UI_SEQ_ID_PREFIX + '1234')).toBeTrue();
      expect(uuidProvider.isUiSeqId('_ui_1')).toBeTrue();
      expect(uuidProvider.isUiSeqId('_ui_0')).toBeTrue();
      expect(uuidProvider.isUiSeqId('_ui_1234567890')).toBeTrue();

      expect(uuidProvider.isUiSeqId(ObjectIdProvider.UI_SEQ_ID_PREFIX)).toBeFalse();
      expect(uuidProvider.isUiSeqId('_ui.1234567890')).toBeFalse();
      expect(uuidProvider.isUiSeqId('ui1234567890')).toBeFalse(); // old style
      expect(uuidProvider.isUiSeqId('1234567890')).toBeFalse();
      expect(uuidProvider.isUiSeqId(ObjectIdProvider.UI_SEQ_ID_PREFIX + '1234a')).toBeFalse();
      expect(uuidProvider.isUiSeqId(ObjectIdProvider.UI_SEQ_ID_PREFIX + '1234_')).toBeFalse();
      expect(uuidProvider.isUiSeqId(ObjectIdProvider.UI_SEQ_ID_PREFIX + '.1234.')).toBeFalse();
      expect(uuidProvider.isUiSeqId(ObjectIdProvider.UI_SEQ_ID_PREFIX + 'a')).toBeFalse();
      expect(uuidProvider.isUiSeqId(ObjectIdProvider.UI_SEQ_ID_PREFIX + '12.34')).toBeFalse();
    });
  });

  describe('_skipParent', () => {

    class TestGroupBox extends GroupBox {
    }

    describe('uuidPathSkipWidgets', () => {
      it('can contain classes that must match exactly and no instanceof', () => {
        expect(uuidProvider._skipParent(null)).toBeTrue(); // skip null objects
        expect(uuidProvider._skipParent(new GroupBox())).toBeTrue();

        expect(uuidProvider._skipParent(new TestGroupBox())).toBeFalse();
        ObjectIdProvider.uuidPathSkipWidgets.add(TestGroupBox);
        expect(uuidProvider._skipParent(new TestGroupBox())).toBeTrue();
      });

      afterEach(() => {
        ObjectIdProvider.uuidPathSkipWidgets.delete(TestGroupBox);
      });
    });

    describe('uuidPathSkipRules', () => {
      let rule;

      it('can contain custom exclusions', () => {
        expect(uuidProvider._skipParent(new TestGroupBox())).toBeFalse();

        rule = w => w instanceof TestGroupBox;
        ObjectIdProvider.uuidPathAlwaysSkipRules.push(rule);
        expect(uuidProvider._skipParent(new TestGroupBox())).toBeTrue();
      });

      afterEach(() => {
        arrays.remove(ObjectIdProvider.uuidPathAlwaysSkipRules, rule);
      });
    });
  });

  describe('uuid', () => {

    let session: SandboxSession;
    beforeEach(() => {
      setFixtures(sandbox());
      session = sandboxSession();
    });

    describe('fallback', () => {
      class MyObj {
      }

      class MyCustomButton extends Button {
      }

      class MyCustomButtonWithStaticModel extends Button {
        protected override _jsonModel(): WidgetModel {
          return {objectType: Button};
        }
      }

      it('uses id if set', () => {
        assertUuid({id: 'myId', objectType: 'obj'}, 'myId');
      });

      it('uses object type if there is no id', () => {
        assertUuid({objectType: 'obj'}, 'obj');
      });

      it('resolves the object type if objectType is not a string or not set at all', () => {
        assertUuid(new Button(), 'Button'); // No objectType -> will be resolved based on constructor
        assertUuid(scout.create(Button, {parent: session.desktop}), 'Button'); // string objectType available -> no resolving necessary
        // @ts-expect-error
        assertUuid({objectType: Button}, 'Button');
        assertUuid(scout.create(MyCustomButtonWithStaticModel, {parent: session.desktop}), 'Button'); // MyCustomButtonWithStaticModel is not registered but objectType is explicitly set

        // Button is registered but MyCustomButton is not -> Button cannot be resolved
        assertUuid(new MyCustomButton(), null);
        assertUuid(scout.create(MyCustomButton, {parent: session.desktop}), null);
        assertUuid(scout.create({parent: session.desktop, objectType: MyCustomButton}), null);

        // Object is not registered and cannot be resolved
        assertUuid(new MyObj(), null);
        assertUuid(scout.create(MyObj), null);
        // @ts-expect-error
        assertUuid({objectType: MyObj}, null);
      });

      it('ignores model adapter ids', () => {
        assertUuid({id: '123'}, null); // Model adapter ids are just numbers
        assertUuid({id: '123', objectType: 'Button'}, 'Button'); // considers object type if id is ignored
      });

      it('ignores ui sequence ids', () => {
        assertUuid({id: uuidProvider.createUiSeqId()}, null);
        assertUuid({id: uuidProvider.createUiSeqId(), objectType: 'Button'}, 'Button'); // considers object type if id is ignored
      });

      it('ignores object type and id if fallback is disabled', () => {
        assertUuid({id: 'id3', objectType: '4'}, null, false);
        assertUuid({objectType: '4'}, null, false);
        assertUuid({id: 'id3'}, null, false);
      });
    });

    it('prefers classId if set', () => {
      assertUuid({classId: '1', uuid: '2', id: 'id3', objectType: '4'}, '1');
    });

    it('uses uuid if set', () => {
      assertUuid({uuid: '2', id: 'id3', objectType: '4'}, '2');
      assertUuid({}, null);
    });

    it('prefers uuid over fallback properties', () => {
      assertUuid({uuid: '2', id: 'myId', objectType: 'Button'}, '2');
    });

    function assertUuid(object: ObjectUuidSource, expectedUuid: string, useFallback?: boolean) {
      expect(uuidProvider.uuid(object, useFallback)).toBe(expectedUuid);
    }
  });

  describe('uuidPath', () => {
    it('uses uuid if no parent present', () => {
      assertUuidPath({uuid: '1'}, '1');
      assertUuidPath({id: 'myId'}, 'myId');
      assertUuidPath({classId: '3'}, '3');
    });

    it('returns null if object has no id', () => {
      assertUuidPath({}, null);
    });

    it('includes uuid of parents', () => {
      const parent = scout.create(Widget, {parent: session.desktop, id: 'id3'});
      const object = {
        uuid: '4',
        parent
      };
      assertUuidPath(object, '4|id3');
    });

    it('prefers given parent', () => {
      const root = scout.create(Widget, {parent: session.desktop, uuid: '2'});
      const parent = scout.create(Widget, {parent: root, uuid: '3'});
      const object = {
        uuid: '4'
      };
      expect(uuidProvider.uuidPath(object, {parent})).toBe('4|3|2');
    });

    it('ignores parents if they are in skip list', () => {
      const root = scout.create(Widget, {parent: session.desktop, id: 'id2'});
      const group = scout.create(GroupBox, {parent: root, uuid: '3'}); // GroupBoxes are ignored
      const object = {
        uuid: '4',
        parent: group
      };
      assertUuidPath(object, '4|id2');
    });

    it('ignores parents if a skip rule matches', () => {
      class CustomOutline extends Outline {
      }

      session.desktop.id = '1'; // ensure desktop has an id. Must be ignored for uuidPath because there is a skip rule.
      const root = scout.create(Widget, {parent: session.desktop, id: 'id2'});
      const group = scout.create(CustomOutline, {parent: root, uuid: '3'}); // Outline and subclasses of Outline are ignored because there is a skip rule
      const object = {
        uuid: '4',
        parent: group
      };
      assertUuidPath(object, '4|id2');
    });

    it('does not consider skip widgets if object only has an objectType', () => {
      session.desktop.id = '1'; // ensure desktop has an id. Should be ignored for uuidPath.
      const root = scout.create(Widget, {parent: session.desktop, uuid: '2'});
      const group = scout.create(GroupBox, {parent: root, id: 'id1'}); // GroupBox is in skip list but must not be ignored
      const object = scout.create(Button, {parent: group}); // Does not have an id, uuid or classId -> it is not unique enough to ignore parents
      assertUuidPath(object, 'Button|id1|2');

      // SkipWidgets are not considered because every object only has an object type
      // SkipRules are always considered -> Desktop and NullWidget must never be part of the uuidPath
      const root2 = scout.create(Widget, {parent: session.desktop});
      const group2 = scout.create(GroupBox, {parent: root2});
      const object2 = scout.create(Button, {parent: group2});
      assertUuidPath(object2, 'Button|GroupBox|Widget');
    });

    it('does not consider skip widgets if requested', () => {
      session.desktop.id = '1'; // ensure desktop has an id. Should be ignored for uuidPath.
      const root = scout.create(Widget, {parent: session.desktop, uuid: '2'});
      const group = scout.create(GroupBox, {parent: root, id: 'id1'}); // Won't be skipped
      const object = scout.create(Button, {parent: group, uuid: '1'});
      expect(uuidProvider.uuidPath(object, {considerSkipWidgets: false})).toBe('1|id1|2');
    });

    it('works recursively', () => {
      const root = scout.create(Widget, {parent: session.desktop, id: 'id2'});
      const group = scout.create(Widget, {parent: root, uuid: '3'});
      const parent = scout.create(Widget, {parent: group});
      const object = {
        uuid: '4',
        parent: parent
      };
      assertUuidPath(object, '4|Widget|3|id2');
    });

    it('returns null if object has no uuid candidates', () => {
      const parent = scout.create(Widget, {parent: session.desktop, id: 'id3'});
      const object = {
        id: uuidProvider.createUiSeqId(),
        parent
      };
      assertUuidPath(object, null);
    });

    it('ignores parents without uuid and classId if fallback is disabled', () => {
      const root = scout.create(Widget, {parent: session.desktop, uuid: '2'});
      const group = scout.create(Widget, {parent: root, id: 'id3'});
      const parent = scout.create(Widget, {parent: group});
      const object = {
        uuid: '4',
        parent: parent
      };
      assertUuidPath(object, '4|2', false);
    });

    it('aborts computing on parents without uuid and classId if requested', () => {
      // abortIfNoUuidFound is mainly used by the ObjectIdProvider itself but may also be set explicitly, but it is questionable how useful this is
      // If aborting is explicitly enabled, parent uuids will only be appended if every parent in between has a relevant id
      const root = scout.create(Widget, {parent: session.desktop, uuid: '2'});
      const group = scout.create(Widget, {parent: root, id: 'id3'});
      const parent = scout.create(Widget, {parent: group});
      const object = {
        uuid: '4',
        parent: parent
      };
      expect(uuidProvider.uuidPath(object, {abortIfNoUuidFound: true, useFallback: false})).toBe('4');

      // If aborting is disabled, an uuid of a parent may be returned instead the uuid of the starting element
      const root2 = scout.create(Widget, {parent: session.desktop, uuid: '2'});
      const group2 = scout.create(Widget, {parent: root2, id: 'id3'});
      const parent2 = scout.create(Widget, {parent: group2});
      const object2 = {
        parent: parent2 // Does not have an uuid -> will be ignored
      };
      expect(uuidProvider.uuidPath(object2, {abortIfNoUuidFound: false, useFallback: false})).toBe('2');
    });

    it('stops on classId by default', () => {
      const root = scout.create(Widget, {parent: session.desktop, id: 'id2' /* ignored because child uses classId which stops the parent visit */});
      const group = scout.create(Widget, {parent: root, classId: '3'});
      const object = {
        classId: '4',
        parent: group
      };
      assertUuidPath(object, '4');
    });

    it('ignores parent if classId from remote is used', () => {
      const remoteParent = {
        id: 'id1',
        classId: '2'
      } as Widget;
      const remoteElement = {
        classId: '3',
        parent: remoteParent // should not be used for classId
      };
      assertUuidPath(remoteElement, '3');
    });

    function assertUuidPath(object: ObjectUuidSource, expectedUuidPath: string, useFallback?: boolean) {
      expect(uuidProvider.uuidPath(object, {useFallback})).toBe(expectedUuidPath);
    }
  });

  describe('createDependentUuid', () => {
    it('calls buildUuid and prepends a prefix', () => {
      expect(uuidProvider.createDependentUuid('abc', {uuid: '123'})).toBe(`abc${ObjectIdProvider.DEPENDENT_UUID_DELIMITER}123`);
      expect(uuidProvider.createDependentUuid('abc', {classId: 'cde'})).toBe(`abc${ObjectIdProvider.DEPENDENT_UUID_DELIMITER}cde`);
      expect(uuidProvider.createDependentUuid('abc', {})).toBe(null);
    });
  });

  describe('setDependentUuid', () => {
    it('uses createDependentUuid to set a uuid if the object does not have one yet', () => {
      let button = scout.create(Button, {parent: session.desktop});
      uuidProvider.setDependentUuid('abc', {uuid: '123'}, button);
      expect(button.uuid).toBe(`abc${ObjectIdProvider.DEPENDENT_UUID_DELIMITER}123`);

      button.setUuid('qqq');
      expect(button.uuid).toBe('qqq');

      uuidProvider.setDependentUuid('abc', {uuid: '123'}, button);
      expect(button.uuid).toBe('qqq'); // Not changed because button already had an uuid

      let button2 = scout.create(Button, {parent: session.desktop, classId: 'zzz'});
      uuidProvider.setDependentUuid('abc', {uuid: '123'}, button2);
      expect(button2.uuid).toBe(null); // Not changed because button already had a classId
      expect(button2.classId).toBe('zzz');

      let button3 = scout.create(Button, {parent: session.desktop});
      uuidProvider.setDependentUuid('abc', {classId: '123'}, button3);
      expect(button3.uuid).toBe(`abc${ObjectIdProvider.DEPENDENT_UUID_DELIMITER}123`); // Considers classId
      expect(button3.classId).toBe(null); // Does not set classId because it is not needed
    });
  });
});
