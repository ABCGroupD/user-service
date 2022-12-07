package com.profatar.user.service.blockfrost;

import com.bloxbean.cardano.client.api.exception.ApiException;
import com.bloxbean.cardano.client.exception.AddressExcepion;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.profatar.user.model.Profile;
import com.profatar.user.model.ProfileDTO;
import com.profatar.user.model.form.ProfileRequest;
import io.blockfrost.sdk.api.exception.APIException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
@Service
public interface AssetHandler {
    Profile create(ProfileRequest request) throws CborSerializationException, APIException, IOException, ApiException, AddressExcepion;
    String saveFile(MultipartFile file) throws IOException, APIException;
    ProfileDTO find(String assetName) throws APIException, ApiException;
}
