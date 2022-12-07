package com.profatar.user.model.form;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;


@AllArgsConstructor
@Data
@NoArgsConstructor
public class ProfileRequest {
    private String address;
    private String fullName;
    private String emailAddress;
    private String phoneNumber;
    private MultipartFile image;
}
