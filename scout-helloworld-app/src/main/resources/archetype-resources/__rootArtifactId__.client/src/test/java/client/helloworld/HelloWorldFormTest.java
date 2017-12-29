#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.client.helloworld;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import ${package}.client.helloworld.HelloWorldForm.MainBox.TopBox.MessageField;
import ${package}.shared.helloworld.HelloWorldFormData;
import ${package}.shared.helloworld.IHelloWorldService;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.mock.BeanMock;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Contains Tests for the {@link HelloWorldForm}.
 *
 * @author ${userName}
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("anonymous")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class HelloWorldFormTest {

  private static final String MESSAGE_VALUE = "testData";

  // Register a mock service for {@link IHelloWorldService}
  @BeanMock
  private IHelloWorldService m_mockSvc;

  /**
   * Return a reference {@link HelloWorldFormData} on method
   * {@link IHelloWorldService${symbol_pound}load(HelloWorldFormData)}.
   */
  @Before
  public void setup() {
    HelloWorldFormData result = new HelloWorldFormData();
    result.getMessage().setValue(MESSAGE_VALUE);

    when(m_mockSvc.load(any())).thenReturn(result);
  }

  /**
   * Tests that the {@link MessageField} is disabled.
   */
  @Test
  public void testMessageFieldDisabled() {
    HelloWorldForm frm = new HelloWorldForm();
    assertFalse(frm.getMessageField().isEnabled());
  }

  /**
   * Tests that the {@link MessageField} is correctly filled after start.
   */
  @Test
  public void testMessageCorrectlyImported() {
    HelloWorldForm frm = new HelloWorldForm();
    frm.start();

    assertEquals(MESSAGE_VALUE, frm.getMessageField().getValue());
  }
}
