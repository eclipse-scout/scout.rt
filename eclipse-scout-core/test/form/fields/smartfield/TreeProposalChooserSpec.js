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
import {scout} from '../../../../src/index';

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

    it('should display inactive state', () => {
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

  });

});
