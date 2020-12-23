package com.example.simpleappdemo

import android.app.Dialog
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.gson.Gson
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        CallApiLoginAsyncTask("rubens", "123456").execute()
    }

    private inner class CallApiLoginAsyncTask(val username: String, val password: String) :
        AsyncTask<Any, Void, String>() {
        private lateinit var custonProgressDialog: Dialog

        override fun onPreExecute() {
            super.onPreExecute()

            showProgressDialog()
        }

        override fun doInBackground(vararg params: Any?): String {
            var result: String

            var connection: HttpURLConnection? = null

            try {
                var url = URL("https://api.mocki.io/v1/8d3d86e9")
                connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.doOutput = true

                connection.instanceFollowRedirects = false

                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("charset", "utf-8")
                connection.setRequestProperty("accept", "application/json")


                connection.useCaches = false

                val writeDataOutputStream = DataOutputStream(connection.outputStream)

                val jsonRequest = JSONObject()
                jsonRequest.put("username", username)
                jsonRequest.put("password", password)

                writeDataOutputStream.writeBytes(jsonRequest.toString())
                writeDataOutputStream.flush()
                writeDataOutputStream.close()


                val httpResult: Int = connection.responseCode

                if (httpResult == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream

                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val stringBuilder = StringBuilder()
                    var line: String?

                    try {
                        while (reader.readLine().also { line = it } != null) {
                            stringBuilder.append(line + "\n")
                        }
                    } catch (e: IOException) {
                        e.stackTrace
                    } finally {
                        try {
                            inputStream.close()
                        } catch (e: IOException) {
                            e.stackTrace
                        }

                    }
                    result = stringBuilder.toString()
                } else {
                    result = connection.responseMessage
                }
            } catch (e: SocketTimeoutException) {
                result = "connection TimeOut"
            } catch (e: Exception) {
                result = "Error: ${e.message}"
            } finally {
                connection?.disconnect()
            }
            return result
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)

            cancelProgressDialog()

            if (result != null) {
                Log.i("JSON RESULT", result)
            }
            //usando Gson
            val responseData = Gson().fromJson(result, ResponseData::class.java)
            Log.i("Message:", responseData.message)
            Log.i("User ID:", "${responseData.user_id}")
            Log.i("Name:", responseData.name)
            Log.i("Email:", responseData.email)
            Log.i("Mobile:", "${responseData.mobile}")

            //parse um objeto
            Log.i("Is Profile complete:", "${responseData.profile_details.is_profile_completed}")
            Log.i("Rating:", "${responseData.profile_details.rating}")

            //parse de uma lista de objetos
            Log.i("Data List Size:", "${responseData.data_list.size}")

            for (item in responseData.data_list.indices) {
                Log.i("Value $item", "${responseData.data_list[item]}")

                Log.i("ID", "${responseData.data_list[item].id}")
                Log.i("Value", responseData.data_list[item].value)
            }


/*
            // chamada manual
            val jsonObject = JSONObject(result)
            val message = jsonObject.optString("message")
            Log.i("Message:", message)
            val userId = jsonObject.optInt("user_id")
            Log.i("UserID:", "$userId")
            val name = jsonObject.optString("name")
            Log.i("Name:", name)

            val profileDetailObject = jsonObject.optJSONObject("profile_details")
            val isProfileCompleted = profileDetailObject.optBoolean("is_profile_completed")
            Log.i("Is profile completed", "$isProfileCompleted")

            val dataListArray = jsonObject.optJSONArray("data_list")
            Log.i("Data list size", "${dataListArray.length()}")

            for(item in 0 until dataListArray.length()) {
                Log.i("Value $item", "${dataListArray[item]}")

                val dataItemObject: JSONObject = dataListArray[item] as JSONObject

                val id = dataItemObject.optInt("id")
                Log.i("Id:", "$id")
                val value = dataItemObject.optString("value")
                Log.i("value", "$value")

            }

 */
        }


        private fun showProgressDialog() {
            custonProgressDialog = Dialog(this@MainActivity)
            custonProgressDialog.setContentView(R.layout.dialog_custom_progress)
            custonProgressDialog.show()
        }

        private fun cancelProgressDialog() {
            custonProgressDialog.dismiss()
        }
    }
}

