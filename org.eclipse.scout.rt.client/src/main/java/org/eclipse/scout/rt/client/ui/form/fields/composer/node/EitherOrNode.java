/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
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
import org.eclipse.scout.rt.platform.classid.ClassId;
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
  @ClassId("86864de8-85bf-4274-a134-2ec8ae602315")
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
  @ClassId("7a388969-391d-4920-8f90-23759f53f86c")
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
  @ClassId("28fc0085-83af-4b19-95f7-1ed70c43f1c8")
  public class Separator1Menu extends AbstractMenuSeparator {
  }

  @Order(20)
  @ClassId("b8237afc-8794-498b-a6a5-efa1848ef201")
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
  @ClassId("7461e694-1f88-438f-91b4-93e65db76f3e")
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
      if (isBeginOfEitherOr() && next instanceof EitherOrNode) {
        final EitherOrNode eitherOrNode = (EitherOrNode) next;
        if (!eitherOrNode.isBeginOfEitherOr()) {
          eitherOrNode.setBeginOfEitherOr(true);
          eitherOrNode.update();
        }
      }
      getTree().selectPreviousParentNode();
      getTree().removeNode(EitherOrNode.this);
    }
  }

  @Order(40)
  @ClassId("59c754f8-684b-4eea-80ff-11e1b1b916c2")
  public class Separator2Menu extends AbstractMenuSeparator {
  }

  @Order(50)
  @ClassId("b444ace8-d032-44d0-941a-a261bc48777b")
  public class AddAttributeOnEitherOrMenu extends AbstractAddAttributeMenu {
    public AddAttributeOnEitherOrMenu() {
      super(getComposerField(), EitherOrNode.this);
    }
  }

  @Order(60)
  @ClassId("1914f76c-4635-4b54-ae29-6ec1857bca90")
  public class Separator3Menu extends AbstractMenuSeparator {
  }

  @Order(70)
  @ClassId("567ee492-6de3-4801-a4bc-2476c5392b7c")
  public class AddEntityOnEitherOrPlaceholderMenu extends AbstractMenuSeparator {
  }
}
