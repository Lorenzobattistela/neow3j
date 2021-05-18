package io.neow3j.contract;

import io.neow3j.contract.exceptions.UnexpectedReturnTypeException;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.stackitem.ByteStringStackItem;
import io.neow3j.protocol.core.stackitem.StackItem;
import io.neow3j.protocol.core.response.NFTokenState;
import io.neow3j.transaction.TransactionBuilder;
import io.neow3j.types.ContractParameter;
import io.neow3j.types.Hash160;
import io.neow3j.wallet.Wallet;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Map;

import static io.neow3j.types.ContractParameter.byteArray;
import static io.neow3j.types.ContractParameter.hash160;
import static io.neow3j.types.StackItemType.MAP;
import static io.neow3j.transaction.Signer.calledByEntry;
import static io.neow3j.utils.Numeric.toHexString;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singletonList;

/**
 * Represents a NEP-11 non-fungible token contract and provides methods to invoke it.
 */
public class NonFungibleToken extends Token {

    private static final String OWNER_OF = "ownerOf";
    private static final String PROPERTIES = "properties";
    private static final String BALANCE_OF = "balanceOf";
    private static final String TRANSFER = "transfer";

    /**
     * Constructs a new {@code NFT} representing the contract with the given script hash. Uses
     * the given {@link Neow3j} instance for all invocations.
     *
     * @param scriptHash the token contract's script hash.
     * @param neow       the {@link Neow3j} instance to use for invocations.
     */
    public NonFungibleToken(Hash160 scriptHash, Neow3j neow) {
        super(scriptHash, neow);
    }

    /**
     * Creates a transaction script to transfer a non-fungible token and initializes a
     * {@link TransactionBuilder} based on this script.
     * <p>
     * The token owner is set as the signer of the transaction, thus, the given wallet must
     * contain the owner account. The returned builder is ready to be signed and sent.
     *
     * @param wallet  the wallet that holds the account of the token owner.
     * @param to      the receiver of the token.
     * @param tokenID the token ID.
     * @return a transaction builder.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public TransactionBuilder transfer(Wallet wallet, Hash160 to, byte[] tokenID)
            throws IOException {
        return transfer(wallet, to, tokenID, null);
    }

    /**
     * Creates a transaction script to transfer a non-fungible token and initializes a
     * {@link TransactionBuilder} based on this script.
     * <p>
     * The token owner is set as the signer of the transaction, thus, the given wallet must
     * contain the owner account. The returned builder is ready to be signed and sent.
     *
     * @param wallet  the wallet that holds the account of the token owner.
     * @param to      the receiver of the token.
     * @param tokenID the token ID.
     * @param data    the data that is passed to the {@code onNEP11Payment} method of the receiving
     *                smart contract.
     * @return a transaction builder.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public TransactionBuilder transfer(Wallet wallet, Hash160 to, byte[] tokenID,
            ContractParameter data) throws IOException {
        Hash160 tokenOwner = ownerOf(tokenID);
        if (!wallet.holdsAccount(tokenOwner)) {
            throw new IllegalArgumentException("The provided wallet does not contain the account " +
                    "that owns the token with ID " + toHexString(tokenID) + ". The address of the" +
                    " owner of this token is " + tokenOwner.toAddress() + ".");
        }

        return invokeFunction(TRANSFER, hash160(to), byteArray(tokenID), data)
                .wallet(wallet)
                .signers(calledByEntry(tokenOwner));
    }

    /**
     * Gets the owner of the token with {@code tokenId}.
     *
     * @param tokenId the token ID.
     * @return a list of owners of the token.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public Hash160 ownerOf(byte[] tokenId) throws IOException {
        return callFunctionReturningScriptHash(OWNER_OF, byteArray(tokenId));
    }

    /**
     * Gets the balance of the token with {@code tokenID} for the given account.
     * <p>
     * The balance is returned in token fractions. E.g., a balance of 0.5 of a token with 2
     * decimals is returned as 50 (= 0.5 * 10^2) token fractions.
     * <p>
     * The balance is not cached locally. Every time this method is called requests are send to
     * the Neo node.
     *
     * @param owner the script hash of the account to fetch the balance for.
     * @return the token balance of the given account.
     * @throws IOException                   if there was a problem fetching information from
     *                                       the Neo node.
     * @throws UnexpectedReturnTypeException if the contract invocation did not return something
     *                                       interpretable as a number.
     */
    public BigInteger balanceOf(Hash160 owner)
            throws IOException {
        return callFuncReturningInt(BALANCE_OF, hash160(owner));
    }

    /**
     * Gets the properties of the token with {@code tokenID}.
     *
     * @param tokenID the token ID.
     * @return the properties of the token as {@link NFTokenState}.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public NFTokenState properties(byte[] tokenID) throws IOException {
        StackItem item = callInvokeFunction(PROPERTIES, singletonList(byteArray(tokenID)))
                .getInvocationResult().getStack().get(0);
        if (item.getType().equals(MAP)) {
            Map<StackItem, StackItem> map = item.getMap();

            return new NFTokenState(
                    map.get(new ByteStringStackItem("name".getBytes(UTF_8))).getString());
        }
        throw new UnexpectedReturnTypeException(item.getType(), MAP);
    }

}
