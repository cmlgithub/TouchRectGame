package com.cml.touchrectgame

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

class MainActivity : AppCompatActivity(), ImpactDismissGameView.DismissFinish {
    override fun dismissFinish() {
        finish()
    }

    var mView: ImpactDismissGameView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mView = findViewById(R.id.impactDismissGameView)

        mView!!.setDismissFinish(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        mView!!.destoryView()
    }
}
