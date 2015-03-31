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
package org.eclipse.scout.rt.server.context;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.ToStringBuilder;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.platform.OBJ;
import org.eclipse.scout.rt.platform.context.RunContext;
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

  // TODO [dwi]: implement this class

  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder(this);
    builder.attr("subject", subject());
    builder.attr("locale", locale());
    return builder.toString();
  }

  // === fill methods ===

  @Override
  protected void copyValues(final RunContext origin) {
    final JmsRunContext originRunContext = (JmsRunContext) origin;

    super.copyValues(originRunContext);
  }

  @Override
  protected void fillCurrentValues() {
    super.fillCurrentValues();
  }

  @Override
  protected void fillEmptyValues() {
    super.fillEmptyValues();
  }

  @Override
  public JmsRunContext copy() {
    final JmsRunContext copy = OBJ.get(JmsRunContext.class);
    copy.copyValues(this);
    return copy;
  }
}
