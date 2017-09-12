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
package org.eclipse.scout.rt.platform.holders;

/**
 * <p>
 * Holder class for beans.
 * </p>
 * <h3>Simple example</h3>
 * <p>
 * The <code>Order</code> class would contain at least the attributes <code>OrderNr</code>, <code>Reporting </code>and
 * <code>Year</code>.
 * </p>
 * 
 * <pre>
 * BeanArrayHolder&lt;Order&gt; orders = new BeanArrayHolder&lt;Order&gt;(Order.class);
 * SQL.selectInto(
 *     &quot;select ORDER_NR, &quot; +
 *         &quot;       REPORTING_UID, &quot; +
 *         &quot;       YEAR &quot; +
 *         &quot;  from order &quot; +
 *         &quot; where 1=1 into :orderNr, :reporting, :year &quot;,
 *     orders);
 * Order[] allOrders = orders.getBeans();
 * </pre>
 * 
 * @since 3.0
 */

public interface IBeanArrayHolder<T> {
  /**
   * States a bean in an {@link IBeanArrayHolder} can have.
   */
  enum State {
    NON_CHANGED,
    INSERTED,
    UPDATED,
    DELETED
  }

  /**
   * Gets the the number of beans with a state that is contained in the passed states. If no states are passed the
   * number of all beans in the holder is returned.
   * 
   * @param states
   * @return a value corresponding to the number of beans in this holder with a given state
   */
  int getBeanCount(State... states);

  /**
   * gets all beans with belonging to the set of supplied states
   * 
   * @param states
   *          that should be returned. If <code>null</code> or an empty array is passed, then all beans regardless of
   *          state are returned.
   * @return an array of beans, does never return <code>null</code>
   */
  T[] getBeans(State... states);

  /**
   * Creates a new bean by calling <code>newInstance</code> on the class of beans. An constructor without parameters
   * must exist. The bean is added to this holder.
   * 
   * @return the newly created bean.
   */
  T addBean();

  /**
   * Adjusts the size of the holder to the size given in the parameter. New beans are added or existing beans removed.
   * 
   * @param size
   */
  void ensureSize(int size);

  void setRowState(T bean, State state);

  /**
   * @param bean
   * @return the state of the supplied bean in the holder
   */
  State getRowState(T bean);

  /**
   * @return the bean class contained in the holder
   */
  Class<T> getHolderType();

}
