/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {LookupRow, scout, SmartField, SmartFieldPopup, StaticLookupCall, TreeProposalChooser} from '../../../../src/index';
import {QueryBy} from '../../../../src';

describe('TreeProposalChooser', () => {

  let session: SandboxSession;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    jasmine.clock().install();
  });

  afterEach(() => {
    jasmine.clock().uninstall();
  });

  function createLookupRow<T>(key: T, parentKey: T, text: string): LookupRow<T> {
    return scout.create((LookupRow<T>), {
      key: key,
      parentKey: parentKey,
      text: text,
      enabled: true,
      active: true
    });
  }

  describe('ProposalTreeNode', () => {

    it('sets inactive class correctly', () => {
      // dummy smart field
      let smartField = scout.create(SmartField, {
        parent: session.desktop
      });

      let chooser = scout.create(TreeProposalChooser, {
        parent: session.desktop,
        smartField: smartField
      });

      let lookupRows = [
        createLookupRow(1, null, 'root'),
        createLookupRow(2, 1, 'nodeA'),
        createLookupRow(3, 1, 'nodeB')
      ];
      lookupRows[2].active = false;

      chooser.setLookupResult({
        queryBy: QueryBy.ALL,
        lookupRows: lookupRows
      });
      chooser.render();

      // nodeB must have inactive class, find by .inactive and check text
      let $nodeB = chooser.$container.find('.inactive');
      expect($nodeB.text()).toBe('nodeB');
    });

    it('does not get messed up with null keys', () => {
      // dummy smart field
      let smartField = scout.create((SmartField<number>), {
        parent: session.desktop
      });

      let chooser = scout.create((TreeProposalChooser<number>), {
        parent: session.desktop,
        smartField: smartField
      });

      let lookupRows: LookupRow<number>[] = [
        scout.create((LookupRow<number>), {
          key: null,
          text: 'Explicit null Key, No Parent'
        }),
        scout.create((LookupRow<number>), {
          key: 1,
          text: 'No Parent'
        }),
        scout.create((LookupRow<number>), {
          key: 2,
          text: 'Explicit null Parent',
          parentKey: null
        }),
        scout.create((LookupRow<number>), {
          key: 3,
          parentKey: 2,
          text: 'Child'
        })
      ];

      chooser.setLookupResult({
        queryBy: QueryBy.ALL,
        lookupRows: lookupRows
      });
      // Lookup rows with parentKey = null are top level nodes. They must never be linked to another lookup row, even if there is a lookup row with key = null.
      // Key = null has a special behavior: when such a row is clicked the text is cleared since the new value is null.
      expect(chooser.content.nodes.length).toBe(3);
      expect(chooser.content.nodes[0].parentNode).toBe(null);
      expect(chooser.content.nodes[1].parentNode).toBe(null);
      expect(chooser.content.nodes[2].parentNode).toBe(null);
      expect(chooser.content.nodes[2].expanded).toBe(true);
      expect(chooser.content.nodes[2].childNodes[0].parentNode).toBe(chooser.content.nodes[2]);
    });

    it('does not get messed up with undefined keys', () => {
      // dummy smart field
      let smartField = scout.create((SmartField<number>), {
        parent: session.desktop
      });

      let chooser = scout.create((TreeProposalChooser<number>), {
        parent: session.desktop,
        smartField: smartField
      });

      let lookupRows: LookupRow<number>[] = [
        // @ts-expect-error
        scout.create((LookupRow<number>), {
          text: 'No Key, No Parent'
        }),
        scout.create((LookupRow<number>), {
          key: 1,
          text: 'No Parent'
        })
      ];

      chooser.setLookupResult({
        queryBy: QueryBy.ALL,
        lookupRows: lookupRows
      });
      expect(chooser.content.nodes.length).toBe(2);
      expect(chooser.content.nodes[0].parentNode).toBe(null);
      expect(chooser.content.nodes[1].parentNode).toBe(null);
    });

    it('clears the field if a lookup row with key null is selected', () => {
      let smartField = scout.create(SmartField, {
        parent: session.desktop,
        browseHierarchy: true,
        lookupCall: {
          objectType: StaticLookupCall,
          data: [
            [null, 'Null Key'],
            [1, 'Value 1']
          ]
        }
      });
      smartField.render();
      smartField.setValue(1);
      jasmine.clock().tick(300);
      expect(smartField.value).toBe(1);
      expect(smartField.displayText).toBe('Value 1');

      // Selecting lookup row with null key clears the field
      smartField.requestInput();
      jasmine.clock().tick(300);
      let popup = smartField.popup as SmartFieldPopup<number>;
      let chooser = popup.proposalChooser;
      chooser.content.selectNode(chooser.content.nodes[0]);
      popup.selectLookupRow();
      expect(smartField.value).toBe(null);
      expect(smartField.displayText).toBe('');
    });

    it('allows selecting lookup row with null key by typing', () => {
      let smartField = scout.create(SmartField, {
        parent: session.desktop,
        browseHierarchy: true,
        lookupCall: {
          objectType: StaticLookupCall,
          data: [
            [null, 'Null Key', null], // Set parentKey explicitly to null to force an infinite loop
            [1, 'Value 1'],
            [2, 'Value 2', null],
            [3, 'Child Value 2', 1]
          ]
        }
      });
      smartField.render();
      smartField.setValue(1);
      jasmine.clock().tick(300);
      expect(smartField.value).toBe(1);
      expect(smartField.displayText).toBe('Value 1');

      smartField.requestInput();
      smartField.$field.val('Null');
      // @ts-expect-error
      smartField._onFieldKeyUp({});
      jasmine.clock().tick(300);
      let popup = smartField.popup as SmartFieldPopup<number>;
      let chooser = popup.proposalChooser;
      expect(chooser.content.nodes.length).toBe(1);
      expect(chooser.content.selectedNode().text).toBe('Null Key');
      popup.selectLookupRow();
      expect(smartField.value).toBe(null);
      expect(smartField.displayText).toBe('');
    });

    it('filters lookup rows with same key', () => {
      let smartField = scout.create(SmartField, {
        parent: session.desktop
      });

      let chooser = scout.create(TreeProposalChooser, {
        parent: session.desktop,
        smartField: smartField
      });

      let lookupRows = [
        createLookupRow(0, null, 'Value 1'),
        createLookupRow(1, null, 'Value 2'),
        createLookupRow(1, null, 'Duplicate Value 2'),
        createLookupRow(2, 1, 'Child Value 2'),
        createLookupRow(2, 0, 'Duplicate Child Value 2')
      ];
      lookupRows[2].active = false;

      chooser.setLookupResult({
        queryBy: QueryBy.ALL,
        lookupRows: lookupRows
      });

      expect(chooser.content.nodes.length).toBe(2);
      expect(chooser.content.nodes[0].parentNode).toBe(null);
      expect(chooser.content.nodes[1].parentNode).toBe(null);
      expect(chooser.content.nodes[1].text).toBe('Value 2');
      expect(chooser.content.nodes[1].childNodes.length).toBe(1);
      expect(chooser.content.nodes[1].childNodes[0].text).toBe('Child Value 2');
      expect(chooser.content.nodes[1].childNodes[0].parentNode).toBe(chooser.content.nodes[1]);
    });
  });
});
