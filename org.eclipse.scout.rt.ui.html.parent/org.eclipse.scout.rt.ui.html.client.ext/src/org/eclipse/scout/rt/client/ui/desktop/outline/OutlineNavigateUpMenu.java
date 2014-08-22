package org.eclipse.scout.rt.client.ui.desktop.outline;

import java.util.Set;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.nls.NlsUtility;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.form.FormMenuType;
import org.eclipse.scout.rt.shared.ui.menu.AbstractMenu5;

public class OutlineNavigateUpMenu extends AbstractMenu5 {
  private IOutline m_outline;

  public OutlineNavigateUpMenu(IOutline outline) {
    super(false);
    m_outline = outline;
    callInitializer();
  }

  public void notifyParentNodeChanged() {
    updateText();
  }

  protected void updateText() {
    String parentName = m_outline.getSelectedNode().getParentNode().getCell().getText();
    String text = NlsUtility.bindText("Zurück zu {0}", parentName); //FIXME CGU translation
    setText(text);
  }

  @Override
  protected void execInitAction() throws ProcessingException {
    updateText();
  }

  @Override
  protected Set<? extends IMenuType> getConfiguredMenuTypes() {
    return CollectionUtility.hashSet(FormMenuType.System);
  }

  //FIXME CGU maybe do this in gui to make it more responsive?
  @Override
  protected void execAction() throws ProcessingException {
    ITreeNode parentNode = m_outline.getSelectedNode().getParentNode();
    m_outline.selectNode(parentNode);
    parentNode.setExpanded(false);

  }
}
