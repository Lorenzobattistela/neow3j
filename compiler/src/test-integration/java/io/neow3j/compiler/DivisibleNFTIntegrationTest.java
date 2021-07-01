package io.neow3j.compiler;

import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.Helper;
import io.neow3j.devpack.Iterator;
import io.neow3j.devpack.Storage;
import io.neow3j.devpack.StorageContext;
import io.neow3j.devpack.StorageMap;
import io.neow3j.devpack.annotations.ContractHash;
import io.neow3j.devpack.annotations.Permission;
import io.neow3j.devpack.constants.FindOptions;
import io.neow3j.devpack.contracts.DivisibleNonFungibleToken;
import io.neow3j.protocol.core.response.NeoInvokeFunction;
import io.neow3j.types.Hash256;
import io.neow3j.utils.Await;
import io.neow3j.wallet.Account;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.IOException;
import java.math.BigInteger;

import static io.neow3j.devpack.StringLiteralHelper.addressToScriptHash;
import static io.neow3j.types.ContractParameter.byteArrayFromString;
import static io.neow3j.types.ContractParameter.hash160;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class DivisibleNFTIntegrationTest {

    @Rule
    public TestName testName = new TestName();

    @ClassRule
    public static ContractTestRule ct = new ContractTestRule(
            DivisibleNFTTestContract.class.getName());

    @BeforeClass
    public static void setUp() throws Throwable {
        Hash256 gasTxHash = ct.transferGas(ct.getDefaultAccount().getScriptHash(),
                new BigInteger("10000"));
        Hash256 neoTxHash = ct.transferNeo(ct.getDefaultAccount().getScriptHash(),
                new BigInteger("10000"));
        Await.waitUntilTransactionIsExecuted(gasTxHash, ct.getNeow3j());
        Await.waitUntilTransactionIsExecuted(neoTxHash, ct.getNeow3j());
        ct.deployContract(ConcreteDivisibleNFT.class.getName());
    }

    @Test
    public void testTransfer() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        assertTrue(response.getInvocationResult().getStack().get(0).getBoolean());
    }

    @Test
    public void testOwnerOf() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, byteArrayFromString("test"));
        io.neow3j.types.Hash160 owner = io.neow3j.types.Hash160.fromAddress(response
                .getInvocationResult().getStack().get(0).getAddress());
        assertThat(owner, is(io.neow3j.types.Hash160.ZERO));
    }

    @Test
    public void testBalanceOf() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName,
                hash160(Account.create()), byteArrayFromString("id1"));
        assertThat(response.getInvocationResult().getStack().get(0).getInteger().intValue(),
                is(38));
    }

    @Permission(contract = "f2d861c58d9f6a9d5a645016d77735a39b06197e")
    static class DivisibleNFTTestContract {

        public static boolean testTransfer(Hash160 from, Hash160 to, int amount,
                ByteString tokenId, Object data) {

            return CustomDivisibleNFT.transfer(from, to, amount, tokenId, data);
        }

        public static Iterator<Hash160> testOwnerOf(ByteString tokenId) {
            return CustomDivisibleNFT.ownerOf(tokenId);
        }

        public static int testBalanceOf(Hash160 account, ByteString tokenId) {
            return CustomDivisibleNFT.balanceOf(account, tokenId);
        }

    }

    static class ConcreteDivisibleNFT {
        static final StorageContext ctx = Storage.getStorageContext();
        static final byte[] mapPrefix = Helper.toByteArray((byte) 1);

        public static boolean transfer(Hash160 from, Hash160 to, int amount, ByteString tokenId,
                Object data) {

            return true;
        }

        public static Iterator<ByteString> ownerOf(ByteString tokenId) {
            StorageMap map = ctx.createMap(mapPrefix);
            map.put(Helper.toByteArray((byte) 1),
                    addressToScriptHash("NSdNMyrz7Bp8MXab41nTuz1mRCnsFr5Rsv"));
            map.put(Helper.toByteArray((byte) 2),
                    addressToScriptHash("NhxK1PEmijLVD6D4WSuPoUYJVk855L21ru"));

            return (Iterator<ByteString>) Storage.find(ctx,
                    mapPrefix,
                    FindOptions.ValuesOnly);
        }

        public static int balanceOf(Hash160 account, ByteString tokenId) {
            return 38;
        }

    }

    @ContractHash("f2d861c58d9f6a9d5a645016d77735a39b06197e")
    static class CustomDivisibleNFT extends DivisibleNonFungibleToken {
    }

}
