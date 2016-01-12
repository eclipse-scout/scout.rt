package org.eclipse.scout.rt.platform.exception;

import static org.eclipse.scout.rt.platform.html.HTML.bold;
import static org.eclipse.scout.rt.platform.html.HTML.fragment;
import static org.eclipse.scout.rt.platform.html.HTML.li;
import static org.eclipse.scout.rt.platform.html.HTML.ul;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.eclipse.scout.rt.platform.html.IHtmlContent;
import org.junit.Before;
import org.junit.Test;

/**
 * @since 5.2
 */
public class VetoExceptionTest {

  private static final String PROCESSING_STATUS_MESSAGE = "title: body";
  private static final String HTML_CONTENT_PLAIN_TEXT = "messages: first entry second entry";

  private IHtmlContent m_htmlContent;
  private ProcessingStatus m_processingStatus;

  @Before
  public void before() {
    m_processingStatus = new ProcessingStatus("title", "body", IProcessingStatus.ERROR);
    m_htmlContent = fragment(
        bold("messages:"),
        ul(
            li("first entry"),
            li("second entry")));
  }

  @Test
  public void testHtmlContentPlainText() {
    assertEquals(HTML_CONTENT_PLAIN_TEXT, m_htmlContent.toPlainText());
  }

  @Test
  public void testStatusGetMessage() {
    assertEquals(PROCESSING_STATUS_MESSAGE, m_processingStatus.getMessage());
  }

  @Test
  public void testGetMessageIHtmlMessageConstructor() {
    @SuppressWarnings("deprecation")
    VetoException vetoException = new VetoException(m_htmlContent);
    assertEquals(m_htmlContent, vetoException.getHtmlMessage());
    assertEquals(HTML_CONTENT_PLAIN_TEXT + " [severity=ERROR]", vetoException.getMessage());
    assertEquals(HTML_CONTENT_PLAIN_TEXT, vetoException.getDisplayMessage());
  }

  @Test
  public void testGetMessageDefaultConstructor() {
    VetoException vetoException = new VetoException();
    assertNull(vetoException.getHtmlMessage());
    assertEquals("undefined [severity=ERROR]", vetoException.getMessage());
    assertEquals("undefined", vetoException.getDisplayMessage());
  }

  @Test
  public void testGetMessageDefaultConstructorWithHtmlMessage() {
    VetoException vetoException = new VetoException().withHtmlMessage(m_htmlContent);
    assertEquals(m_htmlContent, vetoException.getHtmlMessage());
    assertEquals(HTML_CONTENT_PLAIN_TEXT + " [severity=ERROR]", vetoException.getMessage());
    assertEquals(HTML_CONTENT_PLAIN_TEXT, vetoException.getDisplayMessage());
  }

  @Test
  public void testGetMessageMessageConstructor() {
    VetoException vetoException = new VetoException("message");
    assertNull(vetoException.getHtmlMessage());
    assertEquals("message [severity=ERROR]", vetoException.getMessage());
    assertEquals("message", vetoException.getDisplayMessage());
  }

  @Test
  public void testGetMessageMessageConstructorWithHtmlMessage() {
    VetoException vetoException = new VetoException("message").withHtmlMessage(m_htmlContent);
    assertEquals(m_htmlContent, vetoException.getHtmlMessage());
    assertEquals(HTML_CONTENT_PLAIN_TEXT + " [severity=ERROR]", vetoException.getMessage());
    assertEquals(HTML_CONTENT_PLAIN_TEXT, vetoException.getDisplayMessage());
  }

  @Test
  public void testGetMessageStatusConstructor() {
    VetoException vetoException = new VetoException(m_processingStatus);
    assertNull(vetoException.getHtmlMessage());
    assertEquals("title: body [severity=ERROR]", vetoException.getMessage());
    assertEquals(PROCESSING_STATUS_MESSAGE, vetoException.getDisplayMessage());
  }

  @Test
  public void testGetMessageStatusConstructorWithHtmlMessage() {
    VetoException vetoException = new VetoException(m_processingStatus).withHtmlMessage(m_htmlContent);
    assertEquals(m_htmlContent, vetoException.getHtmlMessage());
    assertEquals(HTML_CONTENT_PLAIN_TEXT + " [severity=ERROR]", vetoException.getMessage());
    assertEquals(HTML_CONTENT_PLAIN_TEXT, vetoException.getDisplayMessage());
  }
}
