package com.profatar.user.service.profile;

import com.bloxbean.cardano.client.api.exception.ApiException;
import com.bloxbean.cardano.client.exception.AddressExcepion;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.profatar.user.model.Profile;
import com.profatar.user.model.ProfileDTO;
import com.profatar.user.model.form.ProfileRequest;
import com.profatar.user.service.blockfrost.AssetHandler;
import io.blockfrost.sdk.api.exception.APIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class BlockfrostProfileService implements ProfileService {

    @Autowired
    private AssetHandler assetHandler;

    @Override
    public Profile create(ProfileRequest request) throws
            APIException, AddressExcepion, IOException,
            CborSerializationException, ApiException {
        return assetHandler.create(request);
    }

    @Override
    public ProfileDTO find(String assetName) throws APIException, ApiException {
        return assetHandler.find(assetName);
    }

}
