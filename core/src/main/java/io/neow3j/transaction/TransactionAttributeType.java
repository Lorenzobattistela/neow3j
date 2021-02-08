package io.neow3j.transaction;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TransactionAttributeType {

    /**
     * This attribute allows committee members to prioritize a transaction.
     */
    HIGH_PRIORITY(TransactionAttributeType.HIGH_PRIORITY_VALUE, 0x01,
            HighPriorityAttribute.class);

    public static final String HIGH_PRIORITY_VALUE = "HighPriority";

    private String jsonValue;
    private byte byteValue;
    private Class<? extends TransactionAttribute> clazz;

    TransactionAttributeType(String jsonValue, int byteValue,
            Class<? extends TransactionAttribute> clazz) {
        this.jsonValue = jsonValue;
        this.byteValue = (byte) byteValue;
        this.clazz = clazz;
    }

    public static TransactionAttributeType valueOf(byte byteValue) {
        for (TransactionAttributeType e : TransactionAttributeType.values()) {
            if (e.byteValue == byteValue) {
                return e;
            }
        }
        throw new IllegalArgumentException(String.format("%s value type not found.",
                TransactionAttributeType.class.getName()));
    }

    @JsonCreator
    public static TransactionAttributeType fromJson(Object value) {
        if (value instanceof String) {
            return fromJsonValue((String) value);
        }
        throw new IllegalArgumentException(String.format("%s value type not found.",
                TransactionAttributeType.class.getName()));
    }

    public static TransactionAttributeType fromJsonValue(String jsonValue) {
        for (TransactionAttributeType e : TransactionAttributeType.values()) {
            if (e.jsonValue.equals(jsonValue)) {
                return e;
            }
        }
        throw new IllegalArgumentException(String.format("%s value type not found.",
                TransactionAttributeType.class.getName()));
    }

    @JsonValue
    public String jsonValue() {
        return this.jsonValue;
    }

    public byte byteValue() {
        return this.byteValue;
    }

    public Class<? extends TransactionAttribute> clazz() {
        return this.clazz;
    }

}
