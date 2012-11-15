package org.eclipse.scout.rt.extension.client.ui.action.menu;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.activitymap.IActivityMap;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.plannerfield.IPlannerField;

/**
 * This interface is used for modifying {@link IMenu}s. Classes implementing this interface must provide a default
 * constructor.
 * 
 * @since 3.9.0
 */
public interface IMenuModifier<T extends IMenu> {

  /**
   * This method allows modifying configured menus. The anchor represents the reference object a menu extension is
   * attached to. It is an instance of the following types:
   * <ul>
   * <li>{@link IPage}</li>
   * <li>{@link IFormField}</li>
   * <li>{@link IMenu}</li>
   * </ul>
   * <p/>
   * The container is the direct parent the menu is attached to. Some examples are:
   * <ul>
   * <li>{@link ITable} of an {@link IPageWithTable}</li>
   * <li>{@link IActivityMap} of an {@link IPlannerField}</li>
   * <li>etc.</li>
   * </ul>
   * 
   * @param anchor
   *          the reference object the menu extension is attached to. e.g. an {@link IPage}, an {@link IFormField} or an
   *          {@link IMenu}.
   * @param container
   *          the direct parent the menu is attached to. Sometimes the same object as <code>anchor</code>.
   * @param menu
   *          the menu to be modified.
   */
  void modify(Object anchor, Object container, T menu) throws ProcessingException;
}
