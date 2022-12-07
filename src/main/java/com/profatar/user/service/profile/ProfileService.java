package com.profatar.user.service.profile;

import com.bloxbean.cardano.client.api.exception.ApiException;
import com.bloxbean.cardano.client.exception.AddressExcepion;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.profatar.user.model.Profile;
import com.profatar.user.model.ProfileDTO;
import com.profatar.user.model.form.ProfileRequest;
import io.blockfrost.sdk.api.exception.APIException;

import java.io.IOException;

public interface ProfileService {
    Profile create(ProfileRequest request) throws APIException, AddressExcepion, IOException, CborSerializationException, ApiException;
    ProfileDTO find(String assetName) throws ApiException, APIException;
}
