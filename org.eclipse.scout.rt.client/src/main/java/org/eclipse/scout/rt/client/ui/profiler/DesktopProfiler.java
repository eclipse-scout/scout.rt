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
package org.eclipse.scout.rt.client.ui.profiler;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.platform.util.NumberFormatProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is Thread safe
 */
public final class DesktopProfiler {
  private static final Logger LOG = LoggerFactory.getLogger(DesktopProfiler.class);
  private static DesktopProfiler instance = new DesktopProfiler();

  public static DesktopProfiler getInstance() {
    return instance;
  }

  private boolean m_enabled = false;
  private Object m_listLock = new Object();
  private final List<WeakReference<ITree>> m_trees;
  private final List<WeakReference<ITable>> m_tables;
  private final List<WeakReference<ITreeNode>> m_treeNodes;
  private final List<WeakReference<ITableRow>> m_tableRows;
  private final List<WeakReference<IForm>> m_forms;
  private final List<WeakReference<IFormField>> m_formFields;
  private final List<WeakReference<Object>> m_objects;

  private DesktopProfiler() {
    m_listLock = new Object();
    m_trees = new ArrayList<WeakReference<ITree>>();
    m_treeNodes = new ArrayList<WeakReference<ITreeNode>>();
    m_tables = new ArrayList<WeakReference<ITable>>();
    m_tableRows = new ArrayList<WeakReference<ITableRow>>();
    m_forms = new ArrayList<WeakReference<IForm>>();
    m_formFields = new ArrayList<WeakReference<IFormField>>();
    m_objects = new ArrayList<WeakReference<Object>>();
  }

  public boolean isEnabled() {
    return m_enabled;
  }

  public void setEnabled(boolean enabled) {
    m_enabled = enabled;
  }

  /**
   * Add a weak reference to a tree NOTE: the passed argument MUST be referenced by the source type, otherwise it is
   * garbage collected immediately after adding
   */
  public void registerTree(ITree tree) {
    if (!m_enabled) {
      return;
    }
    synchronized (m_listLock) {
      m_trees.add(new WeakReference<ITree>(tree));
    }
  }

  /**
   * Add a weak reference to a tree node NOTE: the passed argument MUST be referenced by the source type, otherwise it
   * is garbage collected immediately after adding
   */
  public void registerTreeNode(ITreeNode node) {
    if (!m_enabled) {
      return;
    }
    synchronized (m_listLock) {
      m_treeNodes.add(new WeakReference<ITreeNode>(node));
    }
  }

  /**
   * Add a weak reference to a table NOTE: the passed argument MUST be referenced by the source type, otherwise it is
   * garbage collected immediately after adding
   */
  public void registerTable(ITable table) {
    if (!m_enabled) {
      return;
    }
    synchronized (m_listLock) {
      m_tables.add(new WeakReference<ITable>(table));
    }
  }

  /**
   * Add a weak reference to a table row NOTE: the passed argument MUST be referenced by the source type, otherwise it
   * is garbage collected immediately after adding
   */
  public void registerTableRow(ITableRow row) {
    if (!m_enabled) {
      return;
    }
    synchronized (m_listLock) {
      m_tableRows.add(new WeakReference<ITableRow>(row));
    }
  }

  /**
   * Add a weak reference to a form NOTE: the passed argument MUST be referenced by the source type, otherwise it is
   * garbage collected immediately after adding
   */
  public void registerForm(IForm form) {
    if (!m_enabled) {
      return;
    }
    synchronized (m_listLock) {
      m_forms.add(new WeakReference<IForm>(form));
    }
  }

  /**
   * Add a weak reference to a form field NOTE: the passed argument MUST be referenced by the source type, otherwise it
   * is garbage collected immediately after adding
   */
  public void registerFormField(IFormField formField) {
    if (!m_enabled) {
      return;
    }
    synchronized (m_listLock) {
      m_formFields.add(new WeakReference<IFormField>(formField));
    }
  }

  /**
   * Add a weak reference to an arbitrary object NOTE: the passed argument MUST be referenced by the source type,
   * otherwise it is garbage collected immediately after adding
   */
  public void registerObject(Object o) {
    if (!m_enabled) {
      return;
    }
    synchronized (m_listLock) {
      m_objects.add(new WeakReference<Object>(o));
    }
  }

  public void dump() {
    dump(System.out);
  }

  public void dump(OutputStream o) {
    /**
     * this call to gc is intended
     */
    System.gc();
    if (!m_enabled) {
      return;
    }
    PrintWriter out = new PrintWriter(o, true);
    try {
      synchronized (m_listLock) {
        manageListsWithoutLocking();
        //
        NumberFormat fmt = BEANS.get(NumberFormatProvider.class).getIntegerInstance(NlsLocale.get());
        out.println("Max memory:   " + fmt.format(Runtime.getRuntime().maxMemory()));
        out.println("Total memory: " + fmt.format(Runtime.getRuntime().totalMemory()));
        out.println("Free memory:  " + fmt.format(Runtime.getRuntime().freeMemory()));
        out.println("(Used memory):" + fmt.format(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
        out.println();
        dumpWeakList(out, "TREE", m_trees);
        dumpWeakList(out, "TREE NODE", m_trees);
        dumpWeakList(out, "TABLE", m_tables);
        dumpWeakList(out, "TABLE ROW", m_tableRows);
        dumpWeakList(out, "FORM", m_forms);
        dumpWeakList(out, "FORM FIELD", m_formFields);
        dumpWeakList(out, "VARIOUS", m_objects);
      }
    }
    catch (Exception t) {
      LOG.error("Error writing dump", t);
    }
    finally {
      if (o != System.out) {
        out.close();
      }
    }
  }

  private <T> void dumpWeakList(PrintWriter out, String title, List<WeakReference<T>> weakList) {
    HashMap<Class, Integer> typesMap = new HashMap<Class, Integer>();
    for (WeakReference<T> ref : weakList) {
      T o = ref.get();
      if (o != null) {
        Integer n = typesMap.get(o.getClass());
        typesMap.put(o.getClass(), n != null ? n + 1 : 1);
      }
    }
    TreeMap<String, Integer> sortMap = new TreeMap<String, Integer>();
    for (Map.Entry<Class, Integer> entry : typesMap.entrySet()) {
      sortMap.put(entry.getKey().getName(), entry.getValue());
    }
    out.println(title);
    for (Map.Entry<String, Integer> entry : sortMap.entrySet()) {
      out.println(" " + entry.getKey() + " " + entry.getValue());
    }
  }

  private void manageListsWithoutLocking() {
    for (Iterator<WeakReference<ITreeNode>> it = m_treeNodes.iterator(); it.hasNext();) {
      WeakReference<ITreeNode> ref = it.next();
      if (ref.get() == null) {
        it.remove();
      }
    }
    for (Iterator<WeakReference<ITableRow>> it = m_tableRows.iterator(); it.hasNext();) {
      WeakReference<ITableRow> ref = it.next();
      if (ref.get() == null) {
        it.remove();
      }
    }
  }

}
