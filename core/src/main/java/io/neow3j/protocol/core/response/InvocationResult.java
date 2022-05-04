package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.protocol.core.stackitem.StackItem;
import io.neow3j.types.ContractParameter;
import io.neow3j.types.NeoVMStateType;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InvocationResult {

    @JsonProperty("script")
    private String script;

    @JsonProperty("state")
    private NeoVMStateType state;

    @JsonProperty("gasconsumed")
    private String gasConsumed;

    @JsonProperty("exception")
    private String exception;

    @JsonProperty("stack")
    private List<StackItem> stack;

    @JsonProperty("tx")
    private String tx;

    @JsonProperty("pendingsignature")
    private PendingSignature pendingSignature;

    public InvocationResult() {
    }

    public InvocationResult(String script, NeoVMStateType state, String gasConsumed, String exception,
            List<StackItem> stack, String tx, PendingSignature pendingSignature) {
        this.script = script;
        this.state = state;
        this.gasConsumed = gasConsumed;
        this.exception = exception;
        this.stack = stack;
        this.tx = tx;
        this.pendingSignature = pendingSignature;
    }

    public String getScript() {
        return script;
    }

    public NeoVMStateType getState() {
        return state;
    }

    public boolean hasStateFault() {
        return state == NeoVMStateType.FAULT;
    }

    public String getGasConsumed() {
        return gasConsumed;
    }

    public String getException() {
        return exception;
    }

    public List<StackItem> getStack() {
        return stack;
    }

    public String getTx() {
        return tx;
    }

    public PendingSignature getPendingSignature() {
        return pendingSignature;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof InvocationResult)) {
            return false;
        }
        InvocationResult that = (InvocationResult) o;
        return Objects.equals(getScript(), that.getScript()) &&
                Objects.equals(getState(), that.getState()) &&
                Objects.equals(getGasConsumed(), that.getGasConsumed()) &&
                Objects.equals(getException(), that.getException()) &&
                Objects.equals(getStack(), that.getStack()) &&
                Objects.equals(getTx(), that.getTx()) &&
                Objects.equals(getPendingSignature(), that.getPendingSignature());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getScript(), getState(), getGasConsumed(), getException(), getStack(), getTx(),
                getPendingSignature());
    }

    @Override
    public String toString() {
        return "InvocationResult{" +
                "script='" + script + '\'' +
                ", state=" + state +
                ", gasconsumed=" + gasConsumed +
                ", exception='" + exception + '\'' +
                ", stack=" + stack +
                ", tx='" + tx + '\'' +
                ", pendingsignature='" + pendingSignature + '\'' +
                '}';
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PendingSignature {

        @JsonProperty("type")
        private String type; // the string "Transaction"

        @JsonProperty("data")
        private String data; // base64 string of the serialized, unsigned transaction

        @JsonProperty("items")
        Map<String, Item> items;

        @JsonProperty("network")
        private long network;

        public PendingSignature() {
        }

        public PendingSignature(String type, String data, Map<String, Item> items, long network) {
            this.type = type;
            this.data = data;
            this.items = items;
            this.network = network;
        }

        public String getType() {
            return type;
        }

        public String getData() {
            return data;
        }

        public Map<String, Item> getItems() {
            return items;
        }

        public long getNetwork() {
            return network;
        }

        public static class Item {

            @JsonProperty("script")
            private String script; // Base64 string

            @JsonProperty("parameters")
            private List<ContractParameter> parameters;

            // encoded, hexadeximal EC-Point mapped to base64-encoded, signature
            @JsonProperty("signatures")
            private Map<String, String> signatures;

            public Item() {
            }

            public Item(String script, List<ContractParameter> parameters, Map<String, String> signatures) {
                this.script = script;
                this.parameters = parameters;
                this.signatures = signatures;
            }

            public String getScript() {
                return script;
            }

            public List<ContractParameter> getParameters() {
                return parameters;
            }

            public Map<String, String> getSignatures() {
                return signatures;
            }
        }

    }

}