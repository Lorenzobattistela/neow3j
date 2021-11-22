package io.neow3j.transaction.witnessrule;

import io.neow3j.serialization.BinaryReader;
import io.neow3j.serialization.BinaryWriter;
import io.neow3j.serialization.exceptions.DeserializationException;

import java.io.IOException;


/**
 * Reverses another condition.
 */
public class NotCondition extends WitnessCondition {

    private WitnessCondition condition;

    public NotCondition() {
        type = WitnessConditionType.NOT;
    }

    /**
     * Constructs the reverse of the given condition.
     *
     * @param condition the condition to reverse.
     */
    public NotCondition(WitnessCondition condition) {
        this.condition = condition;
    }

    @Override
    protected void deserializeWithoutType(BinaryReader reader) throws DeserializationException {
        condition = reader.readSerializable(WitnessCondition.class);
    }

    @Override
    protected void serializeWithoutType(BinaryWriter writer) throws IOException {
        writer.writeSerializableFixed(condition);
    }

    @Override
    public int getSize() {
        return condition.getSize();
    }
}
