package com.example.ipcalink.encryptedSharedPreferences


import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys


lateinit var encryptedSharedPreferences : SharedPreferences


class ESP {

    val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    //Here i encrypt the Shared Preferences with the AES algorithm
    //The encrypted Shared Preferences when used will automatically Decrypt the data
    constructor(context: Context){
        encryptedSharedPreferences = EncryptedSharedPreferences.create(
            "encrypted_preferences", // fileName
            masterKeyAlias, // masterKeyAlias
            context, // context
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, // prefKeyEncryptionScheme
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM // prefvalueEncryptionScheme
        )
    }

    //Here i save the keys this way "GroupId - key value"
    //i use the group name so i can identify the keys
    var keysPref : MutableSet<String>
        get() = encryptedSharedPreferences.getStringSet("KEY_PREF", HashSet<String>()) as MutableSet<String>
        set(value) {
            val editor: SharedPreferences.Editor = encryptedSharedPreferences.edit()
            editor.putStringSet("KEY_PREF", HashSet<String>(value))
            editor.apply()
        }

}