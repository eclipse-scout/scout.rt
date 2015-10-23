/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.basic.graph;

import org.eclipse.scout.rt.shared.data.basic.graph.GraphNode;

/**
 * @since 5.2
 */
public interface IGraphUIFacade {

  void fireNodeActionFromUI(GraphNode node);

  void fireAppLinkActionFromUI(String ref);
}
