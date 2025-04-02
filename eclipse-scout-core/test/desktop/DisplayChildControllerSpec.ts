/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {DisplayChild, DisplayChildController, DisplayParent, InitModelOf, scout, Widget} from '../../src/index';
import {FormSpecHelper} from '../../src/testing';

describe('DisplayChildController', () => {
  let session: SandboxSession;
  let displayParent: SpecDisplayParent;
  let displayChildController: SpecDisplayChildController;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    displayParent = new FormSpecHelper(session).createFormWithOneField();
    displayChildController = scout.create(SpecDisplayChildController, {displayParent, session});
  });

  describe('_registerChild', () => {

    it('adds element to child list', () => {
      expect(displayParent.displayChildren).toBeUndefined();

      const displayChild1 = scout.create(SpecDisplayChild, {parent: session.desktop, displayParent});
      displayChildController._registerChild(displayChild1, 'displayChildren');
      expect(displayParent.displayChildren).toEqual([displayChild1]);

      const displayChild3 = scout.create(SpecDisplayChild, {parent: session.desktop, displayParent});
      displayChildController._registerChild(displayChild3, 'displayChildren');
      expect(displayParent.displayChildren).toEqual([displayChild1, displayChild3]);

      const displayChild0 = scout.create(SpecDisplayChild, {parent: session.desktop, displayParent});
      displayChildController._registerChild(displayChild0, 'displayChildren', 0);
      expect(displayParent.displayChildren).toEqual([displayChild0, displayChild1, displayChild3]);

      const displayChild2 = scout.create(SpecDisplayChild, {parent: session.desktop, displayParent});
      displayChildController._registerChild(displayChild2, 'displayChildren', 2);
      expect(displayParent.displayChildren).toEqual([displayChild0, displayChild1, displayChild2, displayChild3]);
    });

    it('does not add element to child list twice', () => {
      expect(displayParent.displayChildren).toBeUndefined();

      const displayChild = scout.create(SpecDisplayChild, {parent: session.desktop, displayParent});
      displayChildController._registerChild(displayChild, 'displayChildren');
      expect(displayParent.displayChildren).toEqual([displayChild]);

      displayChildController._registerChild(displayChild, 'displayChildren');
      expect(displayParent.displayChildren).toEqual([displayChild]);
    });
  });

  describe('_unregisterChild', () => {

    it('removes element from child list', () => {
      expect(displayParent.displayChildren).toBeUndefined();

      const displayChild0 = scout.create(SpecDisplayChild, {parent: session.desktop, displayParent});
      const displayChild1 = scout.create(SpecDisplayChild, {parent: session.desktop, displayParent});
      const displayChild2 = scout.create(SpecDisplayChild, {parent: session.desktop, displayParent});

      displayChildController._registerChild(displayChild0, 'displayChildren');
      displayChildController._registerChild(displayChild1, 'displayChildren');
      displayChildController._registerChild(displayChild2, 'displayChildren');
      expect(displayParent.displayChildren).toEqual([displayChild0, displayChild1, displayChild2]);

      displayChildController._unregisterChild(displayChild1, 'displayChildren');
      expect(displayParent.displayChildren).toEqual([displayChild0, displayChild2]);

      displayChildController._unregisterChild(displayChild2, 'displayChildren');
      expect(displayParent.displayChildren).toEqual([displayChild0]);

      displayChildController._unregisterChild(displayChild0, 'displayChildren');
      expect(displayParent.displayChildren).toEqual([]);
    });

    it('does nothing if element is not a child', () => {
      expect(displayParent.displayChildren).toBeUndefined();

      const displayChild0 = scout.create(SpecDisplayChild, {parent: session.desktop, displayParent});
      const displayChild1 = scout.create(SpecDisplayChild, {parent: session.desktop, displayParent});

      displayChildController._unregisterChild(displayChild0, 'displayChildren');
      expect(displayParent.displayChildren).toBeUndefined();

      displayChildController._registerChild(displayChild0, 'displayChildren');
      expect(displayParent.displayChildren).toEqual([displayChild0]);

      displayChildController._unregisterChild(displayChild1, 'displayChildren');
      expect(displayParent.displayChildren).toEqual([displayChild0]);
    });

    it('is called automatically if child is destroyed', () => {
      expect(displayParent.displayChildren).toBeUndefined();

      const displayChild0 = scout.create(SpecDisplayChild, {parent: session.desktop, displayParent});
      const displayChild1 = scout.create(SpecDisplayChild, {parent: session.desktop, displayParent});
      const displayChild2 = scout.create(SpecDisplayChild, {parent: session.desktop, displayParent});

      displayChildController._registerChild(displayChild0, 'displayChildren');
      displayChildController._registerChild(displayChild1, 'displayChildren');
      displayChildController._registerChild(displayChild2, 'displayChildren');
      expect(displayParent.displayChildren).toEqual([displayChild0, displayChild1, displayChild2]);

      displayChild1.destroy();
      expect(displayParent.displayChildren).toEqual([displayChild0, displayChild2]);

      displayChild2.destroy();
      expect(displayParent.displayChildren).toEqual([displayChild0]);

      displayChild0.destroy();
      expect(displayParent.displayChildren).toEqual([]);
    });
  });

  interface SpecDisplayParent extends DisplayParent {
    displayChildren?: SpecDisplayChild[];
  }

  class SpecDisplayChild extends Widget implements DisplayChild {
    displayParent: SpecDisplayParent;

    protected override _init(model: InitModelOf<this>) {
      scout.assertValue(model.displayParent);
      super._init(model);
    }
  }

  class SpecDisplayChildController extends DisplayChildController {

    override _registerChild(child: DisplayChild, propertyName: string, position?: number) {
      super._registerChild(child, propertyName, position);
    }

    override _unregisterChild(child: DisplayChild, propertyName: string) {
      super._unregisterChild(child, propertyName);
    }
  }
});
