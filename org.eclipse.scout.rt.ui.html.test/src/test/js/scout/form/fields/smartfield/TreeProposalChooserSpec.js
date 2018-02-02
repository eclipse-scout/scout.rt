/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
describe('TreeProposalChooser', function() {

  var session;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    session.textMap.add('InactiveState', 'inactive');
    jasmine.clock().install();
  });

  afterEach(function() {
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

  describe('ProposalTreeNode', function() {

    it('should display inactive state', function() {
      // dummy smart field
      var smartField = scout.create('SmartField', {
        parent: session.desktop
      });

      var chooser = scout.create('TreeProposalChooser', {
        parent: session.desktop,
        smartField: smartField
      });

      var lookupRows = [
        createLookupRow(1, null, 'root'),
        createLookupRow(2, 1, 'nodeA'),
        createLookupRow(3, 1, 'nodeB'),
      ];
      lookupRows[2].active = false;

      chooser.setLookupResult({
        lookupRows: lookupRows
      });
      chooser.render();

      // Node must have inactive class and suffix ' (inactive)'
      var $nodeB = chooser.$container.find('.inactive');
      expect($nodeB.text()).toBe('nodeB (inactive)');
    });

  });

});
