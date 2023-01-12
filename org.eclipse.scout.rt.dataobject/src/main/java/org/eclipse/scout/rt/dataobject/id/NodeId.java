/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.id;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.NodeIdentifier;
import org.eclipse.scout.rt.platform.util.StringUtility;

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
    if (StringUtility.isNullOrEmpty(id)) {
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
