package io.neow3j.protocol.core.methods.response;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.neow3j.contract.ScriptHash;
import io.neow3j.model.types.StackItemType;
import io.neow3j.utils.BigIntegers;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ByteStringStackItem extends StackItem {

    @JsonProperty("value")
    private byte[] value;

    public ByteStringStackItem() {
        super(StackItemType.BYTE_STRING);
    }

    public ByteStringStackItem(byte[] value) {
        super(StackItemType.BYTE_STRING);
        this.value = value;
    }

    /**
     * Decodes the stack item's base64-encoded value and returns it as a byte array.
     *
     * @return the value of this stack item.
     */
    public byte[] getValue() {
        return this.value;
    }

    /**
     * <p>Gets this byte array's value as an address.</p>
     * <br>
     * <p>Expects the byte array to be a script hash in little-endian order.</p>
     *
     * @return the address represented by this byte array.
     */
    public String getAsAddress() {
        return new ScriptHash(getValue()).toAddress();
    }

    /**
     * <p>Gets this byte array's value as string.</p>
     * <br>
     * <p>Expects the byte array to be UTF-8-encoded.</p>
     *
     * @return the string represented by the byte array.
     */
    public String getAsString() {
        return new String(getValue(), UTF_8);
    }

    /**
     * Gets this byte array's value as an integer. Expects the byte array to be in little-endian
     * order.
     *
     * @return the integer represented by the byte array.
     */
    public BigInteger getAsNumber() {
        if (getValue().length == 0) {
            return BigInteger.ZERO;
        }
        return BigIntegers.fromLittleEndianByteArray(getValue());
    }

    /**
     * Deserializes this byte array's value into a {@code clazz} object.
     *
     * @return the deserialized JSON content of the byte array's value.
     */
    public <T> T getAsJson(Class<T> clazz) throws IOException {
        String json = new String(getValue(), UTF_8);
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(json, clazz);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ByteStringStackItem)) return false;
        ByteStringStackItem other = (ByteStringStackItem) o;
        return getType() == other.getType() && Arrays.equals(this.getValue(), other.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, Arrays.hashCode(getValue()));
    }
}
