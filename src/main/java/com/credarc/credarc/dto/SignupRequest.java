package com.credarc.credarc.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class SignupRequest {

    @NotBlank(message = "Name cannot be empty.")
    @Size(min = 2 , max = 30)
    private String name;

    @NotBlank(message = "Email cannot be empty.")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Mobile number cannot be empty.")
    @Pattern(regexp = "\\d{10}", message = "Mobile number must contain exactly 10 digits")
    private String mobile;

    @NotBlank(message = "Password cannot be empty.")
    @Size(min = 8,message = "Password must be at least 8 characters")
    private String password;

    /** Getters **/

    public String getEmail() {
        return email;
    }

    public String getName(){
        return name;
    }

    public String getMobile() {
        return mobile;
    }

    public String getPassword(){ return password; }



    /** Setters **/

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public void setEmail(String email) {
        this.email = email == null ? null : email.trim();
    }

    public void setPassword(String password) {
        this.password = password == null ? null : password.trim();
    }

    public void setMobile(String mobile) {
        this.mobile = mobile == null ? null : mobile.trim();
    }
}

