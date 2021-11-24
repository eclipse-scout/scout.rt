/*
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.dataobject;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public final class DataObjectVisitors {

  private DataObjectVisitors() {
  }

  /**
   * Visits all nodes and calls on each element of given type the provided consumer. Matching nodes are visited
   * <b>not</b> recursively. If a node matches child nodes of this node are not visited.
   */
  public static <T> void forEach(Object root, Class<? extends T> elementType, Consumer<T> consumer) {
    forEach(root, new P_TypedElementConsumer<>(elementType, consumer, false));
  }

  /**
   * Visits all nodes and calls on each element of given type the provided consumer. Matching nodes are visited
   * recursively. If a node matches, child nodes of this node are also visited.
   */
  public static <T> void forEachRec(Object root, Class<? extends T> elementType, Consumer<T> consumer) {
    forEach(root, new P_TypedElementConsumer<>(elementType, consumer, true));
  }

  /**
   * Visits all nodes and calls on each element of given type the provided consumer. If consumer returns {@code true}
   * then matching node is visited recursively.
   */
  public static <T> void forEachRecIf(Object root, Class<? extends T> elementType, Predicate<T> consumer) {
    forEach(root, new P_TypedElementConsumer<>(elementType, consumer));
  }

  private static void forEach(Object root, Predicate<Object> elementConsumer) {
    new P_DataObjectVisitor(elementConsumer).visit(root);
  }

  /**
   * Visits all nodes and calls on each element of given type the provided operator. The current value will be replace
   * with the new value from calling the operator. Children of nodes that match element type are not visited.
   * <p>
   * Note: this operation might fail if data object contains unmodifiable collections or maps.
   */
  public static <T> void replaceEach(Object root, Class<? extends T> elementType, UnaryOperator<T> operator) {
    new P_ReplaceDataObjectVisitor<>(elementType, operator).visit(root);
  }

  private static final class P_DataObjectVisitor extends AbstractDataObjectVisitor {

    private final Predicate<Object> m_elementConsumer;

    private P_DataObjectVisitor(Predicate<Object> elementConsumer) {
      m_elementConsumer = elementConsumer;
    }

    @Override
    protected <T> void caseNode(T node, Consumer<T> chain) {
      if (m_elementConsumer.test(node)) {
        super.caseNode(node, chain); // call chain
      }
    }
  }

  private static final class P_TypedElementConsumer<T> implements Predicate<Object> {

    private final Class<? extends T> m_elementType;
    private final Predicate<T> m_elementConsumer;

    private P_TypedElementConsumer(Class<? extends T> elementType, Consumer<T> elementConsumer, boolean recursive) {
      this(elementType, o -> {
        elementConsumer.accept(o);
        return recursive;
      });
    }

    private P_TypedElementConsumer(Class<? extends T> elementType, Predicate<T> elementConsumer) {
      m_elementType = elementType;
      m_elementConsumer = elementConsumer;
    }

    @Override
    public boolean test(Object t) {
      if (m_elementType.isInstance(t)) {
        return m_elementConsumer.test(m_elementType.cast(t));
      }
      return true;
    }
  }

  private static final class P_ReplaceDataObjectVisitor<T> extends AbstractReplacingDataObjectVisitor {

    private final Class<? extends T> m_elementType;
    private final UnaryOperator<T> m_operator;

    public P_ReplaceDataObjectVisitor(Class<? extends T> elementType, UnaryOperator<T> operator) {
      m_elementType = elementType;
      m_operator = operator;
    }

    @Override
    protected <OT> OT replaceOrVisit(OT o) {
      if (m_elementType.isInstance(o)) {
        //noinspection unchecked
        return (OT) m_operator.apply(m_elementType.cast(o));
      }
      else {
        return super.replaceOrVisit(o);
      }
    }
  }
}
