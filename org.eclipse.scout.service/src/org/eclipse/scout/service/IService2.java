/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.service;

import org.osgi.framework.ServiceRegistration;

/**
 * Convenience service interface for services that are interested in beeing
 * notified when they are lazily created.
 * <p>
 * When working with osgi services in eclipse scout, the following service factories are relevant:
 * <ul>
 * <li>ClientProxyServiceFactory</li>
 * <li>ClientServiceFactory</li>
 * <li>ServerServiceFactory</li>
 * </ul>
 * <p>
 * You may also consider the layering / tier indicators
 * <ul>
 * <li>OfflineState - indicates whether the osgi is running in offline mode or online mode</li>
 * <li>TierState - indicates whether the osgi is running in back-end, front-end or undetermined. The value can be set
 * using <code>scout.osgi.tier=frontend (or backend)</code></li>
 * </ul>
 * <h2>Service visibilities</h2>
 * 
 * <pre>
 * FE=Front-End
 * BE=Back-End
 * 
 * ON=Online
 * OFF=Offline
 * 
 * P=Client Proxy
 * C=Client Service
 * S=Server Service
 * G=Global Service
 * </pre>
 * 
 * <h3>Summary</h3>
 * <p>
 * ClientProxyServiceFactory.visible = ClientJob.getCurrentSession()!=null && FE
 * </p>
 * <p>
 * ClientServiceFactory = ClientJob.getCurrentSession()!=null
 * </p>
 * <p>
 * ServerServiceFactory = ServerJob.getCurrentSession()!=null && (BE || OFF)
 * </p>
 * <h3>Visibility overview per tier and offline state</h3>
 * <table border=1>
 * <tr>
 * <td>&nbsp;</td>
 * <td>P</td>
 * <td>C</td>
 * <td>S</td>
 * <td>G</td>
 * </tr>
 * <tr>
 * <td>FE-ON</td>
 * <td>X</td>
 * <td>X</td>
 * <td>&nbsp;</td>
 * <td>X</td>
 * </tr>
 * <tr>
 * <td>FE-OFF</td>
 * <td>X</td>
 * <td>X</td>
 * <td>X</td>
 * <td>X</td>
 * </tr>
 * <tr>
 * <td>BE</td>
 * <td>&nbsp;</td>
 * <td>X</td>
 * <td>X</td>
 * <td>X</td>
 * </tr>
 * </table>
 * <h3>Proxy visibility per tier and offline state</h3>
 * <table border=1>
 * <tr>
 * <td>&nbsp;</td>
 * <td>ON</td>
 * <td>OFF</td>
 * </tr>
 * <tr>
 * <td>FE</td>
 * <td>X</td>
 * <td>X (via IOfflineDispatcher)</td>
 * </tr>
 * <tr>
 * <td>BE</td>
 * <td>&nbsp;</td>
 * <td>&nbsp;</td>
 * </tr>
 * </table>
 * Special care has to be taken when implementing pseudo proxies as client services. <h3>Client service visibility per
 * tier and offline state</h3>
 * <table border=1>
 * <tr>
 * <td>&nbsp;</td>
 * <td>ON</td>
 * <td>OFF</td>
 * </tr>
 * <tr>
 * <td>FE</td>
 * <td>X</td>
 * <td>X</td>
 * </tr>
 * <tr>
 * <td>BE</td>
 * <td>X</td>
 * <td>X</td>
 * </tr>
 * </table>
 * <h3>Server service visibility per tier and offline state</h3>
 * <table border=1>
 * <tr>
 * <td>&nbsp;</td>
 * <td>ON</td>
 * <td>OFF</td>
 * </tr>
 * <tr>
 * <td>FE</td>
 * <td>&nbsp;</td>
 * <td>X</td>
 * </tr>
 * <tr>
 * <td>BE</td>
 * <td>X</td>
 * <td>X</td>
 * </tr>
 * </table>
 * <p>
 * Offline Mode in client is FE and OFF
 * </p>
 * <p>
 * Online Mode in client (even though there are offline plugins available) is FE and ON
 * </p>
 * <p>
 * Wicket in server is BE and ON
 * </p>
 * 
 * @since 1.0.0 see also {@link org.eclipse.scout.commons.annotations.Priority} for defining service ranking
 */
public abstract interface IService2 extends IService {

  /**
   * The method is called by the framework just after the service was lazily
   * created by the service factory (not when it was registered!). The service
   * exists once per osgi and is cached by the {@link IServiceFactory} The
   * default implementation in {@link AbstractService} calls {@link
   * ServiceUtility#injectConfigProperties(this)}.
   * 
   * @param registration
   *          the service registration
   */
  void initializeService(ServiceRegistration registration);

}
