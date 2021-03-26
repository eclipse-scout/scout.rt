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
import {scout, StaticLookupCall} from '../../../../src/index';

describe('TreeProposalChooser', () => {

  let session;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    session.textMap.add('InactiveState', 'inactive');
    jasmine.clock().install();
  });

  afterEach(() => {
    jasmine.clock().uninstall();
  });

  function createLookupRow(key, parentKey, text) {
    return {
      key: key,
      parentKey: parentKey,
      text: text,
      enabled: true,
      active: true
    };
  }

  describe('ProposalTreeNode', () => {

    it('displays inactive state correctly', () => {
      // dummy smart field
      let smartField = scout.create('SmartField', {
        parent: session.desktop
      });

      let chooser = scout.create('TreeProposalChooser', {
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
        lookupRows: lookupRows
      });
      chooser.render();

      // Node must have inactive class and suffix ' (inactive)'
      let $nodeB = chooser.$container.find('.inactive');
      expect($nodeB.text()).toBe('nodeB (inactive)');
    });

    it('does not get messed up with null keys', () => {
      // dummy smart field
      let smartField = scout.create('SmartField', {
        parent: session.desktop
      });

      let chooser = scout.create('TreeProposalChooser', {
        parent: session.desktop,
        smartField: smartField
      });

      let lookupRows = [
        scout.create('LookupRow', {
          text: 'No Key, No Parent'
        }),
        scout.create('LookupRow', {
          key: null,
          text: 'Explicit null Key, No Parent'
        }),
        scout.create('LookupRow', {
          key: 1,
          text: 'No Parent'
        }),
        scout.create('LookupRow', {
          key: 2,
          text: 'Explicit null Parent',
          parentKey: null
        }),
        scout.create('LookupRow', {
          key: 3,
          parentKey: 2,
          text: 'Child'
        })
      ];

      chooser.setLookupResult({
        lookupRows: lookupRows
      });
      // Lookup rows with parentKey = null are top level nodes. They must never be linked to another lookup row, even if there is a lookup row with key = null.
      // Key = null has a special behavior: when such a row is clicked the text is cleared since the new value is null.
      expect(chooser.model.nodes.length).toBe(4);
      expect(chooser.model.nodes[0].parentNode).toBe(null);
      expect(chooser.model.nodes[1].parentNode).toBe(null);
      expect(chooser.model.nodes[2].parentNode).toBe(null);
      expect(chooser.model.nodes[3].parentNode).toBe(null);
      expect(chooser.model.nodes[3].expanded).toBe(true);
      expect(chooser.model.nodes[3].childNodes[0].parentNode).toBe(chooser.model.nodes[3]);
    });

    it('clears the field if a lookup row with key null is selected', () => {
      let lookupCall = new StaticLookupCall();
      let smartField = scout.create('SmartField', {
        parent: session.desktop,
        browseHierarchy: true,
        lookupCall: {
          objectType: 'StaticLookupCall',
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
      let chooser = smartField.popup.proposalChooser;
      chooser.model.selectNode(chooser.model.nodes[0]);
      smartField.popup.selectLookupRow();
      expect(smartField.value).toBe(null);
      expect(smartField.displayText).toBe('');
    });

    it('allows selecting lookup row with null key by typing', () => {
      let lookupCall = new StaticLookupCall();
      let smartField = scout.create('SmartField', {
        parent: session.desktop,
        browseHierarchy: true,
        lookupCall: {
          objectType: 'StaticLookupCall',
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
      smartField._onFieldKeyUp({});
      jasmine.clock().tick(300);
      let chooser = smartField.popup.proposalChooser;
      expect(chooser.model.nodes.length).toBe(1);
      expect(chooser.model.selectedNode().text).toBe('Null Key');
      smartField.popup.selectLookupRow();
      expect(smartField.value).toBe(null);
      expect(smartField.displayText).toBe('');
    });
  });
});
