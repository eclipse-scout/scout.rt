/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.services.common.bookmark;

import static org.junit.Assert.*;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.services.common.bookmark.Bookmark;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

/**
 * Test for {@link IBookmarkService}
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class BookmarkServiceTest {

  private IDesktop m_mockDesktop;

  @Before
  public void setUp() {
    m_mockDesktop = Mockito.mock(IDesktop.class);
    TestEnvironmentClientSession.get().replaceDesktop(m_mockDesktop);
  }

  @After
  public void tearDown() {
    TestEnvironmentClientSession.get().replaceDesktop(null);
  }

  @Test
  public void testSetStartBookmark() {
    Bookmark bookmark = new Bookmark();
    bookmark.setText("My Bookmark Text");
    Mockito.when(m_mockDesktop.createBookmark()).thenReturn(bookmark);
    IBookmarkService s = BEANS.get(IBookmarkService.class);
    Assert.assertNotNull(s);
    s.setStartBookmark();

    // Get the Bookmark
    Bookmark startBookmark = s.getStartBookmark();
    assertEquals("Kind", Bookmark.USER_BOOKMARK, startBookmark.getKind());
    assertEquals("Text", "My Bookmark Text", startBookmark.getText());
  }

  @Test
  public void testDeleteBookmark() {
    Bookmark bookmark = new Bookmark();
    bookmark.setText("My Bookmark Text");
    Mockito.when(m_mockDesktop.createBookmark()).thenReturn(bookmark);
    IBookmarkService s = BEANS.get(IBookmarkService.class);
    Assert.assertNotNull(s);
    s.setStartBookmark();

    // Get the Bookmark
    Bookmark startBookmark = s.getStartBookmark();
    assertNotNull(startBookmark);

    // Delete the bookmark
    s.deleteStartBookmark();

    startBookmark = s.getStartBookmark();
    assertNull(startBookmark);
  }
}
