import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

val LocalContext = staticCompositionLocalOf<Context> { error("No Context provided") }

@Composable
fun ProvideContext(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalContext provides LocalContext.current) {
        content()
    }
}
