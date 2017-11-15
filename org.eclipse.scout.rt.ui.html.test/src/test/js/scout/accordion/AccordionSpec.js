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
describe("Accordion", function() {
  var session;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  function createAccordion(numGroups, model) {
    var groups = [];
    for (var i = 0; i < numGroups; i++) {
      groups.push({
        objectType: 'Group',
        label: "Group " + i
      });
    }
    var defaults = {
      parent: session.desktop,
      groups: groups
    };
    model = $.extend({}, defaults, model);
    return scout.create('Accordion', model);
  }

  function createGroup(model) {
    var defaults = {
      parent: session.desktop
    };
    model = $.extend({}, defaults, model);
    return scout.create('Group', model);
  }

  describe('insertGroups', function() {
    it('inserts the given groups', function() {
      var accordion = createAccordion(0);
      var group0 = createGroup();
      var group1 = createGroup();
      var group2 = createGroup();
      expect(accordion.groups.length).toBe(0);

      accordion.insertGroups(group0);
      expect(accordion.groups.length).toBe(1);
      expect(accordion.groups[0]).toBe(group0);

      accordion.insertGroups([group1, group2]);
      expect(accordion.groups.length).toBe(3);
      expect(accordion.groups[0]).toBe(group0);
      expect(accordion.groups[1]).toBe(group1);
      expect(accordion.groups[2]).toBe(group2);
    });

    it('triggers a property change event', function() {
      var accordion = createAccordion(0);
      var group0 = createGroup();
      var eventTriggered = false;
      accordion.on('propertyChange', function(event) {
        if (event.propertyName === 'groups') {
          eventTriggered = true;
        }
      });
      accordion.insertGroups(group0);
      expect(eventTriggered).toBe(true);
    });

    it('links the inserted groups with the groups container', function() {
      var accordion = createAccordion(0);
      var group0 = createGroup();
      var group1 = createGroup();
      var group2 = createGroup();
      expect(accordion.groups.length).toBe(0);
      expect(group0.parent).toBe(session.desktop);
      expect(group1.parent).toBe(session.desktop);
      expect(group2.parent).toBe(session.desktop);

      accordion.insertGroup(group0);
      expect(group0.parent).toBe(accordion);

      accordion.insertGroups([group1, group2]);
      expect(group1.parent).toBe(accordion);
      expect(group2.parent).toBe(accordion);
    });
  });

  describe('deleteGroups', function() {
    it('deletes the given groups', function() {
      var accordion = createAccordion(0);
      var group0 = createGroup();
      var group1 = createGroup();
      var group2 = createGroup();
      accordion.insertGroups([group0, group1, group2]);
      expect(accordion.groups.length).toBe(3);

      accordion.deleteGroups(group1);
      expect(accordion.groups.length).toBe(2);
      expect(accordion.groups[0]).toBe(group0);
      expect(accordion.groups[1]).toBe(group2);

      accordion.deleteGroups([group0, group2]);
      expect(accordion.groups.length).toBe(0);
    });

    it('triggers a property change event', function() {
      var accordion = createAccordion(3);
      var eventTriggered = false;
      accordion.on('propertyChange', function(event) {
        if (event.propertyName === 'groups') {
          eventTriggered = true;
        }
      });
      accordion.deleteGroups(accordion.groups[0]);
      expect(eventTriggered).toBe(true);
    });

    it('destroys the deleted groups', function() {
      var accordion = createAccordion(0);
      var group0 = createGroup({
        parent: accordion
      });
      var group1 = createGroup({
        parent: accordion
      });
      var group2 = createGroup({
        parent: accordion
      });
      accordion.render();
      accordion.insertGroups([group0, group1, group2]);
      expect(group0.destroyed).toBe(false);
      expect(group0.rendered).toBe(true);
      expect(group1.destroyed).toBe(false);
      expect(group1.rendered).toBe(true);
      expect(group2.destroyed).toBe(false);
      expect(group2.rendered).toBe(true);

      accordion.deleteGroup(group1);
      expect(group1.destroyed).toBe(true);
      expect(group1.rendered).toBe(false);

      accordion.deleteGroups([group0, group2]);
      expect(group0.destroyed).toBe(true);
      expect(group0.rendered).toBe(false);
      expect(group2.destroyed).toBe(true);
      expect(group2.rendered).toBe(false);
    });

    /**
     * This spec is important if a group should be moved from one groups container to another.
     */
    it('does not destroy the deleted groups if the groups container is not the owner', function() {
      var accordion = createAccordion(0);
      var group0 = createGroup({
        owner: session.desktop
      });
      var group1 = createGroup({
        owner: session.desktop
      });
      var group2 = createGroup({
        owner: session.desktop
      });
      accordion.render();
      accordion.insertGroups([group0, group1, group2]);
      expect(group0.destroyed).toBe(false);
      expect(group0.rendered).toBe(true);
      expect(group1.destroyed).toBe(false);
      expect(group1.rendered).toBe(true);
      expect(group2.destroyed).toBe(false);
      expect(group2.rendered).toBe(true);

      accordion.deleteGroup(group1);
      expect(group1.destroyed).toBe(false);
      expect(group1.rendered).toBe(false);

      accordion.deleteGroups([group0, group2]);
      expect(group0.destroyed).toBe(false);
      expect(group0.rendered).toBe(false);
      expect(group2.destroyed).toBe(false);
      expect(group2.rendered).toBe(false);
    });
  });

  describe('deleteAllGroups', function() {
    it('deletes all groups', function() {
      var accordion = createAccordion(0);
      var group0 = createGroup();
      var group1 = createGroup();
      var group2 = createGroup();
      accordion.insertGroups([group0, group1, group2]);
      expect(accordion.groups.length).toBe(3);

      accordion.deleteAllGroups();
      expect(accordion.groups.length).toBe(0);
    });
  });

  describe('setGroups', function() {

    it('applies the order of the new groups to groups and filteredGroups', function() {
      var accordion = createAccordion(0);
      var group0 = createGroup();
      var group1 = createGroup();
      var group2 = createGroup();
      accordion.insertGroups([group0, group1, group2]);

      accordion.setGroups([group2, group1, group0]);
      expect(accordion.groups[0]).toBe(group2);
      expect(accordion.groups[1]).toBe(group1);
      expect(accordion.groups[2]).toBe(group0);
    });

    it('applies the order of the new groups to the rendered elements', function() {
      var accordion = createAccordion(0);
      var group0 = createGroup();
      var group1 = createGroup();
      var group2 = createGroup();
      accordion.insertGroups([group0, group1, group2]);
      accordion.render();
      var $groups = accordion.$container.children('.group');
      expect($groups.eq(0).data('widget')).toBe(group0);
      expect($groups.eq(1).data('widget')).toBe(group1);
      expect($groups.eq(2).data('widget')).toBe(group2);

      accordion.setGroups([group2, group1, group0]);
      $groups = accordion.$container.children('.group');
      expect($groups.eq(0).data('widget')).toBe(group2);
      expect($groups.eq(1).data('widget')).toBe(group1);
      expect($groups.eq(2).data('widget')).toBe(group0);
    });

  });

});
