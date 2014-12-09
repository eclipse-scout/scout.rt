package org.eclipse.scout.rt.client.extension.ui.action.tree;

import org.eclipse.scout.rt.client.extension.ui.action.AbstractActionExtension;
import org.eclipse.scout.rt.client.ui.action.tree.AbstractActionNode;
import org.eclipse.scout.rt.client.ui.action.tree.IActionNode;

public abstract class AbstractActionNodeExtension<T extends IActionNode, OWNER extends AbstractActionNode<T>> extends AbstractActionExtension<OWNER> implements IActionNodeExtension<T, OWNER> {

  public AbstractActionNodeExtension(OWNER owner) {
    super(owner);
  }
}
