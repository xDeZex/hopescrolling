package com.hopescrolling

import android.content.Context
import androidx.room.Room
import com.hopescrolling.data.article.ArticleContentFetcher
import com.hopescrolling.data.article.ArticleRepository
import com.hopescrolling.data.article.DefaultArticleRepository
import com.hopescrolling.data.article.RssFeedFetcher
import com.hopescrolling.data.article.httpRssFeedFetcher
import com.hopescrolling.data.article.jsoupArticleContentFetcher
import com.hopescrolling.data.feed.DataStoreFeedSourceRepository
import com.hopescrolling.data.feed.FeedSourceRepository
import com.hopescrolling.data.feed.feedSourceDataStore
import com.hopescrolling.data.readstate.AppDatabase
import com.hopescrolling.data.readstate.ReadStateRepository
import com.hopescrolling.data.readstate.RoomReadStateRepository
import com.hopescrolling.data.update.AppUpdateRepository
import com.hopescrolling.data.update.HttpAppUpdateRepository
import com.hopescrolling.data.update.UpdateState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AppContainer(context: Context) {
    val feedSourceRepository: FeedSourceRepository by lazy {
        DataStoreFeedSourceRepository(context.feedSourceDataStore)
    }

    val rssFeedFetcher: RssFeedFetcher by lazy {
        httpRssFeedFetcher()
    }

    val articleRepository: ArticleRepository by lazy {
        DefaultArticleRepository(feedSourceRepository, rssFeedFetcher)
    }

    val db: AppDatabase by lazy {
        Room.databaseBuilder(context, AppDatabase::class.java, "hopescrolling-db")
            .fallbackToDestructiveMigration()
            .build()
    }

    val readStateRepository: ReadStateRepository by lazy {
        RoomReadStateRepository(db.readArticleDao())
    }

    val articleContentFetcher: ArticleContentFetcher by lazy {
        jsoupArticleContentFetcher()
    }

    val appUpdateRepository: AppUpdateRepository by lazy {
        HttpAppUpdateRepository(
            apiUrl = GITHUB_RELEASES_API_URL,
            currentVersionCode = BuildConfig.VERSION_CODE,
        )
    }

    private val _updateAvailable = MutableStateFlow(false)
    val updateAvailable: StateFlow<Boolean> = _updateAvailable

    init {
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            _updateAvailable.value = appUpdateRepository.getUpdateState() is UpdateState.UpdateAvailable
        }
    }

    companion object {
        const val GITHUB_RELEASES_API_URL =
            "https://api.github.com/repos/xDeZex/hopescrolling/releases"
    }
}
