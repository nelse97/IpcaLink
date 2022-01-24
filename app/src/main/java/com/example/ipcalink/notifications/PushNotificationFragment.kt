package com.example.ipcalink.notifications

import android.annotation.SuppressLint
import android.content.ContentValues
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.ipcalink.FcmToken
import com.example.ipcalink.encryption_algorithm.AES.GeneratingRandomIv
import com.example.ipcalink.databinding.FragmentPushNotificationBinding
import com.example.ipcalink.encryptedSharedPreferences.ESP
import com.example.ipcalink.encryption_algorithm.AES.AesEncrypt
import com.example.ipcalink.models.Notification
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedOutputStream
import java.io.BufferedWriter
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import javax.net.ssl.HttpsURLConnection

const val KEY = "AAAAfFA9oPI:APA91bHkqDvaPJ0RLIFd_Txz6yLoumLl_qsBmd61DeuwQuzs0bswl1yQpCQgapoPulTtGaa3FtBhLNSpig43aPebEb4ACodEbHP7ATUNePkvjT2q-Q74SqU2K2HNVZqpLuE2MSKvm_wR"
const val senderId = "533922160882"

class PushNotificationFragment : Fragment() {

    private var _binding: FragmentPushNotificationBinding? = null
    private val binding get() = _binding!!

    //Devices to be added to a group
    private var device1Token : String = "d-NN8p1oTZqb8rc3gAXJYp:APA91bFNx3wtLpSAp04wQwYG0UdYIzjpKpUKwkb1zSViB81gr2Pz7wNOyIctvNrYH22Lf-dJD_CKk5c5kmFEyBoMTbdaAXE6IGiHxulibUCaECSlNRagfs0H9vXOZG3RAC2eUNWugfuB"
    private var device2Token : String = "cjb2nyKXSXe52aDkAfQBxU:APA91bEW6gmAWzoc6s2Hs22hX1e55Co1PA8eeN8b71PPeWyZge-y-qfVrF6_e90yRlfXrTsIwK7PUDArwm2S3SUdHlBisv5aMSDaklc90CD_lEyfiihbEp9AUEQ4aMvBBzoVLP1jknJi"

    //Notification Group Info
    private var notificationKey : String = "APA91bGmGPmuwTdidq_lmVwQoOnYe6oyosl8_TF1P1SHEqFNgU0R3oryGWriZIi8oyFzdZtFYkc6acAs7T9ULYtUoGwCURjSxJxBQhcBtwtcbuaUCDtCJ34"
    lateinit var notificationKeyName : String

    //private var fcmToken : String = "cbTnqtSCQ66yt7ZMj6qaMU:APA91bGuUEHDHr3YcqxPqq_VsyV8C_guLdARu0hhziThefQLYovzKGb7MjiBN5108YhiCr_e6gft66d2G2XUyeQxEqlrvvSVV8EjJ8DYTbH1oBAtyier1a_BkAX881AIiZmnbEPlTMuZ"

    private val dbFirebase = Firebase.firestore

    /*val encryptedTitle : ByteArray? = null
    val encryptedMessage : ByteArray? = null*/

    private val userUID = Firebase.auth.uid


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPushNotificationBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Hides top bar
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()

        val list : List<String> = listOf(device1Token, device2Token)

        val registrationIds = JSONArray(list)

        //This needs to be execute when a user creates a group

        //Here i generate an encrypted key and save it in encrypted shared preferences
        /*val secretKey = AES.generateAESKey()
        val secretKeyString = Base64.encodeToString(secretKey, Base64.DEFAULT)
        val keySet = ESP(this).keysPref
        keySet.add("ka4vgKgo8QzsVkdn5brt - $secretKeyString")
        ESP(this).keysPref = keySet*/


        binding.buttonPushNotif.setOnClickListener {

            val title = binding.editTextTitle.text.toString()
            val message = binding.editTextMessage.text.toString()

            //Here i get the notification key name
            //getNotificationKeyName()

            GlobalScope.launch(Dispatchers.IO) {

                //I create a notification group
                //createNotificationGroup("Aplicacoes Moveis S77po7vNGjtKja2Rinyb", registrationIds)
                //createNotificationGroup("hhhhghgh", registrationIds)
                //I add elements to a notification group
                /*addElementsToGroup("Aplicacoes Moveis S77po7vNGjtKja2Rinyb",
                    notificationKey,
                    registrationIds)*/

                //I remove elements from a notification group
                /*removeElementsFromGroup("Aplicacoes Moveis awdnh7weh392",
                                        notificationKey,
                                        registrationIds)*/


                //I will use this code to encrypt data

                //I generate a random iv
                /*val iv = GeneratingRandomIv()

                //I have to get all the key and search for the key that corresponds to the group where the user is situated
                val keys = ESP(requireContext()).keysPref
                var secretKeyString = ""
                val correspondingGroupId = "ka4vgKgo8QzsVkdn5brt"


                //I search for the key that corresponds to the group
                for (k in keys){
                    if (k.contains(correspondingGroupId)){
                        val key = k.removePrefix("$correspondingGroupId - ")
                        secretKeyString = key
                    }
                }

                //I get the key as bytes array and then rebuild the Secret key from the bytes array
                val secretKeyBytes = Base64.decode(secretKeyString, Base64.DEFAULT)

                val secretKey : SecretKey =
                    SecretKeySpec(secretKeyBytes, 0, secretKeyBytes.size, "AES")


                //I encrypt the data that the user is sending
                //val encryptedTitle = AesEncrypt(title, iv, secretKey)
                val encryptedMessage = AesEncrypt(message, iv, secretKey)

                //I need the iv so the user can decrypt the message in the future
                val ivTitleString = Base64.encodeToString(iv, Base64.DEFAULT)*/

                val chatGroupIcon = ""
                val chatId = ""
                //Here I should encrypt the message and then upon the user receiving the notification, i should decrypt it
                //I send a notification to a group of users
                sendNotificationToGroup(title, message, chatGroupIcon, chatId,  notificationKey)


                //I send a notification to a user
                //sendNotificationToUser(title, message, fcmToken)


                //I save a message that has been send from a group to the firebase
                //sendNotificationToFirebase(title, message, ivTitleString, userUID!!)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
    }

    private suspend fun sendNotificationToUser(title: String, message: String, fcmToken : String) {
        delay(1000)

        try {

            //The url of the API i want to access (Firebase Cloud Messaging)
            val url = URL("https://fcm.googleapis.com/fcm/send")

            //Establish a connection to fcm (Firebase Cloud Messaging) so i can send a push notification to a specific topic
            val httpsURLConnection: HttpsURLConnection =
                url.openConnection() as HttpsURLConnection

            //Here i configure the connection to fcm
            //The time available to read from the input stream when the connection is established
            httpsURLConnection.readTimeout = 10000
            //The time available to connect to the url
            httpsURLConnection.connectTimeout = 15000
            //Defining the type of request to be made to the fcm
            httpsURLConnection.requestMethod = "POST"
            //Defining that the url connection can be used to send and receive data
            httpsURLConnection.doInput = true
            httpsURLConnection.doOutput = true

            //Here i give my server key so i can make a request to fcm (Firebase Cloud Messaging) of my application (FirebaseDemo)
            //and i define as well the type of content that i will be sending (json object)
            httpsURLConnection.setRequestProperty("authorization", "key=$KEY")
            httpsURLConnection.setRequestProperty("Content-Type", "application/json")

            val body = JSONObject()
            val data = JSONObject()

            data.put("title", title)
            data.put("content", message)
            data.put("click_action", ".LoginActivity")

            //val condition = "'$TOPIC1' in topics && '$TOPIC2' in topics && '$TOPIC3' in topics"
            //body.put("condition", condition)

            //here i define the body of the post request
            body.put("data", data)
            //Here i define the group via notification key in which i want to send the notification/message
            body.put("to", fcmToken)

            val outputStream: OutputStream =
                BufferedOutputStream(httpsURLConnection.outputStream)
            val writer = BufferedWriter(OutputStreamWriter(outputStream, "utf-8"))

            //here i write the body of the post request and then i send the request
            //then i close the post request
            writer.write(body.toString())
            writer.flush()
            writer.close()

            outputStream.close()

            //The response code and message of the post requests
            val responseCode: Int = httpsURLConnection.responseCode
            val responseMessage: String = httpsURLConnection.responseMessage


            Log.d(TAG, "Response from sendMes: $responseCode $responseMessage")


            // Check if the connection is successful
            if (responseCode in 400..499) {
                httpsURLConnection.errorStream
            } else {
                httpsURLConnection.inputStream
            }

            if (responseCode == 200) {
                Log.e(
                    TAG,
                    "Notification Sent Title: $title Body: $message to user $fcmToken"
                )

            } else {
                Log.e(TAG, "Notification Error")
            }

            httpsURLConnection.disconnect()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //This function sends push notifications to devices that are subscribed to a specific topic
    private suspend fun sendNotificationToGroup(title: String, message: String, chatGroupIcon : String, chatId : String, notificationKey : String) {

        delay(1500)

        try {

            //The url of the API i want to access (Firebase Cloud Messaging)
            val url = URL("https://fcm.googleapis.com/fcm/send")

            //Establish a connection to fcm (Firebase Cloud Messaging) so i can send a push notification to a specific topic
            val httpsURLConnection: HttpsURLConnection =
                url.openConnection() as HttpsURLConnection

            //Here i configure the connection to fcm
            //The time available to read from the input stream when the connection is established
            httpsURLConnection.readTimeout = 10000
            //The time available to connect to the url
            httpsURLConnection.connectTimeout = 15000
            //Defining the type of request to be made to the fcm
            httpsURLConnection.requestMethod = "POST"
            //Defining that the url connection can be used to send and receive data
            httpsURLConnection.doInput = true
            httpsURLConnection.doOutput = true

            //Here i give my server key so i can make a request to fcm (Firebase Cloud Messaging) of my application (FirebaseDemo)
            //and i define as well the type of content that i will be sending (json)
            httpsURLConnection.setRequestProperty("authorization", "key=$KEY")
            httpsURLConnection.setRequestProperty("Content-Type", "application/json")

            val body = JSONObject()
            val data = JSONObject()

            data.put("title", title)
            data.put("content", message)
            //Here i define the Activity in which i want the user to navigate when they click the notification
            data.put("click_action", ".LoginActivity")
            data.put("icon", chatGroupIcon)
            data.put("chat_id", chatId)


            //here i define the body of the post request
            body.put("data", data)
            //Here i define the group via notification key in which i want to send the notification/message
            body.put("to", notificationKey)

            val outputStream: OutputStream =
                BufferedOutputStream(httpsURLConnection.outputStream)
            val writer = BufferedWriter(OutputStreamWriter(outputStream, "utf-8"))

            //here i write the body of the post request and then i send the request
            //then i close the post request
            writer.write(body.toString())
            writer.flush()
            writer.close()

            outputStream.close()

            //The response code and message of the post requests
            val responseCode: Int = httpsURLConnection.responseCode
            val responseMessage: String = httpsURLConnection.responseMessage


            Log.d(TAG, "Response from sendMes: $responseCode $responseMessage")


            // Check if the connection is successful or not
            if (responseCode in 400..499) {
                httpsURLConnection.errorStream
            } else {
                httpsURLConnection.inputStream
            }
            if (responseCode == 200) {
                Log.e(
                    TAG,
                    "Notification Sent \n Title: $title \n Body: $message"
                )
            } else {
                Log.e(TAG, "Notification Error")
            }

            httpsURLConnection.disconnect()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getNotificationKeyName() {
        dbFirebase.collection("chats").
        document("S77po7vNGjtKja2Rinyb").
        get().addOnCompleteListener {
            if(it.isSuccessful) {
                val result = it.result
                notificationKeyName = result!!["notificationName"].toString()
                println("Notification Key Name")
                println(notificationKeyName)
            }
        }
    }


    private suspend fun createNotificationGroup(notificationKeyName : String, registrationIds : JSONArray)  {

        delay(1000)

        try {
            //The url of the API i want to access (Firebase Cloud Messaging)
            val endPoint = URL("https://fcm.googleapis.com/fcm/notification")

            //Establish a connection to fcm (Firebase Cloud Messaging) so i can send a push notification to a specific topic
            val httpsURLConnection: HttpsURLConnection =
                endPoint.openConnection() as HttpsURLConnection

            //Here i configure the connection to fcm
            //The time available to read from the input stream when the connection is established
            httpsURLConnection.readTimeout = 10000
            //The time available to connect to the url
            httpsURLConnection.connectTimeout = 15000
            //Defining the type of request to be made to the fcm
            httpsURLConnection.requestMethod = "POST"
            //Defining that the url connection can be used to send and receive data
            httpsURLConnection.doInput = true
            httpsURLConnection.doOutput = true

            //Here i give my server key so i can make a request to fcm (Firebase Cloud Messaging) of my application
            //and i define as well the type of content that i will be sending (json object)
            httpsURLConnection.setRequestProperty("Content-Type", "application/json")
            httpsURLConnection.setRequestProperty("authorization", "key=$KEY")
            httpsURLConnection.setRequestProperty("project_id", senderId)

            val json = JSONObject()

            //Here i need to verify if the chat has already been created
            //----------------------------------------------------------

            //Here i define the name of the group "chatName" and
            //the fcm tokens of the users that are going to be in the group "registrationIds"
            json.put("operation", "create")
            json.put("notification_key_name", notificationKeyName)
            json.put("registration_ids", registrationIds)

            //json.put("notification_key_name", "AM_7")

            val outputStream: OutputStream =
                BufferedOutputStream(httpsURLConnection.outputStream)
            val writer = BufferedWriter(OutputStreamWriter(outputStream, "utf-8"))

            //here i write the body of the post request and then i send the request (flush)
            //then i close the post request
            writer.write(json.toString())
            writer.flush()
            writer.close()

            outputStream.close()

            //The response code and message of the post requests
            val responseCode: Int = httpsURLConnection.responseCode
            val responseMessage = httpsURLConnection.responseMessage

            Log.d(TAG, "$responseCode $responseMessage")


            // Check if the connection is successful
            if (responseCode in 400..499) {
                httpsURLConnection.errorStream
            } else {
                httpsURLConnection.inputStream
            }

            if (responseCode == 200) {
                Log.e(TAG, "Group Created!!")

                val response = httpsURLConnection.inputStream.bufferedReader()
                    .use { it.readText() }  // defaults to UTF-8
                withContext(Dispatchers.Main) {
                    // Convert raw JSON to pretty JSON using GSON library

                    //Here i get the notification_key that has been defined to the group that got created
                    val jsonObject  = JSONObject(response)
                    val notifKey = jsonObject.getString("notification_key")
                    println("NotifKey:")
                    println(notifKey)
                }
            } else {
                Log.e(TAG, "Error it didn´t work")
            }

            //Here i close the connection to the endPoint
            httpsURLConnection.disconnect()


        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun removeElementsFromGroup(notificationKeyName : String, notificationKey : String, registrationIds : JSONArray) {
        delay(1500)

        try {
            //The url of the API i want to access (Firebase Cloud Messaging)
            val endPoint = URL("https://fcm.googleapis.com/fcm/notification")

            //Establish a connection to fcm (Firebase Cloud Messaging) so i can remove elements to a specific group
            val httpsURLConnection: HttpsURLConnection =
                endPoint.openConnection() as HttpsURLConnection

            //Here i configure the connection to fcm
            //The time available to read from the input stream when the connection is established
            httpsURLConnection.readTimeout = 10000
            //The time available to connect to the url
            httpsURLConnection.connectTimeout = 15000
            //Defining the type of request to be made to the fcm
            httpsURLConnection.requestMethod = "POST"
            //Defining that the url connection can be used to send and receive data
            httpsURLConnection.doInput = true
            httpsURLConnection.doOutput = true

            //Here i give my server key so i can make a request to fcm (Firebase Cloud Messaging) of my application (FirebaseDemo)
            //and i define as well the type of content that i will be sending (json object)
            httpsURLConnection.setRequestProperty("Content-Type", "application/json")
            httpsURLConnection.setRequestProperty("authorization", "key=$KEY")
            httpsURLConnection.setRequestProperty("project_id", senderId)

            val json = JSONObject()

            println("jsonArray")
            println(registrationIds)


            //Here i define the user i want to remove from the group
            json.put("operation", "remove")
            json.put("notification_key_name", notificationKeyName)
            json.put("notification_key", notificationKey)
            json.put("registration_ids", registrationIds)


            val outputStream: OutputStream =
                BufferedOutputStream(httpsURLConnection.outputStream)
            val writer = BufferedWriter(OutputStreamWriter(outputStream, "utf-8"))

            //here i write the body of the post request and then i send the request (flush)
            //then i close the post request
            writer.write(json.toString())
            writer.flush()
            writer.close()

            outputStream.close()

            //The response code and message of the post requests
            val responseCode: Int = httpsURLConnection.responseCode
            val responseMessage = httpsURLConnection.responseMessage

            Log.d(TAG, "$responseCode $responseMessage")


            // Check if the connection is successful
            if (responseCode in 400..499) {
                httpsURLConnection.errorStream
            } else {
                httpsURLConnection.inputStream
            }

            if (responseCode == 200) {
                Log.e(TAG, "Elements have been successfully remove from the group")

                val response = httpsURLConnection.inputStream.bufferedReader()
                    .use { it.readText() }  // defaults to UTF-8
                withContext(Dispatchers.Main) {
                    // Convert raw JSON to pretty JSON using GSON library

                    //Here i get the notification_key that has been defined to the group that got created
                    val jsonObject  = JSONObject(response)
                    val notifKey_ = jsonObject.getString("notification_key")

                    println("NotifKey:")
                    println(notifKey_)
                }
            } else {
                Log.e(TAG, "Error couldn't remove elements from the group")
            }

            //Here i close the connection to the endPoint
            httpsURLConnection.disconnect()


        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun addElementsToGroup(notificationKeyName : String, notificationKey : String, registrationIds : JSONArray) {
        delay(1000)

        try {
            //The url of the API i want to access (Firebase Cloud Messaging)
            val endPoint = URL("https://fcm.googleapis.com/fcm/notification")

            //Establish a connection to fcm (Firebase Cloud Messaging) so i can add elements to a specific group
            val httpsURLConnection: HttpsURLConnection =
                endPoint.openConnection() as HttpsURLConnection

            //Here i configure the connection to fcm
            //The time available to read from the input stream when the connection is established
            httpsURLConnection.readTimeout = 10000
            //The time available to connect to the url
            httpsURLConnection.connectTimeout = 15000
            //Defining the type of request to be made to the fcm
            httpsURLConnection.requestMethod = "POST"
            //Defining that the url connection can be used to send and receive data
            httpsURLConnection.doInput = true
            httpsURLConnection.doOutput = true

            //Here i give my server key so i can make a request to fcm (Firebase Cloud Messaging) of my application (FirebaseDemo)
            //and i define as well the type of content that i will be sending (json object)
            httpsURLConnection.setRequestProperty("Content-Type", "application/json")
            httpsURLConnection.setRequestProperty("authorization", "key=$KEY")
            httpsURLConnection.setRequestProperty("project_id", senderId)

            val json = JSONObject()

            println("jsonArray")
            println(registrationIds)

            //Here i define the user i want to add to a certain group
            json.put("operation", "add")
            json.put("notification_key_name", notificationKeyName)
            json.put("notification_key", notificationKey)
            json.put("registration_ids", registrationIds)


            val outputStream: OutputStream =
                BufferedOutputStream(httpsURLConnection.outputStream)
            val writer = BufferedWriter(OutputStreamWriter(outputStream, "utf-8"))

            //here i write the body of the post request and then i send the request (flush)
            //then i close the post request
            writer.write(json.toString())
            writer.flush()
            writer.close()

            outputStream.close()

            //The response code and message of the post requests
            val responseCode: Int = httpsURLConnection.responseCode
            val responseMessage = httpsURLConnection.responseMessage

            Log.d(TAG, "$responseCode $responseMessage")


            // Check if the connection is successful
            if (responseCode in 400..499) {
                httpsURLConnection.errorStream
            } else {
                httpsURLConnection.inputStream
            }

            if (responseCode == 200) {
                Log.e(TAG, "Elements have been successfully added to the group")

                val response = httpsURLConnection.inputStream.bufferedReader()
                    .use { it.readText() }  // defaults to UTF-8
                withContext(Dispatchers.Main) {
                    // Convert raw JSON to pretty JSON using GSON library

                    //Here i get the notification_key that has been defined to the group that got created
                    val jsonObject  = JSONObject(response)
                    val notifKey_ = jsonObject.getString("notification_key")

                    println("NotifKey:")
                    println(notifKey_)
                }
            } else {
                Log.e(TAG, "Error couldn't add elements from the group")
            }

            //Here i close the connection to the endPoint
            httpsURLConnection.disconnect()


        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun sendNotificationToFirebase(title: String, body: String, iv : String, senderId : String) {

        delay(1500)


        val notificationChat =
            dbFirebase.collection("chats").
            document("S77po7vNGjtKja2Rinyb").
            collection("notifications").
            document()

        //Send Date needs to be formatted in this way 14/11/2021 16:38
        val calendar = Calendar.getInstance()
        val format = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")


        val notification = Notification(notificationChat.id, title, body, iv, format.format(calendar.time), senderId).toHash()


        notificationChat.set(notification).addOnCompleteListener {
            if (!it.isSuccessful) {
                Log.d(TAG, "Error adding notif to chat: $notificationChat")
            } else {
                Log.d(TAG, "Notif added to chat: $notificationChat")
            }
        }

        val notificationUser =
            dbFirebase.collection("users").
            document("EJ1NUwpOoziRyiWWzNej").
            collection("notifications").
            document()

        notificationUser.set(notification).addOnCompleteListener {
            if (!it.isSuccessful) {
                Log.d(TAG, "Error adding notif to user: $notificationChat")
            } else {
                Log.d(TAG, "Notif added to user: $notificationChat")
            }
        }
    }

    companion object {
        const val TAG = "PushNotifFragment"
    }
}