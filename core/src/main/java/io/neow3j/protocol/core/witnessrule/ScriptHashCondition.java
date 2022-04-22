package io.neow3j.protocol.core.witnessrule;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.transaction.witnessrule.WitnessConditionType;
import io.neow3j.types.Hash160;

import java.util.Objects;

public class ScriptHashCondition extends WitnessCondition {

    @JsonProperty("hash")
    private Hash160 scriptHash;

    public ScriptHashCondition() {
        super(WitnessConditionType.SCRIPT_HASH);
    }

    public ScriptHashCondition(Hash160 scriptHash) {
        this();
        this.scriptHash = scriptHash;
    }

    @Override
    public Hash160 getScriptHash() {
        return scriptHash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ScriptHashCondition)) {
            return false;
        }
        ScriptHashCondition other = (ScriptHashCondition) o;
        return getType() == other.getType() &&
                Objects.equals(getScriptHash(), other.getScriptHash());
    }

    @Override
    public String toString() {
        return "ScriptHashCondition{" +
                "hash=" + getScriptHash() +
                "}";
    }

}
