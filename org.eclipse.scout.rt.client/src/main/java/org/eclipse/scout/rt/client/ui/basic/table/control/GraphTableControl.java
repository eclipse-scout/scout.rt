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
package org.eclipse.scout.rt.client.ui.basic.table.control;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.graph.AbstractGraph;
import org.eclipse.scout.rt.client.ui.basic.graph.IGraph;
import org.eclipse.scout.rt.client.ui.form.fields.graphfield.AbstractGraphField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.rt.shared.TEXTS;

public class GraphTableControl<T extends IGraph> extends AbstractTableControl implements IGraphTableControl<T> {

  private T m_graph;

  public GraphTableControl() {
    this(true);
  }

  public GraphTableControl(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setTooltipText(TEXTS.get("ui.Network"));
    setIconId(AbstractIcons.Graph);

    setGraphInternal(createGraph());
    // local enabled listener
    addPropertyChangeListener(PROP_ENABLED, new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent e) {
        if (m_graph != null) {
          m_graph.setEnabled(isEnabled());
        }
      }
    });
  }

  protected Class<? extends IGraph> getConfiguredGraph() {
    Class<?>[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    List<Class<IGraph>> fc = ConfigurationUtility.filterClasses(dca, IGraph.class);
    if (fc.size() == 1) {
      return CollectionUtility.firstElement(fc);
    }
    else {
      for (Class<? extends IGraph> c : fc) {
        if (c.getDeclaringClass() != AbstractGraphField.class) {
          return c;
        }
      }
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  protected T createGraph() {
    Class<? extends IGraph> configuredGraph = getConfiguredGraph();
    if (configuredGraph != null) {
      try {
        return (T) ConfigurationUtility.newInnerInstance(this, configuredGraph);
      }
      catch (Exception e) {
        BEANS.get(ExceptionHandler.class).handle(new ProcessingException("Error while creating instance of class '" + configuredGraph.getName() + "'.", e));
      }
    }
    return null;
  }

  @Override
  public final T getGraph() {
    return m_graph;
  }

  @Override
  public void setGraph(T graph) {
    setGraphInternal(graph);
  }

  protected void setGraphInternal(T graph) {
    if (m_graph == graph) {
      return;
    }
    if (m_graph != null) {
      m_graph.setContainerInternal(null);
    }
    m_graph = graph;
    if (m_graph != null) {
      m_graph.setContainerInternal(this);
      m_graph.setEnabled(isEnabled());
    }
    boolean changed = propertySupport.setProperty(PROP_GRAPH, m_graph);
    if (changed) {
      // nothing to do in table control
    }
  }

  public class Graph extends AbstractGraph {
  }
}
