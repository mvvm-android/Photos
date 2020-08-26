package com.github.sikv.photos.viewmodel

import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.sikv.photos.App
import com.github.sikv.photos.enumeration.LoginStatus
import com.github.sikv.photos.event.Event
import com.github.sikv.photos.util.AccountManager
import javax.inject.Inject

class MoreViewModel: ViewModel(), AccountManager.Callback {

    @Inject
    lateinit var accountManager: AccountManager

    private val loginStatusChangedMutableEvent = MutableLiveData<Event<LoginStatus>>()
    val loginStatusChangedEvent: LiveData<Event<LoginStatus>> = loginStatusChangedMutableEvent

    init {
        App.instance.appComponent.inject(this)

        accountManager.subscribe(this)

        loginStatusChangedMutableEvent.value = Event(accountManager.loginStatus)
    }

    override fun onCleared() {
        super.onCleared()

        accountManager.unsubscribe(this)
    }

    override fun onLoginStatusChanged(status: LoginStatus) {
        loginStatusChangedMutableEvent.value = Event(status)
    }

    fun signInWithGoogle(fragment: Fragment) {
        accountManager.signInWithGoogle(fragment)
    }

    fun handleSignInResult(requestCode: Int, resultCode: Int, data: Intent?) {
        accountManager.handleSignInResult(requestCode, resultCode, data)
    }

    fun signOut() {
        accountManager.signOut()
    }
}