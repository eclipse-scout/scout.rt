package org.eclipse.scout.commons.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.scout.commons.annotations.FormData.SdkCommand;

/**
 * Annotation on a class used to replace the first occurrence of its super class. The Annotation can be applied to
 * several scout objects types like columns, menus, form fields. Further, the annotation can be applied recursively
 * (i.e. replaced objects can be replaced as well).
 * <p/>
 * <h3>Usage on form fields</h3> If this annotation is added to a form field in an extended form, it works like the
 * {@link InjectFieldTo} annotation except that the original field is removed. The replaced field's container is used by
 * the replacing field as well. If the {@link Order} annotation is missing on the replacing class, the order of the
 * extended class is used. Finally, the {@link FormData} annotation is inherited form the replaced field. If the
 * replaced field does not have a form field data, the replacing field may crate on using {@link SdkCommand#CREATE} or
 * {@link SdkCommand#USE}.
 * <p/>
 * <b>Example:</b> The NameExField replaces the NameField in the original BaseForm without changing its order within the
 * FirstGroupBox. <b>Note:</b> the weird looking super constructor call is required for initializing the extended
 * <em>inner class</em>.
 * 
 * <pre>
 * public class BaseForm extends AbstractForm {
 *   &#064;Order(10)
 *   public class MainBox extends AbstractGroupBox {
 *     &#064;Order(10)
 *     public class FirstGroupBox extends AbstractGroupBox {
 *       &#064;Order(10)
 *       public class NameField extends AbstractStringField {
 *       }
 *     }
 *   }
 * }
 * 
 * public class ExtendedForm extends BaseForm {
 *   &#064;Replace
 *   public class NameExField extends NameField {
 *     public NameExField(BaseForm.MainBox.FirstGroupBox container) {
 *       container.super();
 *     }
 *   }
 * }
 * </pre>
 * 
 * <h3>Usage on table columns</h3> The annotation can be used to modify or move columns within a table. By default, the
 * replaced column uses the same {@link Order} of the replaced column.
 * <p/>
 * <b>Example 1:</b> The FirstExColumn replaces the FirstColumn in the original Table without changing its order.
 * 
 * <pre>
 * public class Table extends AbstractTable {
 *   &#064;Order(10)
 *   public class FirstColumn extends AbstractStringColumn {
 *   }
 * 
 *   &#064;Order(20)
 *   public class SecondColumn extends AbstractStringColumn {
 *   }
 * }
 * 
 * public class ExtendedTable extends Table {
 *   &#064;Replace
 *   public class FirstExColumn extends FirstColumn {
 *   }
 * }
 * </pre>
 * 
 * <b>Example 2:</b> Modifying the table of a table field requires to replace the table field as well:
 * 
 * <pre>
 * public class BaseForm extends AbstractForm {
 *   &#064;Order(10)
 *   public class MainBox extends AbstractGroupBox {
 *     &#064;Order(10)
 *     public class FirstGroupBox extends AbstractGroupBox {
 *       &#064;Order(10)
 *       public class TableField extends AbstractTableField {
 *         public class Table extends AbstractTable {
 *           &#064;Order(10)
 *           public class FirstColumn extends AbstractStringColumn {
 *           }
 *         }
 *       }
 *     }
 *   }
 * }
 * 
 * public class ExtendedForm extends BaseForm {
 *   &#064;Replace
 *   public class TableExField extends NameField {
 *     public TableExField(BaseForm.MainBox.FirstGroupBox container) {
 *       container.super();
 *     }
 * 
 *     public class TableEx extends Table {
 *       &#064;Replace
 *       public class FirstExColumn extends FirstColumn {
 *       }
 *     }
 *   }
 * }
 * </pre>
 * 
 * <h3>Usage on action containers</h3> The annotation can be used to change the behavior of an action. Menus, key
 * strokes, tool buttons and view buttons are actions as well.
 * <p/>
 * <b>Example:</b> Modify the behavior of a menu defined on a smart field:
 * 
 * <pre>
 * public class BaseForm extends AbstractForm {
 *   &#064;Order(10)
 *   public class MainBox extends AbstractGroupBox {
 *     &#064;Order(10)
 *     public class MySmartField extends AbstractSmartField&lt;Long&gt; {
 *       &#064;Order(10)
 *       public class MyMenu extends AbstractMenu {
 *       }
 *     }
 *   }
 * }
 * 
 * public class ExtendedForm extends BaseForm {
 *   &#064;Replace
 *   public class MyExSmartField extends MySmartField {
 *     public MyExSmartField(BaseForm.MainBox container) {
 *       container.super();
 *     }
 * 
 *     &#064;Replace
 *     public class MyExMenu extends MyMenu {
 *     }
 *   }
 * }
 * </pre>
 * 
 * @since 3.8.2
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Replace {

}
