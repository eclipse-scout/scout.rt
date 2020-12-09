package org.eclipse.scout.rt.dataobject.id;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.NodeIdentifier;

/**
 * Represents the unique identifier for one node.
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
   * Returns the {@link NodeId} of the current node.
   *
   * @see NodeIdentifier
   */
  public static NodeId current() {
    return NodeId.of(BEANS.get(NodeIdentifier.class).get());
  }
}
