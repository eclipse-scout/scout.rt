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
package org.eclipse.scout.rt.dataobject.id;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.NodeIdentifier;

/**
 * Represents the unique identifier for one node.
 *
 * @see NodeIdentifier
 */
@IdTypeName("scout.NodeId")
public final class NodeId extends AbstractStringId {
  private static final long serialVersionUID = 1L;

  private NodeId(String id) {
    super(id);
  }

  public static NodeId of(String id) {
    if (id == null) {
      return null;
    }
    return new NodeId(id);
  }

  /**
   * Returns the {@link NodeId} of the current node.
   *
   * @see NodeIdentifier
   */
  public static NodeId current() {
    //noinspection deprecation
    return NodeId.of(BEANS.get(NodeIdentifier.class).get());
  }
}
