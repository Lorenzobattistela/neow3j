package io.neow3j.test;

import io.neow3j.compiler.CompilationUnit;
import io.neow3j.compiler.Compiler;
import io.neow3j.contract.ContractUtils;
import io.neow3j.contract.SmartContract;
import io.neow3j.crypto.WIF;
import io.neow3j.protocol.Neow3jExpress;
import io.neow3j.protocol.ObjectMapperFactory;
import io.neow3j.protocol.core.response.NeoApplicationLog;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.script.VerificationScript;
import io.neow3j.types.Hash160;
import io.neow3j.types.Hash256;
import io.neow3j.utils.Await;
import io.neow3j.utils.Numeric;
import io.neow3j.wallet.Account;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.utility.MountableFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

import static io.neow3j.test.NeoExpressTestContainer.CONTAINER_WORKDIR;

public class ContractTestExtension implements BeforeAllCallback, AfterAllCallback {

    private static final String DEFAULT_NEOXP_CONFIG = "default.neo-express";

    // Extension Context Store
    final static String CONTAINER_STORE_KEY = "neoExpressContainer";
    final static String NEOW3J_STORE_KEY = "neow3j";
    final static String CONTRACT_STORE_KEY = "contractHash";

    private String neoxpConfigFileName = DEFAULT_NEOXP_CONFIG;

    private NeoExpressTestContainer container;
    private Neow3jExpress neow3j;
    private SmartContract contractUnderTest;

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        ContractTest annotation = context.getTestClass().get().getAnnotation(ContractTest.class);
        if (annotation == null) {
            throw new ExtensionConfigurationException("Using the " + this.getClass().getSimpleName()
                    + " without the @" + ContractTest.class.getSimpleName() + " annotation.");
        }
        container = new NeoExpressTestContainer(annotation.blockTime());
        if (!annotation.neoxpConfig().isEmpty()) {
            neoxpConfigFileName = annotation.neoxpConfig();
        }
        container.withNeoxpConfig(neoxpConfigFileName);
        if (!annotation.batchFile().isEmpty()) {
            container.withBatchFile(annotation.batchFile());
        }
        if (!annotation.checkpoint().isEmpty()) {
            container.withCheckpoint(annotation.checkpoint());
        }
        container.start();
        neow3j = Neow3jExpress.build(new HttpService(container.getNodeUrl()));
        contractUnderTest = compileAndDeployContract(annotation.contractClass(), container, neow3j);

        ExtensionContext.Store store = context.getStore(ExtensionContext.Namespace.GLOBAL);
        store.put(CONTAINER_STORE_KEY, container);
        store.put(NEOW3J_STORE_KEY, neow3j);
        store.put(CONTRACT_STORE_KEY, contractUnderTest);
    }

    private SmartContract compileAndDeployContract(Class<?> contractClass,
            NeoExpressTestContainer container, Neow3jExpress neow3j) throws Exception {

        Path tmpDir = Files.createTempDirectory("compilation-output");
        tmpDir.toFile().deleteOnExit();
        CompilationUnit res = new Compiler().compile(contractClass.getCanonicalName());

        String contractName = res.getManifest().getName();
        String nefFile = ContractUtils.writeNefFile(res.getNefFile(), contractName, tmpDir);
        String manifestFile = ContractUtils.writeContractManifestFile(res.getManifest(), tmpDir);
        String destNefFile = CONTAINER_WORKDIR + "contract.nef";
        String destManifestFile = CONTAINER_WORKDIR + "contract.manifest.json";
        container.copyFileToContainer(MountableFile.forHostPath(nefFile, 777), destNefFile);
        container.copyFileToContainer(MountableFile.forHostPath(manifestFile, 777),
                destManifestFile);

        Hash256 deployTxHash = new Hash256(container.deployContract(destNefFile));
        Await.waitUntilTransactionIsExecuted(deployTxHash, neow3j);
        NeoApplicationLog log = neow3j.getApplicationLog(deployTxHash).send().getApplicationLog();
        Hash160 contractHash = new Hash160(Numeric.reverseHexString(log.getExecutions().get(0)
                .getNotifications().get(0)
                .getState().getList().get(0)
                .getHexString()));
        return new SmartContract(contractHash, neow3j);
    }

    @Override
    public void afterAll(ExtensionContext context) {
        container.stop();
    }

    public SmartContract getContractUnderTest() {
        return contractUnderTest;
    }

    public Neow3jExpress getNeow3j() {
        return neow3j;
    }

    public void runExpress() throws Exception {
        container.runExpress();
    }

    public void stopExpress() throws Exception {
        container.stopExpress();
    }

    public String createAccount(String name) throws Exception {
        return container.createAccount(name);
    }

    /**
     * Transfers assets.
     *
     * @param amount   The amount of assets to transfer.
     * @param asset    The asset to transfer. Can be a symbol, e.g., "NEO", or the hash of a
     *                 contract.
     * @param sender   The sender. Can be a name of a wallet, e.g., "genesis", or an address.
     * @param receiver The receiver. Can be a name of a wallet, e.g., "genesis", or an address.
     * @return The transaction hash of the transfer.
     * @throws Exception if the transfer transaction cannot be created or propagated.
     */
    public Hash256 transfer(BigInteger amount, String asset, String sender, String receiver)
            throws Exception {
        return new Hash256(container.transfer(amount, asset, sender, receiver));
    }

    /**
     * Gets the account for the given name if it exists on the neo-express instance. If the
     * account is a multi-sig account, it will not have a private key available for transaction
     * signing.
     *
     * @param name The account's name
     * @return The account
     * @throws IOException                     if an error occurs reading the neo-express
     *                                         configuration.
     * @throws ExtensionConfigurationException if the account name cannot be found.
     */
    public Account getAccount(String name) throws IOException {
        InputStream s = ContractTestExtension.class.getClassLoader()
                .getResourceAsStream(neoxpConfigFileName);
        NeoExpressConfig config = ObjectMapperFactory.getObjectMapper()
                .readValue(s, NeoExpressConfig.class);

        Optional<NeoExpressConfig.Wallet.Account> acc = Stream.concat(
                        config.getConsensusNodes().stream().flatMap(n -> n.getWallet().getAccounts().stream()),
                        config.getWallets().stream().flatMap(w -> w.getAccounts().stream()))
                .filter(a -> a.label != null && a.label.equals(name)).findFirst();

        if (!acc.isPresent()) {
            throw new ExtensionConfigurationException("Account '" + name + "' not found.");
        }
        VerificationScript verifScript = new VerificationScript(
                Numeric.hexStringToByteArray(acc.get().getContract().getScript()));
        if (verifScript.isMultiSigScript()) {
            return Account.fromVerificationScript(verifScript);
        }
        return Account.fromWIF(WIF.getWIFFromPrivateKey(
                Numeric.hexStringToByteArray(acc.get().privateKey)));
    }

}
