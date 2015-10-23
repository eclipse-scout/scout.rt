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
package org.eclipse.scout.rt.client.ui.basic.graph;

import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.ITypeWithClassId;
import org.eclipse.scout.commons.annotations.ClassId;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.beans.AbstractPropertyObserver;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ModelContextProxy;
import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.shared.data.basic.graph.GraphModel;
import org.eclipse.scout.rt.shared.data.basic.graph.GraphNode;

/**
 * @since 5.2
 */
@ClassId("f490fe36-4506-47a4-af68-3b173cc2054e")
public abstract class AbstractGraph extends AbstractPropertyObserver implements IGraph {
  // TODO make extensible

  private IGraphUIFacade m_uiFacade;
  private final EventListenerList m_listenerList = new EventListenerList();

  public AbstractGraph() {
    this(true);
  }

  public AbstractGraph(boolean callInitializer) {
    if (callInitializer) {
      callInitializer();
    }
  }

  @Override
  public String classId() {
    String simpleClassId = ConfigurationUtility.getAnnotatedClassIdWithFallback(getClass());
    if (getContainer() != null) {
      return simpleClassId + ID_CONCAT_SYMBOL + getContainer().classId();
    }
    return simpleClassId;
  }

  protected void callInitializer() {
    initConfig();
  }

  /*
   * Configuration
   */
  protected void initConfig() {
    m_uiFacade = BEANS.get(ModelContextProxy.class).newProxy(new P_UIFacade(), ModelContext.copyCurrent());
    setAutoColor(getConfiguredAutoColor());
    setEnabled(getConfiguredEnabled());
    setVisible(getConfiguredVisible());
    setClickable(getConfiguredClickable());
    setModelHandlesClick(getConfiguredModelHandelsClick());
    setAnimated(getConfiguredAnimated());
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(20)
  protected boolean getConfiguredAutoColor() {
    return true;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(20)
  protected boolean getConfiguredEnabled() {
    return true;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(20)
  protected boolean getConfiguredVisible() {
    return true;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(40)
  protected boolean getConfiguredModelHandelsClick() {
    return false;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(40)
  protected boolean getConfiguredClickable() {
    return false;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(40)
  protected boolean getConfiguredAnimated() {
    return true;
  }

  @ConfigOperation
  @Order(230)
  protected void execAppLinkAction(String ref) {
  }

  @Override
  public IGraphUIFacade getUIFacade() {
    return m_uiFacade;
  }

  /**
   * do not use this internal method unless you are implementing a container that holds and controls an {@link IGraph}
   */
  @Override
  public void setContainerInternal(ITypeWithClassId container) {
    propertySupport.setProperty(PROP_CONTAINER, container);
  }

  @Override
  public ITypeWithClassId getContainer() {
    return (ITypeWithClassId) propertySupport.getProperty(PROP_CONTAINER);
  }

  @Override
  public void addGraphListener(GraphListener listener) {
    m_listenerList.add(GraphListener.class, listener);
  }

  @Override
  public void removeGraphListener(GraphListener listener) {
    m_listenerList.remove(GraphListener.class, listener);
  }

  public void fireNodeAction(GraphNode node) {
    GraphEvent event = new GraphEvent(this, GraphEvent.TYPE_NODE_ACTION);
    event.setNode(node);
    GraphListener[] listeners = m_listenerList.getListeners(GraphListener.class);
    for (GraphListener listener : listeners) {
      listener.graphChanged(event);
    }
  }

  @Override
  public void setAutoColor(boolean isAutoColor) {
    propertySupport.setProperty(PROP_AUTO_COLOR, isAutoColor);
  }

  @Override
  public boolean isAutoColor() {
    return propertySupport.getPropertyBool(PROP_AUTO_COLOR);
  }

  @Override
  public void setGraphModel(GraphModel graphModel) {
    propertySupport.setProperty(PROP_GRAPH_MODEL, graphModel);
  }

  @Override
  public GraphModel getGraphModel() {
    return (GraphModel) propertySupport.getProperty(PROP_GRAPH_MODEL);
  }

  @Override
  public void setEnabled(boolean enabled) {
    propertySupport.setPropertyBool(PROP_ENABLED, enabled);
  }

  @Override
  public boolean isEnabled() {
    return propertySupport.getPropertyBool(PROP_ENABLED);
  }

  @Override
  public void setVisible(boolean visible) {
    propertySupport.setPropertyBool(PROP_VISIBLE, visible);
  }

  @Override
  public boolean isVisible() {
    return propertySupport.getPropertyBool(PROP_VISIBLE);
  }

  @Override
  public boolean isClickable() {
    return propertySupport.getPropertyBool(PROP_CLICKABLE);
  }

  @Override
  public void setClickable(boolean clickable) {
    propertySupport.setPropertyBool(PROP_CLICKABLE, clickable);
  }

  @Override
  public boolean isModelHandlesClick() {
    return propertySupport.getPropertyBool(PROP_MODEL_HANDLES_CLICK);
  }

  @Override
  public void setModelHandlesClick(boolean modelHandlesClick) {
    propertySupport.setPropertyBool(PROP_MODEL_HANDLES_CLICK, modelHandlesClick);
  }

  @Override
  public boolean isAnimated() {
    return propertySupport.getPropertyBool(PROP_ANIMATED);
  }

  @Override
  public void setAnimated(boolean animated) {
    propertySupport.setPropertyBool(PROP_ANIMATED, animated);
  }

  @Override
  public void doAppLinkAction(String ref) {
    execAppLinkAction(ref);
  }

  protected class P_UIFacade implements IGraphUIFacade {

    @Override
    public void fireNodeActionFromUI(GraphNode node) {
      try {
        if (isEnabled() && isVisible()) {
          fireNodeAction(node);
        }
      }
      catch (Exception e) {
        BEANS.get(ExceptionHandler.class).handle(e);
      }
    }

    @Override
    public void fireAppLinkActionFromUI(String ref) {
      try {
        if (isEnabled() && isVisible()) {
          doAppLinkAction(ref);
        }
      }
      catch (ProcessingException e) {
        BEANS.get(ExceptionHandler.class).handle(e);
      }
    }
  }
}
