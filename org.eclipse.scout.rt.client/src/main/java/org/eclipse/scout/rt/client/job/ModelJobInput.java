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

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientContext;
import org.eclipse.scout.rt.platform.job.IJobManager;
import org.eclipse.scout.rt.shared.ui.UserAgent;

/**
 * Describes a model job with context information to be applied onto the executing worker thread during the time
 * of the job's execution.
 * <p/>
 * The 'setter-methods' returns <code>this</code> in order to support for method chaining.
 *
 * @see ClientContext
 * @see ModelJobs
 * @see IJobManager
 * @since 5.1
 */
public class ModelJobInput extends ClientJobInput {

  protected ModelJobInput(final ClientJobInput origin) {
    super(origin);
  }

  @Override
  public ModelJobInput setId(final String id) {
    return (ModelJobInput) super.setId(id);
  }

  @Override
  public ModelJobInput setName(final String name) {
    return (ModelJobInput) super.setName(name);
  }

  @Override
  public Object getMutex() {
    return Assertions.assertNotNull(getContext().getSession(), "For model jobs, the session must not be null because used as mutex object, so that no two jobs of the same session run in parallel at any time.");
  }

  @Override
  public ModelJobInput setMutex(final Object mutexObject) {
    throw new UnsupportedOperationException("For model jobs, the mutex object cannot be set. Implicitly, the session is used as mutex object so that no two jobs of the same session run in parallel at any time.");
  }

  @Override
  public ModelJobInput setExpirationTime(final long time, final TimeUnit timeUnit) {
    return (ModelJobInput) super.setExpirationTime(time, timeUnit);
  }

  @Override
  public ModelJobInput setContext(final ClientContext context) {
    return (ModelJobInput) super.setContext(context);
  }

  @Override
  public ModelJobInput setSubject(final Subject subject) {
    return (ModelJobInput) super.setSubject(subject);
  }

  @Override
  public ModelJobInput setLocale(final Locale locale) {
    return (ModelJobInput) super.setLocale(locale);
  }

  @Override
  public ModelJobInput setSession(final IClientSession session) {
    return (ModelJobInput) super.setSession(session);
  }

  @Override
  public ModelJobInput setSessionRequired(final boolean sessionRequired) {
    return (ModelJobInput) super.setSessionRequired(true);
  }

  @Override
  public ModelJobInput setUserAgent(final UserAgent userAgent) {
    return (ModelJobInput) super.setUserAgent(userAgent);
  }

  @Override
  public String getThreadName() {
    return "scout-model-thread";
  }

  //=== construction methods ===

  @Override
  public ModelJobInput copy() {
    return new ModelJobInput(this).setContext(getContext().copy());
  }

  public static ModelJobInput defaults() {
    return new ModelJobInput(ClientJobInput.defaults()).setContext(ClientContext.defaults());
  }

  public static ModelJobInput empty() {
    return new ModelJobInput(ClientJobInput.empty()).setContext(ClientContext.empty());
  }
}
