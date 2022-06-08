import {LookupRow} from "../index";

export default interface LookupResult {
    lookupRow: LookupRow[],
    /**
     * A value of the QueryBy object
     */
    queryBy: string,
    byAll: boolean,
    byText: boolean,
    byKey: boolean,
    byKeys: boolean,
    byRec: boolean,
    rec: boolean,
    appendResult: boolean,
    uniqueMatch: boolean,
    seqNo: number
}
