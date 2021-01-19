package io.neow3j.compiler;

import static io.neow3j.contract.ContractParameter.byteArray;
import static io.neow3j.contract.ContractParameter.string;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import io.neow3j.contract.ContractParameter;
import io.neow3j.contract.NeoToken;
import io.neow3j.contract.ScriptHash;
import io.neow3j.contract.SmartContract;
import io.neow3j.devpack.contracts.ManagementContract;
import io.neow3j.devpack.neo.Contract;
import io.neow3j.protocol.ObjectMapperFactory;
import io.neow3j.protocol.core.methods.response.ArrayStackItem;
import io.neow3j.protocol.core.methods.response.NeoGetContractState;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.protocol.core.methods.response.NeoSendRawTransaction;
import io.neow3j.transaction.Signer;
import io.neow3j.utils.Await;
import io.neow3j.utils.Numeric;
import java.io.IOException;
import org.junit.BeforeClass;
import org.junit.Test;

public class ManagementContractIntegrationTest extends ContractTest {

    @BeforeClass
    public static void setUp() throws Throwable {
        setUp(ManagementContractIntegrationTestContract.class.getName());
    }

    @Test
    public void getContract() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(
                ContractParameter.hash160(io.neow3j.contract.NeoToken.SCRIPT_HASH));
        ArrayStackItem arrayStackItem = response.getInvocationResult().getStack().get(0).asArray();
        assertThat(arrayStackItem.get(0).asInteger().getValue().intValue(), is(-1)); // ID
        assertThat(arrayStackItem.get(1).asInteger().getValue().intValue(), is(0)); // updateCounter
        assertThat(Numeric.reverseHexString(arrayStackItem.get(2).asByteString().getAsHexString()),
                is(NeoToken.SCRIPT_HASH.toString())); // contract hash
        // script
        assertThat(arrayStackItem.get(3).asByteString().getAsHexString(), not(isEmptyString()));
        // manifest
        assertThat(arrayStackItem.get(4).asByteString().getAsHexString(), not(isEmptyString()));
    }

    @Test
    public void deploy() throws Throwable {
        CompilationUnit compUnit = new Compiler().compileClass(
                ManagementContractIntegrationTestContractNew.class.getName());
        String manifestString = ObjectMapperFactory.getObjectMapper()
                .writeValueAsString(compUnit.getManifest());
        String txHash = invokeFunctionAndAwaitExecution(byteArray(compUnit.getNefFile().toArray()),
                string(manifestString));

        NeoInvokeFunction result = callInvokeFunction("newMethod");
        assertThat(result.getInvocationResult().getStack().get(0).asByteString().getAsString(),
                is("new method"));
    }

    @Test
    public void update() throws Throwable {
        CompilationUnit compUnit = new Compiler().compileClass(
                ManagementContractIntegrationTestContractUpdated.class.getName());
        String manifestString = ObjectMapperFactory.getObjectMapper()
                .writeValueAsString(compUnit.getManifest());
        String txHash = invokeFunctionAndAwaitExecution(byteArray(compUnit.getNefFile().toArray()),
                string(manifestString));

        NeoInvokeFunction result = callInvokeFunction("updatedMethod");
        assertThat(result.getInvocationResult().getStack().get(0).asByteString().getAsString(),
                is("updated method"));
    }

    @Test
    public void destroy() throws Throwable {
        CompilationUnit res = new Compiler().compileClass(
                ManagementContractIntegrationTestContractToDestroy.class.getName());
        NeoSendRawTransaction response = new io.neow3j.contract.ManagementContract(neow3j)
                .deploy(res.getNefFile(), res.getManifest())
                .wallet(wallet)
                .signers(Signer.calledByEntry(committee.getScriptHash()))
                .sign().send();
        Await.waitUntilTransactionIsExecuted(response.getSendRawTransaction().getHash(), neow3j);

        ScriptHash contractHash = SmartContract.getContractHash(committee.getScriptHash(),
                res.getNefFile().getScript());
        SmartContract sc = new SmartContract(contractHash, neow3j);
        String txHash = sc.invokeFunction("destroy")
                .wallet(wallet)
                .signers(Signer.calledByEntry(committee.getScriptHash()))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        Await.waitUntilTransactionIsExecuted(txHash, neow3j);

        NeoGetContractState contractState = neow3j.getContractState(contractHash.toString()).send();
        assertThat(contractState.getError().getMessage(), is("Unknown contract"));
    }

    @Test
    public void getHash() throws Throwable {
        NeoInvokeFunction response = callInvokeFunction();
        assertThat(response.getInvocationResult().getStack().get(0).asByteString().getAsHexString(),
                is(io.neow3j.contract.ManagementContract.SCRIPT_HASH.toString()));
    }

    static class ManagementContractIntegrationTestContract {

        public static Contract getContract(byte[] contractHash) {
            return ManagementContract.getContract(contractHash);
        }

        public static byte[] getHash() {
            return ManagementContract.getHash();
        }

        public static void update(byte[] nefFile, String manifest) {
            ManagementContract.update(nefFile, manifest);
        }

        public static Contract deploy(byte[] nefFile, String manifest) {
            return ManagementContract.deploy(nefFile, manifest);
        }

    }

    static class ManagementContractIntegrationTestContractUpdated {

        public static byte[] getHash() {
            return ManagementContract.getHash();
        }

        public static String updatedMethod() {
            return "updated method";
        }

    }

    static class ManagementContractIntegrationTestContractNew {

        public static String newMethod() {
            return "new method";
        }
    }

    static class ManagementContractIntegrationTestContractToDestroy {

        public static void destroy() {
            ManagementContract.destroy();
        }
    }
}
