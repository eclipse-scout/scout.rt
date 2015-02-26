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

import java.security.AccessControlContext;
import java.security.AccessController;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.StringUtility;

/**
 * Default implementation of {@link IJobInput}.
 *
 * @since 5.1
 */
public class JobInput implements IJobInput {

  private long m_id;
  private String m_name;
  private Subject m_subject;
  private JobContext m_context;

  protected JobInput() {
  }

  /**
   * Creates a copy from the given template.
   */
  protected JobInput(final IJobInput template) {
    id(template.getId());
    name(template.getName());
    context(template.getContext());
    subject(template.getSubject());
  }

  @Override
  public IJobInput id(final long id) {
    m_id = id;
    return this;
  }

  @Override
  public IJobInput name(final String name) {
    m_name = name;
    return this;
  }

  @Override
  public IJobInput subject(final Subject subject) {
    m_subject = subject;
    return this;
  }

  @Override
  public IJobInput context(final JobContext context) {
    m_context = context;
    return this;
  }

  @Override
  public long getId() {
    return m_id;
  }

  @Override
  public String getName() {
    return m_name;
  }

  @Override
  public Subject getSubject() {
    return m_subject;
  }

  @Override
  public JobContext getContext() {
    return m_context;
  }

  @Override
  public String getIdentifier(final String defaultIdentifier) {
    if (m_id != 0L && StringUtility.hasText(m_name)) {
      return String.format("%s;%s", m_id, m_name);
    }
    else if (StringUtility.hasText(m_name)) {
      return m_name;
    }
    else if (m_id != 0L) {
      return String.valueOf(m_id);
    }
    else {
      return defaultIdentifier;
    }
  }

  /**
   * Creates a {@link IJobInput} that is only filled with the {@link JobContext} of the current thread, or if not
   * available, an empty one.
   */
  public static IJobInput empty() {
    return new JobInput().context(JobContext.copy(JobContext.CURRENT.get()));
  }

  /**
   * Creates a {@link JobInput} filled with the defaults from the current calling context.
   * <ul>
   * <li>{@link IJobInput#getSubject()}: subject of the current {@link AccessControlContext};</li>
   * <li>{@link IJobInput#getContext()}: copy of the job-context associated with the current thread, or if not
   * available, an empty {@link JobContext};
   * </ul>
   */
  public static IJobInput defaults() {
    return JobInput.empty().subject(Subject.getSubject(AccessController.getContext()));
  }
}
