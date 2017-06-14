/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.platform.util.collection;

import static org.eclipse.scout.rt.platform.util.Assertions.assertNotNull;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;

/**
 * <h3>{@link TransformingSet}</h3>
 * <p>
 * {@link Set} implementation that allows to wrap another {@link Set}. For each input or output operation of this set
 * the specified {@link ITransformer} is used to convert the values from the wrapped {@link Set} to this {@link Set}.
 * <p>
 * This {@link Set} is modifiable if the wrapped {@link Set} is modifiable.
 * <p>
 * Please note: The operations {@link #contains(Object)} and {@link #remove(Object)} only accept the matching
 * {@link Set} type T. If another value is passed a {@link ClassCastException} is thrown.
 */
@SuppressWarnings("squid:S2160") // 'Subclasses that add fields should override "equals"' not necessary. implementation of AbstractSet is sufficient.
public class TransformingSet<F, T> extends AbstractSet<T> implements Serializable {

  private static final long serialVersionUID = 0;

  private final Set<F> m_wrappedSet;
  private final ITransformer<T, F> m_extToIntTransformer;

  /**
   * @param sourceSet
   *          The wrapped {@link Set}. Must not be {@code null}.
   * @param function
   *          The wrapping {@link ITransformer} function. Must not be {@code null}.
   */
  public TransformingSet(Set<F> sourceSet, ITransformer<T, F> function) {
    this.m_wrappedSet = assertNotNull(sourceSet);
    this.m_extToIntTransformer = assertNotNull(function);
  }

  @Override
  public Iterator<T> iterator() {
    final Iterator<F> inner = m_wrappedSet.iterator();
    return new Iterator<T>() {
      @Override
      public boolean hasNext() {
        return inner.hasNext();
      }

      @Override
      public T next() {
        return m_extToIntTransformer.revert(inner.next());
      }

      @Override
      public void remove() {
        inner.remove();
      }
    };
  }

  @Override
  public int size() {
    return m_wrappedSet.size();
  }

  @Override
  @SuppressWarnings("unchecked")
  public boolean contains(Object o) {
    return m_wrappedSet.contains(m_extToIntTransformer.transform((T) o));
  }

  @Override
  public boolean isEmpty() {
    return m_wrappedSet.isEmpty();
  }

  @Override
  public void clear() {
    m_wrappedSet.clear();
  }

  @Override
  public boolean add(T e) {
    return m_wrappedSet.add(m_extToIntTransformer.transform(e));
  }

  @Override
  @SuppressWarnings("unchecked")
  public boolean remove(Object o) {
    return m_wrappedSet.remove(m_extToIntTransformer.transform((T) o));
  }
}
