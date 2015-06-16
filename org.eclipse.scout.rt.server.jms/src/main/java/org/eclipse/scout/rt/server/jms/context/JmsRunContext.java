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
package org.eclipse.scout.rt.server.jms.context;

import java.util.Locale;

import javax.jms.Message;
import javax.security.auth.Subject;

import org.eclipse.scout.commons.ToStringBuilder;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.job.PropertyMap;

/**
 * The <code>JmsRunContext</code> facilitates propagation of the <i>JMS Java Message Service</i> state. This
 * context is not intended to be propagated across different threads.
 * <p/>
 * A context typically represents a "snapshot" of the current calling state. This class facilitates propagation of that
 * state.
 * <p/>
 * The 'setter-methods' returns <code>this</code> in order to support for method chaining. The context has the following
 * characteristics:
 * <ul>
 * <li>{@link Subject}</li>
 * <li>{@link NlsLocale#CURRENT}</li>
 * <li>{@link PropertyMap#CURRENT}</li>
 * </ul>
 *
 * @since 5.1
 * @see RunContext
 */
public class JmsRunContext extends RunContext {

  /**
   * The {@link Message} which is currently associated with the current thread.
   */
  public static final ThreadLocal<Message> CURRENT_JMS_MESSAGE = new ThreadLocal<>();

  protected Message m_jmsMessage;

  @Override
  public JmsRunContext runMonitor(RunMonitor runMonitor) {
    super.runMonitor(runMonitor);
    return this;
  }

  @Override
  public JmsRunContext subject(final Subject subject) {
    super.subject(subject);
    return this;
  }

  @Override
  public JmsRunContext locale(final Locale locale) {
    super.locale(locale);
    return this;
  }

  public JmsRunContext jmsMessage(final Message jmsMessage) {
    m_jmsMessage = jmsMessage;
    return this;
  }

  public Message jmsMessage() {
    return m_jmsMessage;
  }

  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder(this);
    builder.attr("subject", subject());
    builder.attr("locale", locale());
    builder.ref("message", jmsMessage());
    return builder.toString();
  }

  // === fill methods ===

  @Override
  protected void copyValues(final RunContext origin) {
    final JmsRunContext originRunContext = (JmsRunContext) origin;

    super.copyValues(originRunContext);
    m_jmsMessage = originRunContext.m_jmsMessage;
  }

  @Override
  protected void fillCurrentValues() {
    super.fillCurrentValues();
    m_jmsMessage = JmsRunContext.CURRENT_JMS_MESSAGE.get();
  }

  @Override
  protected void fillEmptyValues() {
    super.fillEmptyValues();
    m_jmsMessage = null;
  }

  @Override
  public JmsRunContext copy() {
    final JmsRunContext copy = BEANS.get(JmsRunContext.class);
    copy.copyValues(this);
    return copy;
  }

}
