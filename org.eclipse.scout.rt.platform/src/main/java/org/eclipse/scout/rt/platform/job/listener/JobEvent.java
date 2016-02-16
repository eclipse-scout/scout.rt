/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.job.listener;

import java.util.EventObject;

import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.IJobManager;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.ToStringBuilder;

/**
 * Event object describing a {@link IFuture} or {@link IJobManager} change.
 * <p>
 *
 * @since 5.1
 */
public class JobEvent extends EventObject {

  private static final long serialVersionUID = 1L;

  private final JobEventType m_type;
  private final transient JobEventData m_data;

  public JobEvent(final IJobManager jobManager, final JobEventType type, final JobEventData data) {
    super(jobManager);
    m_type = Assertions.assertNotNull(type, "missing jobEventType");
    m_data = Assertions.assertNotNull(data, "missing jobEventData");
  }

  @Override
  public IJobManager getSource() {
    return (IJobManager) super.getSource();
  }

  /**
   * Returns the type which describes this event; is never <code>null</code>.
   */
  public JobEventType getType() {
    return m_type;
  }

  /**
   * Returns data associated with this event; is never <code>null</code>.
   * <p>
   * Depending on {@link JobEventType}, different data properties are set. See documentation of {@link JobEventType} for
   * more information.
   */
  public JobEventData getData() {
    return m_data;
  }

  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder(this);
    builder.attr("type", m_type.name());
    builder.attr("data", m_data);
    builder.ref("source", getSource());
    return builder.toString();
  }
}
