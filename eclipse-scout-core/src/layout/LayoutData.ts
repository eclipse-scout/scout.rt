// Extends Object is necessary to prevent weak type checking.
// -> TS2559: Type 'xy' has no properties in common with type 'LayoutData' in implementing classes
export default interface LayoutData extends Object {
  isValidateRoot?(): boolean;
}
