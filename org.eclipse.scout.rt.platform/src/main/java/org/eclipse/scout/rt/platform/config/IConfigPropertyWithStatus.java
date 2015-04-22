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
package org.eclipse.scout.rt.platform.config;

import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.commons.exception.ProcessingStatus;

/**
 * A configuration property with validity status.
 */
public interface IConfigPropertyWithStatus<DATA_TYPE> extends IConfigProperty<DATA_TYPE> {
  /**
   * Gets the {@link IProcessingStatus} describing the value of this property.
   * 
   * @return the {@link IProcessingStatus} of this property value. May not return <code>null</code>. Should return
   *         {@link ProcessingStatus#OK_STATUS} if the value is valid.
   */
  IProcessingStatus getStatus();
}
