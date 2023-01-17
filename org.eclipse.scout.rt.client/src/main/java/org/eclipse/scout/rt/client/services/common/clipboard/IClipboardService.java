/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.services.common.clipboard;

import java.util.Collection;

import org.eclipse.scout.rt.client.ui.dnd.TransferObject;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.resource.MimeType;
import org.eclipse.scout.rt.platform.service.IService;

/**
 * This service provides access to the system clipboard and therefore to exchange data with other applications. It is
 * not intended to be used for application-internal communication. In particular, all methods of this interface are
 * considered to be executed asynchronously (in fact, this is a requirement by the underlying implementations which use
 * UI resources for accessing the system clipboard).
 */
public interface IClipboardService extends IService {

  /**
   * Puts the given transfer object into the system clipboard. Typically this is done asynchronously because the
   * clipboard is a UI resource.
   *
   * @param transferObject
   */
  void setContents(TransferObject transferObject);

  /**
   * Reads the requested contents of the clipboard and returns them as {@link Collection} of {@link BinaryResource}.
   * Empty collection is returned if user cancels this request. The optional requests var arg parameter conditions the
   * content types of the {@link BinaryResource} which are returned, see {@link BinaryResource#getContentType()}.
   *
   * @param allowedMimeTypes
   */
  Collection<BinaryResource> getClipboardContents(MimeType... allowedMimeTypes);

  /**
   * Convenience method for putting the given string into the system clipboard.See {@link #setContents(TransferObject)}
   * for semantics.
   *
   * @param textContents
   */
  void setTextContents(String textContents);

}
