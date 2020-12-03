package org.eclipse.scout.rt.dataobject.id;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.NodeIdentifier;

/**
 * Representing unique identifier of one node.
 *
 * @see NodeIdentifier
 */
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
   * Returns {@link NodeId} of current node.
   *
   * @see NodeIdentifier
   */
  public static NodeId current() {
    return NodeId.of(BEANS.get(NodeIdentifier.class).get());
  }
}
