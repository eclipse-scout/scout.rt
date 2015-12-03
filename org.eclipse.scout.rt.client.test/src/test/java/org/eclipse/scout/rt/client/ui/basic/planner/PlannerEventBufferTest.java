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
package org.eclipse.scout.rt.client.ui.basic.planner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link PlannerEventBuffer}
 */
public class PlannerEventBufferTest {

  private PlannerEventBuffer m_testBuffer;

  // Maps to generate each resource/column only once (PlannerEventBuffer checks for reference equality)
  private Map<Integer, Resource> m_mockResources;

  @Before
  public void setup() {
    m_testBuffer = new PlannerEventBuffer();
    m_mockResources = new HashMap<>();
  }

  /**
   * EventBuffer should be initially empty.
   */
  @Test
  public void testEmpty() {
    assertTrue(m_testBuffer.isEmpty());
    assertTrue(m_testBuffer.consumeAndCoalesceEvents().isEmpty());
  }

  /**
   * Multiple update events should be merged into a single event with the resources combined in the correct order.
   */
  @Test
  public void testCombineMultipleUpdates() {
    List<Resource> resources1 = new ArrayList<>();
    final Resource r1 = mockResource(0);
    final Resource r2 = mockResource(1);
    resources1.add(r1);
    resources1.add(r2);
    final PlannerEvent e1 = new PlannerEvent(mock(IPlanner.class), PlannerEvent.TYPE_RESOURCES_UPDATED, resources1);
    List<Resource> resources2 = new ArrayList<>();
    final Resource r3 = mockResource(2);
    resources2.add(r3);
    final PlannerEvent e2 = new PlannerEvent(mock(IPlanner.class), PlannerEvent.TYPE_RESOURCES_UPDATED, resources2);
    m_testBuffer.add(e1);
    m_testBuffer.add(e2);

    final List<PlannerEvent> events = m_testBuffer.consumeAndCoalesceEvents();

    assertEquals(1, events.size());
    final List<? extends Resource> resultResources = events.get(0).getResources();
    assertEquals(3, resultResources.size());

    List<Resource> expected = new ArrayList<>();
    expected.add(r1);
    expected.add(r2);
    expected.add(r3);
    assertTrue(CollectionUtility.equalsCollection(expected, resultResources));
  }

  /**
   * Multiple insert events on the same resource should be merged into a single event with the resources combined in the
   * correct order.
   */
  @Test
  public void testCombineMultipleInsertsSameResources() {
    final PlannerEvent e1 = new PlannerEvent(mock(IPlanner.class), PlannerEvent.TYPE_RESOURCES_INSERTED, mockResources(0, 1, 2));
    final PlannerEvent e2 = new PlannerEvent(mock(IPlanner.class), PlannerEvent.TYPE_RESOURCES_INSERTED, mockResources(1, 3));
    m_testBuffer.add(e1);
    m_testBuffer.add(e2);

    final List<PlannerEvent> events = m_testBuffer.consumeAndCoalesceEvents();

    assertEquals(1, events.size());
    final List<? extends Resource> resultResources = events.get(0).getResources();
    assertEquals(4, resultResources.size());
  }

  /**
   * Updates that are not consecutive are not combined.
   */
  @Test
  public void testCombineOnlyConsecutiveUpdates() {
    final PlannerEvent e1 = createTestUpdateEvent();
    m_testBuffer.add(e1);
    m_testBuffer.add(mockEvent(PlannerEvent.TYPE_RESOURCES_INSERTED, 1));
    final PlannerEvent e2 = createTestUpdateEvent();
    m_testBuffer.add(e2);
    final List<PlannerEvent> events = m_testBuffer.consumeAndCoalesceEvents();
    assertEquals(3, events.size());
    assertEquals(2, events.get(0).getResources().size());
    assertEquals(1, events.get(1).getResources().size());
    assertEquals(1, events.get(2).getResources().size()); // one was merge to insert
  }

  ////// REPLACE

  /**
   * If a resource is inserted and later updated, only an insert event with the updated value needs to be kept.
   */
  @Test
  public void testInsertedFollowedByUpdated() {
    final PlannerEvent insert = new PlannerEvent(mock(IPlanner.class), PlannerEvent.TYPE_RESOURCES_INSERTED, mockResources(0));

    final List<Resource> updatedResources = mockResources(0);
    when(updatedResources.get(0).getCell()).thenReturn(mock(Cell.class));
    final PlannerEvent update = new PlannerEvent(mock(IPlanner.class), PlannerEvent.TYPE_RESOURCES_UPDATED, updatedResources);

    m_testBuffer.add(insert);
    m_testBuffer.add(update);
    final List<PlannerEvent> events = m_testBuffer.consumeAndCoalesceEvents();
    assertEquals(1, events.size());
    assertNotNull(events.get(0).getResources().get(0).getCell());
  }

  /**
   * If multiple resources are inserted later a resource is updated, it should be merged and the event removed.
   */
  @Test
  public void testInsertedFollowedUpdatedMultipleResources() {
    final PlannerEvent insert = new PlannerEvent(mock(IPlanner.class), PlannerEvent.TYPE_RESOURCES_INSERTED, mockResources(0, 1));
    final List<Resource> updatedResources = mockResources(0);
    when(updatedResources.get(0).getCell()).thenReturn(mock(Cell.class));
    final PlannerEvent otherUpdate = new PlannerEvent(mock(IPlanner.class), PlannerEvent.TYPE_RESOURCES_UPDATED, mockResources(3, 4));
    final PlannerEvent update = new PlannerEvent(mock(IPlanner.class), PlannerEvent.TYPE_RESOURCES_UPDATED, updatedResources);
    m_testBuffer.add(insert);
    m_testBuffer.add(otherUpdate);
    m_testBuffer.add(update);
    final List<PlannerEvent> events = m_testBuffer.consumeAndCoalesceEvents();
    assertEquals(2, events.size());
    assertEquals(2, events.get(0).getResourceCount());
    assertNotNull(events.get(0).getResources().get(0).getCell());
  }

  /**
   * If a resource is inserted and another resource is updated, the events are not merged
   */
  @Test
  public void testInsertedFollowedUpdatedNoMatch() {
    final PlannerEvent insert = new PlannerEvent(mock(IPlanner.class), PlannerEvent.TYPE_RESOURCES_INSERTED, mockResources(1, 2, 3));
    final PlannerEvent update = new PlannerEvent(mock(IPlanner.class), PlannerEvent.TYPE_RESOURCES_UPDATED, mockResources(0));
    m_testBuffer.add(insert);
    m_testBuffer.add(update);
    final List<PlannerEvent> events = m_testBuffer.consumeAndCoalesceEvents();
    assertEquals(2, events.size());
  }

  @Test
  public void testUpdateFollowedByDeleteRemoved() {
    final PlannerEvent update = new PlannerEvent(mock(IPlanner.class), PlannerEvent.TYPE_RESOURCES_UPDATED, mockResources(0));
    final PlannerEvent delete = new PlannerEvent(mock(IPlanner.class), PlannerEvent.TYPE_RESOURCES_DELETED, mockResources(0));
    m_testBuffer.add(update);
    m_testBuffer.add(delete);
    final List<PlannerEvent> events = m_testBuffer.consumeAndCoalesceEvents();
    assertEquals(1, events.size());
    assertEquals(PlannerEvent.TYPE_RESOURCES_DELETED, events.get(0).getType());
    assertEquals(1, events.get(0).getResourceCount());
  }

  /**
   * Insert(r0,r1) + Update(r0,r2) + Delete(r0,r3) = Insert(r1) + Update(r2) + Delete(r3)
   */
  @Test
  public void testInsertAndUpdateFollowedByDelete() {
    final PlannerEvent insert = new PlannerEvent(mock(IPlanner.class), PlannerEvent.TYPE_RESOURCES_INSERTED, mockResources(0, 1));
    final PlannerEvent update = new PlannerEvent(mock(IPlanner.class), PlannerEvent.TYPE_RESOURCES_UPDATED, mockResources(0, 2));
    final PlannerEvent delete = new PlannerEvent(mock(IPlanner.class), PlannerEvent.TYPE_RESOURCES_DELETED, mockResources(0, 3));
    m_testBuffer.add(insert);
    m_testBuffer.add(update);
    m_testBuffer.add(delete);
    final List<PlannerEvent> events = m_testBuffer.consumeAndCoalesceEvents();
    assertEquals(3, events.size());
    assertEquals(PlannerEvent.TYPE_RESOURCES_INSERTED, events.get(0).getType());
    assertEquals(PlannerEvent.TYPE_RESOURCES_UPDATED, events.get(1).getType());
    assertEquals(PlannerEvent.TYPE_RESOURCES_DELETED, events.get(2).getType());
    assertEquals(1, events.get(0).getResourceCount());
    assertEquals(1, events.get(1).getResourceCount());
    assertEquals(1, events.get(2).getResourceCount());
  }

  /**
   * <b>Events:</b>
   * <ul>
   * <li>Insert(r0,r1,r2,r3,4,r5,r6)
   * <li>Update(r0, r6)
   * <li>DeleteAll()
   * <li>Insert(r0,r1)
   * <li>Update(r0,r2)
   * <li>Delete(r0,r3)
   * </ul>
   * <b>Expected result:</b>
   * <ul>
   * <li>Insert(r1)
   * <li>Update(r2)
   * <li>Delete(r3)
   * </ul>
   */
  @Test
  public void testInsertDeleteAllInsertUpdateFollowedByDelete() {
    final PlannerEvent insert1 = new PlannerEvent(mock(IPlanner.class), PlannerEvent.TYPE_RESOURCES_INSERTED, mockResources(0, 1, 2, 3, 4, 5, 6));
    final PlannerEvent update1 = new PlannerEvent(mock(IPlanner.class), PlannerEvent.TYPE_RESOURCES_UPDATED, mockResources(0, 6));
    final PlannerEvent deleteAll = new PlannerEvent(mock(IPlanner.class), PlannerEvent.TYPE_ALL_RESOURCES_DELETED);
    final PlannerEvent insert2 = new PlannerEvent(mock(IPlanner.class), PlannerEvent.TYPE_RESOURCES_INSERTED, mockResources(0, 1));
    final PlannerEvent update2 = new PlannerEvent(mock(IPlanner.class), PlannerEvent.TYPE_RESOURCES_UPDATED, mockResources(0, 2));
    final PlannerEvent delete = new PlannerEvent(mock(IPlanner.class), PlannerEvent.TYPE_RESOURCES_DELETED, mockResources(0, 3));
    m_testBuffer.add(insert1);
    m_testBuffer.add(update1);
    m_testBuffer.add(deleteAll);
    m_testBuffer.add(insert2);
    m_testBuffer.add(update2);
    m_testBuffer.add(delete);
    final List<PlannerEvent> events = m_testBuffer.consumeAndCoalesceEvents();
    assertEquals(4, events.size());
    assertEquals(PlannerEvent.TYPE_ALL_RESOURCES_DELETED, events.get(0).getType());
    assertEquals(PlannerEvent.TYPE_RESOURCES_INSERTED, events.get(1).getType());
    assertEquals(PlannerEvent.TYPE_RESOURCES_UPDATED, events.get(2).getType());
    assertEquals(PlannerEvent.TYPE_RESOURCES_DELETED, events.get(3).getType());
    assertEquals(0, events.get(0).getResourceCount());
    assertEquals(1, events.get(1).getResourceCount());
    assertEquals(1, events.get(2).getResourceCount());
    assertEquals(1, events.get(3).getResourceCount());
  }

  @Test
  public void testUpdateFollowedByDeleteNoMatch() {
    final PlannerEvent update = new PlannerEvent(mock(IPlanner.class), PlannerEvent.TYPE_RESOURCES_UPDATED, mockResources(0));
    final PlannerEvent delete = new PlannerEvent(mock(IPlanner.class), PlannerEvent.TYPE_RESOURCES_DELETED, mockResources(1));
    m_testBuffer.add(update);
    m_testBuffer.add(delete);
    final List<PlannerEvent> events = m_testBuffer.consumeAndCoalesceEvents();
    assertEquals(2, events.size());
    assertEquals(PlannerEvent.TYPE_RESOURCES_DELETED, events.get(1).getType());
  }

//
//  @Test
//  public void testInsertChangeResourceOrder() {
//    final PlannerEvent insert1 = new PlannerEvent(mock(IPlanner.class), PlannerEvent.TYPE_RESOURCES_INSERTED, mockResources(0, 1, 2, 3, 4));
//    final PlannerEvent insert2 = new PlannerEvent(mock(IPlanner.class), PlannerEvent.TYPE_RESOURCES_INSERTED, mockResources(6, 5));
//    final PlannerEvent orderChanged = new PlannerEvent(mock(IPlanner.class), PlannerEvent.TYPE_RESOURCE_ORDER_CHANGED, mockResources(6, 5, 4, 3, 2, 1, 0));
//    m_testBuffer.add(insert1);
//    m_testBuffer.add(insert2);
//    m_testBuffer.add(orderChanged);
//    final List<PlannerEvent> events = m_testBuffer.consumeAndCoalesceEvents();
//    assertEquals(1, events.size());
//    assertEquals(PlannerEvent.TYPE_RESOURCES_INSERTED, events.get(0).getType());
//    assertEquals(7, events.get(0).getResourceCount());
//    assertEquals(6, events.get(0).getResources().get(0).getResourceIndex());
//    assertEquals(4, events.get(0).getResources().get(2).getResourceIndex());
//    assertEquals(0, events.get(0).getResources().get(6).getResourceIndex());
//  }
//
//
//  /**
//   * If two resources are deleted separately, both should be present in the event list. This test checks that checks are
//   * _not_
//   * done by checking for the same resourceIndex.
//   */
//  @Test
//  public void testDeleteTwoResources() {
//    List<Resource> mockResources = mockResources(0, 1, 2, 3, 4);
//    IPlanner planner = mock(IPlanner.class);
//    final PlannerEvent event1 = new PlannerEvent(planner, PlannerEvent.TYPE_RESOURCES_UPDATED, Collections.singletonList(mockResources.get(2)));
//    final PlannerEvent event2 = new PlannerEvent(planner, PlannerEvent.TYPE_RESOURCES_DELETED, Collections.singletonList(mockResources.get(2)));
//    final PlannerEvent event3 = new PlannerEvent(planner, PlannerEvent.TYPE_RESOURCES_UPDATED, Collections.singletonList(mockResources.get(3)));
//    final PlannerEvent event4 = new PlannerEvent(planner, PlannerEvent.TYPE_RESOURCES_DELETED, Collections.singletonList(mockResources.get(3)));
//    final PlannerEvent event5 = new PlannerEvent(planner, PlannerEvent.TYPE_RESOURCE_ORDER_CHANGED, Arrays.asList(mockResources.get(4), mockResources.get(1), mockResources.get(0)));
//    m_testBuffer.add(event1);
//    m_testBuffer.add(event2);
//    m_testBuffer.add(event3);
//    m_testBuffer.add(event4);
//    m_testBuffer.add(event5);
//    final List<PlannerEvent> events = m_testBuffer.consumeAndCoalesceEvents();
//    assertEquals(2, events.size());
//    assertEquals(PlannerEvent.TYPE_RESOURCES_DELETED, events.get(0).getType());
//    assertEquals(2, events.get(0).getResourceCount());
//    assertEquals(PlannerEvent.TYPE_RESOURCE_ORDER_CHANGED, events.get(1).getType());
//    assertEquals(3, events.get(1).getResourceCount());
//  }

  private PlannerEvent createTestUpdateEvent() {
    return new PlannerEvent(mock(IPlanner.class), PlannerEvent.TYPE_RESOURCES_UPDATED, mockResources(0, 1));
  }

  private PlannerEvent mockEvent(int type, int resourceCount) {
    List<Resource> resources = null;
    if (resourceCount > 0) {
      resources = new ArrayList<>();
      for (int i = 0; i < resourceCount; i++) {
        resources.add(mockResource(i));
      }
    }
    return new PlannerEvent(mock(IPlanner.class), type, resources);
  }

  private List<Resource> mockResources(int... indexes) {
    List<Resource> resources = new ArrayList<>();
    for (int i : indexes) {
      resources.add(mockResource(i));
    }
    return resources;
  }

  private Resource mockResource(int id) {
    Resource resource = m_mockResources.get(id);
    if (resource == null) {
      resource = mock(Resource.class, "MockResource[" + id + "]");
      when(resource.getId()).thenReturn(id);
      m_mockResources.put(id, resource);
    }
    return resource;
  }
}
