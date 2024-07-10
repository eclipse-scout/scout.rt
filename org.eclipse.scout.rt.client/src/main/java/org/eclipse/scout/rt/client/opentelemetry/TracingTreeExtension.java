/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.opentelemetry;

import org.eclipse.scout.rt.client.extension.ui.basic.tree.AbstractTreeExtension;
import org.eclipse.scout.rt.client.extension.ui.basic.tree.TreeChains.TreeNodeClickChain;
import org.eclipse.scout.rt.client.ui.MouseButton;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.opentelemetry.ITracingHelper;

import io.opentelemetry.api.trace.Tracer;

public class TracingTreeExtension extends AbstractTreeExtension<AbstractTree> {

  private Tracer m_tracer;

  public TracingTreeExtension(AbstractTree owner) {
    super(owner);
    m_tracer = BEANS.get(ITracingHelper.class).createTracer(TracingTreeExtension.class);
  }

  @Override
  public void execNodeClick(TreeNodeClickChain chain, ITreeNode node, MouseButton mouseButton) {
    String name = getOwner().getClass().getSimpleName() + "#execNodeClick";
    BEANS.get(ITracingHelper.class).wrapInSpan(m_tracer, name, span -> {
      span.setAttribute("scout.client.tree.class", getOwner().getClass().getName());
      span.setAttribute("scout.client.tree.text", getOwner().getTitle());
      span.setAttribute("scout.client.tree.event.mouseButton", mouseButton.name());
      span.setAttribute("scout.client.tree.event.node.id", node.getNodeId());
      span.setAttribute("scout.client.tree.event.node.text", node.getCell().getText());
      super.execNodeClick(chain, node, mouseButton);
    });
  }
}
