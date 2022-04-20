package io.neow3j.transaction.witnessrule;

import io.neow3j.serialization.BinaryReader;
import io.neow3j.serialization.BinaryWriter;
import io.neow3j.serialization.IOUtils;
import io.neow3j.serialization.exceptions.DeserializationException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the condition that at least one of the sub-conditions must meet.
 */
public class OrCondition extends CompositeCondition {

    private List<WitnessCondition> conditions;

    public OrCondition() {
        type = WitnessConditionType.OR;
        conditions = new ArrayList<>();
    }

    /**
     * Constructs an OR condition with the given sub-conditions.
     *
     * @param conditions The conditions.
     * @throws IllegalArgumentException if more than {@link WitnessCondition#MAX_SUBITEMS} are
     *                                  added.
     */
    public OrCondition(List<WitnessCondition> conditions) {
        this();
        if (conditions.size() > MAX_SUBITEMS) {
            throw new IllegalArgumentException("A maximum of " + MAX_SUBITEMS + " subitems is " +
                    "allowed for the OR witness condition.");
        }
        this.conditions = conditions;
    }

    @Override
    protected void deserializeWithoutType(BinaryReader reader) throws DeserializationException {
        try {
            long nrOfConditions = reader.readVarInt();
            for (int i = 0; i < nrOfConditions; i++) {
                this.conditions.add(WitnessCondition.deserializeWitnessCondition(reader));
            }
        } catch (IOException e) {
            throw new DeserializationException(e);
        }
    }

    @Override
    protected void serializeWithoutType(BinaryWriter writer) throws IOException {
        writer.writeSerializableVariable(conditions);
    }

    @Override
    public int getSize() {
        return super.getSize() + IOUtils.getVarSize(conditions);
    }

    @Override
    public List<WitnessCondition> getConditions() {
        return conditions;
    }

}
