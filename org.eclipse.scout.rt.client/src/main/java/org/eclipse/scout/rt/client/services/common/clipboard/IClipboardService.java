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

import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.dnd.TransferObject;
import org.eclipse.scout.commons.dnd.TransferObjectRequest;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.service.IService;

/**
 * This service provides access to the system clipboard and therefore to exchange
 * data with other applications. It is not intended to be used for application-internal
 * communication. In particular, all methods of this interface are considered to be
 * executed asynchronously (in fact, this is a requirement by the underlying
 * implementations which use UI resources for accessing the system clipboard).
 */
@Priority(-3)
public interface IClipboardService extends IService {

  /**
   * Puts the given transfer object into the system clipboard. Typically this is done
   * asynchronously because the clipboard is a UI resource.
   * 
   * @param transferObject
   * @throws ProcessingException
   */
  void setContents(TransferObject transferObject) throws ProcessingException;

  /**
   * Reads the requested contents of the clipboard and invokes the call back clipboard
   * consumer. The optional requests var arg parameter conditions the parameters of the
   * call back method {@link IClipboardConsumer#consume(TransferObject...)}.
   * 
   * @param clipboardConsumer
   * @param requests
   * @throws ProcessingException
   */
  void consumeContents(IClipboardConsumer clipboardConsumer, TransferObjectRequest... requests) throws ProcessingException;

  /**
   * Convenience method for putting the given string into the system clipboard.See {@link #setContents(TransferObject)}
   * for semantics.
   * 
   * @param textContents
   * @throws ProcessingException
   */
  void setTextContents(String textContents) throws ProcessingException;
}
