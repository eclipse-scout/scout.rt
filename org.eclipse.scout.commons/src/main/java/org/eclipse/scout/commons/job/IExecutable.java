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

/**
 * Marker interface for an executable to be given to a job manager for execution.
 * <p/>
 * The job manager accepts one of the following implementing interfaces:
 * <ul>
 * <li>{@link IRunnable}: If executing a task that does not return a result to the caller.</li>
 * <li>{@link ICallable}: If executing a task that returns a result to the caller.</li>
 * </ul>
 *
 * @see IRunnable
 * @see ICallable
 * @since 5.1
 */
public interface IExecutable<RESULT> {
}
