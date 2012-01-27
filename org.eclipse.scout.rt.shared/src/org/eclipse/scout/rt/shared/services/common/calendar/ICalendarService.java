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
package org.eclipse.scout.rt.shared.services.common.calendar;

import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.service.IService;

/**
 * This interface is simply a marker interface for grouping calendar realated
 * data services in BSI CASE
 * <p>
 * BSI CASE will add the following example operations to the service interface / implementation
 * 
 * <pre>
 * ICalendarItem[] getItems(Date minDate, Date maxDate) throws ProcessingException;
 * 
 * void storeItems(ICalendarItem[] items, boolean delta) throws ProcessingException;
 * </pre>
 * 
 * However, these operations are simply a basic sample and implementations might have more actual parameters to the
 * method
 */
@Priority(-3)
public interface ICalendarService extends IService {

}
