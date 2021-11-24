/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {OutlineSpecHelper, TableSpecHelper} from '../../../src/testing';

describe('OutlineAdapter', () => {
  let session, helper, tableHelper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession({
      desktop: {
        navigationVisible: true,
        headerVisible: true,
        benchVisible: true
      }
    });
    helper = new OutlineSpecHelper(session);
    tableHelper = new TableSpecHelper(session);
    $.fx.off = true;
  });

  afterEach(() => {
    session = null;
    $.fx.off = false;
  });

  function createOutlineWithDetailTable() {
    let model = helper.createModelFixture();
    let outline = helper.createOutline(model);
    linkWidgetAndAdapter(outline, 'OutlineAdapter');
    let table = tableHelper.createTable(tableHelper.createModelFixture(2, 0));
    linkWidgetAndAdapter(table, 'TableAdapter');

    outline.insertNode({
      id: 'root',
      text: 'Table Page',
      detailTable: table,
      detailTableVisible: true
    });
    return outline;
  }

  it('updates filter if rows of a detail table are inserted', () => {
    let outline = createOutlineWithDetailTable();
    let node = outline.nodes[0];
    session.desktop.setOutline(outline);
    outline.selectNode(node);
    outline.expandNode(node);

    let table = node.detailTable;
    expect(outline.$nodes().length).toBe(1);

    // Insert row and node separately, in Scout classic the OutlineMediator.js does not sync the rows to nodes because it happens on server side.
    outline.insertNode({
      id: 'node1',
      text: 'node 1'
    }, node);
    table.insertRow({
      nodeId: 'node1',
      cells: ['cell 1']
    });
    expect(table.$rows().length).toBe(1);
    expect(outline.$nodes().length).toBe(2);

    let filter = tableHelper.createTableTextFilter(table, 'cell 1');
    table.addFilter(filter);

    // Insert cell 2 -> must not be shown
    outline.insertNode({
      id: 'node2',
      text: 'node 2'
    }, node);
    table.insertRow({
      nodeId: 'node2',
      cells: ['cell 2']
    });
    expect(table.$rows().length).toBe(1);
    expect(outline.$nodes().length).toBe(2);

    // Insert another cell 1 -> must be shown
    outline.insertNode({
      id: 'node3',
      text: 'node 1'
    }, node);
    table.insertRow({
      nodeId: 'node3',
      cells: ['cell 1']
    });
    expect(table.$rows().length).toBe(2);
    expect(outline.$nodes().length).toBe(3);
  });

  it('updates filter if rows of a detail table are updated', () => {
    let outline = createOutlineWithDetailTable();
    let node = outline.nodes[0];
    session.desktop.setOutline(outline);
    outline.selectNode(node);
    outline.expandNode(node);

    let table = node.detailTable;
    expect(outline.$nodes().length).toBe(1);

    let filter = tableHelper.createTableTextFilter(table, 'cell 1');
    table.addFilter(filter);

    // Insert row and node separately, in Scout classic the OutlineMediator.js does not sync the rows to nodes because it happens on server side.
    table.insertRow({
      id: 'row1',
      nodeId: 'node1',
      cells: ['cell 1']
    });
    outline.insertNode({
      id: 'node1',
      text: 'node 1'
    }, node);
    expect(table.$rows().length).toBe(1);
    expect(outline.$nodes().length).toBe(2);

    // Insert cell 2 -> must not be shown
    table.insertRow({
      id: 'row2',
      nodeId: 'node2',
      cells: ['cell 2']
    });
    outline.insertNode({
      id: 'node2',
      text: 'node 2'
    }, node);
    expect(table.$rows().length).toBe(1);
    expect(outline.$nodes().length).toBe(2);

    // Update visible row -> filter still accepts, nothing happens
    outline.updateNode({
      id: 'node1',
      text: 'node 1 new'
    }, node);
    table.updateRow({
      id: 'row1',
      nodeId: 'node1',
      cells: ['cell 1 new']
    });
    expect(table.$rows().length).toBe(1);
    expect(outline.$nodes().length).toBe(2);

    // Update invisible row -> filter still does not accept, nothing happens
    outline.updateNode({
      id: 'node2',
      text: 'node 2 new'
    }, node);
    table.updateRow({
      id: 'row2',
      nodeId: 'node2',
      cells: ['cell 2 new']
    });
    expect(table.$rows().length).toBe(1);
    expect(outline.$nodes().length).toBe(2);

    // Update invisible row -> filter now accepts, row and node will get visible
    outline.updateNode({
      id: 'node2',
      text: 'node 1 from 2'
    }, node);
    table.updateRow({
      id: 'row2',
      nodeId: 'node2',
      cells: ['cell 1 from 2']
    });
    expect(table.$rows().length).toBe(2);
    expect(outline.$nodes().length).toBe(3);
  });

  it('shows all nodes if column is removed that contains a filter', () => {
    let outline = createOutlineWithDetailTable();
    let node = outline.nodes[0];
    session.desktop.setOutline(outline);
    outline.selectNode(node);
    outline.expandNode(node);

    let table = node.detailTable;
    expect(outline.$nodes().length).toBe(1);

    let filter = tableHelper.createTextColumnFilter(table, table.columns[0], 'cell 1');
    table.addFilter(filter);

    // Insert 2 rows / nodes. The first row is accepted by the filter, the second one isn't.
    table.insertRow({
      id: 'row1',
      nodeId: 'node1',
      cells: ['cell 1']
    });
    outline.insertNode({
      id: 'node1',
      text: 'node 1'
    }, node);
    table.insertRow({
      id: 'row2',
      nodeId: 'node2',
      cells: ['cell 2']
    });
    outline.insertNode({
      id: 'node2',
      text: 'node 2'
    }, node);
    expect(table.$rows().length).toBe(1);
    expect(outline.$nodes().length).toBe(2);

    // Remove the first column (the following events are triggered by the ui server when a column is removed)
    let message = {
      events: [
        tableHelper.createColumnStructureChangedEvent(table, [table.columns[1]]),
        tableHelper.createAllRowsDeletedEvent(table),
        tableHelper.createFiltersChangedEvent(table, []),
        tableHelper.createRowsInsertedEvent(table, [
          {id: 'row1', nodeId: 'node1', cells: ['cell 1']},
          {id: 'row2', nodeId: 'node2', cells: ['cell 2']}
        ])
      ]
    };
    session._processSuccessResponse(message);

    // Expect that all nodes and rows are visible
    expect(table._filterCount()).toBe(0);
    expect(table.$rows().length).toBe(2);
    expect(outline.$nodes().length).toBe(3);
  });
});
