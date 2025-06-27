/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.jaxws.consumer.pool;

import java.net.URL;
import java.util.concurrent.TimeUnit;

import jakarta.xml.ws.Service;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.transaction.AbstractTransactionMember;
import org.eclipse.scout.rt.platform.transaction.ITransaction;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.server.jaxws.consumer.IPortProvider;
import org.eclipse.scout.rt.server.jaxws.implementor.JaxWsImplementorSpecifics;
import org.quartz.SimpleScheduleBuilder;

/**
 * Port provider that is backed by a pool for {@link Service}s and ports, respectively. Ports returned are attached to
 * the current transaction and are transparently put back into the pool when the transaction ends.<br>
 * <b>Note:</b> This provider works only within a valid Scout transaction.
 *
 * @since 6.0.300
 */
public class PooledPortProvider<SERVICE extends Service, PORT> implements IPortProvider<PORT> {

  protected final Class<PORT> m_portTypeClazz;
  protected final ServicePool<SERVICE> m_servicePool;
  protected final PortPool<SERVICE, PORT> m_portPool;

  public PooledPortProvider(final Class<SERVICE> serviceClazz, final Class<PORT> portTypeClazz, final String serviceName, final URL wsdlLocation, final String targetNamespace, final IPortInitializer initializer) {
    m_portTypeClazz = portTypeClazz;
    m_servicePool = new ServicePool<>(serviceClazz, serviceName, wsdlLocation, targetNamespace, initializer);
    m_portPool = new PortPool<>(m_servicePool, portTypeClazz, initializer);
    installCleanupWorker();
  }

  /**
   * Returns the same port that was already created within the current transaction (and therefore within the current
   * thread) or creates a new one, if none has been used so far.
   */
  @Override
  public PORT provide() {
    final ITransaction txn = Assertions.assertNotNull(ITransaction.CURRENT.get());
    final String txnMemberId = m_portTypeClazz.getName() + ".transaction";

    @SuppressWarnings("unchecked")
    P_PooledPortTransactionMember member = (P_PooledPortTransactionMember) txn.getMember(txnMemberId);
    if (member != null) {
      PORT port = member.getPort();
      // reset request context if port is used another time within the same transaction
      BEANS.get(JaxWsImplementorSpecifics.class).resetRequestContext(port);
      return port;
    }

    // create new port
    final PORT port = m_portPool.lease();

    member = new P_PooledPortTransactionMember(port, txnMemberId);
    txn.registerMember(member);
    return port;
  }

  /**
   * Schedules a job that is executed every minute
   */
  protected void installCleanupWorker() {
    Jobs.schedule(() -> {
      m_portPool.discardExpiredPoolEntries();
      m_servicePool.discardExpiredPoolEntries();
    }, Jobs.newInput()
        .withName("Cleaning up JAX-WS service and port pools")
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withStartIn(1, TimeUnit.MINUTES)
            .withSchedule(SimpleScheduleBuilder.repeatMinutelyForever())));
  }

  /**
   * Discards all pool entries.<br>
   * <b>Note:<b/> Should be used for testing purposes only.
   */
  public void discardAllPoolEntries() {
    m_portPool.discardAllPoolEntries();
    m_servicePool.discardAllPoolEntries();
  }

  private class P_PooledPortTransactionMember extends AbstractTransactionMember {

    private final PORT m_port;

    public P_PooledPortTransactionMember(PORT port, String txnMemberId) {
      super(txnMemberId);
      m_port = port;
    }

    public PORT getPort() {
      return m_port;
    }

    @Override
    public boolean needsCommit() {
      return true;
    }

    @Override
    public void release() {
      m_portPool.release(m_port);
    }
  }
}
