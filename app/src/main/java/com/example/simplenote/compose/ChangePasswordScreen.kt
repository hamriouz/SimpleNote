package com.example.simplenote.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.simplenote.BuildConfig
import com.example.simplenote.R
import com.example.simplenote.core.util.showError
import com.example.simplenote.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

@Composable
fun ChangePasswordScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var retypeNewPassword by remember { mutableStateOf("") }
    var isSubmitEnabled by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(currentPassword, newPassword, retypeNewPassword) {
        isSubmitEnabled = currentPassword.isNotBlank() && 
                         newPassword.isNotBlank() && 
                         retypeNewPassword.isNotBlank() &&
                         newPassword == retypeNewPassword &&
                         newPassword.length >= 6
    }
    
    fun submitPasswordChange() {
        if (!isSubmitEnabled || isLoading) return
        
        isLoading = true
        
        scope.launch(Dispatchers.IO) {
            try {
                val masterKey = MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()

                val sharedPreferences = EncryptedSharedPreferences.create(
                    context,
                    "secure_prefs",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
                
                val token = sharedPreferences.getString("access_token", "") ?: ""
                
                val client = OkHttpClient()
                val mediaType = "application/json".toMediaType()
                val body = """
                    {
                        "old_password": "$currentPassword",
                        "new_password": "$newPassword"
                    }
                """.trimIndent().toRequestBody(mediaType)
                
                val request = Request.Builder()
                    .url("${BuildConfig.BASE_URL}/api/auth/change-password/")
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .addHeader("Authorization", "Bearer $token")
                    .build()
                    
                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        isLoading = false
                        showError(context, e.message ?: "Network error")
                    }

                    override fun onResponse(call: Call, response: Response) {
                        isLoading = false
                        if (response.isSuccessful) {
                            scope.launch(Dispatchers.Main) {
                                onBackClick()
                            }
                        } else {
                            val errorBody = response.body?.string()
                            val errorMsg = try {
                                val jsonObject = JSONObject(errorBody)
                                jsonObject.getString("message")
                            } catch (e: Exception) {
                                "Password change failed"
                            }
                            showError(context, errorMsg)
                        }
                    }
                })
            } catch (e: Exception) {
                isLoading = false
                showError(context, "An error occurred")
            }
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFFF))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BackgroundWhite)
                .statusBarsPadding()
                .height(72.dp)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.clickable { onBackClick() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.purple_arrow),
                    contentDescription = stringResource(R.string.back),
                    tint = PrimaryBlue,
                    modifier = Modifier.size(10.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.back),
                    color = PrimaryBlue,
                    fontSize = 18.sp
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Text(
                text = stringResource(R.string.change_password),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.weight(1f))
        }
        
        HorizontalDivider(
            color = BackgroundLightGray,
            thickness = 1.dp
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(Color(0xFFFFF))
                .padding(24.dp)
        ) {
            Text(
                text = stringResource(R.string.please_input_your_current_password_first),
                color = PrimaryBlue,
                fontSize = 15.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            OutlinedTextField(
                value = currentPassword,
                onValueChange = { currentPassword = it },
                label = { Text(stringResource(R.string.current_password)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            
            HorizontalDivider(
                color = BackgroundLightGray,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            
            Text(
                text = stringResource(R.string.now_create_your_new_password),
                color = PrimaryBlue,
                fontSize = 15.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text(stringResource(R.string.new_password)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            
            Text(
                text = stringResource(R.string.password_should_contain_a_z_a_z_0_9),
                color = TextDisabled,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            OutlinedTextField(
                value = retypeNewPassword,
                onValueChange = { retypeNewPassword = it },
                label = { Text(stringResource(R.string.retype_new_password)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = retypeNewPassword.isNotBlank() && newPassword != retypeNewPassword
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = { submitPasswordChange() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = isSubmitEnabled && !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue,
                    disabledContainerColor = ButtonDisabled
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = TextWhite
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.submit_new_password),
                            color = TextWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            painter = painterResource(id = R.drawable.arrowright),
                            contentDescription = null,
                            tint = TextWhite,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
} 