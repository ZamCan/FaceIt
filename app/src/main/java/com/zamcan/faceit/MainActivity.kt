package com.zamcan.faceit
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.zamcan.faceit.ui.FaceItApp
import com.zamcan.faceit.ui.theme.FaceItTheme
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { FaceItTheme { FaceItApp() } }
    }
}
