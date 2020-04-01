/*
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui.form.fields.composer;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.form.fields.composer.node.AddEntityMenu;
import org.eclipse.scout.rt.client.ui.form.fields.composer.node.EitherOrNode;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.data.form.fields.composer.ComposerEitherOrNodeData;
import org.eclipse.scout.rt.shared.data.form.fields.treefield.AbstractTreeFieldData;
import org.eclipse.scout.rt.shared.data.form.fields.treefield.TreeNodeData;
import org.eclipse.scout.rt.shared.data.model.AbstractDataModelAttribute;
import org.eclipse.scout.rt.shared.data.model.AbstractDataModelEntity;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @since 3.8.2
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class ComposerFieldTest {

  private ComposerField m_composerField;
  private ComposerFieldData m_fieldData;

  @Before
  public void before() {
    m_composerField = new ComposerField();
    m_fieldData = new ComposerFieldData();
  }

  @Test
  public void testFixture() {
    m_composerField.exportFormFieldData(m_fieldData);
    assertEquals(0, m_fieldData.getRootCount());
  }

  @Test
  public void testExportEitherOrNode() {
    // setup field
    ITreeNode parentNode = m_composerField.getTree().getRootNode();
    EitherOrNode eitherNode = m_composerField.addEitherNode(parentNode, false);
    m_composerField.addAdditionalOrNode(eitherNode, false);

    // export
    m_composerField.exportFormFieldData(m_fieldData);

    // verify export
    assertEquals(2, m_fieldData.getRootCount());
    //
    TreeNodeData eitherNodeData = m_fieldData.getRoots().get(0);
    assertTrue(eitherNodeData instanceof ComposerEitherOrNodeData);
    assertTrue(((ComposerEitherOrNodeData) eitherNodeData).isBeginOfEitherOr());
    assertFalse(((ComposerEitherOrNodeData) eitherNodeData).isNegative());
    //
    TreeNodeData orNodeData = m_fieldData.getRoots().get(1);
    assertTrue(orNodeData instanceof ComposerEitherOrNodeData);
    assertFalse(((ComposerEitherOrNodeData) orNodeData).isBeginOfEitherOr());
    assertFalse(((ComposerEitherOrNodeData) orNodeData).isNegative());
  }

  @Test
  public void testImportEitherOrNode() {
    // setup field data
    ComposerEitherOrNodeData eitherNodeData = new ComposerEitherOrNodeData();
    eitherNodeData.setBeginOfEitherOr(true);
    ComposerEitherOrNodeData orNodeData = new ComposerEitherOrNodeData();
    List<TreeNodeData> rootList = Arrays.<TreeNodeData> asList(eitherNodeData, orNodeData);
    m_fieldData.setRoots(rootList);

    // import
    m_composerField.importFormFieldData(m_fieldData, false);

    // verify import
    ITreeNode rootNode = m_composerField.getTree().getRootNode();
    assertEquals(2, rootNode.getChildNodeCount());
    //
    ITreeNode eitherNode = rootNode.getChildNode(0);
    assertTrue(eitherNode instanceof EitherOrNode);
    assertTrue(((EitherOrNode) eitherNode).isBeginOfEitherOr());
    assertFalse(((EitherOrNode) eitherNode).isNegative());
    //
    ITreeNode orNode = rootNode.getChildNode(1);
    assertTrue(orNode instanceof EitherOrNode);
    assertFalse(((EitherOrNode) orNode).isBeginOfEitherOr());
    assertFalse(((EitherOrNode) orNode).isNegative());
  }

  @Test
  public void testImportEitherOrOrNode() {
    // setup field data
    ComposerEitherOrNodeData eitherNodeData = new ComposerEitherOrNodeData();
    eitherNodeData.setBeginOfEitherOr(true);
    ComposerEitherOrNodeData or1NodeData = new ComposerEitherOrNodeData();
    ComposerEitherOrNodeData or2NodeData = new ComposerEitherOrNodeData();
    List<TreeNodeData> rootList = Arrays.<TreeNodeData> asList(eitherNodeData, or1NodeData, or2NodeData);
    m_fieldData.setRoots(rootList);

    // import
    m_composerField.importFormFieldData(m_fieldData, false);

    // verify import
    ITreeNode rootNode = m_composerField.getTree().getRootNode();
    assertEquals(3, rootNode.getChildNodeCount());
    //
    ITreeNode eitherNode = rootNode.getChildNode(0);
    assertTrue(eitherNode instanceof EitherOrNode);
    assertTrue(((EitherOrNode) eitherNode).isBeginOfEitherOr());
    assertFalse(((EitherOrNode) eitherNode).isNegative());
    //
    ITreeNode or1Node = rootNode.getChildNode(1);
    assertTrue(or1Node instanceof EitherOrNode);
    assertFalse(((EitherOrNode) or1Node).isBeginOfEitherOr());
    assertFalse(((EitherOrNode) or1Node).isNegative());
    //
    ITreeNode or2Node = rootNode.getChildNode(2);
    assertTrue(or2Node instanceof EitherOrNode);
    assertFalse(((EitherOrNode) or2Node).isBeginOfEitherOr());
    assertFalse(((EitherOrNode) or2Node).isNegative());
  }

  /**
   * Expects that add entity menu is initialized during init() of the composer rather than during creating time of the
   * node.
   * <p>
   * Reason: AbstractTreeNode executes execInit while constructor runs (and not during init as for every other widget).
   * Since the entity nodes create actions which add listeners to the data model during that time, the listeners might
   * never get removed, because execDipose might never be called. This can happen if a form is created, but never
   * started and thus init never called. If it is not started, calling close won't call dispose. This behavior of the
   * form is correct, because dispose() is the counterpart to init(), so if init() is not called, dispose() won't be
   * called as well.
   */
  @Test
  public void testInitMenusLater() {
    ComposerWithDataModelField composerField = new ComposerWithDataModelField();
    ITreeNode rootNode = composerField.getTree().getRootNode();
    assertFalse(rootNode.getMenuByClass(AddEntityMenu.class).isInitDone());

    composerField.init();
    assertTrue(rootNode.getMenuByClass(AddEntityMenu.class).isInitDone());

    // Ensure init is called if nodes are created later
    ITreeNode eitherOrNode = composerField.addEitherNode(rootNode, false);
    composerField.addAdditionalOrNode(eitherOrNode, false);
    assertTrue(eitherOrNode.getMenuByClass(AddEntityMenu.class).isInitDone());
  }

  /* --------------------------------------------------------------------------
   * fixture
   * --------------------------------------------------------------------------
   */

  public static class ComposerField extends AbstractComposerField {
  }

  public static class ComposerWithDataModelField extends AbstractComposerField {

    @Order(10)
    public class TimesheetEntry extends AbstractDataModelEntity {

      private static final long serialVersionUID = 1L;

      @Override
      protected String getConfiguredText() {
        return "Timesheet";
      }

      @Order(20)
      public class PlannedEndAttribute extends AbstractDataModelAttribute {

        private static final long serialVersionUID = 1L;

        @Override
        protected String getConfiguredText() {
          return "Planned end";
        }

        @Override
        protected int getConfiguredType() {
          return TYPE_DATE;
        }
      }
    }
  }

  public static class ComposerFieldData extends AbstractTreeFieldData {
    private static final long serialVersionUID = 1L;
  }
}
