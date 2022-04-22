package io.neow3j.protocol.core.witnessrule;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.transaction.witnessrule.WitnessConditionType;
import io.neow3j.types.Hash160;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CalledByContractCondition extends WitnessCondition {

    @JsonProperty("hash")
    private Hash160 contractHash;

    public CalledByContractCondition() {
        super(WitnessConditionType.CALLED_BY_CONTRACT);
    }

    public CalledByContractCondition(Hash160 contractHash) {
        this();
        this.contractHash = contractHash;
    }

    @Override
    public Hash160 getScriptHash() {
        return contractHash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CalledByContractCondition)) {
            return false;
        }
        CalledByContractCondition other = (CalledByContractCondition) o;
        return getType() == other.getType() &&
                Objects.equals(getScriptHash(), other.getScriptHash());
    }

    @Override
    public String toString() {
        return "CalledByContractCondition{" +
                "hash=" + getScriptHash() +
                "}";
    }

}
