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
import org.eclipse.scout.rt.shared.validate.InputValidation;
import org.eclipse.scout.rt.shared.validate.IValidationStrategy;
import org.eclipse.scout.service.IService;

/**
 * Currently known implementations are {@link org.eclipse.scout.rt.shared.win32.x86.service.internal.WindowsService} and
 * {@link org.eclipse.scout.rt.client.services.common.shell.DefaultShellService}
 */
@Priority(-3)
@InputValidation(IValidationStrategy.PROCESS.class)
public interface IShellService extends IService {

  /**
   * Open the resource at the specified path with the typical application
   * Examples: open URLs in the browser, word documents in Word, mail addresses
   * in the E-Mail client, etc.
   */
  void shellOpen(String path) throws ProcessingException;

}
