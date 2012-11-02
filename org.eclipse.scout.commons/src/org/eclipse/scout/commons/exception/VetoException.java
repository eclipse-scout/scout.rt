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
package org.eclipse.scout.commons.exception;

import java.io.Serializable;

/**
 * This class is a special subclass of {@link ProcessingException} to mark a
 * vetoed exception that is specialized from a general {@link ProcessingException} do not change interface contract
 * since this class
 * is serializable and used in different build versions
 */
public class VetoException extends ProcessingException implements Serializable {
  private static final long serialVersionUID = 1L;

  /**
   * Empty constructor is used to support auto-webservice publishing with java
   * bean support
   */
  public VetoException() {
    super();
  }

  public VetoException(String message) {
    super(new ProcessingStatus(null, message, null, 0, IProcessingStatus.ERROR));
  }

  public VetoException(String message, Throwable cause) {
    super(new ProcessingStatus(null, message, cause, 0, IProcessingStatus.ERROR));
  }

  public VetoException(String message, Throwable cause, int errorCode) {
    super(new ProcessingStatus(null, message, cause, errorCode, IProcessingStatus.ERROR));
  }

  public VetoException(String message, int errorCode, int severity) {
    super(new ProcessingStatus(null, message, null, errorCode, severity));
  }

  public VetoException(String message, Throwable cause, int errorCode, int severity) {
    super(new ProcessingStatus(null, message, cause, errorCode, severity));
  }

  public VetoException(String title, String message) {
    super(new ProcessingStatus(title, message, null, 0, IProcessingStatus.ERROR));
  }

  public VetoException(String title, String message, Throwable cause) {
    super(new ProcessingStatus(title, message, cause, 0, IProcessingStatus.ERROR));
  }

  public VetoException(String title, String message, Throwable cause, int errorCode) {
    super(new ProcessingStatus(title, message, cause, errorCode, IProcessingStatus.ERROR));
  }

  public VetoException(String title, String message, int errorCode, int severity) {
    super(new ProcessingStatus(title, message, null, errorCode, severity));
  }

  public VetoException(String title, String message, Throwable cause, int errorCode, int severity) {
    super(new ProcessingStatus(title, message, cause, errorCode, severity));
  }

  public VetoException(IProcessingStatus status) {
    super(status);
  }
}
