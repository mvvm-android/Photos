package com.github.sikv.photos.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.github.sikv.photos.Event
import com.github.sikv.photos.model.Photo

class PhotoViewModel(
        private val photo: Photo

) : ViewModel() {

    fun loadPhoto(glide: RequestManager): LiveData<Event<Bitmap>> {
        val photoLoadedEvent = MutableLiveData<Event<Bitmap>>()
        var photoLoaded = false

        glide.asBitmap()
                .load(photo.urls.regular)
                .into(object : SimpleTarget<Bitmap>() {
                    override fun onLoadStarted(placeholder: Drawable?) {
                        glide.asBitmap()
                                .load(photo.urls.small)
                                .into(object : SimpleTarget<Bitmap>() {
                                    override fun onResourceReady(bitmap: Bitmap, transition: Transition<in Bitmap>?) {
                                        if (!photoLoaded) {
                                            photoLoadedEvent.value = Event(bitmap)
                                        }
                                    }
                                })
                    }

                    override fun onResourceReady(bitmap: Bitmap, transition: Transition<in Bitmap>?) {
                        photoLoadedEvent.value = Event(bitmap)
                        photoLoaded = true
                    }
                })

        return photoLoadedEvent
    }
}