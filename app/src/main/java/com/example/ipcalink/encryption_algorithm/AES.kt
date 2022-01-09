package com.example.ipcalink.encryption_algorithm

import android.util.Base64
import java.security.*
import java.security.spec.InvalidKeySpecException
import javax.crypto.*
import javax.crypto.spec.GCMParameterSpec


object AES {

    fun generateAESKey() : ByteArray {

        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(128)


        return keyGenerator.generateKey().encoded
    }


    fun AesEncrypt(text : String, iv : ByteArray, secretKey : SecretKey) : String {

        var cipherTextString : String? = null

        try {


            //i feed ivParams to the encryption so that if two message are the same they will never have the same encryption value.
            val ivParams = GCMParameterSpec(128, iv)

            // Encrypting a text using the secret key
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParams)
            val cipherTextArray = cipher.doFinal(text.toByteArray())

            //i need to encode so i can transform the cipherTextArray into string without modifying the data
            cipherTextString = Base64.encodeToString(cipherTextArray, Base64.DEFAULT)


        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: InvalidKeyException) {
            e.printStackTrace()
        } catch (e: NoSuchPaddingException) {
            e.printStackTrace()
        } catch (e: IllegalBlockSizeException) {
            e.printStackTrace()
        } catch (e: BadPaddingException) {
            e.printStackTrace()
        } catch (e: InvalidKeySpecException) {
            e.printStackTrace()
        } catch (e: InvalidAlgorithmParameterException) {
            e.printStackTrace()
        }

        return cipherTextString!!
    }

    fun AesDecrypt(encryptedText : String, iv: ByteArray, secretKey : SecretKey) : String {

        var text : String? = null

        try {

            //i need to decode so i can transform the encryptedText into ByteArray without modifying the data
            val encryptedTextByteArray = Base64.decode(encryptedText, Base64.DEFAULT)

            //i feed ivParams to the encryption so that if two message are the same they will never have the same encryption value.
            val ivParams = GCMParameterSpec(128, iv)

            // Decrypting the encrypted text by using the secret key and AES algorithm
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParams)
            val original = cipher.doFinal(encryptedTextByteArray)

            //Decrypted text
            text = String(original)


        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: InvalidKeySpecException) {
            e.printStackTrace()
        } catch (e: InvalidKeyException) {
            e.printStackTrace()
        } catch (e: NoSuchPaddingException) {
            e.printStackTrace()
        } catch (e: IllegalBlockSizeException) {
            e.printStackTrace()
        } catch (e: BadPaddingException) {
            e.printStackTrace()
        } catch (e: InvalidAlgorithmParameterException) {
            e.printStackTrace()
        }

        return text!!
    }

    //Here i generate a random IV
    fun GeneratingRandomIv() : ByteArray {

        //I generate a random IV byteArray with 12 bytes using the SHA1PRNG algorithm
        val randomSecureRandom = SecureRandom.getInstance("SHA1PRNG")
        val iv = ByteArray(12)
        //this generates a random IV
        randomSecureRandom.nextBytes(iv)

        return iv
    }
}