/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons.annotations;

/**
 * Runtime equivalent of {@link Order} annotation. This interface allows defining an order on dynamically created
 * objects, so that they can be used along with statically ordered objects in ordered collections.
 * <p/>
 * <b>Note</b>: Note if both are available, an {@link Order} annotation and the order provided by the {@link IOrdered}
 * interface, the {@link Order} annotation is used.
 * 
 * @since 3.8.1
 */
public interface IOrdered {

  /**
   * @return Returns the object's order.
   */
  double getOrder();
}
