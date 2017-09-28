/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.config;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.security.SimplePrincipal;

/**
 * Property to represent a {@link Subject}.
 *
 * @since 5.1
 */
public abstract class AbstractSubjectConfigProperty extends AbstractConfigProperty<Subject, String> {

  @Override
  protected Subject parse(final String value) {
    return convertToSubject(value);
  }

  protected Subject convertToSubject(final String user) {
    if (user == null) {
      return null;
    }
    final Subject subject = new Subject();
    subject.getPrincipals().add(new SimplePrincipal(user));
    subject.setReadOnly();
    return subject;
  }
}
