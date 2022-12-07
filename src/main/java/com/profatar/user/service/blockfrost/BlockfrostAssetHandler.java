package com.profatar.user.service.blockfrost;

import com.bloxbean.cardano.client.api.exception.ApiException;
import com.bloxbean.cardano.client.api.helper.FeeCalculationService;
import com.bloxbean.cardano.client.api.helper.TransactionHelperService;
import com.bloxbean.cardano.client.api.helper.model.TransactionResult;
import com.bloxbean.cardano.client.api.model.Result;
import com.bloxbean.cardano.client.backend.api.AssetService;
import com.bloxbean.cardano.client.backend.api.BackendService;
import com.bloxbean.cardano.client.backend.api.BlockService;
import com.bloxbean.cardano.client.backend.api.TransactionService;
import com.bloxbean.cardano.client.backend.blockfrost.service.BFBackendService;
import com.bloxbean.cardano.client.backend.model.Block;
import com.bloxbean.cardano.client.backend.model.TransactionContent;
import com.bloxbean.cardano.client.common.model.Networks;
import com.bloxbean.cardano.client.crypto.SecretKey;
import com.bloxbean.cardano.client.crypto.VerificationKey;
import com.bloxbean.cardano.client.exception.AddressExcepion;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.metadata.Metadata;
import com.bloxbean.cardano.client.metadata.cbor.CBORMetadata;
import com.bloxbean.cardano.client.metadata.cbor.CBORMetadataMap;
import com.bloxbean.cardano.client.transaction.model.MintTransaction;
import com.bloxbean.cardano.client.transaction.model.TransactionDetailsParams;
import com.bloxbean.cardano.client.transaction.spec.Asset;
import com.bloxbean.cardano.client.transaction.spec.MultiAsset;
import com.bloxbean.cardano.client.transaction.spec.Policy;
import com.bloxbean.cardano.client.transaction.spec.script.ScriptPubkey;
import com.bloxbean.cardano.client.util.HexUtil;
import com.bloxbean.cardano.client.util.JsonUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.profatar.user.constants.Constants;
import com.profatar.user.model.ProfatarAccount;
import com.profatar.user.model.Profile;
import com.profatar.user.model.ProfileDTO;
import com.profatar.user.model.form.ProfileRequest;
import io.blockfrost.sdk.api.IPFSService;
import io.blockfrost.sdk.api.exception.APIException;
import io.blockfrost.sdk.api.model.ipfs.IPFSObject;
import io.blockfrost.sdk.api.model.ipfs.PinResponse;
import io.blockfrost.sdk.impl.IPFSServiceImpl;
import lombok.Getter;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Getter
@Service
public class BlockfrostAssetHandler implements AssetHandler {

    public Constants constants = new Constants();
    private static final Logger LOG = LoggerFactory.getLogger(BlockfrostAssetHandler.class);
    private BackendService backendService = new BFBackendService(
            com.bloxbean.cardano.client.backend
                    .blockfrost.common.Constants.BLOCKFROST_PREVIEW_URL,
            constants.getBlockfrostProjectID()
    );
    private IPFSService ipfsService =
            new IPFSServiceImpl(
                    io.blockfrost.sdk.api.util.Constants.BLOCKFROST_IPFS_URL,
                    constants.getIpfsProjectID()
            );
    private FeeCalculationService feeCalculationService = backendService.getFeeCalculationService();
    private TransactionHelperService transactionHelperService = backendService.getTransactionHelperService();
    private TransactionService transactionService = backendService.getTransactionService();
    private BlockService blockService = backendService.getBlockService();
    private AssetService assetService = backendService.getAssetService();

    public long getTtl() throws ApiException {
        Block block = blockService.getLatestBlock().getValue();
        long slot = block.getSlot();
        return slot + 2000;
    }

    public void waitForTransaction(Result<TransactionResult> result) {
        try {
            if (result.isSuccessful()) { //Wait for transaction to be mined
                int count = 0;
                while (count < 180) {
                    Result<TransactionContent> txnResult = transactionService.getTransaction(result.getValue().getTransactionId());
                    if (txnResult.isSuccessful()) {
                        LOG.info(JsonUtil.getPrettyJson(txnResult.getValue()));
                        break;
                    } else {
                        LOG.info("Waiting for transaction to be processed ....");
                    }

                    count++;
                    Thread.sleep(2000);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void waitForTransactionHash(Result<String> result) {
        try {
            if (result.isSuccessful()) { //Wait for transaction to be mined
                int count = 0;
                while (count < 180) {
                    Result<TransactionContent> txnResult = transactionService.getTransaction(result.getValue());
                    if (txnResult.isSuccessful()) {
                        LOG.info(JsonUtil.getPrettyJson(txnResult.getValue()));
                        break;
                    } else {
                        LOG.info("Waiting for transaction to be processed ....");
                    }

                    count++;
                    Thread.sleep(2000);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public Profile create(ProfileRequest request) throws CborSerializationException, APIException, IOException, ApiException, AddressExcepion {
        String ipfsHash = saveFile(request.getImage());
        ProfatarAccount account = new ProfatarAccount(
                Networks.preview(), constants.getMainMnemonic()
        );

        SecretKey secretKey = account.getSecretKey();
        VerificationKey verificationKey = account.getVerificationKey();

        ScriptPubkey scriptPubkey = ScriptPubkey.create(verificationKey);
        Policy policy = new Policy(scriptPubkey);
        policy.addKey(secretKey);
        String policyId = policy.getPolicyId();

        MultiAsset multiAsset = new MultiAsset();
        multiAsset.setPolicyId(policyId);

        Asset asset = new Asset(constants.getProfatar(), BigInteger.ONE);
        multiAsset.getAssets().add(asset);
        String assetName = policyId + HexUtil.encodeHexString(
                constants.getProfatar().getBytes(StandardCharsets.UTF_8)
        );
        CBORMetadataMap nftInfo = new CBORMetadataMap()
                .put("name", request.getFullName())
                .put("email", request.getEmailAddress())
                .put("phoneNumber", request.getPhoneNumber())
                .put("image", "ipfs://" + ipfsHash);
        CBORMetadataMap assetMeta = new CBORMetadataMap()
                .put(constants.getProfatar(), nftInfo);
        CBORMetadataMap policyMeta = new CBORMetadataMap()
                .put(policyId, assetMeta)
                .put("version", "1.0");
        Metadata metadata = new CBORMetadata()
                .put(new BigInteger("721"), policyMeta);
        LOG.info(metadata.getData().toString());
        MintTransaction mint = MintTransaction
                .builder()
                .sender(account)
                .receiver(request.getAddress())
                .mintAssets(Arrays.asList(multiAsset))
                .policy(policy).build();

        long ttl = blockService.getLatestBlock().getValue().getSlot() + 1000;
        TransactionDetailsParams detailsParams = TransactionDetailsParams
                .builder()
                .ttl(ttl)
                .build();

        BigInteger fee = feeCalculationService.calculateFee(mint, detailsParams, metadata);
        mint.setFee(fee);

        Result<TransactionResult> result = transactionHelperService
                .mintToken(mint, detailsParams, metadata);

        if (result.isSuccessful()) {
            return new Profile(
                    assetName,
                    result.getValue().getTransactionId()
            );
        } else {
            LOG.error(result.getResponse());
            return null;
        }
    }

    @Override
    public String saveFile(MultipartFile file) throws IOException, APIException {
        String tmpDir = System.getProperty("java.io.tmpdir");
        String fileName = FilenameUtils.getName(file.getOriginalFilename());
        fileName = changeFileName(fileName);
        String tmpFilePath = tmpDir + "/" + fileName;

        byte[] bytes;
        bytes = file.getBytes();

        Path path = Files.write(Paths.get(tmpFilePath), bytes);

        LOG.info("Uploading " + tmpFilePath + "to IPFS");
        File image = path.toFile();
        IPFSObject ipfsObject = ipfsService.add(image);
        PinResponse pinResponse = ipfsService.pinAdd(ipfsObject.getIpfsHash());
        return pinResponse.getIpfsHash();
    }

    private String changeFileName(String filename) {
        String ext = FilenameUtils.getExtension(filename);
        String randName = UUID.randomUUID().toString();

        return randName + "." + ext;
    }

    @Override
    public ProfileDTO find(String assetName) throws ApiException {
        Result<com.bloxbean.cardano.client
                .backend.model.Asset> assetResult = assetService.getAsset(assetName);
        com.bloxbean.cardano.client
                .backend.model.Asset asset = assetResult.getValue();

        JsonNode metadata = asset.getOnchainMetadata();

        return new ProfileDTO(
                metadata.get("name").asText(),
                metadata.get("email").asText(),
                metadata.get("phoneNumber").asText(),
                metadata.get("image").asText()
        );
    }
}
