/*
 * Copyright (c) 2010-2023 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {graphics, scout, Status} from '../../src/index';

describe('Accordion', () => {
  let session;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  function createAccordion(numGroups, model) {
    let groups = [];
    for (let i = 0; i < numGroups; i++) {
      groups.push({
        objectType: 'Group',
        label: 'Group ' + i
      });
    }
    let defaults = {
      parent: session.desktop,
      groups: groups
    };
    model = $.extend({}, defaults, model);
    return scout.create('Accordion', model);
  }

  function createGroup(model) {
    let defaults = {
      parent: session.desktop
    };
    model = $.extend({}, defaults, model);
    return scout.create('Group', model);
  }

  describe('insertGroups', () => {
    it('inserts the given groups', () => {
      let accordion = createAccordion(0);
      let group0 = createGroup();
      let group1 = createGroup();
      let group2 = createGroup();
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

    it('triggers a property change event', () => {
      let accordion = createAccordion(0);
      let group0 = createGroup();
      let eventTriggered = false;
      accordion.on('propertyChange', event => {
        if (event.propertyName === 'groups') {
          eventTriggered = true;
        }
      });
      accordion.insertGroups(group0);
      expect(eventTriggered).toBe(true);
    });

    it('links the inserted groups with the groups container', () => {
      let accordion = createAccordion(0);
      let group0 = createGroup();
      let group1 = createGroup();
      let group2 = createGroup();
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

  describe('deleteGroups', () => {
    it('deletes the given groups', () => {
      let accordion = createAccordion(0);
      let group0 = createGroup();
      let group1 = createGroup();
      let group2 = createGroup();
      accordion.insertGroups([group0, group1, group2]);
      expect(accordion.groups.length).toBe(3);

      accordion.deleteGroups(group1);
      expect(accordion.groups.length).toBe(2);
      expect(accordion.groups[0]).toBe(group0);
      expect(accordion.groups[1]).toBe(group2);

      accordion.deleteGroups([group0, group2]);
      expect(accordion.groups.length).toBe(0);
    });

    it('triggers a property change event', () => {
      let accordion = createAccordion(3);
      let eventTriggered = false;
      accordion.on('propertyChange', event => {
        if (event.propertyName === 'groups') {
          eventTriggered = true;
        }
      });
      accordion.deleteGroups(accordion.groups[0]);
      expect(eventTriggered).toBe(true);
    });

    it('destroys the deleted groups', () => {
      let accordion = createAccordion(0);
      let group0 = createGroup({
        parent: accordion
      });
      let group1 = createGroup({
        parent: accordion
      });
      let group2 = createGroup({
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
    it('does not destroy the deleted groups if the groups container is not the owner', () => {
      let accordion = createAccordion(0);
      let group0 = createGroup({
        owner: session.desktop
      });
      let group1 = createGroup({
        owner: session.desktop
      });
      let group2 = createGroup({
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

  describe('deleteAllGroups', () => {
    it('deletes all groups', () => {
      let accordion = createAccordion(0);
      let group0 = createGroup();
      let group1 = createGroup();
      let group2 = createGroup();
      accordion.insertGroups([group0, group1, group2]);
      expect(accordion.groups.length).toBe(3);

      accordion.deleteAllGroups();
      expect(accordion.groups.length).toBe(0);
    });
  });

  describe('setGroups', () => {

    it('applies the order of the new groups to groups and filteredGroups', () => {
      let accordion = createAccordion(0);
      let group0 = createGroup();
      let group1 = createGroup();
      let group2 = createGroup();
      accordion.insertGroups([group0, group1, group2]);

      accordion.setGroups([group2, group1, group0]);
      expect(accordion.groups[0]).toBe(group2);
      expect(accordion.groups[1]).toBe(group1);
      expect(accordion.groups[2]).toBe(group0);
    });

    it('applies the order of the new groups to the rendered elements', () => {
      let accordion = createAccordion(0);
      let group0 = createGroup();
      let group1 = createGroup();
      let group2 = createGroup();
      accordion.insertGroups([group0, group1, group2]);
      accordion.render();
      let $groups = accordion.$container.children('.group');
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

  describe('sort', () => {

    it('uses the comparator to sort the groups', () => {
      let accordion = createAccordion(0);
      let group0 = createGroup({label: 'a'});
      let group1 = createGroup({label: 'b'});
      let group2 = createGroup({label: 'c'});
      accordion.insertGroups([group0, group1, group2]);

      accordion.setComparator((g0, g1) => {
        // desc
        return (g0.label < g1.label ? 1 : ((g0.label > g1.label) ? -1 : 0));
      });
      accordion.sort();
      expect(accordion.groups[0]).toBe(group2);
      expect(accordion.groups[1]).toBe(group1);
      expect(accordion.groups[2]).toBe(group0);

      accordion.setComparator((g0, g1) => {
        // asc
        return (g0.label < g1.label ? -1 : ((g0.label > g1.label) ? 1 : 0));
      });
      accordion.sort();
      expect(accordion.groups[0]).toBe(group0);
      expect(accordion.groups[1]).toBe(group1);
      expect(accordion.groups[2]).toBe(group2);
    });

    it('is executed when new groups are added', () => {
      let accordion = createAccordion(0);
      let group0 = createGroup({label: 'a'});
      let group1 = createGroup({label: 'b'});
      let group2 = createGroup({label: 'c'});

      accordion.setComparator((g0, g1) => {
        // desc
        return (g0.label < g1.label ? 1 : ((g0.label > g1.label) ? -1 : 0));
      });
      accordion.insertGroups([group0, group1]);
      expect(accordion.groups[0]).toBe(group1);
      expect(accordion.groups[1]).toBe(group0);

      accordion.insertGroups([group2]);
      expect(accordion.groups[0]).toBe(group2);
      expect(accordion.groups[1]).toBe(group1);
      expect(accordion.groups[2]).toBe(group0);
    });

    it('reorders the DOM elements accordingly', () => {
      let accordion = createAccordion(0);
      let group0 = createGroup({label: 'a'});
      let group1 = createGroup({label: 'b'});
      let group2 = createGroup({label: 'c'});
      accordion.insertGroups([group0, group1, group2]);
      accordion.render();

      accordion.setComparator((g0, g1) => {
        // desc
        return (g0.label < g1.label ? 1 : ((g0.label > g1.label) ? -1 : 0));
      });
      accordion.sort();
      expect(accordion.groups[0]).toBe(group2);
      expect(accordion.groups[1]).toBe(group1);
      expect(accordion.groups[2]).toBe(group0);
      let $groups = accordion.$container.children('.group');
      expect($groups.eq(0).data('widget')).toBe(group2);
      expect($groups.eq(1).data('widget')).toBe(group1);
      expect($groups.eq(2).data('widget')).toBe(group0);
    });

  });

  describe('collapsible', () => {

    beforeEach(() => {
      $('<style>' +
        '.group.collapsed:not(.collapsing) > .group-body { display: none; }' +
        '</style>').appendTo($('#sandbox'));
    });

    it('removes status when collapsed', async () => {
      const accordion = createAccordion(0);
      const group = createGroup({
        parent: accordion,
        body: {
          objectType: 'GroupBox',
          fields: [
            {
              objectType: 'StringField',
              errorStatus: {
                message: 'I am an error!!!'
              }
            }
          ]
        }
      });
      accordion.insertGroup(group);
      const field = group.body.fields[0];

      accordion.render();
      expect(field.fieldStatus.tooltip.rendered).toBeTrue();
      expect(field.fieldStatus.tooltip.$container.isVisible()).toBeTrue();

      group.setCollapsed(true);
      await group.when('bodyHeightChangeDone');
      expect(field.fieldStatus).toBeNull();

      field.clearErrorStatus();
      expect(field.fieldStatus).toBeNull();

      field.addErrorStatus(Status.error('I am a new error!!!'));
      expect(field.fieldStatus).toBeNull();

      group.setCollapsed(false);
      await group.when('bodyHeightChangeDone');
      expect(field.fieldStatus.tooltip.rendered).toBeTrue();
      expect(field.fieldStatus.tooltip.$container.isVisible()).toBeTrue();
    });

    it('moves status when sibling is collapsed', async () => {
      const accordion = createAccordion(0, {exclusiveExpand: false});
      const group0 = createGroup({
        parent: accordion,
        body: {
          objectType: 'GroupBox',
          fields: [
            {
              objectType: 'StringField'
            }
          ]
        }
      });
      const group1 = createGroup({
        parent: accordion,
        body: {
          objectType: 'GroupBox',
          fields: [
            {
              objectType: 'StringField',
              errorStatus: {
                message: 'I am an error!!!'
              }
            }
          ]
        }
      });
      accordion.insertGroups([group0, group1]);
      const fieldWithError = group1.body.fields[0];

      const calcAnchorAndDiffs = t => {
        const anchorBounds = graphics.offsetBounds(t.$anchor);
        const tooltipBounds = graphics.offsetBounds(t.$container);
        const xDiff = anchorBounds.x - tooltipBounds.x;
        const yDiff = anchorBounds.y - tooltipBounds.y;
        return {anchor: anchorBounds.point(), xDiff, yDiff};
      };

      accordion.render();
      accordion.validateLayout();
      const tooltip = fieldWithError.fieldStatus.tooltip;

      expect(tooltip.rendered).toBeTrue();
      expect(tooltip.$container.isVisible()).toBeTrue();

      const anchorAndDiffs = calcAnchorAndDiffs(tooltip);

      group0.setCollapsed(true);
      await group0.when('bodyHeightChangeDone');
      accordion.validateLayout();
      expect(tooltip.$container.isVisible()).toBeTrue();

      const anchorAndDiffsCollapsed = calcAnchorAndDiffs(tooltip);

      expect(anchorAndDiffsCollapsed.anchor).not.toEqual(anchorAndDiffs.anchor);
      expect(anchorAndDiffsCollapsed.xDiff).toBe(anchorAndDiffs.xDiff);
      expect(anchorAndDiffsCollapsed.yDiff).toBe(anchorAndDiffs.yDiff);

      group0.setCollapsed(false);
      await group0.when('bodyHeightChangeDone');
      accordion.validateLayout();
      expect(tooltip.$container.isVisible()).toBeTrue();

      const anchorAndDiffsExpanded = calcAnchorAndDiffs(tooltip);

      expect(anchorAndDiffsExpanded.anchor).not.toEqual(anchorAndDiffsCollapsed.anchor);
      expect(anchorAndDiffsExpanded.anchor).toEqual(anchorAndDiffs.anchor);
      expect(anchorAndDiffsExpanded.xDiff).toBe(anchorAndDiffs.xDiff);
      expect(anchorAndDiffsExpanded.yDiff).toBe(anchorAndDiffs.yDiff);
    });
  });
});
