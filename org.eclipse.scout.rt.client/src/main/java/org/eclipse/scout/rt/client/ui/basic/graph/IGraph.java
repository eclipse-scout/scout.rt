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

import org.eclipse.scout.commons.ITypeWithClassId;
import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.rt.client.ui.IAppLinkCapable;
import org.eclipse.scout.rt.shared.data.basic.graph.GraphModel;

/**
 * @since 5.2
 */
public interface IGraph extends IPropertyObserver, ITypeWithClassId, IAppLinkCapable {

  String PROP_CONTAINER = "container";
  String PROP_AUTO_COLOR = "autoColor";
  String PROP_GRAPH_MODEL = "graphModel";
  String PROP_ENABLED = "enabled";
  String PROP_VISIBLE = "visible";
  String PROP_CLICKABLE = "clickable";
  String PROP_MODEL_HANDLES_CLICK = "modelHandlesClick";
  String PROP_ANIMATED = "animated";

  IGraphUIFacade getUIFacade();

  void setContainerInternal(ITypeWithClassId container);

  ITypeWithClassId getContainer();

  void addGraphListener(GraphListener listener);

  void removeGraphListener(GraphListener listener);

  void setAutoColor(boolean autoColor);

  boolean isAutoColor();

  // TODO BSH Use interface?
  void setGraphModel(GraphModel graphModel);

  GraphModel getGraphModel();

  void setEnabled(boolean enabled);

  boolean isEnabled();

  void setVisible(boolean visible);

  boolean isVisible();

  void setClickable(boolean clickable);

  boolean isClickable();

  void setModelHandlesClick(boolean modelHandlesClick);

  boolean isModelHandlesClick();

  boolean isAnimated();

  void setAnimated(boolean animated);
}
