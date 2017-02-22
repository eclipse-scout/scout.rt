/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.jaxws.consumer.pool;

import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.xml.ws.Service;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.server.admin.diagnostic.DiagnosticFactory;
import org.eclipse.scout.rt.server.admin.diagnostic.IDiagnostic;
import org.eclipse.scout.rt.server.jaxws.consumer.IPortProvider;
import org.eclipse.scout.rt.server.jaxws.implementor.JaxWsImplementorSpecifics;
import org.eclipse.scout.rt.server.transaction.AbstractTransactionMember;
import org.eclipse.scout.rt.server.transaction.ITransaction;
import org.quartz.SimpleScheduleBuilder;

/**
 * Port provider that is backed by a pool for {@link Service}s and ports, respectively. Ports returned are attached to
 * the current transaction and are transparently put back into the pool when the transaction ends.<br>
 * <b>Note:</b> This provider works only within a valid Scout transaction.
 *
 * @since 6.0.300
 */
public class PooledPortProvider<SERVICE extends Service, PORT> implements IPortProvider<PORT>, IDiagnostic {

  protected final Class<PORT> m_portTypeClazz;
  protected final ServicePool<SERVICE> m_servicePool;
  protected final PortPool<SERVICE, PORT> m_portPool;

  public PooledPortProvider(final Class<SERVICE> serviceClazz, final Class<PORT> portTypeClazz, final String serviceName, final URL wsdlLocation, final String targetNamespace, final IPortInitializer initializer) {
    m_portTypeClazz = portTypeClazz;
    m_servicePool = new ServicePool<>(serviceClazz, serviceName, wsdlLocation, targetNamespace, initializer);
    m_portPool = new PortPool<>(m_servicePool, portTypeClazz, initializer);
    installCleanupWorker();
    DiagnosticFactory.addDiagnosticStatusProvider(this);
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
    Jobs.schedule(new IRunnable() {
      @Override
      public void run() throws Exception {
        m_portPool.discardExpiredPoolEntries();
        m_servicePool.discardExpiredPoolEntries();
      }
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

  @Override
  public void addDiagnosticItemToList(List<List<String>> result) {
    DiagnosticFactory.addDiagnosticItemToList(result, "JAX-WS Pool", "", DiagnosticFactory.STATUS_TITLE);
    DiagnosticFactory.addDiagnosticItemToList(result, "Service Class", m_servicePool.m_serviceClazz.getName(), DiagnosticFactory.STATUS_INFO);
    DiagnosticFactory.addDiagnosticItemToList(result, "Port Class", m_portPool.m_portTypeClazz.getName(), DiagnosticFactory.STATUS_INFO);
    DiagnosticFactory.addDiagnosticItemToList(result, "Service Name", m_servicePool.m_serviceName, DiagnosticFactory.STATUS_INFO);
    DiagnosticFactory.addDiagnosticItemToList(result, "Service Pool", m_servicePool.createStateSnapshot(), DiagnosticFactory.STATUS_INFO);
    DiagnosticFactory.addDiagnosticItemToList(result, "Port Pool", m_portPool.createStateSnapshot(), DiagnosticFactory.STATUS_INFO);
  }

  @Override
  public String[] getPossibleActions() {
    return null;
  }

  @Override
  public void addSubmitButtonsHTML(List<List<String>> result) {
  }

  @Override
  public void call(String action, Object[] values) {
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
