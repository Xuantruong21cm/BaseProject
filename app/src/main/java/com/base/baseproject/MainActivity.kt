package com.base.baseproject

import android.view.View
import android.widget.Toast
import com.base.baseproject.databinding.ActivityMainBinding
import com.base.common.base.BaseActivity
import com.base.common.extensions.setOnSingleClickListener


class MainActivity : BaseActivity<ActivityMainBinding>() {


    override fun initViews() {
        binding.tvHello.setOnSingleClickListener{
            Toast.makeText(this,binding.tvHello.text,Toast.LENGTH_LONG).show()
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }
}