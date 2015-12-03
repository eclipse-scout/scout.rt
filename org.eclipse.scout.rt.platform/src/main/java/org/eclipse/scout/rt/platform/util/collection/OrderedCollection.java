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
package org.eclipse.scout.rt.platform.util.collection;

import static org.eclipse.scout.rt.platform.IOrdered.DEFAULT_ORDER_STEP;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.scout.rt.platform.IOrdered;
import org.eclipse.scout.rt.platform.OrderedComparator;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

/**
 * Instances of this class hold references on {@link IOrdered} objects. It provides a subset of methods available on the
 * {@link List} interface.
 *
 * @since 4.2
 */
public class OrderedCollection<ORDERED extends IOrdered> implements Iterable<ORDERED> {

  public static final double DEFAULT_EMPTY_ORDER = 0;

  private final LinkedList<ORDERED> m_orderedObjects;
  private final Comparator<? super ORDERED> m_comparator;

  public OrderedCollection() {
    m_orderedObjects = new LinkedList<ORDERED>();
    m_comparator = new OrderedComparator();
  }

  public int size() {
    return m_orderedObjects.size();
  }

  public boolean isEmpty() {
    return m_orderedObjects.isEmpty();
  }

  public boolean contains(Object o) {
    return m_orderedObjects.contains(o);
  }

  @Override
  public Iterator<ORDERED> iterator() {
    return m_orderedObjects.iterator();
  }

  /**
   * Adds an {@link IOrdered} object to this collection without changing its order.
   *
   * @param o
   *          The {@link IOrdered} object to add.
   * @return Returns <code>true</code> if the given ordered object has been added. Otherwise <code>false</code>.
   */
  public boolean addOrdered(ORDERED o) {
    if (o == null) {
      return false;
    }
    return m_orderedObjects.add(o);
  }

  public boolean addAllOrdered(Collection<? extends ORDERED> c) {
    if (c == null) {
      return false;
    }
    boolean changed = false;
    for (ORDERED o : c) {
      changed |= addOrdered(o);
    }
    return changed;
  }

  public boolean addOrdered(ORDERED o, double order) {
    if (o == null) {
      return false;
    }
    o.setOrder(order);
    return addOrdered(o);
  }

  /**
   * Removes the given element form this ordered collection.
   *
   * @param o
   *          The object to remove.
   * @return Returns <code>true</code> if the given element was part of this ordered collection. Otherwise
   *         <code>false</code>.
   */
  public boolean remove(Object o) {
    return m_orderedObjects.remove(o);
  }

  /**
   * Removes the given elements form this ordered collection.
   *
   * @param o
   *          The object to remove.
   * @return Returns <code>true</code> if the given element was part of this ordered collection. Otherwise
   *         <code>false</code>.
   */
  public boolean removeAll(Collection<?> c) {
    if (CollectionUtility.isEmpty(c)) {
      return false;
    }
    return m_orderedObjects.removeAll(c);
  }

  public void clear() {
    m_orderedObjects.clear();
  }

  public ORDERED get(int index) {
    return getReferenceObjectAt(index);
  }

  // new basic methods
  public boolean addFirst(ORDERED o) {
    if (o == null) {
      return false;
    }
    if (m_orderedObjects.isEmpty()) {
      return addOrdered(o, DEFAULT_EMPTY_ORDER);
    }
    ensureSorted();
    ORDERED first = m_orderedObjects.getFirst();
    return addOrdered(o, first.getOrder() - DEFAULT_ORDER_STEP);
  }

  private void ensureSorted() {
    Collections.sort(m_orderedObjects, m_comparator);
  }

  public boolean addLast(ORDERED o) {
    if (o == null) {
      return false;
    }
    if (m_orderedObjects.isEmpty()) {
      return addOrdered(o, DEFAULT_EMPTY_ORDER);
    }
    ensureSorted();
    ORDERED last = m_orderedObjects.getLast();
    return addOrdered(o, last.getOrder() + DEFAULT_ORDER_STEP);
  }

  public boolean addBefore(ORDERED o, ORDERED reference) {
    if (o == null) {
      return false;
    }
    if (reference == null) {
      throw new IllegalArgumentException("reference must not be null.");
    }
    if (!m_orderedObjects.contains(reference)) {
      throw new IllegalArgumentException("reference object is not part of this ordered collection.");
    }
    ORDERED lower = getAdjacentObject(reference, true);
    if (lower == null) {
      return addFirst(o);
    }
    return insertAfter(Collections.singleton(o), lower, lower.getOrder(), (reference.getOrder() - lower.getOrder()) / 2d);
  }

  public boolean addAfter(ORDERED o, ORDERED reference) {
    if (o == null) {
      return false;
    }
    if (reference == null) {
      throw new IllegalArgumentException("reference must not be null.");
    }
    if (!m_orderedObjects.contains(reference)) {
      throw new IllegalArgumentException("reference object is not part of this ordered collection.");
    }
    ORDERED higher = getAdjacentObject(reference, false);
    if (higher == null) {
      return addLast(o);
    }
    return insertAfter(Collections.singleton(o), reference, higher.getOrder(), (reference.getOrder() - higher.getOrder()) / 2d);
  }

  public boolean addAt(ORDERED o, int index) {
    if (o == null) {
      return false;
    }
    if (index < 0) {
      throw new IllegalArgumentException("index must not be negative.");
    }
    if (index > size()) {
      throw new IllegalArgumentException("index out of bounds.");
    }
    if (index == 0) {
      return addFirst(o);
    }
    if (index == size()) {
      return addLast(o);
    }
    ORDERED reference = getReferenceObjectAt(index);
    return addBefore(o, reference);
  }

  public ORDERED removeAt(int index) {
    if (index < 0 || index >= size()) {
      throw new IllegalArgumentException("index out of bounds.");
    }
    ensureSorted();
    int i = 0;
    for (Iterator<ORDERED> it = m_orderedObjects.iterator(); it.hasNext();) {
      ORDERED reference = it.next();
      if (i == index) {
        it.remove();
        return reference;
      }
      i++;
    }
    return null;
  }

  public List<ORDERED> getOrderedList() {
    List<ORDERED> list = new ArrayList<ORDERED>(size());
    ORDERED prev = null;
    boolean unsorted = false;
    boolean first = true;
    for (Iterator<ORDERED> it = m_orderedObjects.iterator(); it.hasNext();) {
      ORDERED next = it.next();
      list.add(next);
      if (unsorted) {
        // ordered collection is not sorted correctly. Skip successive checks.
        continue;
      }

      // check whether order is ok
      if (!first) {
        unsorted = m_comparator.compare(prev, next) > 0;
      }
      first = false;
      prev = next;
    }

    if (unsorted) {
      Collections.sort(list, m_comparator);
    }
    return list;
  }

  public boolean addBefore(ORDERED o, Class<? extends ORDERED> referenceClass) {
    if (o == null) {
      return false;
    }
    if (referenceClass == null) {
      throw new IllegalArgumentException("referenceClass must not be null.");
    }
    ORDERED reference = getReferenceObjectByClass(referenceClass);
    return addBefore(o, reference);
  }

  public boolean addAfter(ORDERED o, Class<? extends ORDERED> referenceClass) {
    if (o == null) {
      return false;
    }
    if (referenceClass == null) {
      throw new IllegalArgumentException("referenceClass must not be null.");
    }
    ORDERED reference = getReferenceObjectByClass(referenceClass);
    return addAfter(o, reference);
  }

  public boolean addAllBefore(Collection<? extends ORDERED> objectToAdd, Class<? extends ORDERED> referenceClass) {
    ArrayList<ORDERED> cleanObjects = CollectionUtility.arrayListWithoutNullElements(objectToAdd);
    if (cleanObjects.isEmpty()) {
      return false;
    }
    if (referenceClass == null) {
      throw new IllegalArgumentException("referenceClass must not be null.");
    }
    ORDERED reference = getReferenceObjectByClass(referenceClass);
    return addAllBefore(cleanObjects, reference);
  }

  public boolean addAllAfter(Collection<? extends ORDERED> objectToAdd, Class<? extends ORDERED> referenceClass) {
    ArrayList<ORDERED> cleanObjects = CollectionUtility.arrayListWithoutNullElements(objectToAdd);
    if (cleanObjects.isEmpty()) {
      return false;
    }
    if (referenceClass == null) {
      throw new IllegalArgumentException("referenceClass must not be null.");
    }
    ORDERED reference = getReferenceObjectByClass(referenceClass);
    return addAllAfter(cleanObjects, reference);
  }

  public boolean addAllFirst(Collection<? extends ORDERED> objectsToAdd) {
    ArrayList<ORDERED> cleanObjects = CollectionUtility.arrayListWithoutNullElements(objectsToAdd);
    if (cleanObjects.isEmpty()) {
      return false;
    }
    double baseOrder = DEFAULT_ORDER_STEP;
    if (!m_orderedObjects.isEmpty()) {
      ensureSorted();
      ORDERED first = m_orderedObjects.getFirst();
      baseOrder = first.getOrder();
    }

    int i = 1;
    for (ListIterator<ORDERED> it = cleanObjects.listIterator(cleanObjects.size()); it.hasPrevious();) {
      ORDERED o = it.previous();
      addOrdered(o, baseOrder - i * DEFAULT_ORDER_STEP);
      i++;
    }
    return true;
  }

  public boolean addAllLast(Collection<? extends ORDERED> objectsToAdd) {
    ArrayList<ORDERED> cleanObjects = CollectionUtility.arrayListWithoutNullElements(objectsToAdd);
    if (cleanObjects.isEmpty()) {
      return false;
    }
    double baseOrder = -DEFAULT_ORDER_STEP;
    if (!m_orderedObjects.isEmpty()) {
      ensureSorted();
      ORDERED last = m_orderedObjects.getLast();
      baseOrder = last.getOrder();
    }

    int i = 1;
    for (ORDERED o : cleanObjects) {
      addOrdered(o, baseOrder + i * DEFAULT_ORDER_STEP);
      i++;
    }
    return true;
  }

  public boolean addAllBefore(Collection<? extends ORDERED> objectsToAdd, ORDERED reference) {
    ArrayList<ORDERED> cleanObjects = CollectionUtility.arrayListWithoutNullElements(objectsToAdd);
    if (cleanObjects.isEmpty()) {
      return false;
    }
    if (reference == null) {
      throw new IllegalArgumentException("reference must not be null.");
    }
    if (!m_orderedObjects.contains(reference)) {
      throw new IllegalArgumentException("reference object is not part of this ordered collection.");
    }
    ORDERED lower = getAdjacentObject(reference, true);
    if (lower == null) {
      return addAllFirst(cleanObjects);
    }

    return insertAfter(cleanObjects, lower, lower.getOrder(), (reference.getOrder() - lower.getOrder()) / (double) (cleanObjects.size() + 1));
  }

  public boolean addAllAfter(Collection<? extends ORDERED> objectsToAdd, ORDERED reference) {
    ArrayList<ORDERED> cleanObjects = CollectionUtility.arrayListWithoutNullElements(objectsToAdd);
    if (cleanObjects.isEmpty()) {
      return false;
    }
    if (reference == null) {
      throw new IllegalArgumentException("reference must not be null.");
    }
    if (!m_orderedObjects.contains(reference)) {
      throw new IllegalArgumentException("reference object is not part of this ordered collection.");
    }

    ORDERED higher = getAdjacentObject(reference, false);
    if (higher == null) {
      return addAllLast(cleanObjects);
    }

    return insertAfter(cleanObjects, reference, reference.getOrder(), (higher.getOrder() - reference.getOrder()) / (double) (cleanObjects.size() + 1));
  }

  public boolean addAllAt(Collection<? extends ORDERED> objectsToAdd, int index) {
    ArrayList<ORDERED> cleanObjects = CollectionUtility.arrayListWithoutNullElements(objectsToAdd);
    if (cleanObjects.isEmpty()) {
      return false;
    }
    if (index < 0) {
      throw new IllegalArgumentException("index must not be negative.");
    }
    if (index > size()) {
      throw new IllegalArgumentException("index out of bounds.");
    }
    if (index == 0) {
      return addAllFirst(cleanObjects);
    }
    if (index == size()) {
      return addAllLast(cleanObjects);
    }
    ORDERED reference = getReferenceObjectAt(index);
    return addAllBefore(cleanObjects, reference);
  }

  private ORDERED getReferenceObjectAt(int index) {
    int i = 0;
    ensureSorted();
    ORDERED reference = null;
    for (Iterator<ORDERED> it = m_orderedObjects.iterator(); it.hasNext();) {
      reference = it.next();
      if (i == index) {
        return reference;
      }
      i++;
    }
    return reference;
  }

  private ORDERED getReferenceObjectByClass(Class<? extends ORDERED> referenceClass) {
    ensureSorted();
    ORDERED reference = null;
    for (Iterator<ORDERED> it = m_orderedObjects.iterator(); it.hasNext();) {
      reference = it.next();
      if (referenceClass.isInstance(reference)) {
        return reference;
      }
    }
    throw new IllegalArgumentException("there is no reference object in this ordered collection that extends the given reference class.");
  }

  private ORDERED getAdjacentObject(ORDERED reference, boolean previous) {
    ensureSorted();
    for (ListIterator<ORDERED> it = m_orderedObjects.listIterator(); it.hasNext();) {
      // 1. search reference object
      ORDERED next = it.next();
      if (next != reference) {
        continue;
      }

      // 2. get adjacent object
      if (previous) {
        // the list is traversed from begin to end. Therefore the iterator's next element index is already incremented.
        // Go to previous element (see ListIterator java doc for details).
        it.previous();
        // 2.a.1 check if there is another previous element and return it if so
        if (it.hasPrevious()) {
          return it.previous();
        }
      }
      else if (it.hasNext()) {
        // 2.b return next element
        return it.next();
      }

      // 2.c there is no previous or next element
      return null;
    }
    throw new IllegalArgumentException("the given reference element is not part of this ordered collection. reference: '" + reference + "'");
  }

  /**
   * @param objectsToAdd
   * @param reference
   * @param baseOrder
   * @param delta
   * @return
   */
  private boolean insertAfter(Collection<? extends ORDERED> objectsToAdd, ORDERED reference, double baseOrder, double delta) {
    for (ListIterator<ORDERED> it = m_orderedObjects.listIterator(); it.hasNext();) {
      // 1. search reference Object
      if (it.next() != reference) {
        continue;
      }

      // 2. insert objects
      int i = 1;
      for (ORDERED o : objectsToAdd) {
        o.setOrder(baseOrder + (double) i * delta);
        it.add(o);
        i++;
      }

      return true;
    }
    return false;
  }
}
