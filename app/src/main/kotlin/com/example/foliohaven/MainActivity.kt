package com.example.foliohaven

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.foliohaven.data.repository.BookRepository
import com.example.foliohaven.data.repository.UserPreferencesRepository
import com.example.foliohaven.ui.greeting.GreetingScreen
import com.example.foliohaven.ui.reader.ReaderScreen
import com.example.foliohaven.ui.theme.FolioHavenComposeTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    enableEdgeToEdge()
    val repository = BookRepository(applicationContext)
    val preferences = UserPreferencesRepository(applicationContext)

    setContent {
      val useDynamicColor by preferences.useDynamicColor.collectAsStateWithLifecycle()
      FolioHavenComposeTheme(dynamicColor = useDynamicColor) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          val coroutineScope = rememberCoroutineScope()
          var isGreetingActive by remember { mutableStateOf(!preferences.isGreetingCompleted()) }
          var activeReadingBookId by remember { mutableStateOf<Long?>(null) }
          var activeReadingPage by remember { mutableIntStateOf(1) }

          LaunchedEffect(Unit) {
            repository.refreshBooks()
          }

          if (isGreetingActive) {
            GreetingScreen(
              initialLanguage = preferences.getAppLanguage(),
              onGetStarted = { theme, seedSampleBooks, language ->
                preferences.setAppLanguage(language)
                preferences.setDefaultReadingTheme(theme)
                if (language == "en") {
                  preferences.setTtsLanguage("en-US")
                } else {
                  preferences.setTtsLanguage("vi-VN")
                }
                preferences.setGreetingCompleted(true)
                if (seedSampleBooks) {
                  coroutineScope.launch {
                    repository.seedDefaultBooksIfEmpty()
                  }
                }
                isGreetingActive = false
              }
            )
          } else {
            val bookId = activeReadingBookId
            if (bookId != null) {
              ReaderScreen(
                bookId = bookId,
                initialPage = activeReadingPage,
                initialTheme = preferences.getDefaultReadingTheme(),
                bookRepository = repository,
                onBack = { activeReadingBookId = null }
              )
            } else {
              HomeScreen(
                bookRepository = repository,
                preferencesRepository = preferences,
                onBookClick = { clickedId, targetPage ->
                  activeReadingPage = targetPage
                  activeReadingBookId = clickedId
                },
                onShowGreeting = { isGreetingActive = true }
              )
            }
          }
        }
      }
    }
  }
}
