/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
describe('SequenceBoxAdapter', function() {
  var session, helper, menuHelper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new scout.FormSpecHelper(session);
    menuHelper = new scout.MenuSpecHelper(session);
  });

  describe("clone", function() {
    it("does not accidentally create a logical grid", function() {
      var cloneHelper = new scout.CloneSpecHelper();
      var model = {
        id: 'seq01',
        parent: session.desktop,
        session: session,
        objectType: 'SequenceBox',
        modelClass: 'asdf',
        classId: 'bbb',
        fields: [{
          objectType: 'StringField',
          gridData: {
            useUiWidth: true
          }
        }, {
          objectType: 'DateField'
        }]
      };
      var adapter = scout.create('SequenceBoxAdapter', createAdapterModel(model));
      var seqBox = adapter.createWidget(model, session.desktop);
      linkWidgetAndAdapter(seqBox.fields[0], 'StringFieldAdapter');
      linkWidgetAndAdapter(seqBox.fields[1], 'DateFieldAdapter');
      var clone = seqBox.clone({
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
