import {FormField} from "../../index";

export default interface ValidationResult  {
    valid: boolean
    validByErrorStatus: boolean
    validByMandatory: boolean,
    field: FormField,
    label: string,
    reveal: Function
}