//package org.eclipse.scout.rt.client.ui.form.fields.smartfield2;
//
//import java.util.List;
//
//import org.eclipse.scout.rt.client.extension.ui.form.fields.IValueFieldExtension;
//import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.ContentAssistFieldChains.ContentAssistFieldBrowseNewChain;
//import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.ContentAssistFieldChains.ContentAssistFieldFilterBrowseLookupResultChain;
//import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.ContentAssistFieldChains.ContentAssistFieldFilterKeyLookupResultChain;
//import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.ContentAssistFieldChains.ContentAssistFieldFilterLookupResultChain;
//import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.ContentAssistFieldChains.ContentAssistFieldFilterRecLookupResultChain;
//import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.ContentAssistFieldChains.ContentAssistFieldFilterTextLookupResultChain;
//import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.ContentAssistFieldChains.ContentAssistFieldPrepareBrowseLookupChain;
//import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.ContentAssistFieldChains.ContentAssistFieldPrepareKeyLookupChain;
//import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.ContentAssistFieldChains.ContentAssistFieldPrepareLookupChain;
//import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.ContentAssistFieldChains.ContentAssistFieldPrepareRecLookupChain;
//import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.ContentAssistFieldChains.ContentAssistFieldPrepareTextLookupChain;
//import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.MixedSmartFieldChains.MixedSmartFieldConvertKeyToValueChain;
//import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.MixedSmartFieldChains.MixedSmartFieldConvertValueToKeyChain;
//import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
//import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
//
///**
// * <h3>{@link ISmartField2Extension}</h3>
// *
// * @author awe
// */
//public interface ISmartField2Extension<VALUE, OWNER extends AbstractSmartField2<VALUE>> extends IValueFieldExtension<VALUE, OWNER> {
//
//  void execFilterBrowseLookupResult(ContentAssistFieldFilterBrowseLookupResultChain<VALUE, VALUE> chain, ILookupCall<VALUE> call, List<ILookupRow<VALUE>> result);
//
//  ILookupRow<VALUE> execBrowseNew(ContentAssistFieldBrowseNewChain<VALUE, VALUE> chain, String searchText);
//
//  void execFilterKeyLookupResult(ContentAssistFieldFilterKeyLookupResultChain<VALUE, VALUE> chain, ILookupCall<VALUE> call, List<ILookupRow<VALUE>> result);
//
//  void execPrepareLookup(ContentAssistFieldPrepareLookupChain<VALUE, VALUE> chain, ILookupCall<VALUE> call);
//
//  void execPrepareTextLookup(ContentAssistFieldPrepareTextLookupChain<VALUE, VALUE> chain, ILookupCall<VALUE> call, String text);
//
//  void execPrepareBrowseLookup(ContentAssistFieldPrepareBrowseLookupChain<VALUE, VALUE> chain, ILookupCall<VALUE> call, String browseHint);
//
//  void execFilterTextLookupResult(ContentAssistFieldFilterTextLookupResultChain<VALUE, VALUE> chain, ILookupCall<VALUE> call, List<ILookupRow<VALUE>> result);
//
//  void execPrepareRecLookup(ContentAssistFieldPrepareRecLookupChain<VALUE, VALUE> chain, ILookupCall<VALUE> call, VALUE parentKey);
//
//  void execFilterLookupResult(ContentAssistFieldFilterLookupResultChain<VALUE, VALUE> chain, ILookupCall<VALUE> call, List<ILookupRow<VALUE>> result);
//
//  void execFilterRecLookupResult(ContentAssistFieldFilterRecLookupResultChain<VALUE, VALUE> chain, ILookupCall<VALUE> call, List<ILookupRow<VALUE>> result);
//
//  void execPrepareKeyLookup(ContentAssistFieldPrepareKeyLookupChain<VALUE, VALUE> chain, ILookupCall<VALUE> call, VALUE key);
//
//  VALUE execConvertValueToKey(MixedSmartFieldConvertValueToKeyChain<VALUE, VALUE> chain, VALUE value);
//
//  VALUE execConvertKeyToValue(MixedSmartFieldConvertKeyToValueChain<VALUE, VALUE> chain, VALUE key);
//}
