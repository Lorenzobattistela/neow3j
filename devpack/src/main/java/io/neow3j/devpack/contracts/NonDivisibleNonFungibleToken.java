package io.neow3j.devpack.contracts;

import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.Hash160;

/**
 * Base class for non-divisible, non-fungible token contracts that are compliant with the NEP-11 standard. Initialize
 * this class with a {@link Hash160} to create an "interface" to a NEP-11 (non-divisible) token contract on the Neo
 * blockchain.
 * <p>
 * When this class is extended, the constructor of the extending class must take exactly one parameter of type
 * {@link Hash160} and pass it to the {@code super()} call without any additional logic.
 */
public class NonDivisibleNonFungibleToken extends NonFungibleToken {

    public NonDivisibleNonFungibleToken(Hash160 contractHash) {
        super(contractHash);
    }

    /**
     * Gets the owner of the token with {@code tokenId}.
     *
     * @param tokenId the token id.
     * @return the token owner.
     */
    public native Hash160 ownerOf(ByteString tokenId);

}
