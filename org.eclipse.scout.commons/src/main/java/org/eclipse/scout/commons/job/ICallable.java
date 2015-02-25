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
package org.eclipse.scout.commons.job;

import java.util.concurrent.Callable;

import org.eclipse.scout.commons.job.Executables.IExecutable;

/**
 * Represents a callable to be given to a job manager for execution.
 *
 * @see Callable
 * @since 5.1
 */
public interface ICallable<RESULT> extends IExecutable<RESULT>, Callable<RESULT> {
}
