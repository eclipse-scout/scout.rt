/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swt.basic.table.internal;

/*
 * TableEditor example snippet: edit a cell in a table (in place, fancy)
 *
 * For a list of all SWT example snippets see
 * http://dev.eclipse.org/viewcvs/index.cgi/%7Echeckout%7E/platform-swt-home/dev.html#snippets
 */

import java.util.Vector;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorDeactivationEvent;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class SnippetForTableEditor {
  final String[][] database = new String[][]{new String[]{"A1", "B1", "C1"}, new String[]{"A2", "B2", "C2"}, new String[]{"A3", "B3", "C3"}};
  final TableViewer viewer;

  public static void main(String[] args) {
    new SnippetForTableEditor();
  }

  SnippetForTableEditor() {
    Display display = new Display();
    Shell shell = new Shell(display);
    shell.setLayout(new FillLayout());
    final Table table = new Table(shell, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
    table.setLinesVisible(true);
    table.setHeaderVisible(true);
    for (int i = 0; i < 3; i++) {
      TableColumn column = new TableColumn(table, SWT.NONE);
      column.setWidth(100);
      column.setMoveable(true);
    }
    //
    viewer = new TableViewer(table);
    ContentProvider content = new ContentProvider();
    viewer.setContentProvider(content);
    viewer.setLabelProvider(content);
    //editors
    viewer.setColumnProperties(new String[]{"A", "B", "C"});
    CellEditor cellEditor = new AdHocCellEditor(viewer.getTable());
    viewer.setCellEditors(new CellEditor[]{cellEditor, null, cellEditor});
    viewer.setCellModifier(new CellModifier());
    viewer.setInput(System.currentTimeMillis());//force changed

    shell.pack();
    shell.open();
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch()) {
        display.sleep();
      }
    }
    display.dispose();
  }

  class CellModifier implements ICellModifier {
    @Override
    public void modify(Object element, String property, Object value) {
      //Note: element is a TableItem, NOT the row value
      if (element instanceof TableItem) {
        TableItem item = (TableItem) element;
        String[] row = (String[]) item.getData();
        System.out.println("Modified " + property + " " + value);
        if ("A".equals(property)) {
          row[0] = (String) value;
        }
        else if ("B".equals(property)) {
          row[1] = (String) value;
        }
        else if ("C".equals(property)) {
          row[2] = (String) value;
        }
        viewer.setInput(System.currentTimeMillis());
      }
    }

    @Override
    public Object getValue(Object element, String property) {
      //Note: element is the row value, NOT a TableItem
      if ("A".equals(property)) {
        return ((String[]) element)[0];
      }
      else if ("B".equals(property)) {
        return ((String[]) element)[1];
      }
      else if ("C".equals(property)) {
        return ((String[]) element)[2];
      }
      return null;
    }

    @Override
    public boolean canModify(Object element, String property) {
      return true;
    }
  }

  class ContentProvider implements ITableLabelProvider, IStructuredContentProvider {
    private final Vector<ILabelProviderListener> listeners = new Vector<ILabelProviderListener>();

    @Override
    public Object[] getElements(Object inputElement) {
      return database;
    }

    @Override
    public void dispose() {
    }

    @Override
    public void inputChanged(Viewer viewer2, Object oldInput, Object newInput) {
    }

    @Override
    public Image getColumnImage(Object element, int columnIndex) {
      return null;
    }

    @Override
    public String getColumnText(Object element, int columnIndex) {
      return ((String[]) element)[columnIndex];
    }

    @Override
    public void addListener(ILabelProviderListener listener) {
      listeners.add(listener);
    }

    @Override
    public boolean isLabelProperty(Object element, String property) {
      return false;
    }

    @Override
    public void removeListener(ILabelProviderListener listener) {
      listeners.remove(listener);
    }
  }

  class AdHocCellEditor extends CellEditor {
    private Composite m_container;

    public AdHocCellEditor(Composite parent) {
      super(parent);
    }

    @Override
    public void activate(ColumnViewerEditorActivationEvent e) {
      if (e.getSource() instanceof ViewerCell) {
        ViewerCell cell = (ViewerCell) e.getSource();
        System.out.println("row: " + cell.getElement() + ", col: " + cell.getColumnIndex());
        Text text = new Text(m_container, SWT.BORDER);
        text.setText("abc");
        m_container.layout();
        m_container.setVisible(true);
      }
    }

    @Override
    protected void deactivate(ColumnViewerEditorDeactivationEvent event) {
      for (Control c : m_container.getChildren()) {
        c.dispose();
      }
      super.deactivate(event);
      if (event.eventType == ColumnViewerEditorDeactivationEvent.EDITOR_CANCELED) {
        System.out.println("canceled");//XXX
      }
    }

    @Override
    protected Control createControl(Composite parent) {
      m_container = new Composite(parent, SWT.NONE);
      m_container.setLayout(new FillLayout());
      return m_container;
    }

    @Override
    protected Object doGetValue() {
      return "ABC";
    }

    @Override
    protected void doSetFocus() {
      m_container.setFocus();
    }

    @Override
    protected void doSetValue(Object value) {
    }

  }

}
