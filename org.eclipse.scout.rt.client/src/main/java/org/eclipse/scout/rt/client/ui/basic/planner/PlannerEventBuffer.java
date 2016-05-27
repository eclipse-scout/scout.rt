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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.AbstractEventBuffer;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

/**
 * A buffer for planner events ({@link PlannerEvent}) with coalesce functionality:
 * <ul>
 * <li>Unnecessary events are removed.
 * <li>Events are merged, if possible.
 * </ul>
 * Not thread safe, to be accessed in client model job.
 */
public class PlannerEventBuffer extends AbstractEventBuffer<PlannerEvent> {

  /**
   * Removes unnecessary events or combines events in the list.
   */
  @Override
  protected List<PlannerEvent> coalesce(List<PlannerEvent> events) {
    removeObsolete(events);
    replacePrevious(events, PlannerEvent.TYPE_RESOURCES_INSERTED, PlannerEvent.TYPE_RESOURCES_UPDATED);
    removeEmptyEvents(events);
    removeIdenticalEvents(events);
    coalesceSameType(events);
    return events;
  }

  /**
   * Remove previous events that are now obsolete.
   */
  protected void removeObsolete(List<PlannerEvent> events) {
    //traverse the list in reversed order
    //previous events may be deleted from the list
    for (int j = 0; j < events.size() - 1; j++) {
      int i = events.size() - 1 - j;
      PlannerEvent event = events.get(i);

      int type = event.getType();
      if (type == PlannerEvent.TYPE_ALL_RESOURCES_DELETED) {
        //remove all previous resource related events
        remove(getResourceRelatedEvents(), events.subList(0, i));
      }
      else if (isIgnorePrevious(type)) {
        //remove all previous events of the same type
        remove(type, events.subList(0, i));
      }
      else if (type == PlannerEvent.TYPE_RESOURCES_DELETED) {
        List<Resource> remainingResources = removeResourcesFromPreviousEvents(event.getResources(), events.subList(0, i), PlannerEvent.TYPE_RESOURCES_INSERTED);
        event.setResources(remainingResources);
      }
    }
  }

  /**
   * Removes the given 'resourcesToRemove' from all 'events'. The event list is traversed backwards. This process is
   * stopped, when a event that may change resource indexes is encountered.
   *
   * @return a list with the same resources as 'resourcesToRemove', except those that were removed from an event whose
   *         type matches one of the 'creationTypes'. This allows for completely removing a resource that was created
   *         and deleted in the same request.
   */
  protected List<Resource> removeResourcesFromPreviousEvents(List<? extends Resource> resourcesToRemove, List<PlannerEvent> events, Integer... creationTypes) {
    List<Integer> creationTypesList = Arrays.asList(creationTypes);
    List<Resource> remainingResources = new ArrayList<Resource>();

    for (Resource resourceToRemove : resourcesToRemove) {
      boolean resourceRemovedFromCreationEvent = false;

      for (ListIterator<PlannerEvent> it = events.listIterator(events.size()); it.hasPrevious();) {
        PlannerEvent event = it.previous();
        boolean removed = removeResource(event, resourceToRemove);
        if (removed && creationTypesList.contains(event.getType())) {
          resourceRemovedFromCreationEvent = true;
        }
        if (!isResourceOrderUnchanged(event.getType())) {
          break;
        }
      }

      if (!resourceRemovedFromCreationEvent) {
        remainingResources.add(resourceToRemove);
      }
    }

    return remainingResources;
  }

  protected boolean removeResource(PlannerEvent event, Resource resourceToRemove) {
    boolean removed = false;
    List<? extends Resource> resources = event.getResources();
    for (Iterator<? extends Resource> it = resources.iterator(); it.hasNext();) {
      Resource resource = it.next();
      if (resource == resourceToRemove) {
        it.remove();
        removed = true;
      }
    }
    event.setResources(resources);
    return removed;
  }

  /**
   * Update a previous event of given type and removes a newer one of another type.
   */
  protected void replacePrevious(List<PlannerEvent> events, int oldType, int newType) {
    for (int j = 0; j < events.size() - 1; j++) {
      int i = events.size() - 1 - j;
      PlannerEvent event = events.get(i);

      if (event.getType() == newType) {
        //merge current update event with previous insert event of the same resource
        updatePreviousResource(event, events.subList(0, i), oldType);
      }
    }
  }

  /**
   * Merge previous events of the same type (resources and columns) into the current and delete the previous events
   */
  protected void coalesceSameType(List<PlannerEvent> events) {
    for (int j = 0; j < events.size() - 1; j++) {
      int i = events.size() - 1 - j;
      PlannerEvent event = events.get(i);

      if (isCoalesceConsecutivePrevious(event.getType())) {
        coalesceConsecutivePrevious(event, events.subList(0, i));
      }
    }
  }

  /**
   * Updates previous resources in the list, if it is of the given type. Breaks, if events are encountered, that may
   * change the resource order.
   */
  protected void updatePreviousResource(PlannerEvent event, List<PlannerEvent> events, int type) {
    for (ListIterator<PlannerEvent> it = events.listIterator(events.size()); it.hasPrevious();) {
      PlannerEvent previous = it.previous();
      if (previous.getType() == type) {
        List<? extends Resource> resources = event.getResources();
        replaceResources(previous, resources);
        event.setResources(resources);
      }
      if (!isResourceOrderUnchanged(previous.getType())) {
        break;
      }
    }
  }

  protected void replaceResources(PlannerEvent event, List<? extends Resource> newResources) {
    for (Iterator<? extends Resource> it = newResources.iterator(); it.hasNext();) {
      Resource newResource = it.next();
      boolean replaced = tryReplaceResource(event, newResource);
      if (replaced) {
        it.remove();
      }
    }
  }

  /**
   * Replaces the resource in the event, if it is contained.
   *
   * @return <code>true</code> if successful.
   */
  protected boolean tryReplaceResource(PlannerEvent event, Resource newResource) {
    List<Resource> targetResources = new ArrayList<>();
    boolean replaced = false;
    for (Resource resource : event.getResources()) {
      if (resource == newResource) {
        resource = newResource;
        replaced = true;
      }
      targetResources.add(resource);
    }
    event.setResources(targetResources);
    return replaced;
  }

  /**
   * Merge events of the same type in the given list (resources and columns) into the current and delete the other
   * events from the list.
   */
  protected void coalesceConsecutivePrevious(PlannerEvent event, List<PlannerEvent> list) {
    for (ListIterator<PlannerEvent> it = list.listIterator(list.size()); it.hasPrevious();) {
      PlannerEvent previous = it.previous();
      if (event.getType() == previous.getType()) {
        merge(previous, event);
        it.remove();
      }
      else {
        // Stop only if the event and the previous event are of the same "relation type" (e.g. both resource-related or both non-resource-related)
        if (isResourceRelatedEvent(event.getType()) == isResourceRelatedEvent(previous.getType())) {
          return;
        }
      }
    }
  }

  /**
   * Adds resources and columns
   */
  protected PlannerEvent merge(PlannerEvent first, PlannerEvent second) {
    second.setResources(mergeResources(first.getResources(), second.getResources()));
    return second;
  }

  /**
   * Merge list of resources, such that, if a resource is in both lists, only the one of the second list (later event)
   * is kept.
   */
  protected List<Resource> mergeResources(List<? extends Resource> first, List<? extends Resource> second) {
    List<Resource> resources = new ArrayList<>();
    Set<Resource> secondResourceSet = new HashSet<>(second);
    for (Resource resource : first) {
      if (!secondResourceSet.contains(resource)) {
        resources.add(resource);
      }
    }
    for (Resource resource : second) {
      resources.add(resource);
    }
    return resources;
  }

//  /**
//   * If a RESOURCE_ORDER_CHANGED event happens directly after RESOURCES_INSERTED, we may removed the RESOURCE_ORDER_CHANGED event
//   * and send the new order in the RESOURCES_INSERTED event instead.
//   */
//  protected void applyResourceOrderChangedToResourcesInserted(List<PlannerEvent> events) {
//    for (int j = 0; j < events.size() - 1; j++) {
//      int i = events.size() - 1 - j;
//      PlannerEvent event = events.get(i);
//
//      if (event.getType() == PlannerEvent.TYPE_RESOURCE_ORDER_CHANGED) {
//        PlannerEvent previous = findInsertionBeforeResourceOrderChanged(events.subList(0, i));
//        // Check if previous is RESOURCES_INSERTED and they have the same resources
//        if (previous != null && previous.getType() == PlannerEvent.TYPE_RESOURCES_INSERTED &&
//            event.getResourceCount() == previous.getResourceCount() && CollectionUtility.equalsCollection(event.getResources(), previous.getResources(), false)) {
//          // replace resources and remove RESOURCE_ORDER_CHANGED event
//          previous.setResources(event.getResources());
//          events.remove(i);
//        }
//      }
//    }
//  }

  /**
   * Finds previous RESOURCES_INSERTED event while ignoring events that don't change resource order (e.g.
   * COLUMN_HEADERS_UPDATED)
   */
  protected PlannerEvent findInsertionBeforeResourceOrderChanged(List<PlannerEvent> events) {
    for (ListIterator<PlannerEvent> it = events.listIterator(events.size()); it.hasPrevious();) {
      PlannerEvent previous = it.previous();
      if (previous.getType() == PlannerEvent.TYPE_RESOURCES_INSERTED) {
        return previous;
      }
      if (!isResourceOrderUnchanged(previous.getType())) {
        break;
      }
    }
    return null;
  }

  protected void removeEmptyEvents(List<PlannerEvent> events) {
    for (Iterator<PlannerEvent> it = events.iterator(); it.hasNext();) {
      PlannerEvent event = it.next();
      if (isResourcesRequired(event.getType()) && event.getResources().isEmpty()) {
        it.remove();
      }
    }
  }

  /**
   * Removes identical events (same type and content) when they occur consecutively (not necessarily directly, but
   * within the same type group). The oldest event is preserved.
   */
  protected void removeIdenticalEvents(List<PlannerEvent> events) {
    // Please note: In contrast to all other methods in this class, this method loops through the
    // list in FORWARD direction (so the oldest event will be kept).
    for (int i = 0; i < events.size(); i++) {
      PlannerEvent event = events.get(i);

      List<PlannerEvent> subList = events.subList(i + 1, events.size());
      for (Iterator<PlannerEvent> it = subList.iterator(); it.hasNext();) {
        PlannerEvent next = it.next();
        if (next.getType() != event.getType()) {
          // Stop when a node of different type occurs
          break;
        }
        if (isIdenticalEvent(event, next)) {
          it.remove();
        }
      }
    }
  }

  @Override
  protected boolean isIdenticalEvent(PlannerEvent event1, PlannerEvent event2) {
    if (event1 == null && event2 == null) {
      return true;
    }
    if (event1 == null || event2 == null) {
      return false;
    }
    boolean identical = (event1.getType() == event2.getType() && CollectionUtility.equalsCollection(event1.getResources(), event2.getResources(), true) && CollectionUtility.equalsCollection(event1.getPopupMenus(), event2.getPopupMenus()));
    return identical;
  }

  /**
   * @return <code>true</code>, if the event does not influence the resource order.
   */
  protected boolean isResourceOrderUnchanged(int type) {
    switch (type) {
      case PlannerEvent.TYPE_RESOURCES_SELECTED:
      case PlannerEvent.TYPE_RESOURCES_UPDATED:
        return true;
      default:
        return false;
    }
  }

  protected Set<Integer> getResourceRelatedEvents() {
    Set<Integer> res = new HashSet<>();
    res.add(PlannerEvent.TYPE_ALL_RESOURCES_DELETED);
//    res.add(PlannerEvent.TYPE_RESOURCE_ORDER_CHANGED);
    res.add(PlannerEvent.TYPE_RESOURCES_DELETED);
    res.add(PlannerEvent.TYPE_RESOURCES_INSERTED);
    res.add(PlannerEvent.TYPE_RESOURCES_UPDATED);
    res.add(PlannerEvent.TYPE_RESOURCES_SELECTED);
    return res;
  }

  protected boolean isResourceRelatedEvent(int type) {
    return getResourceRelatedEvents().contains(type);
  }

  /**
   * @param type
   *          {@link PlannerEvent} type
   * @return true, if previous events of the same type can be ignored. false otherwise
   */
  protected boolean isIgnorePrevious(int type) {
    switch (type) {
//      case PlannerEvent.TYPE_RESOURCE_ORDER_CHANGED:
      case PlannerEvent.TYPE_RESOURCES_SELECTED:
      case PlannerEvent.TYPE_ALL_RESOURCES_DELETED: {
        return true;
      }
      default: {
        return false;
      }
    }
  }

  /**
   * @return true, if previous consecutive events of the same type can be coalesced.
   */
  protected boolean isCoalesceConsecutivePrevious(int type) {
    switch (type) {
      case PlannerEvent.TYPE_RESOURCES_UPDATED:
      case PlannerEvent.TYPE_RESOURCES_INSERTED:
      case PlannerEvent.TYPE_RESOURCES_DELETED: {
        return true;
      }
      default: {
        return false;
      }
    }
  }

  protected boolean isResourcesRequired(int type) {
    switch (type) {
//      case PlannerEvent.TYPE_RESOURCE_ORDER_CHANGED:
      case PlannerEvent.TYPE_RESOURCES_DELETED:
      case PlannerEvent.TYPE_RESOURCES_INSERTED:
      case PlannerEvent.TYPE_RESOURCES_UPDATED: {
        return true;
      }
      default: {
        return false;
      }
    }
  }
}
