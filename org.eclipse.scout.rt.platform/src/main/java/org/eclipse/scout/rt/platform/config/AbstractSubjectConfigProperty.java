/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
