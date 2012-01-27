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
package org.eclipse.scout.rt.client.services.common.clipboard;

import org.eclipse.scout.commons.dnd.TransferObject;
import org.eclipse.scout.commons.exception.ProcessingException;

/**
 * Call back interface for clipboard consumers.
 */
public interface IClipboardConsumer {

  /**
   * Call back method invoked by the
   * {@link IClipboardService#consumeContents(IClipboardConsumer, com.bsiag.commons.dnd.TransferObjectRequest...)}
   * service.
   * 
   * @param transferObjects
   * @throws ProcessingException
   */
  void consume(TransferObject... transferObjects) throws ProcessingException;
}
