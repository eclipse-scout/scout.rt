package org.eclipse.scout.rt.client.extension.ui.action.tree;

import org.eclipse.scout.rt.client.extension.ui.action.IActionExtension;
import org.eclipse.scout.rt.client.ui.action.tree.AbstractActionNode;
import org.eclipse.scout.rt.client.ui.action.tree.IActionNode;

public interface IActionNodeExtension<T extends IActionNode, OWNER extends AbstractActionNode<T>> extends IActionExtension<OWNER> {
}
