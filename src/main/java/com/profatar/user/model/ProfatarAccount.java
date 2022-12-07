package com.profatar.user.model;

import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.api.exception.ApiException;
import com.bloxbean.cardano.client.api.helper.model.TransactionResult;
import com.bloxbean.cardano.client.api.model.Result;
import com.bloxbean.cardano.client.common.model.Network;
import com.bloxbean.cardano.client.crypto.SecretKey;
import com.bloxbean.cardano.client.crypto.VerificationKey;
import com.bloxbean.cardano.client.exception.AddressExcepion;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.transaction.model.MintTransaction;
import com.bloxbean.cardano.client.transaction.model.TransactionDetailsParams;
import com.bloxbean.cardano.client.transaction.spec.MultiAsset;
import com.bloxbean.cardano.client.transaction.spec.Policy;
import com.bloxbean.cardano.client.transaction.spec.script.ScriptPubkey;
import com.profatar.user.service.blockfrost.BlockfrostAssetHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Arrays;

public class ProfatarAccount extends Account {
    private static final Logger LOG = LoggerFactory.getLogger(ProfatarAccount.class);

    public ProfatarAccount() {}
    public ProfatarAccount(Network network) {
        super(network, 0);
    }

    public ProfatarAccount(Network network, String mnemonic) {
        super(network, mnemonic, 0);
    }

    public SecretKey getSecretKey(){
        /**
         * Creates Secret key from private key
         * */
        SecretKey sKey = null;
        try {
            sKey = SecretKey.create(this.privateKeyBytes());
        } catch (CborSerializationException e) {
            LOG.error(String.valueOf(e));
        }
        return sKey;
    }

    public VerificationKey getVerificationKey(){
        /**
         * Creates verification from public key
         * */
        VerificationKey vKey = null;
        try {
            vKey = VerificationKey.create(this.publicKeyBytes());
        } catch (CborSerializationException e) {
            LOG.error(String.valueOf(e));
        }
        return vKey;
    }

    public String getPolicyId(ScriptPubkey scriptPubkey) {
        String policyId = "";
        try {
            policyId = scriptPubkey.getPolicyId();
        } catch (CborSerializationException e) {
            LOG.error(String.valueOf(e));
        }
        return policyId;
    }

    public TransactionDetailsParams getDetailParams(BlockfrostAssetHandler baseService){
        TransactionDetailsParams detailsParams = null;
        try {
            long ttl = baseService.getTtl();
            detailsParams = TransactionDetailsParams.builder()
                    .ttl(ttl)
                    .build();
        } catch (ApiException e) {
            LOG.error(String.valueOf(e));
        }
        return detailsParams;
    }

    public BigInteger getCalculatedFee(
            BlockfrostAssetHandler baseService,
            MintTransaction mintTransaction,
            TransactionDetailsParams detailsParams) {
        BigInteger fee = null;
        try {
            fee = baseService.getFeeCalculationService().calculateFee(mintTransaction, detailsParams, null);
        } catch (ApiException e) {
            LOG.error(String.valueOf(e));
        } catch (CborSerializationException e) {
            LOG.error(String.valueOf(e));
        } catch (AddressExcepion e) {
            LOG.error(String.valueOf(e));
        }
        return fee;
    }

    public Result<TransactionResult> getResult(BlockfrostAssetHandler baseService, MintTransaction mintTransaction) {
        Result<TransactionResult> result = null;
        try {
            result = baseService.getTransactionHelperService().mintToken(mintTransaction,
                    TransactionDetailsParams.builder().build());
        } catch (AddressExcepion e) {
            LOG.error(String.valueOf(e));
        } catch (ApiException e) {
            LOG.error(String.valueOf(e));
        } catch (CborSerializationException e) {
            LOG.error(String.valueOf(e));
        }
        return result;
    }

    public MintTransaction getMintedTransaction(ProfatarAccount sender, ProfatarAccount UserAccount, MultiAsset multiAsset, Policy policy){
        MintTransaction mintTransaction =
                MintTransaction.builder()
                        .sender(sender)
                        .receiver(UserAccount.baseAddress())
                        .mintAssets(Arrays.asList(multiAsset))
                        .policy(policy)
                        .build();
        return mintTransaction;
    }

}
