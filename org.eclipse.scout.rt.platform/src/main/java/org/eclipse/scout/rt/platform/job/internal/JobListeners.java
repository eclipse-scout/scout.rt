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
package org.eclipse.scout.rt.platform.job.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.filter.IFilter;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.listener.IJobListener;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;
import org.eclipse.scout.rt.platform.util.IAdaptable;
import org.eclipse.scout.rt.platform.util.IRegistrationHandle;

/**
 * Responsible for notifying all job listeners about job lifecycle events.
 * <p/>
 * This implementation works with global and local listeners to reduce contention among threads by registering listeners
 * directly on the related Future whenever possible. Also, a {@link CopyOnWriteArrayList} is used to hold global
 * listeners as more read than write operations occur.
 *
 * @since 5.1
 */
@Bean
public class JobListeners {

  private final List<JobListenerWithFilter> m_globalListeners = new CopyOnWriteArrayList<>();

  /**
   * Registers the given listener to be notified about job lifecycle events. If the listener is already registered, that
   * previous registration is replaced.
   *
   * @param listener
   *          listener to be registered.
   * @param filter
   *          filter to only get notified about events of interest - that is for events accepted by the filter.
   * @return A token representing the registration of the given {@link IJobListener}. This token can later be used to
   *         unregister the listener.
   */
  IRegistrationHandle add(final IFilter<JobEvent> filter, final IJobListener listener) {
    final IFuture[] futures = getFilteredFutures(filter);
    if (futures != null) {
      return addLocalListener(filter, listener, futures); // register the listener directly on the future to reduce contention.
    }
    else {
      return addGlobalListener(filter, listener);
    }
  }

  /**
   * Registers the given listener as global listeners.
   */
  protected IRegistrationHandle addGlobalListener(final IFilter<JobEvent> filter, final IJobListener listener) {
    final JobListenerWithFilter globalListener = new JobListenerWithFilter(listener, filter);
    m_globalListeners.add(globalListener);

    return new IRegistrationHandle() {

      @Override
      public void dispose() {
        m_globalListeners.remove(globalListener);
      }
    };
  }

  /**
   * Registers the given listener locally on the given Futures to reduce contention.
   */
  protected IRegistrationHandle addLocalListener(final IFilter<JobEvent> filter, final IJobListener listener, final IFuture[] futures) {
    final List<IRegistrationHandle> registrations = new ArrayList<>(futures.length);
    for (final IFuture<?> future : futures) {
      registrations.add(future.addListener(filter, listener));
    }

    return new IRegistrationHandle() {

      @Override
      public void dispose() {
        for (final IRegistrationHandle registration : registrations) {
          registration.dispose();
        }
      }
    };
  }

  /**
   * Notifies all listener about an event, unless not accept by the filter.<br/>
   * This method never throws an exception.
   *
   * @param eventToFire
   *          The event to fire.
   */
  public void notifyListeners(final JobEvent eventToFire) {
    notifyGlobalListeners(eventToFire);
    notifyLocalListeners(eventToFire);
  }

  /**
   * Notifies all global listeners accepting the given event.
   */
  public void notifyGlobalListeners(final JobEvent eventToFire) {
    for (final JobListenerWithFilter globalListener : m_globalListeners) {
      globalListener.changed(eventToFire);
    }
  }

  /**
   * Notifies all local listeners which are registered on the event's future and accept the given event.
   */
  public void notifyLocalListeners(final JobEvent eventToFire) {
    final JobFutureTask<?> future = (JobFutureTask<?>) eventToFire.getData().getFuture();
    if (future == null) {
      return;
    }
    for (final JobListenerWithFilter localListener : future.getListeners()) {
      localListener.changed(eventToFire);
    }
  }

  /**
   * Returns the futures constrained by the given filter (if any), or <code>null</code> otherwise.<br/>
   * For that to work, the given filter must implement {@link IAdaptable} for the type <code>IFuture[].class</code>.
   */
  protected IFuture[] getFilteredFutures(final IFilter<JobEvent> filter) {
    if (filter instanceof IAdaptable) {
      return ((IAdaptable) filter).getAdapter(IFuture[].class);
    }
    return null; // NOSONAR
  }
}
