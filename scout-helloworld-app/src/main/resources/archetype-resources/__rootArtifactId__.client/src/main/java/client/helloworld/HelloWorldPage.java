#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.client.helloworld;

import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithNodes;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.platform.text.TEXTS;

/**
 * @author ${userName}
 */
public class HelloWorldPage extends AbstractPageWithNodes {

  @Override
  protected boolean getConfiguredLeaf() {
    return true;
  }

  @Override
  protected boolean getConfiguredTableVisible() {
    return false;
  }

  @Override
  protected String getConfiguredTitle() {
    return TEXTS.get("HelloWorld");
  }

  @Override
  protected Class<? extends IForm> getConfiguredDetailForm() {
    return HelloWorldForm.class;
  }
}
