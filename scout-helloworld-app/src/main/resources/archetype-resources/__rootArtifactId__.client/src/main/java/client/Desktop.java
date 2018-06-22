#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.client;

import java.security.AccessController;
import java.security.Principal;
import java.util.List;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.desktop.AbstractDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutlineViewButton;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.form.ScoutInfoForm;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.text.TEXTS;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;

import ${package}.client.search.SearchOutline;
import ${package}.client.settings.SettingsOutline;
import ${package}.client.work.WorkOutline;
import ${package}.shared.Icons;

/**
 * @author ${userName}
 */
public class Desktop extends AbstractDesktop {
  @Override
  protected String getConfiguredTitle() {
    return TEXTS.get("ApplicationTitle");
  }

  @Override
  protected String getConfiguredLogoId() {
    return Icons.AppLogo;
  }

  @Override
  protected List<Class<? extends IOutline>> getConfiguredOutlines() {
    return CollectionUtility.<Class<? extends IOutline>>arrayList(WorkOutline.class, SearchOutline.class,
        SettingsOutline.class);
  }

  @Override
  protected void execDefaultView() {
    selectFirstVisibleOutline();
  }

  protected void selectFirstVisibleOutline() {
    for (IOutline outline : getAvailableOutlines()) {
      if (outline.isEnabled() && outline.isVisible()) {
        setOutline(outline.getClass());
        return;
      }
    }
  }

  @Order(1000)
  public class UserProfileMenu extends AbstractMenu {

    @Override
    protected String getConfiguredKeyStroke() {
      return IKeyStroke.F10;
    }

    @Override
    protected String getConfiguredIconId() {
      return Icons.PersonSolid;
    }

    @Override
    protected String getConfiguredText() {
      Subject subject = Subject.getSubject(AccessController.getContext());
      Principal firstPrincipal = CollectionUtility.firstElement(subject.getPrincipals());
      return StringUtility.uppercaseFirst(firstPrincipal.getName());
    }

    @Order(1000)
    public class AboutMenu extends AbstractMenu {

      @Override
      protected String getConfiguredText() {
        return TEXTS.get("About");
      }

      @Override
      protected void execAction() {
        ScoutInfoForm form = new ScoutInfoForm();
        form.startModify();
      }
    }

    @Order(2000)
    public class LogoutMenu extends AbstractMenu {

      @Override
      protected String getConfiguredText() {
        return TEXTS.get("Logout");
      }

      @Override
      protected void execAction() {
        ClientSessionProvider.currentSession().stop();
      }
    }
  }

  @Order(1000)
  public class WorkOutlineViewButton extends AbstractOutlineViewButton {

    public WorkOutlineViewButton() {
      this(WorkOutline.class);
    }

    protected WorkOutlineViewButton(Class<? extends WorkOutline> outlineClass) {
      super(Desktop.this, outlineClass);
    }

    @Override
    protected String getConfiguredKeyStroke() {
      return IKeyStroke.F2;
    }
  }

  @Order(2000)
  public class SearchOutlineViewButton extends AbstractOutlineViewButton {

    public SearchOutlineViewButton() {
      this(SearchOutline.class);
    }

    protected SearchOutlineViewButton(Class<? extends SearchOutline> outlineClass) {
      super(Desktop.this, outlineClass);
    }

    @Override
    protected DisplayStyle getConfiguredDisplayStyle() {
      return DisplayStyle.TAB;
    }

    @Override
    protected String getConfiguredKeyStroke() {
      return IKeyStroke.F3;
    }
  }

  @Order(3000)
  public class SettingsOutlineViewButton extends AbstractOutlineViewButton {

    public SettingsOutlineViewButton() {
      this(SettingsOutline.class);
    }

    protected SettingsOutlineViewButton(Class<? extends SettingsOutline> outlineClass) {
      super(Desktop.this, outlineClass);
    }

    @Override
    protected DisplayStyle getConfiguredDisplayStyle() {
      return DisplayStyle.TAB;
    }

    @Override
    protected String getConfiguredKeyStroke() {
      return IKeyStroke.F10;
    }
  }
}
