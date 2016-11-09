package org.eclipse.scout.rt.shared.data.model;

import java.util.List;

import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * @since 6.1
 */
@ApplicationScoped
public interface IDataModelAttributeOperatorProvider {

  /**
   * Inject operators for the provided attribute.
   *
   * @param attribute
   *          Data model attribute.
   * @param operatorList
   *          Live and mutable list of operators.
   */
  void injectOperators(IDataModelAttribute attribute, List<IDataModelAttributeOp> operatorList);

  /**
   * Create a data model attribute operation for the given operator.
   *
   * @param operator
   *          Operator
   * @param shortText
   *          Short text (optional, otherwise default will be used)
   * @param text
   *          Text (optional, otherwise default will be used)
   * @return Data model attribute operation
   */
  IDataModelAttributeOp createOperator(int operator, String shortText, String text);
}
