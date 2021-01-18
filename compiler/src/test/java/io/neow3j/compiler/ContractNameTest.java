package io.neow3j.compiler;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import io.neow3j.compiler.SupportedStandardsTest.MultiStandardContract;
import io.neow3j.compiler.SupportedStandardsTest.SingleStandardContract;
import io.neow3j.devpack.annotations.DisplayName;
import io.neow3j.devpack.annotations.SupportedStandards;
import io.neow3j.devpack.neo.Account;
import java.io.IOException;
import org.junit.Test;

public class ContractNameTest {

    @Test
    public void contractNameSetWithDisplayNameAnnotation() throws IOException {
        CompilationUnit res = new Compiler().compileClass(ContractNameTestContract.class.getName());
        assertThat(res.getManifest().getName(), is("Contract Name"));
    }

    @Test
    public void contractNameSetWithoutDisplayNameAnnotation() throws IOException {
        CompilationUnit res = new Compiler().compileClass(ContractNameTestContractWithoutAnnotation.class.getName());
        assertThat(res.getManifest().getName(), is(
                "ContractNameTest$ContractNameTestContractWithoutAnnotation"));
    }

    @DisplayName("Contract Name")
    static class ContractNameTestContract {

        public static void main() {
        }
    }

    static class ContractNameTestContractWithoutAnnotation {

        public static void main() {
        }
    }

}
