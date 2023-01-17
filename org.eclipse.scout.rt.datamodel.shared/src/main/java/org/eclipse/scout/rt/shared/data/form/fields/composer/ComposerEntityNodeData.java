/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.data.form.fields.composer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.shared.data.form.fields.treefield.TreeNodeData;

/**
 * Data representation for a composer entity value instance in a {@link AbstractComposerData}
 */
public class ComposerEntityNodeData extends TreeNodeData {
  private static final long serialVersionUID = 1L;

  private String m_entityExternalId;
  private boolean m_negated = false;

  public String getEntityExternalId() {
    return m_entityExternalId;
  }

  public void setEntityExternalId(String entityExternalId) {
    m_entityExternalId = entityExternalId;
  }

  /**
   * @return all attributes contained in the subtree of this entity that are part of this entity
   *         <p>
   *         This includes all direct attributes and all attributes under either/or nodes.
   *         <p>
   *         This excludes all other child entities and their attributes.
   */
  public List<ComposerAttributeNodeData> getContainingAttributeNodes() {
    List<ComposerAttributeNodeData> list = new ArrayList<>();
    visitContainingAttributeNodesRec(this, list);
    return list;
  }

  private void visitContainingAttributeNodesRec(TreeNodeData node, List<ComposerAttributeNodeData> list) {
    for (TreeNodeData child : node.getChildNodes()) {
      if (child instanceof ComposerAttributeNodeData) {
        list.add((ComposerAttributeNodeData) child);
      }
      else if (child instanceof ComposerEitherOrNodeData) {
        visitContainingAttributeNodesRec(child, list);
      }
    }
  }

  public boolean isNegative() {
    return m_negated;
  }

  public void setNegative(boolean b) {
    m_negated = b;
  }
}
