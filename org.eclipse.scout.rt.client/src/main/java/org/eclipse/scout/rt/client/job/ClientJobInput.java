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
package org.eclipse.scout.rt.client.job;

import java.security.AccessControlContext;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.job.IJobInput;
import org.eclipse.scout.commons.job.JobContext;
import org.eclipse.scout.commons.job.JobInput;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.shared.ISession;

/**
 * Implementation of {@link IJobInput} with some additional data used for client-side jobs.
 *
 * @see JobInput
 * @since 5.1
 */
public class ClientJobInput extends JobInput {

  private IClientSession m_session;

  /**
   * Use {@link #empty()} or {@link #defaults()} to create a {@link ClientJobInput}.
   */
  private ClientJobInput(final IJobInput template) {
    super(template);
  }

  @Override
  public ClientJobInput id(final long id) {
    return (ClientJobInput) super.id(id);
  }

  @Override
  public ClientJobInput name(final String name) {
    return (ClientJobInput) super.name(name);
  }

  @Override
  public ClientJobInput subject(final Subject subject) {
    return (ClientJobInput) super.subject(subject);
  }

  @Override
  public ClientJobInput context(final JobContext context) {
    return (ClientJobInput) super.context(context);
  }

  /**
   * @param session
   *          {@link IClientSession} of behalf of which the job is to be executed; must not be <code>null</code>.
   * @return {@link IJobInput} to be used as builder.
   */
  public ClientJobInput session(final IClientSession session) {
    m_session = session;
    return this;
  }

  /**
   * @return {@link IClientSession} of behalf of which the job is to be executed; must not be <code>null</code>.
   */
  public IClientSession getSession() {
    return m_session;
  }

  /**
   * Creates a {@link ClientJobInput} that is only filled with the job-context of the current thread, or if not
   * available, an empty one.
   */
  public static ClientJobInput empty() {
    return new ClientJobInput(JobInput.empty());
  }

  /**
   * Creates a {@link ClientJobInput} filled with the defaults from the current calling context.
   * <ul>
   * <li>{@link ClientJobInput#getSubject()}: subject of the current {@link AccessControlContext};</li>
   * <li>{@link ClientJobInput#getSession()}: session associated with the current thread;</li>
   * <li>{@link ClientJobInput#getContext()}: copy of the job-context associated with the current thread, or if not
   * available, an empty {@link JobContext};
   * </ul>
   */
  public static ClientJobInput defaults() {
    return new ClientJobInput(JobInput.defaults()).session((IClientSession) ISession.CURRENT.get());
  }
}
