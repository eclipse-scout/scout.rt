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
package org.eclipse.scout.rt.shared.services.common.shell;

import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.platform.service.IService;
import org.eclipse.scout.rt.shared.validate.IValidationStrategy;
import org.eclipse.scout.rt.shared.validate.InputValidation;

/**
 * Currently known implementations are {@link org.eclipse.scout.rt.shared.win32.x86.service.internal.WindowsService} and
 * {@link org.eclipse.scout.rt.client.services.common.shell.DefaultShellService}
 *
 * @deprecated since 6.0.0. Because Scout is a web-framework now, the Scout client should not use the shell-service
 *             anymore. In earlier releases the Scout client layer was deployed on the client-workstation and ran
 *             together with the UI layer in a Java VM. Today the Scout client runs on a server and the UI layer runs in
 *             a browser on the client workstation. Thus it's not possible to do anything useful with the shell-service
 *             in the client. Use <code>IDesktop.openDownloadInBrowser()</code> when you must instruct the UI layer to
 *             download a (binary) resource, like a Word- or Excel document or a PDF.
 */
@Priority(-3)
@InputValidation(IValidationStrategy.PROCESS.class)
@Deprecated
public interface IShellService extends IService {

  /**
   * Open the resource at the specified path with the typical application
   * Examples: open URLs in the browser, word documents in Word, mail addresses
   * in the E-Mail client, etc.
   */
  void shellOpen(String path) throws ProcessingException;

}
