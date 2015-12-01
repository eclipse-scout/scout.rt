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
package org.eclipse.scout.rt.client.ui.form.fields.composer.node;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenuSeparator;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.form.fields.composer.IComposerField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.ScoutTexts;

public class EitherOrNode extends AbstractComposerNode {
  private boolean m_beginEitherOr;
  private boolean m_negated = false;

  public EitherOrNode(IComposerField composerField, boolean beginEitherOr) {
    super(composerField, false);
    m_beginEitherOr = beginEitherOr;
    callInitializer();
  }

  public boolean isBeginOfEitherOr() {
    return m_beginEitherOr;
  }

  public void setBeginOfEitherOr(boolean b) {
    m_beginEitherOr = b;
  }

  public boolean isEndOfEitherOr() {
    ITreeNode next = getSiblingAfter();
    return !(next instanceof EitherOrNode && !((EitherOrNode) next).isBeginOfEitherOr());
  }

  public boolean isNegative() {
    return m_negated;
  }

  public void setNegative(boolean b) {
    m_negated = b;
  }

  @Override
  protected void execInitTreeNode() {
    List<IMenu> menus = new ArrayList<IMenu>();
    for (IMenu m : getMenus()) {
      if (m.getClass() == AddEntityOnEitherOrPlaceholderMenu.class) {
        attachAddEntityMenus(menus);
      }
      else {
        menus.add(m);
      }
    }
    setMenus(menus);
  }

  @Override
  protected void execDecorateCell(Cell cell) {
    String text = "";
    ITreeNode siblingBefore = getSiblingBefore();
    if (isBeginOfEitherOr() && siblingBefore != null) {
      text += ScoutTexts.get("ExtendedSearchAnd") + " ";
    }
    if (isBeginOfEitherOr()) {
      text += ScoutTexts.get("ExtendedSearchEither");
    }
    else {
      text += ScoutTexts.get("ExtendedSearchOr");
    }
    if (isNegative()) {
      text += " " + ScoutTexts.get("ExtendedSearchNot");
    }
    cell.setText(text);
  }

  @Order(1)
  public class AddAdditionalOrMenu extends AbstractMenu {
    @Override
    protected String getConfiguredText() {
      return ScoutTexts.get("ExtendedSearchAddAdditionalOrMenu");
    }

    @Override
    protected void execAction() {
      getComposerField().addAdditionalOrNode(EitherOrNode.this, false);
    }
  }

  @Order(5)
  public class AddEitherOrMenu extends AbstractMenu {
    @Override
    protected String getConfiguredText() {
      return ScoutTexts.get("ExtendedSearchAddEitherOrMenu");
    }

    @Override
    protected void execAction() {
      ITreeNode node = getComposerField().addEitherNode(EitherOrNode.this, false);
      getComposerField().addAdditionalOrNode(node, false);
    }
  }

  @Order(10)
  public class Separator1Menu extends AbstractMenuSeparator {
  }

  @Order(20)
  public class NegateEitherOrMenu extends AbstractMenu {

    @Override
    protected String getConfiguredText() {
      return ScoutTexts.get("ExtendedSearchNegateMenu");
    }

    @Override
    protected void execAction() {
      setNegative(!isNegative());
      if (!isStatusInserted()) {
        setStatusInternal(ITreeNode.STATUS_UPDATED);
      }
      update();
    }
  }

  @Order(30)
  public class DeleteEitherOrMenu extends AbstractMenu {

    @Override
    protected String getConfiguredText() {
      return ScoutTexts.get("ExtendedSearchRemoveMenu");
    }

    @Override
    protected String getConfiguredKeyStroke() {
      return "delete";
    }

    @Override
    protected void execAction() {
      ITreeNode next = getSiblingAfter();
      if (isBeginOfEitherOr()) {
        if (next instanceof EitherOrNode && !((EitherOrNode) next).isBeginOfEitherOr()) {
          ((EitherOrNode) next).setBeginOfEitherOr(true);
          ((EitherOrNode) next).update();
        }
      }
      getTree().selectPreviousParentNode();
      getTree().removeNode(EitherOrNode.this);
    }
  }

  @Order(40)
  public class Separator2Menu extends AbstractMenuSeparator {
  }

  @Order(50)
  public class AddAttributeOnEitherOrMenu extends AbstractAddAttributeMenu {
    public AddAttributeOnEitherOrMenu() {
      super(getComposerField(), EitherOrNode.this);
    }
  }

  @Order(60)
  public class Separator3Menu extends AbstractMenuSeparator {
  }

  @Order(70)
  public class AddEntityOnEitherOrPlaceholderMenu extends AbstractMenuSeparator {
  }
}
