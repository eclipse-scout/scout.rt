/**
 *
 */
package org.eclipse.scout.rt.ui.json;

import static org.junit.Assert.assertEquals;

import org.easymock.EasyMock;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class JsonDesktopTest {
  private JsonDesktop m_jsonDesktop;

  @Before
  public void beforeTest() {
    IDesktop desktop = EasyMock.createMock(IDesktop.class);
    IJsonEnvironment env = EasyMock.createMock(IJsonEnvironment.class);
    m_jsonDesktop = new JsonDesktop(desktop, env);
  }

  @Test
  public void testJsonObject() throws ProcessingException {
    IForm form = EasyMock.createMock(IForm.class);
    EasyMock.expect(form.getFormId()).andReturn("JsonForm");
    EasyMock.expect(form.getTitle()).andReturn("JsonForm");
    EasyMock.expect(form.getIconId()).andReturn("JsonForm");
    EasyMock.replay(form);

    JSONObject json = m_jsonDesktop.toJson(form);
    assertEquals(
        "{\"title\":\"JsonForm\",\"formId\":\"JsonForm\",\"iconId\":\"JsonForm\"}",
        json.toString());
  }

  @Test
  public void testJsonArray() throws Exception {
    JSONArray jsonArray = new JSONArray();
    jsonArray.put("hello");
    jsonArray.put("world");
    assertEquals("[\"hello\",\"world\"]", jsonArray.toString());
  }

}
