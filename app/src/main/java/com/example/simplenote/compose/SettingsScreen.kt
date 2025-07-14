package com.example.simplenote.compose

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.simplenote.R
import com.example.simplenote.activity.LoginActivity
import com.example.simplenote.ui.theme.*

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onChangePasswordClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var profileImageId by remember { mutableStateOf(1) }
    
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
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
        
        val id = sharedPreferences.getInt("id", 1)
        username = sharedPreferences.getString("username", "example examplian") ?: "example examplian"
        email = sharedPreferences.getString("email", "something@example.com") ?: "something@example.com"
        profileImageId = (id % 7) + 1
    }
    
    fun performLogout() {
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

        sharedPreferences.edit {
            putString("access_token", "")
            putString("refresh_token", "")
        }
        
        val intent = Intent(context, LoginActivity::class.java)
        context.startActivity(intent)
        if (context is android.app.Activity) {
            context.finish()
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(BackgroundWhite)
                .padding(horizontal = 24.dp, vertical = 20.dp),
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
                text = stringResource(R.string.settings),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.weight(1f),
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
                .background(BackgroundWhite)
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundWhite),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val drawableName = "image_$profileImageId"
                val resourceId = context.resources.getIdentifier(drawableName, "drawable", context.packageName)
                
                Image(
                    painter = painterResource(id = if (resourceId != 0) resourceId else R.drawable.image_1),
                    contentDescription = null,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(BackgroundEditText)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = username,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.email),
                            contentDescription = null,
                            tint = TextTertiary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = email,
                            fontSize = 14.sp,
                            color = TextTertiary
                        )
                    }
                }
            }
            
            HorizontalDivider(
                color = BackgroundLightGray,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            
            Text(
                text = "APP SETTINGS",
                fontSize = 12.sp,
                color = TextTertiary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundWhite)
                    .clickable { onChangePasswordClick() }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.lock),
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Change Password",
                    fontSize = 16.sp,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    painter = painterResource(id = R.drawable.cheveron_right),
                    contentDescription = null,
                    tint = TextTertiary,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            HorizontalDivider(
                color = BackgroundLightGray,
                thickness = 1.dp
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundWhite)
                    .clickable { showLogoutDialog = true }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.logout),
                    contentDescription = null,
                    tint = AccentRedDark,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.log_out),
                    fontSize = 16.sp,
                    color = AccentRedDark
                )
            }
            
            Spacer(modifier = Modifier.height(350.dp))
            Text(
                text = "Hamir Notes v1.1",
                fontSize = 14.sp,
                color = TextDisabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 100.dp),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
    
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { 
                Text(
                    text = stringResource(R.string.log_out),
                    color = TextPrimary
                ) 
            },
            text = { 
                Text(
                    text = stringResource(R.string.are_you_sure_you_want_to_log_out_from_the_application),
                    color = TextTertiary
                ) 
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        performLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRedDark)
                ) {
                    Text(stringResource(R.string.yes), color = TextWhite)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showLogoutDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(stringResource(R.string.cancel), color = AccentBlue)
                }
            },
            containerColor = BackgroundWhite,
            shape = RoundedCornerShape(16.dp)
        )
    }
} 