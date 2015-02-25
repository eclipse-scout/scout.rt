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
package org.eclipse.scout.commons.annotations;

import java.util.zip.Adler32;

/**
 * The {@link Adler32} checksum of the resource the form data has been created for.
 * This annotation is used to determ a form data has to be created new or is still valid.
 */
public @interface FormDataChecksum {

  long value() default 0L;
}
