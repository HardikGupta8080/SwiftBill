package com.example.swiftbill.model

class User {
    var name:String?=null
    var email:String?=null
    var password:String?=null

    constructor(name: String?, email: String?, passwoard: String?) {
        this.name = name
        this.email = email
        this.password = passwoard
    }
    constructor(email: String?, passwoard: String?) {
        this.email = email
        this.password = passwoard
    }
}