package io.neow3j.contract;

import static io.neow3j.contract.ContractParameter.byteArray;
import static io.neow3j.contract.ContractParameter.hash160;
import static io.neow3j.contract.ContractParameter.integer;
import static io.neow3j.contract.ContractParameter.string;
import static io.neow3j.model.types.StackItemType.MAP;
import static java.util.Collections.singletonList;

import io.neow3j.contract.exceptions.UnexpectedReturnTypeException;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.RecordType;
import io.neow3j.protocol.core.methods.response.InvocationResult;
import io.neow3j.protocol.core.methods.response.MapStackItem;
import io.neow3j.protocol.core.methods.response.NameState;
import io.neow3j.protocol.core.methods.response.StackItem;
import io.neow3j.utils.Numeric;
import io.neow3j.wallet.Wallet;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * Represents the NameService native contract and provides methods to invoke its functions.
 */
public class NeoNameService extends NonFungibleToken {

    public final static String NAME = "NameService";
    public final static long NEF_CHECKSUM = 3740064217L;
    public static final ScriptHash SCRIPT_HASH = getScriptHashOfNativeContract(NEF_CHECKSUM, NAME);

    private static final String ADD_ROOT = "addRoot";
    private static final String SET_PRICE = "setPrice";
    private static final String GET_PRICE = "getPrice";
    private static final String IS_AVAILABLE = "isAvailable";
    private static final String REGISTER = "register";
    private static final String RENEW = "renew";
    private static final String SET_ADMIN = "setAdmin";
    private static final String SET_RECORD = "setRecord";
    private static final String GET_RECORD = "getRecord";
    private static final String DELETE_RECORD = "deleteRecord";
    private static final String RESOLVE = "resolve";

    private static final String PROPERTIES = "properties";

    private static final BigInteger MAXIMAL_PRICE = new BigInteger("1000000000000");
    private static final Pattern ROOT_REGEX_PATTERN = Pattern.compile("^[a-z][a-z0-9]{0,15}$");
    public static final Pattern NAME_REGEX_PATTERN = Pattern.compile(
            "^(?=.{3,255}$)([a-z0-9]{1,62}\\.)+[a-z][a-z0-9]{0,15}$");
    private static final Pattern IPV4_REGEX_PATTERN = Pattern.compile(
            "^(2(5[0-5]|[0-4]\\d))|1?\\d{1,2}(\\.((2(5[0-5]|[0-4]\\d))|1?\\d{1,2})){3}$");
    private static final Pattern IPV6_REGEX_PATTERN = Pattern.compile(
            "^([a-f0-9]{1,4}:){7}[a-f0-9]{1,4}$");

    /**
     * Constructs a new {@code NeoToken} that uses the given {@link Neow3j} instance for
     * invocations.
     *
     * @param neow the {@link Neow3j} instance to use for invocations.
     */
    public NeoNameService(Neow3j neow) {
        super(SCRIPT_HASH, neow);
    }

    /**
     * Returns the name of the NeoToken contract. Doesn't require a call to the Neo node.
     *
     * @return the name.
     */
    @Override
    public String getName() {
        return NAME;
    }

    /**
     * Creates a transaction script to add a root domain and initializes a
     * {@link TransactionBuilder} based on this script.
     * <p>
     * Only committee members are allowed to add a new root domain.
     *
     * @param root the new root domain.
     * @return a transaction builder.
     */
    public TransactionBuilder addRoot(String root) {
        checkRegexMatch(ROOT_REGEX_PATTERN, root);
        return invokeFunction(ADD_ROOT, string(root));
    }

    /**
     * Creates a transaction script to set the price for registering a domain and initializes a
     * {@link TransactionBuilder} based on this script.
     * <p>
     * Only committee members are allowed to set the price.
     *
     * @param price the price for registering a domain.
     * @return a transaction builder.
     */
    public TransactionBuilder setPrice(BigInteger price) {
        if (!isValidPrice(price)) {
            throw new IllegalArgumentException("The price needs to be greater than 0 and smaller " +
                    "than 1_000_000_000_000.");
        }
        return invokeFunction(SET_PRICE, integer(price));
    }

    // true if the price is in the allowed range, false otherwise.
    private boolean isValidPrice(BigInteger price) {
        return price.compareTo(BigInteger.ZERO) > 0 &&
                price.compareTo(MAXIMAL_PRICE) <= 0;
    }

    /**
     * Gets the price to register a domain.
     *
     * @return the price to register a domain.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public BigInteger getPrice() throws IOException {
        return callFuncReturningInt(GET_PRICE);
    }

    /**
     * Checks if the specified second domain name is available.
     *
     * @param name the domain name.
     * @return true if the domain name is available, false otherwise.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public boolean isAvailable(String name) throws IOException {
        checkDomainNameValidity(name);
        try {
            return callFuncReturningBool(IS_AVAILABLE, string(name));
        } catch (IndexOutOfBoundsException e) {
            String root = name.split("\\.")[1];
            throw new IllegalArgumentException("The root domain '" + root + "' does not exist.");
        }
    }

    /**
     * Creates a transaction script to register a new domain and initializes a
     * {@link TransactionBuilder} based on this script.
     *
     * @param name  the domain name.
     * @param owner the address of the domain owner.
     * @return a transaction builder.
     */
    public TransactionBuilder register(String name, ScriptHash owner) throws IOException {
        checkDomainNameAvailability(name, true);
        return invokeFunction(REGISTER, string(name), hash160(owner));
    }

    // checks if a domain name is available
    private void checkDomainNameAvailability(String name, boolean shouldBeAvailable)
            throws IOException {
        boolean isAvailable = isAvailable(name);
        if (shouldBeAvailable && !isAvailable) {
            throw new IllegalArgumentException("The domain name '" + name + "' is already taken.");
        }
        if (!shouldBeAvailable && isAvailable) {
            throw new IllegalArgumentException("The domain name '" + name + "' is not registered.");
        }
    }

    // checks that the domain name matches the required regex and contains two levels.
    private void checkDomainNameValidity(String name) {
        checkRegexMatch(NAME_REGEX_PATTERN, name);
        if (name.split("\\.").length != 2) {
            throw new IllegalArgumentException("Only second domain names are allowed to be " +
                    "registered.");
        }
    }

    // checks if an input matches the provided regex pattern.
    private void checkRegexMatch(Pattern pattern, String input) {
        if (!pattern.matcher(input).matches()) {
            throw new IllegalArgumentException("The provided input does not match the required " +
                    "regex.");
        }
    }

    /**
     * Creates a transaction script to update the TTL of the domain name and initializes a
     * {@link TransactionBuilder} based on this script.
     * <p>
     * Each call will extend the validity period of the domain name by one year.
     * <p>
     * Only supports to renew the second domain name.
     *
     * @param name the domain name.
     * @return a transaction builder.
     */
    public TransactionBuilder renew(String name) throws IOException {
        checkDomainNameAvailability(name, false);
        return invokeFunction(RENEW, string(name));
    }

    /**
     * Creates a transaction script to set the admin for the specified domain name and
     * initializes a {@link TransactionBuilder} based on this script.
     * <p>
     * Requires to be signed by the current owner and the new admin of the domain.
     *
     * @param name  the domain name.
     * @param admin the hash of the admin address.
     * @return a transaction builder.
     */
    public TransactionBuilder setAdmin(String name, ScriptHash admin) throws IOException {
        checkDomainNameAvailability(name, false);
        return invokeFunction(SET_ADMIN, string(name), hash160(admin));
    }

    /**
     * Creates a transaction script to set the type of the specified domain name and the
     * corresponding type data and initializes a {@link TransactionBuilder} based on this script.
     *
     * @param name the domain name.
     * @param type the record type.
     * @param data the corresponding data.
     * @return a transaction builder.
     */
    public TransactionBuilder setRecord(String name, RecordType type, String data)
            throws IOException {
        checkDomainNameAvailability(name, false);
        checkDataMatchingRecordType(type, data);
        return invokeFunction(SET_RECORD, string(name), integer(type.byteValue()), string(data));
    }

    private void checkDataMatchingRecordType(RecordType type, String data) {
        if (type.equals(RecordType.A)) {
            checkRegexMatch(IPV4_REGEX_PATTERN, data);
        } else if (type.equals(RecordType.CNAME)) {
            checkRegexMatch(NAME_REGEX_PATTERN, data);
        } else if (type.equals(RecordType.TXT)) {
            byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
            if (bytes.length > 255) {
                throw new IllegalArgumentException("The provided data is not valid for the record" +
                        " type TXT.");
            }
        } else {
            checkRegexMatch(IPV6_REGEX_PATTERN, data);
        }
    }

    /**
     * Gets the type data of the domain.
     *
     * @param name the domain name.
     * @param type the record type.
     * @return a transaction builder.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public String getRecord(String name, RecordType type) throws IOException {
        checkDomainNameAvailability(name, false);
        try {
            return callFuncReturningString(GET_RECORD, string(name), integer(type.byteValue()));
        } catch (UnexpectedReturnTypeException e) {
            throw new IllegalArgumentException("No record of type " + type.jsonValue() + " found " +
                    "for the domain name '" + name + "'.");
        }
    }

    /**
     * Creates a transaction script to delete the record data initializes a
     * {@link TransactionBuilder} based on this script.
     *
     * @param name the domain name.
     * @param type the record type.
     * @return a transaction builder.
     */
    public TransactionBuilder deleteRecord(String name, RecordType type) {
        return invokeFunction(DELETE_RECORD, string(name), integer(type.byteValue()));
    }

    /**
     * Resolves a domain name.
     *
     * @param name the domain name.
     * @param type the record type.
     * @return the resolution result.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public String resolve(String name, RecordType type) throws IOException {
        checkDomainNameAvailability(name, false);
        try {
            return callFuncReturningString(RESOLVE, string(name), integer(type.byteValue()));
        } catch (UnexpectedReturnTypeException e) {
            throw new IllegalArgumentException("No record of type " + type.jsonValue() + " found " +
                    "for the domain name '" + name + "'.");
        }
    }

    /**
     * Gets the owner of the domain name.
     *
     * @param name the domain name.
     * @return the owner of the domain name.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public ScriptHash ownerOf(String name) throws IOException {
        checkDomainNameAvailability(name, false);
        return ownerOf(name.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Gets the properties of the domain name.
     *
     * @param name the domain name.
     * @return the properties of the domain name as {@link NameState}.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public NameState properties(String name) throws IOException {
        return properties(name.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Gets the properties of the domain name.
     *
     * @param name the domain name.
     * @return the properties of the domain name as {@link NameState}.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    @Override
    public NameState properties(byte[] name) throws IOException {
        String domainAsString = Numeric.hexToString(Numeric.toHexString(name));
        checkDomainNameAvailability(domainAsString, false);
        InvocationResult invocationResult =
                callInvokeFunction(PROPERTIES, singletonList(byteArray(name)))
                        .getInvocationResult();
        return deserializeProperties(invocationResult);
    }

    private NameState deserializeProperties(InvocationResult invocationResult) {
        StackItem stackItem = invocationResult.getStack().get(0);

        if (!stackItem.getType().equals(MAP)) {
            throw new UnexpectedReturnTypeException(stackItem.getType(), MAP);
        }

        MapStackItem map = stackItem.asMap();
        StackItem name = map.get("name");
        StackItem description = map.get("description");
        StackItem expiration = map.get("expiration");

        return new NameState(name.asByteString().getAsString(),
                description.asByteString().getAsString(),
                expiration.asInteger().getValue().intValue());
    }

    /**
     * Creates a transaction script to transfer a domain name and initializes a
     * {@link TransactionBuilder} based on this script.
     * <p>
     * The returned {@link TransactionBuilder} is ready to be signed and sent.
     *
     * @param wallet the wallet.
     * @param to     the receiver of the domain name.
     * @param name   the domain name.
     * @return a transaction builder.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public TransactionBuilder transfer(Wallet wallet, ScriptHash to, String name)
            throws IOException {
        checkDomainNameAvailability(name, false);
        return transfer(wallet, to, name.getBytes(StandardCharsets.UTF_8));
    }
}
