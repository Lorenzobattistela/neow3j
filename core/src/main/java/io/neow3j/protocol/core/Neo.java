package io.neow3j.protocol.core;

import io.neow3j.contract.ContractParameter;
import io.neow3j.contract.Hash160;
import io.neow3j.contract.Hash256;
import io.neow3j.protocol.core.methods.response.NeoBlockCount;
import io.neow3j.protocol.core.methods.response.NeoBlockHash;
import io.neow3j.protocol.core.methods.response.NeoCalculateNetworkFee;
import io.neow3j.protocol.core.methods.response.NeoCloseWallet;
import io.neow3j.protocol.core.methods.response.NeoConnectionCount;
import io.neow3j.protocol.core.methods.response.NeoDumpPrivKey;
import io.neow3j.protocol.core.methods.response.NeoGetApplicationLog;
import io.neow3j.protocol.core.methods.response.NeoBlockHeaderCount;
import io.neow3j.protocol.core.methods.response.NeoGetCommittee;
import io.neow3j.protocol.core.methods.response.NeoGetNativeContracts;
import io.neow3j.protocol.core.methods.response.NeoGetProof;
import io.neow3j.protocol.core.methods.response.NeoGetStateHeight;
import io.neow3j.protocol.core.methods.response.NeoGetStateRoot;
import io.neow3j.protocol.core.methods.response.NeoGetUnclaimedGas;
import io.neow3j.protocol.core.methods.response.NeoGetWalletBalance;
import io.neow3j.protocol.core.methods.response.NeoGetBlock;
import io.neow3j.protocol.core.methods.response.NeoGetContractState;
import io.neow3j.protocol.core.methods.response.NeoGetMemPool;
import io.neow3j.protocol.core.methods.response.NeoGetNep17Balances;
import io.neow3j.protocol.core.methods.response.NeoGetNep17Transfers;
import io.neow3j.protocol.core.methods.response.NeoGetNewAddress;
import io.neow3j.protocol.core.methods.response.NeoGetPeers;
import io.neow3j.protocol.core.methods.response.NeoGetRawBlock;
import io.neow3j.protocol.core.methods.response.NeoGetRawMemPool;
import io.neow3j.protocol.core.methods.response.NeoGetRawTransaction;
import io.neow3j.protocol.core.methods.response.NeoGetStorage;
import io.neow3j.protocol.core.methods.response.NeoGetTransaction;
import io.neow3j.protocol.core.methods.response.NeoGetTransactionHeight;
import io.neow3j.protocol.core.methods.response.NeoGetWalletUnclaimedGas;
import io.neow3j.protocol.core.methods.response.NeoGetNextBlockValidators;
import io.neow3j.protocol.core.methods.response.NeoGetVersion;
import io.neow3j.protocol.core.methods.response.NeoImportPrivKey;
import io.neow3j.protocol.core.methods.response.NeoInvokeContractVerify;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.protocol.core.methods.response.NeoInvokeScript;
import io.neow3j.protocol.core.methods.response.NeoListAddress;
import io.neow3j.protocol.core.methods.response.NeoListPlugins;
import io.neow3j.protocol.core.methods.response.NeoOpenWallet;
import io.neow3j.protocol.core.methods.response.NeoSendFrom;
import io.neow3j.protocol.core.methods.response.NeoSendMany;
import io.neow3j.protocol.core.methods.response.NeoSendRawTransaction;
import io.neow3j.protocol.core.methods.response.NeoSendToAddress;
import io.neow3j.protocol.core.methods.response.NeoSubmitBlock;
import io.neow3j.protocol.core.methods.response.NeoValidateAddress;
import io.neow3j.protocol.core.methods.response.NeoVerifyProof;
import io.neow3j.protocol.core.methods.response.TransactionSendAsset;
import io.neow3j.transaction.Signer;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

/**
 * Core NEO JSON-RPC API.
 */
public interface Neo {

    //region Blockchain Methods

    Request<?, NeoBlockHash> getBestBlockHash();

    Request<?, NeoBlockHash> getBlockHash(BigInteger blockIndex);

    Request<?, NeoGetBlock> getBlock(Hash256 blockHash, boolean returnFullTransactionObjects);

    Request<?, NeoGetBlock> getBlock(BigInteger blockIndex, boolean returnFullTransactionObjects);

    Request<?, NeoGetRawBlock> getRawBlock(Hash256 blockHash);

    Request<?, NeoGetRawBlock> getRawBlock(BigInteger blockIndex);

    Request<?, NeoBlockHeaderCount> getBlockHeaderCount();

    Request<?, NeoBlockCount> getBlockCount();

    Request<?, NeoGetBlock> getBlockHeader(Hash256 hash);

    Request<?, NeoGetBlock> getBlockHeader(BigInteger blockIndex);

    Request<?, NeoGetRawBlock> getRawBlockHeader(Hash256 hash);

    Request<?, NeoGetRawBlock> getRawBlockHeader(BigInteger blockIndex);

    Request<?, NeoGetNativeContracts> getNativeContracts();

    Request<?, NeoGetContractState> getContractState(Hash160 hash160);

    Request<?, NeoGetContractState> getNativeContractState(String contractName);

    Request<?, NeoGetMemPool> getMemPool();

    Request<?, NeoGetRawMemPool> getRawMemPool();

    Request<?, NeoGetTransaction> getTransaction(Hash256 txId);

    Request<?, NeoGetRawTransaction> getRawTransaction(Hash256 txId);

    Request<?, NeoGetStorage> getStorage(Hash160 contractHash, String keyHexString);

    Request<?, NeoGetTransactionHeight> getTransactionHeight(Hash256 txId);

    Request<?, NeoGetNextBlockValidators> getNextBlockValidators();

    Request<?, NeoGetCommittee> getCommittee();

    //endregion

    //region Node Methods

    Request<?, NeoConnectionCount> getConnectionCount();

    Request<?, NeoGetPeers> getPeers();

    Request<?, NeoGetVersion> getVersion();

    Request<?, NeoSendRawTransaction> sendRawTransaction(String rawTransactionHex);

    Request<?, NeoSubmitBlock> submitBlock(String serializedBlockAsHex);

    //endregion

    //region SmartContract Methods

    Request<?, NeoInvokeFunction> invokeFunction(Hash160 contractScriptHash, String functionName,
            Signer... signers);

    Request<?, NeoInvokeFunction> invokeFunction(Hash160 contractScriptHash, String functionName,
            List<ContractParameter> params, Signer... signers);

    Request<?, NeoInvokeScript> invokeScript(String script, Signer... signers);

    Request<?, NeoInvokeContractVerify> invokeContractVerify(Hash160 contractScriptHash,
            List<ContractParameter> methodParameters, Signer... signers);

    Request<?, NeoGetUnclaimedGas> getUnclaimedGas(Hash160 address);

    //endregion

    //region Utilities Methods

    Request<?, NeoListPlugins> listPlugins();

    Request<?, NeoValidateAddress> validateAddress(String address);

    //endregion

    //region Wallet Methods

    Request<?, NeoCloseWallet> closeWallet();

    Request<?, NeoOpenWallet> openWallet(String walletPath, String password);

    Request<?, NeoDumpPrivKey> dumpPrivKey(Hash160 scriptHash);

    Request<?, NeoGetWalletBalance> getWalletBalance(Hash160 assetId);

    Request<?, NeoGetNewAddress> getNewAddress();

    Request<?, NeoGetWalletUnclaimedGas> getWalletUnclaimedGas();

    Request<?, NeoImportPrivKey> importPrivKey(String privateKeyInWIF);

    Request<?, NeoCalculateNetworkFee> calculateNetworkFee(String transactionHex);

    Request<?, NeoListAddress> listAddress();

    Request<?, NeoSendFrom> sendFrom(Hash160 tokenHash, Hash160 from, Hash160 to,
            BigInteger amount);

    Request<?, NeoSendFrom> sendFrom(Hash160 from, TransactionSendAsset txSendAsset);

    Request<?, NeoSendMany> sendMany(List<TransactionSendAsset> txSendAsset);

    Request<?, NeoSendMany> sendMany(Hash160 from, List<TransactionSendAsset> txSendAsset);

    Request<?, NeoSendToAddress> sendToAddress(Hash160 assetId, Hash160 to, BigInteger amount);

    Request<?, NeoSendToAddress> sendToAddress(TransactionSendAsset txSendAsset);

    //endregion

    //region RpcNep17Tracker

    Request<?, NeoGetNep17Balances> getNep17Balances(Hash160 scriptHash);

    Request<?, NeoGetNep17Transfers> getNep17Transfers(Hash160 scriptHash);

    Request<?, NeoGetNep17Transfers> getNep17Transfers(Hash160 scriptHash, Date until);

    Request<?, NeoGetNep17Transfers> getNep17Transfers(Hash160 scriptHash, Date from, Date to);

    //endregion

    //region ApplicationLogs

    Request<?, NeoGetApplicationLog> getApplicationLog(Hash256 txId);

    //endregion

    //region StateService

    Request<?, NeoGetStateRoot> getStateRoot(BigInteger blockIndex);

    Request<?, NeoGetProof> getProof(Hash256 rootHash, Hash160 contractScriptHash,
            String storageKeyHex);

    Request<?, NeoVerifyProof> verifyProof(Hash256 rootHash, String proofDataHex);

    Request<?, NeoGetStateHeight> getStateHeight();

    //endregion

}
