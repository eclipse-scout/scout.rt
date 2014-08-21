/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swing;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;

import org.easymock.EasyMock;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.ui.swing.extension.ISwingApplicationExtension;
import org.junit.Test;

/**
 * @author awe
 */
public class ExtensibleSwingApplicationTest {

  ExtensibleSwingApplication app;

  @Test
  public void testStop() {
    ISwingApplicationExtension ext = EasyMock.createMock(ISwingApplicationExtension.class);
    IClientSession cs = EasyMock.createMock(IClientSession.class);
    expect(ext.getClientSession()).andReturn(cs);
    cs.stopSession();
    EasyMock.expectLastCall();
    replay(ext, cs);
    app = new ExtensibleSwingApplication(Collections.singletonList(ext));

    app.stop();
    verify(ext, cs);
  }

  /**
   * When one of the extensions returns anything other than null, start() is aborted.
   *
   * @throws Exception
   */
  @Test
  public void testStart_ExtensionAbort() throws Exception {
    assertStart(app, IApplication.EXIT_OK, null, IApplication.EXIT_OK);
    assertStart(app, IApplication.EXIT_RELAUNCH, IApplication.EXIT_RELAUNCH, IApplication.EXIT_OK);
    // assertStart(?, null, null) causes the super.start() method to be called
    // which results in a call to startSubject. These cases are testet in the testStartInSubject* methods
  }

  private static void assertStart(ExtensibleSwingApplication app, Object expected, Object firstReturnValue, Object secondReturnValue) throws Exception {
    ISwingApplicationExtension ext1 = EasyMock.createMock(ISwingApplicationExtension.class);
    ISwingApplicationExtension ext2 = EasyMock.createMock(ISwingApplicationExtension.class);
    expect(ext1.execStart(null, null)).andReturn(firstReturnValue);
    expect(ext2.execStart(null, null)).andReturn(secondReturnValue);
    replay(ext1, ext2);
    app = new ExtensibleSwingApplication(Arrays.asList(ext1, ext2));
    assertEquals(expected, app.start(null));
  }

  @Test
  public void testStartInSubject_Abort() throws Exception {
    ISwingApplicationExtension ext = EasyMock.createMock(ISwingApplicationExtension.class);
    expect(ext.execStartInSubject(null, null)).andReturn(IApplication.EXIT_OK);
    replay(ext);
    app = new ExtensibleSwingApplication(Collections.singletonList(ext));
    assertEquals(IApplication.EXIT_OK, app.startInSubject(null));
  }

  @Test
  public void testStartGUI() throws Exception {
    ISwingApplicationExtension ext = EasyMock.createMock(ISwingApplicationExtension.class);
    ISwingEnvironment env = EasyMock.createMock(ISwingEnvironment.class);
    IClientSession cs = EasyMock.createMock(IClientSession.class);
    expect(ext.getEnvironment()).andReturn(env);
    expect(ext.getClientSession()).andReturn(cs);
    env.showGUI(cs);
    EasyMock.expectLastCall();
    replay(ext, env, cs);
    app = new ExtensibleSwingApplication(Collections.singletonList(ext));
    app.startGUI();
    verify(ext, env, cs);
  }

  @Test
  public void testRunWhileActive_NoExtensions() throws Exception {
    app = new ExtensibleSwingApplication(Collections.<ISwingApplicationExtension> emptyList());
    assertEquals(IApplication.EXIT_OK, Integer.valueOf(app.runWhileActive()));
  }

  /**
   * This test requires multiple threads. First we lookup for an active client session.
   * Than the main thread waits on the lock object of this client session. We need a second thread
   * to call the notifyAll() method on that lock object a bit later. After that call the session is
   * not active anymore (the 3rd call for isActive() does return false). So the application should
   * terminate with the exit code of the last session that was terminated.
   *
   * @throws Exception
   */
  @Test
  public void testRunWhileActive_OneExtension() throws Exception {
    final Object stateLock = new Object();
    ISwingApplicationExtension ext = EasyMock.createMock(ISwingApplicationExtension.class);
    IClientSession cs = EasyMock.createMock(IClientSession.class);
    expect(ext.getClientSession()).andReturn(cs).anyTimes();
    expect(cs.isActive()).andReturn(true).times(2);
    expect(cs.isActive()).andReturn(false);
    expect(cs.getStateLock()).andReturn(stateLock).anyTimes();
    expect(cs.getExitCode()).andReturn(IApplication.EXIT_RELAUNCH);
    replay(ext, cs);

    Thread t = new Thread() {
      @Override
      public void run() {
        try {
          Thread.sleep(250);
        }
        catch (InterruptedException e) {
        }
        synchronized (stateLock) {
          stateLock.notifyAll();
        }
      }
    };
    t.start();

    app = new ExtensibleSwingApplication(Collections.singletonList(ext));
    assertEquals(IApplication.EXIT_RELAUNCH, Integer.valueOf(app.runWhileActive()));
  }

  @Test
  public void testInitializeSwing() throws Exception {
    ISwingApplicationExtension ext = EasyMock.createMock(ISwingApplicationExtension.class);
    ext.initializeSwing();
    EasyMock.expectLastCall();
    replay(ext);
    app = new ExtensibleSwingApplication(Collections.singletonList(ext));
    app.initializeSwing();
    verify(ext);
  }

}
