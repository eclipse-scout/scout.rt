/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop.hybrid;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTree;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class HybridActionContextElementsTest {

  @Test
  public void testCreate() {
    IForm form0 = new P_DummyForm("form0");
    IForm form1 = new P_DummyForm("form1");
    IForm form2 = new P_DummyForm("form2");
    IForm form3 = new P_DummyForm("form3");
    ITree tree = new AbstractTree() {
    };
    ITreeNode treeNode = new AbstractTreeNode() {
    };

    Map<String, List<HybridActionContextElement>> baseMap = Map.of(
        "zero", List.of(),
        "one", List.of(HybridActionContextElement.of(form1)),
        "two", List.of(HybridActionContextElement.of(form2), HybridActionContextElement.of(form3))
    );

    HybridActionContextElements contextElements = BEANS.get(HybridActionContextElements.class);
    assertTrue(contextElements.isEmpty());

    contextElements
        .withElement("existing", HybridActionContextElement.of(form0))
        .withElement("one", HybridActionContextElement.of(form0))
        .withMap(baseMap)
        .withElement("three", tree)
        .withElement("four", tree, treeNode)
        .withElements("five", List.of())
        .withElements("six", List.of(HybridActionContextElement.of(form1), HybridActionContextElement.of(form2), HybridActionContextElement.of(form3)));

    assertFalse(contextElements.isEmpty());
    assertEquals(8, contextElements.getMap().size());

    List<HybridActionContextElement> list = contextElements.getList("existing");
    assertEquals(1, list.size());
    assertEquals(form0, list.get(0).getWidget());

    list = contextElements.getList("zero");
    assertEquals(0, list.size());

    list = contextElements.getList("one");
    assertEquals(1, list.size());
    assertEquals(form1, list.get(0).getWidget());

    list = contextElements.getList("two");
    assertEquals(2, list.size());
    assertEquals(form2, list.get(0).getWidget());
    assertEquals(form3, list.get(1).getWidget());

    list = contextElements.getList("three");
    assertEquals(1, list.size());
    assertEquals(tree, list.get(0).getWidget());

    list = contextElements.getList("four");
    assertEquals(1, list.size());
    assertEquals(tree, list.get(0).getWidget());
    assertEquals(treeNode, list.get(0).getElement());

    list = contextElements.getList("five");
    assertEquals(0, list.size());

    list = contextElements.getList("six");
    assertEquals(3, list.size());
    assertEquals(form1, list.get(0).getWidget());
    assertEquals(form2, list.get(1).getWidget());
    assertEquals(form3, list.get(2).getWidget());
  }

  @Test
  public void testAccess() {
    IForm form1 = new P_DummyForm("form1");
    IForm form2 = new P_DummyForm("form2");
    IForm form3 = new P_DummyForm("form3");
    ITree tree = new AbstractTree() {
    };
    ITreeNode treeNode = new AbstractTreeNode() {
    };

    HybridActionContextElements contextElements = BEANS.get(HybridActionContextElements.class)
        .withElement("myTree", tree)
        .withElement("myNode", tree, treeNode)
        .withElements("myForms", List.of(HybridActionContextElement.of(form1), HybridActionContextElement.of(form2), HybridActionContextElement.of(form3)));

    assertEquals(tree, contextElements.getSingle("myTree").getWidget());
    assertEquals(1, contextElements.getList("myTree").size());
    assertSame(contextElements.getSingle("myTree"), contextElements.getList("myTree").get(0));

    assertEquals(tree, contextElements.getSingle("myNode").getWidget());
    assertEquals(treeNode, contextElements.getSingle("myNode").getElement());
    assertEquals(1, contextElements.getList("myNode").size());
    assertSame(contextElements.getSingle("myNode"), contextElements.getList("myNode").get(0));

    assertEquals(form1, contextElements.getSingle("myForms").getWidget());
    assertEquals(3, contextElements.getList("myForms").size());
    assertSame(contextElements.getSingle("myForms"), contextElements.getList("myForms").get(0));
    assertEquals(form2, contextElements.getList("myForms").get(1).getWidget());
    assertEquals(form3, contextElements.getList("myForms").get(2).getWidget());

    assertNull(contextElements.optSingle("doesNotExist"));
    assertNull(contextElements.optList("doesNotExist"));
    assertThrows(AssertionException.class, () -> contextElements.getSingle("doesNotExist"));
    assertThrows(AssertionException.class, () -> contextElements.getList("doesNotExist"));
  }

  private class P_DummyForm extends AbstractForm {

    private final String m_formId;

    public P_DummyForm(String formId) {
      m_formId = formId;
    }

    @Override
    public String getFormId() {
      return m_formId;
    }
  }
}
