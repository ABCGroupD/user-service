package com.profatar.user.controller;

import com.profatar.user.model.form.ProfileRequest;
import com.profatar.user.service.profile.ProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profiles")
public class ProfileController {

    private static final Logger LOG = LoggerFactory.getLogger(ProfileController.class);
    @Autowired
    private ProfileService profileService;

    @PostMapping(path = "/profile", consumes = {"multipart/form-data"})
    public ResponseEntity<?> create(@ModelAttribute ProfileRequest request) {
        try {
            return new ResponseEntity(
                    profileService.create(request),
                    HttpStatus.CREATED
            );
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/asset/{assetName}")
    public ResponseEntity findAsset(@PathVariable String assetName) {
        try {
            return new ResponseEntity(
                    profileService.find(assetName),
                    HttpStatus.OK
            );
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage());
            return new ResponseEntity(
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

}
