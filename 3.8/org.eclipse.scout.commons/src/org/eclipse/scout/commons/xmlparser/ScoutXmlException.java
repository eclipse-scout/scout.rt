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
package org.eclipse.scout.commons.xmlparser;

/**
 * Title : Scout XML Exception Description: Copyright : Copyright (c) 2006 BSI
 * AG, ETH Zürich, Stefan Vogt Company : BSI AG www.bsiag.com
 * 
 * @version 1.0
 */
public class ScoutXmlException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public ScoutXmlException() {
    super();
  }

  public ScoutXmlException(String message) {
    super(message);
  }

  public ScoutXmlException(String message, Throwable cause) {
    super(message, cause);
  }
}
