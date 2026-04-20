package com.example.chatup.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.chatup.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TermsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms)
    }
}