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
package org.eclipse.scout.rt.spec.client.gen.extract.action;

import org.eclipse.scout.rt.client.ui.action.tree.IActionNode;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.spec.client.config.ConfigRegistry;
import org.eclipse.scout.rt.spec.client.gen.extract.AbstractNamedTextExtractor;
import org.eclipse.scout.rt.spec.client.gen.extract.IDocTextExtractor;
import org.eclipse.scout.rt.spec.client.out.mediawiki.MediawikiUtility;

public class HierarchicActionNodeLabelExtractor<T extends IActionNode<?>> extends AbstractNamedTextExtractor<T> implements IDocTextExtractor<T> {

  public HierarchicActionNodeLabelExtractor() {
    super(TEXTS.get("org.eclipse.scout.rt.spec.label"));
  }

  @Override
  public String getText(T actionNode) {
    return getIndent(actionNode) + MediawikiUtility.transformToWiki(actionNode.getText());
  }

  protected String getIndent(T actionNode) {
    IActionNode<?> node = actionNode;
    StringBuilder sb = new StringBuilder();
    do {
      node = node.getParent();
      if (node != null) {
        sb.append(ConfigRegistry.getDocConfigInstance().getIndent());
      }
    }
    while (node != null);
    return sb.toString();
  }

}
