package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.model.types.StackItemType;

import java.math.BigInteger;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BooleanStackItem extends StackItem {

    @JsonProperty("value")
    private Boolean value;

    public BooleanStackItem() {
        super(StackItemType.BOOLEAN);
    }

    public BooleanStackItem(Boolean value) {
        super(StackItemType.BOOLEAN);
        this.value = value;
    }

    @Override
    public Boolean getValue() {
        return this.value;
    }

    @Override
    public boolean getBoolean() {
        nullOrEmptyCheck();
        return value;
    }

    @Override
    public BigInteger getInteger() {
        nullOrEmptyCheck();
        return value ? BigInteger.ONE : BigInteger.ZERO;
    }

    @Override
    public String getString() {
        nullOrEmptyCheck();
        return value.toString();
    }

    @Override
    protected String valueToString() {
        return value.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BooleanStackItem)) return false;
        BooleanStackItem other = (BooleanStackItem) o;
        return getType() == other.getType() &&
                getValue() == other.getValue();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getValue());
    }
}
