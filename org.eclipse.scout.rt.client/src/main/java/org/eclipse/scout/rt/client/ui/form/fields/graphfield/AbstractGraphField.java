/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.client.ui.form.fields.graphfield;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.annotations.ClassId;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.graph.AbstractGraph;
import org.eclipse.scout.rt.client.ui.basic.graph.IGraph;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;

@ClassId("d80d7fe0-b7b5-40e1-af62-a5518d3f67eb")
public class AbstractGraphField<T extends IGraph> extends AbstractFormField implements IGraphField<T> {

  private T m_graph;

  public AbstractGraphField() {
    super(true);
  }

  public AbstractGraphField(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected void initConfig() {
    super.initConfig();
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
    List<IGraph> contributedGraphs = m_contributionHolder.getContributionsByClass(IGraph.class);
    IGraph result = CollectionUtility.firstElement(contributedGraphs);
    if (result != null) {
      return (T) result;
    }
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
      if (getForm() != null) {
        getForm().structureChanged(this);
      }
      updateKeyStrokes();
    }
  }

  public class Graph extends AbstractGraph {
  }
}
