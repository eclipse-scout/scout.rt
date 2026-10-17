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

  describe('maximizedBenchColumn', () => {
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

    it('removes the other columns even if they have views', () => {
      bench.addView(createView('C'));
      bench.addView(createView('E'));
      expect(centerColumn.rendered).toBe(true);

      desktop.setMaximizedBenchColumn('E');
      expect(centerColumn.rendered).toBe(false);
      expect(eastColumn.rendered).toBe(true);
      expect(bench.visibleColumns()).toEqual([eastColumn]);
    });

    it('removes the other columns even if alwaysRendered is set', () => {
      desktop.setProperty('benchLayoutData', {columns: [null, {alwaysRendered: true}, null]});
      bench.addView(createView('E'));
      expect(centerColumn.rendered).toBe(true);

      desktop.setMaximizedBenchColumn('E');
      expect(centerColumn.rendered).toBe(false);

      desktop.setMaximizedBenchColumn(null);
      expect(centerColumn.rendered).toBe(true);
      expect(centerColumn.isAlwaysRendered()).toBe(true);
    });

    it('leaves only the maximized column without splitters', () => {
      bench.addView(createView('W'));
      bench.addView(createView('C'));
      bench.addView(createView('E'));
      expect(bench.getComponents().length).toBe(5);

      desktop.setMaximizedBenchColumn('E');
      expect(bench.getComponents()).toEqual([eastColumn]);
      expect(westColumn.rendered).toBe(false);
    });

    it('renders the other columns with their views when reset', () => {
      let view = createView('C');
      bench.addView(view);
      bench.addView(createView('E'));
      desktop.setMaximizedBenchColumn('E');
      expect(view.rendered).toBe(false);

      desktop.setMaximizedBenchColumn(null);
      expect(centerColumn.rendered).toBe(true);
      expect(view.rendered).toBe(true);
    });

    it('keeps the components when the last view of a hidden column is removed', () => {
      bench.addView(createView('E'));
      let view = createView('C');
      bench.addView(view);
      desktop.setMaximizedBenchColumn('E');
      let components = bench.getComponents();
      expect(components).toEqual([eastColumn]);

      bench.removeView(view);
      expect(centerColumn.rendered).toBe(false);
      expect(bench.getComponents()).toBe(components);
    });

    it('does not render a hidden column when a view is added without activation', () => {
      bench.addView(createView('E'));
      desktop.setMaximizedBenchColumn('E');
      bench.addView(createView('C'), false);
      expect(centerColumn.viewCount()).toBe(1);
      expect(centerColumn.rendered).toBe(false);
      expect(desktop.maximizedBenchColumn).toBe('E');
    });

    it('is reset when a view in another column is activated', () => {
      bench.addView(createView('C'));
      bench.addView(createView('E'));
      desktop.setMaximizedBenchColumn('E');

      let view = createView('C');
      bench.addView(view);
      expect(desktop.maximizedBenchColumn).toBe(null);
      expect(centerColumn.rendered).toBe(true);
      expect(view.rendered).toBe(true);
    });

    it('is not reset when a view in the maximized column is activated', () => {
      bench.addView(createView('C'));
      let view = createView('E');
      bench.addView(view);
      bench.addView(createView('E'));
      desktop.setMaximizedBenchColumn('E');

      bench.activateView(view);
      expect(desktop.maximizedBenchColumn).toBe('E');
      expect(centerColumn.rendered).toBe(false);
    });

    it('is not reset when a view is activated during the initial form rendering', () => {
      let view = createView('C');
      bench.addView(view, false);
      bench.addView(createView('E'));
      desktop.setMaximizedBenchColumn('E');

      desktop.initialFormRendering = true;
      bench.activateView(view);
      desktop.initialFormRendering = false;
      expect(desktop.maximizedBenchColumn).toBe('E');
      expect(centerColumn.rendered).toBe(false);
    });

    it('is applied to a recreated bench', () => {
      desktop.setMaximizedBenchColumn('E');
      desktop.setBenchVisible(false);
      desktop.setBenchVisible(true);
      bench = desktop.bench;
      bench.addView(createView('C'), false);
      bench.addView(createView('E'));

      expect(bench.columns[DesktopBench.VIEW_AREA_COLUMN_INDEX.CENTER].rendered).toBe(false);
      expect(bench.visibleColumns()).toEqual([bench.columns[DesktopBench.VIEW_AREA_COLUMN_INDEX.RIGHT]]);
    });

    describe('with header tab area', () => {

      beforeEach(() => {
        session = sandboxSession({
          desktop: {
            headerVisible: true,
            benchVisible: true
          }
        });
        desktop = session.desktop;
        formHelper = new FormSpecHelper(session);
        bench = desktop.bench;
        centerColumn = bench.columns[DesktopBench.VIEW_AREA_COLUMN_INDEX.CENTER];
      });

      function showView(title: string): Form {
        let form = formHelper.createViewWithOneField();
        form.title = title;
        form.displayViewId = 'C';
        desktop.showForm(form);
        return form;
      }

      it('renders the view tabs again when the maximized column is reset', () => {
        let view1 = showView('view1');
        let view2 = showView('view2');
        let tabArea = desktop.header.tabArea;
        let [tab1, tab2] = tabArea.tabs;
        expect(tab1.view).toBe(view1);
        expect(tab2.view).toBe(view2);
        expect(tab2.selected).toBe(true);

        desktop.setMaximizedBenchColumn('E');
        expect(tab1.rendered).toBe(false);
        expect(tab2.rendered).toBe(false);

        desktop.setMaximizedBenchColumn(null);
        expect(view2.rendered).toBe(true);
        expect(tab1.rendered).toBe(true);
        expect(tab2.rendered).toBe(true);
        expect(tab1.selected).toBe(false);
        expect(tab2.selected).toBe(true);
        expect(tab2.$container.parent()[0]).toBe(tabArea.$container[0]);
        expect(tab2.$container.prevAll('.simple-tab')[0]).toBe(tab1.$container[0]);
      });

      it('keeps the view tab linked to its view after the maximized column was reset', () => {
        let view = showView('title');
        let tab = desktop.header.tabArea.tabs[0];
        desktop.setMaximizedBenchColumn('E');
        desktop.setMaximizedBenchColumn(null);

        view.setTitle('new title');
        expect(tab.title).toBe('new title');
        expect(tab.$title.text()).toBe('new title');

        view.close();
        expect(tab.destroyed).toBe(true);
        expect(desktop.header.tabArea.tabs.length).toBe(0);
      });
    });
  });
});
