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
package org.eclipse.scout.service;

/**
 * This marker interface solves the performance and log issue of
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=299351
 * <p>
 * Whenever a service factory wants to return null (for example due to some context conditions dynamically not met) it
 * merely returns a dynamic proxy implementing this interface instead of null.
 * <p>
 * {@link SERVICES#getService(Class)} detects this and will convert to null.
 * <p>
 * That way no excess framework events are published.
 */
public interface INullService {

}
