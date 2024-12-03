/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop.hybrid;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTree;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.dataobject.DoEntityBuilder;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class HybridManagerTest {

  private String createId() {
    return UUID.randomUUID().toString();
  }

  @Test
  public void testWidgets() {
    HybridManager hybridManager = BEANS.get(HybridManager.class);
    String id1 = createId();
    String id2 = createId();
    DummyForm form1 = new DummyForm();
    DummyForm form2 = new DummyForm();

    assertEquals(0, hybridManager.getWidgets().size());
    hybridManager.addWidget(id1, form1);
    assertEquals(1, hybridManager.getWidgets().size());
    hybridManager.addWidget(id2, form2);
    assertEquals(2, hybridManager.getWidgets().size());

    assertEquals(id1, hybridManager.getWidgetId(form1));
    assertEquals(id2, hybridManager.getWidgetId(form2));
    assertEquals(form1, hybridManager.getWidgetById(id1));
    assertEquals(form2, hybridManager.getWidgetById(id2));

    hybridManager.removeWidget(form1);
    assertEquals(1, hybridManager.getWidgets().size());
    hybridManager.removeWidgetById(id2);
    assertEquals(0, hybridManager.getWidgets().size());

    hybridManager.addWidgets(Map.of(id1, form1, id2, form2));
    assertEquals(2, hybridManager.getWidgets().size());
    hybridManager.removeWidgets(Set.of(form1, form2));
    assertEquals(0, hybridManager.getWidgets().size());

    hybridManager.addWidgets(Map.of(id1, form1, id2, form2));
    assertEquals(2, hybridManager.getWidgets().size());
    hybridManager.addWidget(id2, form2);
    assertEquals(2, hybridManager.getWidgets().size());
    hybridManager.removeWidgetsById(Set.of(id1, id2));
    assertEquals(0, hybridManager.getWidgets().size());

    hybridManager.addWidgets(Map.of(id1, form1, id2, form2));
    assertEquals(2, hybridManager.getWidgets().size());
    hybridManager.clearWidgets();
    assertEquals(0, hybridManager.getWidgets().size());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWidgetUniqueIds() {
    HybridManager hybridManager = BEANS.get(HybridManager.class);
    String id = createId();
    DummyForm form1 = new DummyForm();
    DummyForm form2 = new DummyForm();

    assertEquals(0, hybridManager.getWidgets().size());
    hybridManager.addWidget(id, form1);
    hybridManager.addWidget(id, form2);
  }

  @Test
  public void testWidgetRemoveOnDispose() {
    HybridManager hybridManager = BEANS.get(HybridManager.class);
    String id = createId();
    DummyForm form = new DummyForm();

    assertEquals(0, hybridManager.getWidgets().size());
    hybridManager.addWidget(id, form);
    assertEquals(1, hybridManager.getWidgets().size());

    form.start();
    form.doOk();
    assertEquals(0, hybridManager.getWidgets().size());
  }

  @Test
  public void testEventWithContextElement() {
    IForm form = new AbstractForm() {
    };
    ITree tree = new AbstractTree() {
    };
    ITreeNode treeNode = new AbstractTreeNode() {
    };

    HybridManager hybridManager = BEANS.get(HybridManager.class);
    List<HybridEvent> eventCollector = new ArrayList<>();
    hybridManager.addHybridEventListener(event -> eventCollector.add(event));

    // ----------

    String id = createId();
    String eventType = "foo";
    IDoEntity data = BEANS.get(DoEntityBuilder.class).put("n", 1).build();
    HybridActionContextElements contextElements = BEANS.get(HybridActionContextElements.class)
        .withElement("form", form)
        .withElement("node", tree, treeNode);
    hybridManager.fireHybridEvent(id, eventType, data, contextElements);

    assertEquals(1, eventCollector.size());
    HybridEvent event = eventCollector.get(0);
    assertSame(id, event.getId());
    assertSame(eventType, event.getEventType());
    assertSame(data, event.getData());
    assertSame(contextElements, event.getContextElements());

    // ----------

    String id2 = createId();
    HybridActionContextElements contextElements2 = BEANS.get(HybridActionContextElements.class)
        .withElement("form", form)
        .withElement("node", tree, treeNode);
    hybridManager.fireHybridActionEndEvent(id2, contextElements2);

    assertEquals(2, eventCollector.size());
    HybridEvent event2 = eventCollector.get(1);
    assertSame(id2, event2.getId());
    assertSame(HybridEvent.HYBRID_ACTION_END, event2.getEventType());
    assertNull(event2.getData());
    assertSame(contextElements2, event2.getContextElements());
  }
}
