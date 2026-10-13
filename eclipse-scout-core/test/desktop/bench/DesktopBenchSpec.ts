/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * AI Disclosure: This file was partially AI-generated.
 * The AI-generated portions are made available under CC0-1.0
 * and not subject to the project's licence.
 *
 * SPDX-License-Identifier: EPL-2.0 and CC0-1.0
 */
import {BenchColumn, Desktop, DesktopBench, Form} from '../../../src/index';
import {FormSpecHelper, OutlineSpecHelper} from '../../../src/testing/index';

describe('DesktopBench', () => {
  let helper: OutlineSpecHelper, session: SandboxSession, desktop: Desktop, formHelper: FormSpecHelper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession({
      desktop: {
        benchVisible: true
      }
    });
    desktop = session.desktop;
    helper = new OutlineSpecHelper(session);
    formHelper = new FormSpecHelper(session);
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(() => {
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });

  describe('updateOutlineContent', () => {
    let outline, bench, model, node;

    beforeEach(() => {
      model = helper.createModelFixture(3, 2, true);
      outline = helper.createOutline(model);
      node = outline.nodes[0];
      node.detailForm = formHelper.createFormWithOneField();
      node.detailFormVisible = true;
      bench = desktop.bench;
      desktop.setOutline(outline);
    });

    it('called when an outline page gets selected', () => {
      spyOn(bench, 'updateOutlineContent');
      outline.selectNodes(outline.nodes[1]);
      expect(bench.updateOutlineContent.calls.count()).toEqual(1);
    });

    it('doesn\'t get called if page already is selected', () => {
      spyOn(bench, 'updateOutlineContent');
      outline.selectNodes(outline.nodes[1]);
      expect(bench.updateOutlineContent.calls.count()).toEqual(1);

      outline.selectNodes(outline.nodes[1]);
      expect(bench.updateOutlineContent.calls.count()).toEqual(1);

      outline.selectNodes([]);
      expect(bench.updateOutlineContent.calls.count()).toEqual(2);

      outline.selectNodes([]);
      expect(bench.updateOutlineContent.calls.count()).toEqual(2);
    });

    it('sets detailForm as outlineContent if node gets selected', () => {
      // node 0 has a detail form
      outline.selectNodes(outline.nodes[1]);
      expect(outline.selectedNodes[0].detailForm).toBeFalsy();
      expect(bench.outlineContent).toBeFalsy();

      outline.selectNodes(outline.nodes[0]);
      expect(outline.selectedNodes[0].detailForm).toBeTruthy();
      expect(bench.outlineContent).toBe(outline.selectedNodes[0].detailForm);

      outline.selectNodes(outline.nodes[1]);
      expect(outline.selectedNodes[0].detailForm).toBeFalsy();
      expect(bench.outlineContent).toBeFalsy();
    });

    it('preserves desktop.inBackground when updating outline content', () => {
      // select node 0 (which will be in foreground)
      outline.selectNodes(outline.nodes[0]);
      expect(desktop.inBackground).toBeFalsy();

      // open new form in foreground
      let form = formHelper.createFormWithOneField();
      form.displayHint = Form.DisplayHint.VIEW;
      form.displayViewId = 'C';
      desktop.showForm(form);

      expect(desktop.inBackground).toBeTruthy();

      // test that replace view is not called

      outline.nodes[0].detailForm = formHelper.createFormWithOneField();
      outline.nodes[0].detailFormVisible = true;

      bench.updateOutlineContent();

      // test that replace view is called once

      expect(form.rendered).toBeTruthy();
      expect(bench.outlineContent === outline.nodes[0].detailForm).toBeTruthy();
      expect(bench.outlineContent.rendered).toBeFalsy();
      expect(desktop.inBackground).toBeTruthy();
    });

    it('preserves desktop.inBackground when switching nodes', () => {
      // select node 0 (which will be in foreground)
      outline.selectNodes(outline.nodes[0]);
      expect(desktop.inBackground).toBeFalsy();

      // open new form in foreground
      let form = formHelper.createFormWithOneField();
      form.displayHint = Form.DisplayHint.VIEW;
      form.displayViewId = 'C';
      desktop.showForm(form);

      expect(desktop.inBackground).toBeTruthy();

      // test that replace view is not called

      // switch to node 1
      outline.nodes[1].detailForm = formHelper.createFormWithOneField();
      outline.nodes[1].detailFormVisible = true;

      outline.selectNodes(outline.nodes[1]);

      // test that replace view is called once

      expect(form.rendered).toBeTruthy();
      expect(bench.outlineContent === outline.nodes[1].detailForm).toBeTruthy();
      expect(bench.outlineContent.rendered).toBeFalsy();

      expect(desktop.inBackground).toBeTruthy();
    });
  });

  describe('aria properties', () => {

    it('has aria role main', () => {
      expect(desktop.bench.$container).toHaveAttr('role', 'main');
    });

    it('has aria-label set', () => {
      expect(desktop.bench.$container.attr('aria-label')).toBeTruthy();
      expect(desktop.bench.$container.attr('aria-labelledby')).toBeFalsy();
    });
  });

  describe('alwaysRendered', () => {
    let bench: DesktopBench, column: BenchColumn;

    beforeEach(() => {
      bench = desktop.bench;
      column = bench.columns[DesktopBench.VIEW_AREA_COLUMN_INDEX.CENTER];
    });

    function setAlwaysRendered(alwaysRendered: boolean) {
      desktop.setProperty('benchLayoutData', {columns: [null, {alwaysRendered}, null]});
    }

    function createView(): Form {
      let form = formHelper.createFormWithOneField();
      form.displayViewId = 'C';
      return form;
    }

    it('removes an empty column by default', () => {
      let view = createView();
      bench.addView(view);
      expect(column.rendered).toBe(true);

      bench.removeView(view);
      expect(column.rendered).toBe(false);
      expect(bench.visibleColumns()).not.toContain(column);
    });

    it('renders an empty column when alwaysRendered is set', () => {
      expect(column.rendered).toBe(false);

      setAlwaysRendered(true);
      expect(column.rendered).toBe(true);
      expect(column.viewCount()).toBe(0);
      expect(column.hasViews()).toBe(false);
      expect(bench.visibleColumns()).toContain(column);
    });

    it('keeps the column rendered when its last view is removed', () => {
      setAlwaysRendered(true);
      let view = createView();
      bench.addView(view);
      expect(column.viewCount()).toBe(1);

      bench.removeView(view);
      expect(column.viewCount()).toBe(0);
      expect(column.rendered).toBe(true);
    });

    it('removes the empty column when alwaysRendered is reset', () => {
      setAlwaysRendered(true);
      expect(column.rendered).toBe(true);

      setAlwaysRendered(false);
      expect(column.rendered).toBe(false);
    });

    it('keeps a column with views rendered when alwaysRendered is reset', () => {
      setAlwaysRendered(true);
      bench.addView(createView());

      setAlwaysRendered(false);
      expect(column.rendered).toBe(true);
    });
  });

  describe('column visible', () => {
    let bench: DesktopBench, westColumn: BenchColumn, centerColumn: BenchColumn, eastColumn: BenchColumn;

    beforeEach(() => {
      bench = desktop.bench;
      westColumn = bench.columns[DesktopBench.VIEW_AREA_COLUMN_INDEX.LEFT];
      centerColumn = bench.columns[DesktopBench.VIEW_AREA_COLUMN_INDEX.CENTER];
      eastColumn = bench.columns[DesktopBench.VIEW_AREA_COLUMN_INDEX.RIGHT];
    });

    function createView(displayViewId: 'W' | 'C' | 'E'): Form {
      let form = formHelper.createFormWithOneField();
      form.displayViewId = displayViewId;
      return form;
    }

    it('removes an invisible column even if it has views', () => {
      bench.addView(createView('C'));
      expect(centerColumn.rendered).toBe(true);

      centerColumn.setVisible(false);
      expect(centerColumn.rendered).toBe(false);
      expect(bench.visibleColumns()).not.toContain(centerColumn);
    });

    it('removes an invisible column even if alwaysRendered is set', () => {
      desktop.setProperty('benchLayoutData', {columns: [null, {alwaysRendered: true}, null]});
      expect(centerColumn.rendered).toBe(true);

      centerColumn.setVisible(false);
      expect(centerColumn.rendered).toBe(false);

      centerColumn.setVisible(true);
      expect(centerColumn.rendered).toBe(true);
      expect(centerColumn.isAlwaysRendered()).toBe(true);
    });

    it('leaves only the visible column without splitters', () => {
      bench.addView(createView('W'));
      bench.addView(createView('C'));
      bench.addView(createView('E'));
      expect(bench.getComponents().length).toBe(5);

      westColumn.setVisible(false);
      centerColumn.setVisible(false);
      expect(bench.getComponents()).toEqual([eastColumn]);
      expect(eastColumn.rendered).toBe(true);
    });

    it('renders the column with its views when it becomes visible again', () => {
      let view = createView('C');
      bench.addView(view);
      centerColumn.setVisible(false);
      expect(view.rendered).toBe(false);

      centerColumn.setVisible(true);
      expect(centerColumn.rendered).toBe(true);
      expect(view.rendered).toBe(true);
    });

    it('keeps the splitters when the last view of an invisible column is removed', () => {
      bench.addView(createView('W'));
      bench.addView(createView('E'));
      let view = createView('C');
      bench.addView(view);
      centerColumn.setVisible(false);
      let components = bench.getComponents();
      expect(components.length).toBe(3);

      bench.removeView(view);
      expect(centerColumn.rendered).toBe(false);
      expect(bench.getComponents()).toBe(components);
    });

    it('does not render an invisible column when a view is added', () => {
      centerColumn.setVisible(false);
      bench.addView(createView('C'));
      expect(centerColumn.viewCount()).toBe(1);
      expect(centerColumn.rendered).toBe(false);
    });
  });
});
