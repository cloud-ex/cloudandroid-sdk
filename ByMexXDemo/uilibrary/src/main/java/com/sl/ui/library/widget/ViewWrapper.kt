package com.sl.ui.library.widget

import android.animation.ObjectAnimator
import android.util.Log
import android.view.View

 class ViewWrapper(private val rView: View) {
    var width: Int
        get() = rView.layoutParams.width
        set(width) {
            rView.layoutParams.width = width
            rView.requestLayout()
        }

    var height: Int
        get() = rView.layoutParams.height
        set(height) {
            rView.layoutParams.height = height
            rView.requestLayout()
        }


     var objectAnimator : ObjectAnimator?=null

     fun startAnim(buyCurr:Int){
         if(objectAnimator == null){
             objectAnimator =  ObjectAnimator.ofInt(this , "width", buyCurr).setDuration(200)
         }
         objectAnimator?.let {
             if(it.isRunning){
                 it.cancel()
             }
             it.setIntValues(buyCurr)
             it.start()
         }
     }
}