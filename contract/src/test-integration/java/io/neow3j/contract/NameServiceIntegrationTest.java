package io.neow3j.contract;

import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.RecordType;
import io.neow3j.protocol.core.response.NameState;
import io.neow3j.protocol.core.response.NeoSendRawTransaction;
import io.neow3j.protocol.exceptions.InvocationFaultStateException;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.test.NeoTestContainer;
import io.neow3j.transaction.Transaction;
import io.neow3j.transaction.TransactionBuilder;
import io.neow3j.transaction.Witness;
import io.neow3j.transaction.exceptions.TransactionConfigurationException;
import io.neow3j.types.Hash160;
import io.neow3j.types.Hash256;
import io.neow3j.utils.Files;
import io.neow3j.wallet.Account;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

import static io.neow3j.contract.IntegrationTestHelper.CLIENT_1;
import static io.neow3j.contract.IntegrationTestHelper.CLIENT_2;
import static io.neow3j.contract.IntegrationTestHelper.COMMITTEE_ACCOUNT;
import static io.neow3j.contract.IntegrationTestHelper.DEFAULT_ACCOUNT;
import static io.neow3j.contract.IntegrationTestHelper.fundAccountsWithGas;
import static io.neow3j.crypto.Sign.signMessage;
import static io.neow3j.transaction.AccountSigner.calledByEntry;
import static io.neow3j.transaction.Witness.createMultiSigWitness;
import static io.neow3j.types.ContractParameter.byteArray;
import static io.neow3j.utils.Await.waitUntilBlockCountIsGreaterThanZero;
import static io.neow3j.utils.Await.waitUntilTransactionIsExecuted;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class NameServiceIntegrationTest {

    private static Neow3j neow3j;
    private static NeoNameService nameService;

    private static final String ROOT_DOMAIN = "neo";
    private static final String DOMAIN = "neow3j.neo";
    private static final String A_RECORD = "157.0.0.1";
    private static final String CNAME_RECORD = "cnamerecord.neow3j.neo";
    private static final String TXT_RECORD = "textrecord";
    private static final String AAAA_RECORD = "3001:2:3:4:5:6:7:8";
    private static final long ONE_YEAR_IN_MILLISECONDS = 365L * 24 * 3600 * 1000;
    private static final long BUFFER_MILLISECONDS = 3600 * 1000;

    private static final String NAMESERVICE_NEF = "NameService.nef";
    private static final String NAMESERVICE_MANIFEST = "NameService.manifest.json";

    @ClassRule
    public static NeoTestContainer neoTestContainer = new NeoTestContainer();

    @BeforeClass
    public static void setUp() throws Throwable {
        neow3j = Neow3j.build(new HttpService(neoTestContainer.getNodeUrl()));
        waitUntilBlockCountIsGreaterThanZero(getNeow3j());
        Hash160 nameServiceHash = deployNameServiceContract();
        nameService = new NeoNameService(nameServiceHash, getNeow3j());
        // Make a transaction that can be used for the tests
        fundAccountsWithGas(getNeow3j(), DEFAULT_ACCOUNT, CLIENT_1, CLIENT_2);
        addRoot();
        registerDomainFromDefault(DOMAIN);
        setRecordFromDefault(DOMAIN, RecordType.A, A_RECORD);
    }

    private static Hash160 deployNameServiceContract() throws Throwable {
        URL r = NameServiceIntegrationTest.class.getClassLoader().getResource(NAMESERVICE_NEF);
        byte[] nefBytes = Files.readBytes(new File(r.toURI()));
        r = NameServiceIntegrationTest.class.getClassLoader().getResource(NAMESERVICE_MANIFEST);
        byte[] manifestBytes = Files.readBytes(new File(r.toURI()));

        Transaction tx = new ContractManagement(getNeow3j())
                .invokeFunction("deploy", byteArray(nefBytes), byteArray(manifestBytes))
                .signers(calledByEntry(COMMITTEE_ACCOUNT))
                .getUnsignedTransaction();
        Witness multiSigWitness = createMultiSigWitness(
                asList(signMessage(tx.getHashData(), DEFAULT_ACCOUNT.getECKeyPair())),
                COMMITTEE_ACCOUNT.getVerificationScript());
        Hash256 txHash = tx.addWitness(multiSigWitness).send().getSendRawTransaction().getHash();
        waitUntilTransactionIsExecuted(txHash, getNeow3j());
        return SmartContract.calcContractHash(COMMITTEE_ACCOUNT.getScriptHash(),
                NefFile.getCheckSumAsInteger(NefFile.computeChecksumFromBytes(nefBytes)), "NameService");
    }

    private static Neow3j getNeow3j() {
        return neow3j;
    }

    private static void addRoot() throws Throwable {
        Transaction tx = nameService.addRoot(ROOT_DOMAIN)
                .signers(calledByEntry(COMMITTEE_ACCOUNT))
                .getUnsignedTransaction();
        Witness multiSigWitness = createMultiSigWitness(
                asList(signMessage(tx.getHashData(), DEFAULT_ACCOUNT.getECKeyPair())),
                COMMITTEE_ACCOUNT.getVerificationScript());
        Hash256 txHash = tx.addWitness(multiSigWitness).send().getSendRawTransaction().getHash();
        waitUntilTransactionIsExecuted(txHash, getNeow3j());
    }

    private static void registerDomainFromDefault(String domain) throws Throwable {
        register(domain, DEFAULT_ACCOUNT);
    }

    private static void register(String domain, Account owner) throws Throwable {
        TransactionBuilder b = nameService.register(domain, owner.getScriptHash());
        b.signers(calledByEntry(owner));
        Transaction tx = b.sign();
        NeoSendRawTransaction response = tx.send();
        NeoSendRawTransaction.RawTransaction sendRawTransaction = response.getSendRawTransaction();
        Hash256 hash = sendRawTransaction.getHash();
        waitUntilTransactionIsExecuted(hash, getNeow3j());
    }

    private static void setRecordFromDefault(String domain, RecordType type, String data) throws Throwable {
        setRecord(domain, type, data, DEFAULT_ACCOUNT);
    }

    private static void setRecord(String domain, RecordType type, String data, Account signer) throws Throwable {
        Hash256 txHash = nameService.setRecord(domain, type, data)
                .signers(calledByEntry(signer))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, getNeow3j());
    }

    private static long getNowInMilliSeconds() {
        return new Date().getTime();
    }

    @Test
    public void testGetPrice() throws IOException {
        BigInteger price = nameService.getPrice(10);
        assertThat(price, is(BigInteger.valueOf(1_00000000)));
    }

    @Test
    public void testIsAvailable() throws IOException {
        boolean isAvailable = nameService.isAvailable(DOMAIN);
        assertFalse(isAvailable);
    }

    @Test
    public void testOwnerOf() throws IOException {
        Hash160 owner = nameService.ownerOf(DOMAIN);
        assertThat(owner, is(DEFAULT_ACCOUNT.getScriptHash()));
    }

    @Test
    public void testBalanceOf() throws IOException {
        BigInteger bigInteger = nameService.balanceOf(DEFAULT_ACCOUNT.getScriptHash());
        assertThat(bigInteger, greaterThanOrEqualTo(BigInteger.ONE));
    }

    @Test
    public void testProperties() throws IOException {
        NameState nameState = nameService.getNameState(DOMAIN);
        assertThat(nameState.getName(), is(DOMAIN));
        long inOneYear = getNowInMilliSeconds() + ONE_YEAR_IN_MILLISECONDS;
        long lessThanInOneYear = getNowInMilliSeconds() + ONE_YEAR_IN_MILLISECONDS - BUFFER_MILLISECONDS;
        assertThat(nameState.getExpiration(), lessThanOrEqualTo(inOneYear));
        assertThat(nameState.getExpiration(), greaterThan(lessThanInOneYear));
    }

    @Test
    public void testGetRecord() throws IOException {
        String ipv4 = nameService.getRecord(DOMAIN, RecordType.A);
        assertThat(ipv4, is(A_RECORD));
    }

    @Test
    public void testResolve() throws IOException {
        String ipv4 = nameService.resolve(DOMAIN, RecordType.A);
        assertThat(ipv4, is(A_RECORD));
    }

    @Test
    public void testAddRoot() throws Throwable {
        Transaction tx = nameService.addRoot("root")
                .signers(calledByEntry(COMMITTEE_ACCOUNT))
                .getUnsignedTransaction();
        Witness multiSigWitness = createMultiSigWitness(
                asList(signMessage(tx.getHashData(), DEFAULT_ACCOUNT.getECKeyPair())),
                COMMITTEE_ACCOUNT.getVerificationScript());
        Hash256 txHash = tx.addWitness(multiSigWitness).send().getSendRawTransaction().getHash();
        waitUntilTransactionIsExecuted(txHash, getNeow3j());

        boolean rootExists = false;
        try {
            // Any second-level domain name should still be available for the added root domain.
            rootExists = nameService.isAvailable("neow3j.root");
        } catch (IllegalArgumentException e) {
            fail();
        }
        assertTrue(rootExists);
    }

    @Test
    public void testSetPrice() throws Throwable {
        ArrayList<BigInteger> priceList = new ArrayList<>();
        priceList.add(BigInteger.valueOf(5_00000000));
        priceList.add(BigInteger.valueOf(1_00000000));
        priceList.add(BigInteger.valueOf(2_00000000));
        priceList.add(BigInteger.valueOf(3_00000000));
        priceList.add(BigInteger.valueOf(4_00000000));
        Transaction tx = nameService.setPrice(priceList)
                .signers(calledByEntry(COMMITTEE_ACCOUNT))
                .getUnsignedTransaction();
        Witness multiSigWitness = createMultiSigWitness(
                asList(signMessage(tx.getHashData(), DEFAULT_ACCOUNT.getECKeyPair())),
                COMMITTEE_ACCOUNT.getVerificationScript());
        Hash256 txHash = tx.addWitness(multiSigWitness).send().getSendRawTransaction().getHash();
        waitUntilTransactionIsExecuted(txHash, getNeow3j());

        BigInteger actualPrice = nameService.getPrice(1);
        assertThat(actualPrice, is(BigInteger.valueOf(1_00000000)));
        actualPrice = nameService.getPrice(2);
        assertThat(actualPrice, is(BigInteger.valueOf(2_00000000)));
        actualPrice = nameService.getPrice(3);
        assertThat(actualPrice, is(BigInteger.valueOf(3_00000000)));
        actualPrice = nameService.getPrice(4);
        assertThat(actualPrice, is(BigInteger.valueOf(4_00000000)));
        actualPrice = nameService.getPrice(5);
        assertThat(actualPrice, is(BigInteger.valueOf(5_00000000)));
        actualPrice = nameService.getPrice(50);
        assertThat(actualPrice, is(BigInteger.valueOf(5_00000000)));
    }

    @Test
    public void testRegister() throws Throwable {
        String domain = "register.neo";
        boolean availableBefore = nameService.isAvailable(domain);
        assertTrue(availableBefore);

        Hash256 txHash = nameService.register(domain, DEFAULT_ACCOUNT.getScriptHash())
                .signers(calledByEntry(DEFAULT_ACCOUNT))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, getNeow3j());

        boolean availableAfter = nameService.isAvailable(domain);
        assertFalse(availableAfter);
    }

    @Test
    public void testRenew() throws Throwable {
        String domain = "renew.neo";
        registerDomainFromDefault(domain);
        long inOneYear = getNowInMilliSeconds() + ONE_YEAR_IN_MILLISECONDS;
        long lessThanInOneYear = getNowInMilliSeconds() + ONE_YEAR_IN_MILLISECONDS - BUFFER_MILLISECONDS;
        NameState nameStateBefore = nameService.getNameState(domain);
        assertThat(nameStateBefore.getExpiration(), lessThanOrEqualTo(inOneYear));
        assertThat(nameStateBefore.getExpiration(), greaterThan(lessThanInOneYear));

        Hash256 txHash = nameService.renew(domain)
                .signers(calledByEntry(DEFAULT_ACCOUNT))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, getNeow3j());

        NameState nameStateAfter = nameService.getNameState(domain);
        long inTwoYears = getNowInMilliSeconds() + 2 * ONE_YEAR_IN_MILLISECONDS;
        long lessThanInTwoYears = getNowInMilliSeconds() + 2 * ONE_YEAR_IN_MILLISECONDS - BUFFER_MILLISECONDS;
        assertThat(nameStateAfter.getExpiration(), lessThanOrEqualTo(inTwoYears));
        assertThat(nameStateAfter.getExpiration(), greaterThan(lessThanInTwoYears));
    }

    @Test
    public void testSetAdmin() throws Throwable {
        String domain = "admin.neo";
        register(domain, CLIENT_1);

        // setRecord should throw an exception, since client2 should not be able to create a record.
        assertThrows(TransactionConfigurationException.class,
                () -> setRecord(domain, RecordType.A, A_RECORD, CLIENT_2));

        Hash256 txHash = nameService.setAdmin(domain, CLIENT_2.getScriptHash())
                .signers(calledByEntry(CLIENT_1), calledByEntry(CLIENT_2))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, getNeow3j());

        NameState nameState = nameService.getNameState(domain);
        assertThat(nameState.getAdmin(), is(CLIENT_2.getScriptHash()));

        // Now as admin, client2 should be able to set a record.
        setRecord(domain, RecordType.A, A_RECORD, CLIENT_2);
        String aRecord = nameService.getRecord(domain, RecordType.A);
        assertThat(aRecord, is(A_RECORD));
    }

    @Test
    public void testSetRecord_A() throws Throwable {
        setRecordFromDefault(DOMAIN, RecordType.A, A_RECORD);
        String cnameRecord = nameService.getRecord(DOMAIN, RecordType.A);
        assertThat(cnameRecord, is(A_RECORD));
    }

    @Test
    public void testSetRecord_CNAME() throws Throwable {
        setRecordFromDefault(DOMAIN, RecordType.CNAME, CNAME_RECORD);
        String cnameRecord = nameService.getRecord(DOMAIN, RecordType.CNAME);
        assertThat(cnameRecord, is(CNAME_RECORD));
    }

    @Test
    public void testSetRecord_TXT() throws Throwable {
        setRecordFromDefault(DOMAIN, RecordType.TXT, TXT_RECORD);
        String cnameRecord = nameService.getRecord(DOMAIN, RecordType.TXT);
        assertThat(cnameRecord, is(TXT_RECORD));
    }

    @Test
    public void testSetRecord_AAAA() throws Throwable {
        setRecordFromDefault(DOMAIN, RecordType.AAAA, AAAA_RECORD);
        String cnameRecord = nameService.getRecord(DOMAIN, RecordType.AAAA);
        assertThat(cnameRecord, is(AAAA_RECORD));
    }

    @Test
    public void testDeleteRecord() throws Throwable {
        String domain = "delete.neo";
        registerDomainFromDefault(domain);
        setRecordFromDefault(domain, RecordType.TXT, "textrecordfordelete");
        String textRecordForDelete = nameService.getRecord(domain, RecordType.TXT);
        assertThat(textRecordForDelete, is("textrecordfordelete"));

        Hash256 txHash = nameService.deleteRecord(domain, RecordType.TXT)
                .signers(calledByEntry(DEFAULT_ACCOUNT))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, getNeow3j());
        System.out.println("");
        assertThrows("Could not get any record of type 'TXT' for the domain 'delete.neo'." + RecordType.TXT.jsonValue(),
                InvocationFaultStateException.class,
                () -> nameService.getRecord(domain, RecordType.TXT));
    }

    @Test
    public void testTransfer() throws Throwable {
        String domainForTransfer = "transfer.neo";
        registerDomainFromDefault(domainForTransfer);
        Hash160 ownerBefore = nameService.ownerOf(domainForTransfer);
        assertThat(ownerBefore, is(DEFAULT_ACCOUNT.getScriptHash()));

        Hash256 txHash =
                nameService.transfer(DEFAULT_ACCOUNT, CLIENT_1.getScriptHash(), domainForTransfer)
                        .sign()
                        .send()
                        .getSendRawTransaction()
                        .getHash();
        waitUntilTransactionIsExecuted(txHash, getNeow3j());

        Hash160 ownerAfter = nameService.ownerOf(domainForTransfer);
        assertThat(ownerAfter, is(CLIENT_1.getScriptHash()));
    }

}