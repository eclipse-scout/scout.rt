/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.migration;

import org.eclipse.scout.rt.dataobject.DataObjectHelper;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.ILenientDataObjectMapper;
import org.eclipse.scout.rt.dataobject.enumeration.IEnum;
import org.eclipse.scout.rt.dataobject.id.IId;
import org.eclipse.scout.rt.dataobject.id.IUuId;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.namespace.NamespaceVersion;

/**
 * Interface for a data object value migration handler. In contrast to {@link IDoStructureMigrationHandler}, value
 * migration handlers must not change the structure of a data object, but can replace values of specific attributes.
 * <p>
 * Value migrations are only applied if {@link DoValueMigrationIdsContextData#getAppliedValueMigrationIds()} is
 * non-<code>null</code>.
 * <p>
 * A value migration handler operates on typed values. Values can be of any type, typical use cases include merges,
 * replacements or renaming of built-in IDs (such as {@link IUuId}s. However, values can also be {@link IDoEntity} data
 * objects. Note that enum values from {@link IEnum} <i>cannot</i> be migrated with value migration handlers, but must
 * be handled by custom resolve methods on the enum class itself.
 * <p>
 * Value migration handlers can and should use typed data objects and access ID values as constants defined in the code
 * base. If the data objects or ID constants are refactored, the affected value migration handlers must be refactored
 * too (in contrast to {@link IDoStructureMigrationHandler}s which operate on untyped data objects and must not access
 * constants from the current code base, i.e. they are never refactored, if the code base changes).
 * <p>
 * Value migration handlers are always applied <i>after</i> all {@link IDoStructureMigrationHandler} have been applied.
 * Value migrations are applied in a specific order, defined by the given {@link #primarySortOrder()} and
 * {@link #typeVersion()}. Migration handlers with the same primary sort order and type version are applied in the order
 * provided by the bean manager.
 * <p>
 * Value migration handlers <i>must be idempotent</i>, i.e. the result of a given migration must not change, even if the
 * migration is applied multiple times. In general, value migrations are applied <i>exactly once</i>, as the system
 * keeps track of all value migrations which have been applied already and won't be applied again. Idempotency is
 * required, because structure migrations, which are applied before any value migration, might insert or change values
 * as well - and those values might represent the source or the target of a given value migration, depending on the
 * exact point in time, when the structure migration was introduced.
 * <p>
 * To avoid non-idempotent value migrations, do not re-use renamed or deleted ID values in the (future) code base. If
 * non-idempotent value migrations cannot be avoided, consider using an {@link IDoStructureMigrationHandler} instead (if
 * you are in control of the namespace of the relevant data objects and are able to introduce a new type version).
 * <p>
 * <b>Example 1:</b> built-in ID FOO is merged / replaced by built-in ID BAR<br>
 * Note: this migration is only idempotent, if value FOO is not used anymore in the future code base.
 *
 * <pre>
 * public class FooDoValueMigrationHandler extends AbstractDoValueMigrationHandler<FooTypeId> {
 *
 *   public static final DoValueMigrationId ID = DoValueMigrationId.of("3740d4ce-849b-449e-afb1-0986993c9463");
 *
 *   &#64;Override
 *   public DoValueMigrationId id() {
 *     return ID;
 *   }
 *
 *   &#64;Override
 *   public Class<? extends ITypeVersion> typeVersionClass() {
 *     return Foo_2.class;
 *   }
 *
 *   &#64;Override
 *   public FooTypeId migrate(DoStructureMigrationContext ctx, FooTypeId value) {
 *     return "foo".equals(value.unwrap()) ? FooTypes.BAR : value;
 *   }
 * }
 * </pre>
 *
 * <b>Example 2:</b> Match a DO entity data object and migrate a specific (string) attribute. Make sure to return a
 * modified copy of the input value instead of manipulating the provided input value itself, such that the caller is
 * able to detect the change by an equals-comparison.
 *
 * <pre>
 * public class BarDoValueMigrationHandler extends AbstractDoValueMigrationHandler<BarDo> {
 *
 *   public static final DoValueMigrationId ID = DoValueMigrationId.of("f73240b8-ca5d-4e4a-a257-9d4deb067b68");
 *
 *   protected static final String PREFIX = "prefix-";
 *
 *   &#64;Override
 *   public DoValueMigrationId id() {
 *     return ID;
 *   }
 *
 *   &#64;Override
 *   public Class<? extends ITypeVersion> typeVersionClass() {
 *     return Bar_3.class;
 *   }
 *
 *   &#64;Override
 *   public BarDo migrate(DoStructureMigrationContext ctx, BarDo value) {
 *     if (value.getName().startsWith(PREFIX)) {
 *       return value; // no migration required
 *     }
 *
 *     return BEANS.get(DataObjectHelper.class).cloneLenient(value) // lenient clone of provided value to allow change detection by caller
 *         .withName(PREFIX + value.getName());
 *   }
 * }
 * </pre>
 *
 * <b>Example 3:</b> built-in id FooTypeId with IdTypeName 'FooTypeId' is renamed to BarTypeId with name 'BarTypeId'<br>
 *
 * <pre>
 * public class FooDoValueMigrationHandler extends AbstractDoValueUntypedMigrationHandler<UnknownId> {
 *
 *   public static final DoValueMigrationId ID = DoValueMigrationId.of("3740d4ce-849b-449e-afb1-0986993c9423");
 *
 *   &#64;Override
 *   public DoValueMigrationId id() {
 *     return ID;
 *   }
 *
 *   &#64;Override
 *   public Class<? extends ITypeVersion> typeVersionClass() {
 *     return Foo_2.class;
 *   }
 *
 *   &#64;Override
 *   public Object migrate(DoStructureMigrationContext ctx, UnknownId value) {
 *     if ("FooTypeId".equals(value.getIdTypeName())) {
 *       return BarTypeId.of(value.getId());
 *     }
 *     return value;
 *   }
 * }
 * </pre>
 */
@ApplicationScoped
public interface IDoValueMigrationHandler<T> {

  /**
   * Primary sort order for untyped value migrations, used by {@link AbstractDoValueUntypedMigrationHandler}.
   *
   * @see #primarySortOrder()
   */
  double UNTYPED_PRIMARY_SORT_ORDER = 1000.0;

  /**
   * Default primary sort order used by {@link AbstractDoValueMigrationHandler}.
   *
   * @see #primarySortOrder()
   */
  double DEFAULT_PRIMARY_SORT_ORDER = 2000.0;

  /**
   * Unique identifier for this value migration handler.
   */
  DoValueMigrationId id();

  /**
   * Class of the value nodes that need to be migrated. An assignable check is performed, so {@code ExampleId} will also
   * be migrated in an {@link IId} node. To ensure that a value migration handler does not (unintentionally) change the
   * type of the migrated value, the value class of a migration handler should always be a leaf in the inheritance
   * hierarchy, i.e. {@code Class<ExampleId>} instead of {@code Class<IId>} and {@code Class<ExampleDo>} instead of
   * {@code Class<IDoEntity>}.
   */
  Class<T> valueClass();

  /**
   * Primary sort order used for sorting. The primary sort order can be used to group different types of value migration
   * handlers. Secondary sort order is by {@link #typeVersion()}.
   * <p>
   * Example: Untyped DO value migrations (see {@link AbstractDoValueUntypedMigrationHandler}) use
   * {@link #UNTYPED_PRIMARY_SORT_ORDER} and are always applied before regular migration handlers
   * ({@link AbstractDoValueMigrationHandler}) using {@link #DEFAULT_PRIMARY_SORT_ORDER}).
   */
  double primarySortOrder();

  /**
   * Type version used for sorting. Primary sort order is by {@link #primarySortOrder()}, secondary sort order is by
   * type version.
   * <p>
   * The type version defined on a value migration is used to obtain a sort order over all value migrations, including
   * value migrations from other namespaces. Value migrations are always applied after all structure migrations have
   * been applied, so the type version is independent of the type version defined on a structure migration handler.
   * <p>
   * Also, the type version defined on a value migration handler is independent of the type versions of the migrated
   * data objects themselves, i.e. a value migration is applied to all data objects, including data objects from other
   * namespaces, whenever a value node matches the {@link #valueClass()} of the value migration handler.
   * <p>
   * Example 1: Dependent value migrations within same namespace<br>
   * Value migration M1 from A -> B with type version foo-2.0.0<br>
   * Value migration M2 from B -> C with type version foo-2.0.1: Type version of M2 must be greater than type version of
   * M1 to make sure that M1 is applied <i>before</i> M2.
   * <p>
   * Example 2: Dependent value migrations in different namespaces<br>
   * Value migration M1 from A -> B with type version foo-2.0.0<br>
   * Value migration M2 from B -> C with type version bar-3.1.0: Type version of M2 must define a dependency on type
   * version foo-2.0.0 to make sure that M1 is applied <i>before</i> M2.
   */
  NamespaceVersion typeVersion();

  /**
   * This method is called in order to collect applicable value migration handlers before visiting a data object.
   *
   * @param ctx
   *          Migration context with a non-<code>null</code>
   *          {@link DoValueMigrationIdsContextData#getAppliedValueMigrationIds()}.
   * @return <code>true</code> if value migration is accepted and therefore considered for execution, <code>false</code>
   *         otherwise.
   */
  boolean accept(DataObjectMigrationContext ctx);

  /**
   * Returns the migrated value for a given input value.
   * <p>
   * Callers of this method can detect changes to the migrated values by an equality check on the provided input value
   * and the returned value. Implementations must consider the input value as immutable and either return a completely
   * new object or a modified copy of the input value. If no migration is required, implementations may simply return
   * the provided input value.
   * <p>
   * Be aware that if the {@link #valueClass()} references a {@link IDoEntity}, the given value representing a DO entity
   * was created by lenient deserialization (i.e. using {@link ILenientDataObjectMapper}). Thus, when creating a copy of
   * such a data object before any modification, use {@link DataObjectHelper#cloneLenient(IDoEntity)}.
   *
   * @param value
   *          never {@code null}
   **/
  Object migrate(DataObjectMigrationContext ctx, T value);
}
