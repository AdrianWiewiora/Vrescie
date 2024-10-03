package com.example.vresciecompose

import ProvideContext
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.vresciecompose.authentication.GoogleAuthentication
import com.example.vresciecompose.data.UserChatPrefs
import com.example.vresciecompose.data.UserChatPrefsDao
import com.example.vresciecompose.screens.removeUserFromFirebaseDatabase
import com.example.vresciecompose.ui.theme.VrescieComposeTheme
import com.example.vresciecompose.view_models.ConfigurationProfileViewModel
import com.example.vresciecompose.view_models.ConversationViewModel
import com.example.vresciecompose.view_models.LocationViewModel
import com.example.vresciecompose.view_models.LoginViewModel
import com.example.vresciecompose.view_models.MainViewModel
import com.example.vresciecompose.view_models.ProfileViewModel
import com.example.vresciecompose.view_models.RegistrationViewModel
import com.example.vresciecompose.view_models.UserChatPrefsViewModel
import com.google.android.gms.auth.api.identity.Identity

class MainActivity : ComponentActivity() {
    // Room database
    companion object {
        lateinit var database: AppDatabase
    }
    private lateinit var userChatPrefsViewModel: UserChatPrefsViewModel

    private lateinit var backDispatcher: OnBackPressedDispatcher

    private lateinit var registrationViewModel: RegistrationViewModel
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var configurationProfileViewModel: ConfigurationProfileViewModel
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var locationViewModel: LocationViewModel
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    private lateinit var conversationViewModel: ConversationViewModel

    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(getSharedPreferences("MyPrefs", Context.MODE_PRIVATE))
    }

    val googleAuthClient by lazy {
        GoogleAuthentication(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


//        val MIGRATION_1_2 = object : Migration(1, 2) {
//            override fun migrate(database: SupportSQLiteDatabase) {
//                database.execSQL("ALTER TABLE UserChatPrefs ADD COLUMN isProfileVerified INTEGER NOT NULL DEFAULT 0")
//                database.execSQL("ALTER TABLE UserChatPrefs ADD COLUMN relationshipPreference INTEGER NOT NULL DEFAULT 0")
//                database.execSQL("ALTER TABLE UserChatPrefs ADD COLUMN maxDistance REAL NOT NULL DEFAULT 0")
//            }
//        }


        // Inicjalizacja bazy danych
        database = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "my-database").addMigrations().build()

        // Inicjalizacja ViewModelu z fabryką
        val userChatPrefsDao = database.userChatPrefsDao()
        val viewModelFactory = UserChatPrefsViewModelFactory(userChatPrefsDao)
        userChatPrefsViewModel = ViewModelProvider(this, viewModelFactory).get(UserChatPrefsViewModel::class.java)

        backDispatcher = onBackPressedDispatcher

        installSplashScreen().apply {
            setKeepOnScreenCondition {
                !viewModel.isReady.value
            }
        }

        // Inicjalizacja ViewModel
        registrationViewModel = ViewModelProvider(this).get(RegistrationViewModel::class.java)
        loginViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        configurationProfileViewModel =
            ViewModelProvider(this).get(ConfigurationProfileViewModel::class.java)
        profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        locationViewModel = ViewModelProvider(this).get(LocationViewModel::class.java)

        // Inicjalizacja ActivityResultLauncher dla żądania uprawnień
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (!isGranted) {
                // Obsługa braku udzielonych uprawnień
                Log.e("Location", "Location permission denied")
            }
        }

        conversationViewModel = ViewModelProvider(this).get(ConversationViewModel::class.java)

        if (viewModel.isReady.value) {
            val startDestination = when {
                viewModel.isFirstRun() -> Navigation.Destinations.FIRST_LAUNCH
                viewModel.isLoggedIn() -> Navigation.Destinations.MAIN_MENU+"/1"
                else -> Navigation.Destinations.START
            }
            setContent {
                VrescieComposeTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val navController = rememberNavController()
                        ProvideContext(context = applicationContext) {
                            AppNavigation(
                                navController,
                                startDestination,
                                googleAuthClient,
                                lifecycleScope,
                                applicationContext = applicationContext,
                                registrationViewModel,
                                loginViewModel,
                                configurationProfileViewModel,
                                profileViewModel,
                                locationViewModel,
                                requestPermissionLauncher,
                                conversationViewModel,
                                database = database,
                                userChatPrefsViewModel,
                            )
                        }
                    }
                }
            }
            // Ustawienie isFirstRun na false dopiero po ustawieniu setContent na FirstLaunch
            if (startDestination == Navigation.Destinations.FIRST_LAUNCH) {
                applicationContext.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean("isFirstRun", false)
                    .apply()
            }
        }
    }
}

class MainViewModelFactory(private val sharedPreferences: SharedPreferences) :
    ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(sharedPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


@Database(entities = [UserChatPrefs::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userChatPrefsDao(): UserChatPrefsDao
}

class UserChatPrefsViewModelFactory(private val userChatPrefsDao: UserChatPrefsDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserChatPrefsViewModel::class.java)) {
            return UserChatPrefsViewModel(userChatPrefsDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}