package com.example.dispatchsystem.model;

public class Globals {
    public static void setCurrentUser(User currentUser) {
        if(currentUser.getFirstName() == null) {
            currentUser.setFirstName("");
        }
        if(currentUser.getLastName() == null) {
            currentUser.setLastName("");
        }
        if(currentUser.getEmail() == null) {
            currentUser.setEmail("");
        }
        if(currentUser.getPassword() == null) {
            currentUser.setPassword("");
        }
        if(currentUser.getAge() == null) {
            currentUser.setAge("");
        }
        if(currentUser.getCnp() == null) {
            currentUser.setCnp("");
        }
        if(currentUser.getSex() == null) {
            currentUser.setSex("");
        }
        if(currentUser.getBloodGroup() == null) {
            currentUser.setBloodGroup("");
        }

        Globals.currentUser = currentUser;
    }

    public static User currentUser;
}
