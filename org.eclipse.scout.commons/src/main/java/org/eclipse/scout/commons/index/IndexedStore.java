/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons.index;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class represents an in-memory data store that provides functionality to access its elements by an index.
 * <p>
 * If an element is added to this store, that element is indexed within all registered indices. On the other hand, if an
 * element is removed, that element is removed from all indices.
 * <p>
 * This class is not thread-safe, meaning that the caller is responsible for proper synchronization.
 * <p>
 * Definition and usage:
 *
 * <pre>
 * // Define an indexed store with the indices registered.
 * public class PersonStore extends IndexedStore&lt;Person&gt; {
 * 
 *   private final P_PersonIdIndex m_idxId = registerIndex(new P_PersonIdIndex());
 *   private final P_PersonNameIndex m_idxName = registerIndex(new P_PersonNameIndex());
 * 
 *   public Person getById(long id) {
 *     return m_idxId.get(id);
 *   }
 * 
 *   public List&lt;Person&gt; getByName(String name) {
 *     return m_idxName.get(name);
 *   }
 * 
 *   public Set&lt;String&gt; getNames() {
 *     return m_idxName.indexValues();
 *   }
 * 
 *   // ====  Index definitions ==== //
 * 
 *   private class P_PersonIdIndex extends AbstractSingleValueIndex&lt;Long, Person&gt; {
 * 
 *     &#064;Override
 *     protected Long calculateIndexFor(Person person) {
 *       return person.getId();
 *     }
 *   }
 * 
 *   private class P_PersonNameIndex extends AbstractMultiValueIndex&lt;String, Person&gt; {
 * 
 *     &#064;Override
 *     protected String calculateIndexFor(Person person) {
 *       return person.getName();
 *     }
 *   }
 * }
 * 
 * // Instantiate the store and add some data.
 * PersonStore store = new PersonStore();
 * store.add(new Person().withId(1).withName(&quot;john&quot;));
 * store.add(new Person().withId(2).withName(&quot;anna&quot;));
 * store.add(new Person().withId(3).withName(&quot;john&quot;));
 * 
 * // Access data of the store by indexed values.
 * store.getById(1); // john
 * store.getById(2); // anna
 * store.getById(3); // john
 * store.getByName(&quot;anna&quot;); // anna
 * store.getNames(); // john, anna
 *
 * </pre>
 *
 * @since 5.1
 */
public class IndexedStore<ELEMENT> implements Iterable<ELEMENT> {

  private final List<IIndex<?, ELEMENT>> m_indices = new ArrayList<>();

  private final ElementIndex<ELEMENT> m_elementIndex;

  public IndexedStore() {
    m_indices.add(m_elementIndex = new ElementIndex<ELEMENT>());
  }

  /**
   * Adds the given element to this store and registers it within all indices. If already contained, old index values
   * for this element are first removed.
   */
  public void add(final ELEMENT element) {
    for (final IIndex<?, ELEMENT> index : m_indices) {
      index.addToIndex(element);
    }
  }

  /**
   * Removes the given element from this store and removes all calculated indices for the element. This method call has
   * no effect if not registered.
   */
  public void remove(final ELEMENT element) {
    for (final IIndex<?, ELEMENT> index : m_indices) {
      index.removeFromIndex(element);
    }
  }

  /**
   * Returns all elements contained in this store in the order as inserted.
   */
  public List<ELEMENT> values() {
    return m_elementIndex.values();
  }

  /**
   * Returns whether the element is contained in this store.
   */
  public boolean contains(ELEMENT element) {
    return m_elementIndex.contains(element);
  }

  /**
   * Returns the number of elements contained in this store.
   */
  public int size() {
    return values().size();
  }

  /**
   * Returns <code>true</code> if no elements are contained in this store, or <code>false</code> otherwise.
   */
  public boolean isEmpty() {
    return values().isEmpty();
  }

  /**
   * Removes all elements from this store and discards all calculated indices.
   */
  public void clear() {
    for (final IIndex<?, ELEMENT> index : m_indices) {
      index.clear();
    }
  }

  /**
   * Adds the given {@link IIndex} to index the elements of this store. If the store already contains some elements,
   * those are indexed as well.
   *
   * @param index
   *          the index to be registered to index elements.
   * @return the index as given to this method.
   */
  public <INDEX extends IIndex<?, ELEMENT>> INDEX registerIndex(final INDEX index) {
    m_indices.add(index);

    // Register all elements within the given index.
    for (final ELEMENT element : values()) {
      index.addToIndex(element);
    }

    return index;
  }

  /**
   * Removes the given index and clears the index.
   *
   * @param index
   *          the index to be unregistered.
   * @return the index as given to this method.
   */
  public <INDEX extends IIndex<?, ELEMENT>> INDEX unregisterIndex(final INDEX index) {
    m_indices.remove(index);
    index.clear();
    return index;
  }

  /**
   * Returns an iterator to iterate over all contained elements.
   */
  @Override
  public Iterator<ELEMENT> iterator() {
    return values().iterator();
  }

  // === private classes ===

  /**
   * Index to index the elements itself.
   */
  private static class ElementIndex<ELEMENT> extends AbstractSingleValueIndex<ELEMENT, ELEMENT> {

    @Override
    protected ELEMENT calculateIndexFor(final ELEMENT element) {
      return element;
    }
  }
}
