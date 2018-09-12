/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.mail;

import org.eclipse.scout.rt.platform.resource.BinaryResource;

/**
 * Data source for binary resource.
 *
 * @since 6.0
 * @deprecated Use {@link org.eclipse.scout.rt.mail.BinaryResourceDataSource} instead.
 */
@Deprecated
public class BinaryResourceDataSource extends org.eclipse.scout.rt.mail.BinaryResourceDataSource {

  public BinaryResourceDataSource(BinaryResource binaryResource) {
    super(binaryResource);
  }
}
