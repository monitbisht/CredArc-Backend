package com.credarc.credarc.dto;

public class AccountCreationRequest {

  private String name;
  private String email;
  private String mobile;
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

    public String getPassword() {
        return password;
    }



    /** Setters **/

    public void setName(String name) {
        if(name == null || name.trim().isEmpty())     {
            throw new IllegalArgumentException("Name cannot be empty.");
        }
        else this.name = name;
    }

    public void setEmail(String email) {
        if(email == null || email.trim().isEmpty() || !email.contains("@"))     {
            throw new IllegalArgumentException("Email cannot be empty.");
        }
        else this.email = email;
    }

    public void setPassword(String password) {
        if(name == null || name.trim().isEmpty())     {
            throw new IllegalArgumentException("Name cannot be empty.");
        }
        else this.password = password;
    }

    public void setMobile(String mobile) {
        if(name == null || name.trim().isEmpty())     {
            throw new IllegalArgumentException("Name cannot be empty.");
        }
        else this.mobile = mobile;
    }
}
