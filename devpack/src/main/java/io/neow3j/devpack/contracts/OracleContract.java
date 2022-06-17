package io.neow3j.devpack.contracts;

import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.annotations.NativeContract;

import static io.neow3j.devpack.Helper.reverse;
import static io.neow3j.devpack.StringLiteralHelper.hexToBytes;
import static io.neow3j.devpack.constants.NativeContract.OracleContractScriptHash;

/**
 * Represents an interface to the native OracleContract that provides oracle services.
 */
@NativeContract
public class OracleContract extends ContractInterface {

    public OracleContract() {
        super(new Hash160(reverse(hexToBytes(OracleContractScriptHash).toByteArray())));
    }

    /**
     * The minimum GAS fee necessary on an oracle request to pay for the response.
     */
    public static final int MinResponseFee = 10000000;

    /**
     * The maximum byte length of the url.
     */
    public static final int MaxUrlLength = 1 << 8;

    /**
     * The maximum byte length of the filter.
     */
    public static final int MaxFilterLength = 1 << 7;

    /**
     * The maximum byte length of the callback function.
     */
    public static final int MaxCallbackLength = 1 << 5;

    /**
     * The maximum byte length of the user data.
     */
    public static final int MaxUserDataLength = 1 << 9;

    /**
     * Does a request to the oracle service with the given request data. The given callback function will be called
     * with the response of the oracle as input.
     *
     * @param url            the URL to query.
     * @param filter         the filter to filter returned data with.
     * @param callback       the callback function. May not start with '{@code _}'.
     * @param userData       additional data.
     * @param gasForResponse the GAS amount to pay for the oracle response.
     */
    public native void request(String url, String filter, String callback, Object userData, int gasForResponse);

}
