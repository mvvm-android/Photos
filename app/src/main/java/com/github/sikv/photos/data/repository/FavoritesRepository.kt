package com.github.sikv.photos.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.sqlite.db.SimpleSQLiteQuery
import com.github.sikv.photos.database.FavoritePhotoEntity
import com.github.sikv.photos.database.FavoritesDao
import com.github.sikv.photos.enumeration.SortBy
import com.github.sikv.photos.model.Photo
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoritesRepository @Inject constructor(
        private val favoritesDao: FavoritesDao
) {

    interface Listener {
        fun onFavoriteChanged(photo: Photo, favorite: Boolean)
        fun onFavoritesChanged()
    }

    private val scope = CoroutineScope(Job() + Dispatchers.IO)

    private val subscribers = mutableListOf<Listener>()

    /**
     * This overrides default value for isFavorite flag for photos.
     * Only if [Add to Favorites], [Remove from Favorites] or [Delete All Favorites] action has been occurred.
     */
    private val favorites = mutableMapOf<Photo, Boolean>()

    fun subscribe(listener: Listener) {
        subscribers.add(listener)
    }

    fun unsubscribe(listener: Listener) {
        subscribers.remove(listener)
    }

    fun getFavoritesLiveData(sortBy: SortBy = SortBy.DATE_ADDED_NEWEST): LiveData<List<FavoritePhotoEntity>>  {
        val query = when (sortBy) {
            SortBy.DATE_ADDED_NEWEST ->
                SimpleSQLiteQuery("SELECT * from FavoritePhoto WHERE markedAsDeleted=0 ORDER BY dateAdded DESC")
            SortBy.DATE_ADDED_OLDEST ->
                SimpleSQLiteQuery("SELECT * from FavoritePhoto WHERE markedAsDeleted=0 ORDER BY dateAdded ASC")
        }

        return Transformations.map(favoritesDao.getPhotos(query)) {
            it.forEach { photo ->
                photo.favorite = true
            }

            it
        }
    }

    /**
     * Inverts isFavorite flag for [photo] and notifies all subscribers that [photo] has been changed.
     */
    fun invertFavorite(photo: Photo) {
        val favorite = !isFavorite(photo)

        favorites[photo] = favorite

        subscribers.forEach {
            it.onFavoriteChanged(photo, favorite)
        }

        val favoritePhoto = FavoritePhotoEntity.fromPhoto(photo)

        scope.launch {
            if (favorite) {
                favoritesDao.insert(favoritePhoto)
            } else {
                favoritesDao.delete(favoritePhoto)
            }
        }
    }

    suspend fun markAllAsRemoved(): Boolean = withContext(Dispatchers.IO) {
        val count = favoritesDao.getCount()

        if (count > 0) {
            favoritesDao.markAllAsDeleted()

            favorites.keys.forEach {
                favorites[it] = false
            }

            subscribers.forEach {
                it.onFavoritesChanged()
            }

            true
        } else {
            false
        }
    }

    fun unmarkAllAsRemoved() {
        scope.launch {
            favoritesDao.unmarkAllAsDeleted()

            favorites.keys.forEach {
                favorites[it] = true
            }

            subscribers.forEach {
                it.onFavoritesChanged()
            }
        }
    }

    fun removeAll() {
        scope.launch {
            favoritesDao.deleteAll()
        }
    }

    /**
     * Used to get [photo]'s isFavorite flag. If [photo] is contained in [favorites] map
     * then this function returns overridden local result from the map, if not, the initial one.
     */
    fun isFavorite(photo: Photo?): Boolean = favorites[photo] ?: photo?.favorite ?: false

    suspend fun isFavoriteFromDatabase(photo: Photo): Boolean = withContext(Dispatchers.IO) {
        favoritesDao.getById(photo.getPhotoId()) != null
    }

    suspend fun populateFavorite(photo: Photo): Photo {
        photo.favorite = isFavoriteFromDatabase(photo)
        return photo
    }
}