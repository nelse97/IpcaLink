package com.example.ipcalink.models


import android.content.Context
import android.util.Base64
import com.example.ipcalink.encryption_algorithm.AES.AesDecrypt
import com.example.ipcalink.encryptedSharedPreferences.ESP
import com.google.firebase.firestore.QueryDocumentSnapshot
import javax.crypto.spec.SecretKeySpec

class Notification {
    var id : String? = null
    var title : String? = null
    var body : String? = null
    //var secretKey : String? = null
    var iv : String? = null
    var sendDate : String? = null
    var senderId : String? = null

    constructor(
        id: String?,
        title: String?,
        body: String?,
        iv: String?,
        sendDate: String?,
        senderId: String?
    ) {
        this.id = id
        this.title = title
        this.body = body
        this.iv = iv
        this.sendDate = sendDate
        this.senderId = senderId
    }


    fun toHash() : HashMap<String, Any>{
        val hashMap = HashMap<String, Any>()

        hashMap["id"] = id!!
        hashMap["title"] = title!!
        hashMap["body"] = body!!
        hashMap["iv"] = iv!!
        hashMap["sendDate"] = sendDate!!
        hashMap["senderId"] = senderId!!

        return hashMap
    }

    companion object {
        fun fromHash(hashMap: QueryDocumentSnapshot, context: Context): Notification {


            /*val notifId = hashMap["id"] as String

            val keys = ESP(context).keysPref

            var secretKeyString = hashMap["secretKey"] as String?

            //If there is a secret key in the notification i save it
            // and then i delete in from the databases

            if(secretKeyString != null && secretKeyString != ""){

                keys.add("chatId - $secretKeyString")
                keys.add(secretKeyString)

                ESP(context).keysPref = keys

                //Once the user get the secret key i have to remove it from the database
                //databaseSecretKeyRemoval(secretKeyString)
            }

            var secretKeyPosition : Int

            var a = 0
            var b = 0

            //Here i search for the key that corresponds to the group
            for (k in keys){
                if (k.contains(notifId)){
                    val key = k.removePrefix("$notifId - ")
                    secretKeyString = key
                }
            }

            val secretKeyBytes = Base64.decode(secretKeyString, Base64.DEFAULT)

            //Rebuilding the Secret key from the bytes array
            val secretKey =
                SecretKeySpec(secretKeyBytes, 0, secretKeyBytes.size, "AES")

            val ivString = hashMap["iv"] as String
            val iv = Base64.decode(ivString, Base64.DEFAULT)

            val title = AesDecrypt(hashMap["title"] as String, iv, secretKey)
            val body = AesDecrypt(hashMap["body"] as String, iv, secretKey)*/

            return Notification(
                hashMap["id"] as String,
                hashMap["title"] as String,
                hashMap["body"] as String,
                hashMap["iv"] as String,
                hashMap["sendDate"] as String,
                hashMap["senderId"] as String
            )
        }
    }
}
