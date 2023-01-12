/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.testing.client.runner.statement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.AbstractPageExtension;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PageDisposePageChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PageInitPageChain;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IPlatform.State;
import org.eclipse.scout.rt.platform.IPlatformListener;
import org.eclipse.scout.rt.platform.PlatformEvent;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.extension.IExtensionRegistry;
import org.junit.runners.model.Statement;

public final class CleanupPagesStatements {

  public static class CleanupPagesMethodStatement extends Statement {

    private final Statement m_statement;

    public CleanupPagesMethodStatement(Statement statement) {
      m_statement = statement;
    }

    @Override
    public void evaluate() throws Throwable {
      ISession session = IClientSession.CURRENT.get();
      if (session == null) {
        m_statement.evaluate();
      }
      else {
        TestPageTrackingExtension.startTracking(false);
        m_statement.evaluate();
        Set<IPage<?>> pages = TestPageTrackingExtension.stopTracking(false);
        pages.forEach(IPage::dispose);
      }
    }
  }

  public static class CleanupPagesBeforeClassStatement extends Statement {

    private final Statement m_statement;

    public CleanupPagesBeforeClassStatement(Statement statement) {
      m_statement = statement;
    }

    @Override
    public void evaluate() throws Throwable {
      TestPageTrackingExtension.startTracking(true);
      m_statement.evaluate();
    }
  }

  public static class CleanupPagesAfterClassStatement extends Statement {

    private final Statement m_statement;

    public CleanupPagesAfterClassStatement(Statement statement) {
      m_statement = statement;
    }

    @Override
    public void evaluate() throws Throwable {
      m_statement.evaluate();
      Set<IPage<?>> pages = TestPageTrackingExtension.stopTracking(true);
      pages.forEach(IPage::dispose);
    }
  }

  public static class TestPageTrackingExtensionRegistrator implements IPlatformListener {

    @Override
    public void stateChanged(PlatformEvent event) {
      if (event.getState() == State.PlatformStarted) {
        BEANS.get(IExtensionRegistry.class).register(TestPageTrackingExtension.class);
      }
    }
  }

  public static class TestPageTrackingExtension extends AbstractPageExtension<AbstractPage<?>> {

    private static final String SESSION_DATA_KEY = TestPageTrackingExtension.class.getName() + "#pages";

    public TestPageTrackingExtension(AbstractPage<?> owner) {
      super(owner);
    }

    @Override
    public void execInitPage(PageInitPageChain chain) {
      chain.execInitPage();
      Set<IPage<?>> pages = getSessionPages();
      if (pages != null) {
        pages.add(getOwner());
      }
    }

    @Override
    public void execDisposePage(PageDisposePageChain chain) {
      Set<IPage<?>> pages = getSessionPages();
      if (pages != null) {
        pages.remove(getOwner());
      }
      chain.execDisposePage();
    }

    public static void startTracking(boolean resetTrackers) {
      ISession session = IClientSession.CURRENT.get();
      if (session != null) {
        @SuppressWarnings("unchecked")
        List<Set<IPage<?>>> trackers = (List<Set<IPage<?>>>) session.getData(SESSION_DATA_KEY);
        if (trackers == null || resetTrackers) {
          trackers = new ArrayList<>();
          session.setData(SESSION_DATA_KEY, trackers);
        }
        Set<IPage<?>> pages = new HashSet<>();
        trackers.add(pages);
      }
    }

    public static Set<IPage<?>> stopTracking(boolean all) {
      ISession session = IClientSession.CURRENT.get();
      if (session != null) {
        @SuppressWarnings("unchecked")
        List<Set<IPage<?>>> trackers = (List<Set<IPage<?>>>) session.getData(SESSION_DATA_KEY);
        if (trackers != null) {
          if (all) {
            session.setData(SESSION_DATA_KEY, null);
            return trackers.stream().flatMap(Set::stream).collect(Collectors.toSet());
          }
          else {
            return trackers.remove(trackers.size() - 1);
          }
        }
      }
      return new HashSet<>();
    }

    private static Set<IPage<?>> getSessionPages() {
      ISession session = IClientSession.CURRENT.get();
      if (session != null) {
        @SuppressWarnings("unchecked")
        List<Set<IPage<?>>> trackers = (List<Set<IPage<?>>>) session.getData(SESSION_DATA_KEY);
        if (trackers != null && !trackers.isEmpty()) {
          return trackers.get(trackers.size() - 1);
        }
      }
      return null;
    }
  }
}
