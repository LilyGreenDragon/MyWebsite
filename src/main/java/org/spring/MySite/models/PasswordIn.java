package org.spring.MySite.models;

public class PasswordIn {
    private static  String passwordReg="3011";

    public String getPasswordReg() {
        return passwordReg;
    }

    public void setPasswordReg(String passwordReg) {
        this.passwordReg = passwordReg;
    }

    public void print (){
        System.out.println(passwordReg);
    }

}
