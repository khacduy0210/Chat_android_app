package com.example.chat.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class User(val uid: String, val username: String, val profileImageUrl: String,val password: String): Parcelable{
    constructor() : this("","","","")
}