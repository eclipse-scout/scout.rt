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
import org.eclipse.scout.rt.client.opentelemetry.OpenTelemetryExtensionInstrumenterFactory.OpenTelemetryExtensionRequest;
import org.eclipse.scout.rt.client.ui.MouseButton;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.platform.BEANS;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;

public class OpenTelemetryTreeExtension extends AbstractTreeExtension<AbstractTree> {

  private Instrumenter<OpenTelemetryExtensionRequest<AbstractTree>, Void> m_instrumenter;

  private static final AttributeKey<String> NODE_CLASS = AttributeKey.stringKey("scout.extension.node.class");

  public OpenTelemetryTreeExtension(AbstractTree owner) {
    super(owner);
    m_instrumenter = BEANS.get(OpenTelemetryExtensionInstrumenterFactory.class).createInstrumenter(
        getClass(),
        (request) -> request.getOwner().getTitle());
  }

  @Override
  public void execNodeClick(TreeNodeClickChain chain, ITreeNode node, MouseButton mouseButton) {
    new OpenTelemetryExtensionRequest<>(getOwner(), "execNodeClick")
        .withAdditionalAttributesProvider(attributes -> attributes.put(NODE_CLASS, node.getClass().getName()))
        .wrapCall(() -> super.execNodeClick(chain, node, mouseButton), m_instrumenter);
  }
}
