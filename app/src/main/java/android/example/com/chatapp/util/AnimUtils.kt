package android.example.com.chatapp.util

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import androidx.core.animation.addListener
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlin.math.hypot

class AnimUtils {

    companion object {
        fun scaleFab(actionButton: FloatingActionButton) {
            Handler(Looper.getMainLooper()).postDelayed({
                actionButton.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100)
            }, 450)
        }
        fun unscaleFab(actionButton: FloatingActionButton) {
            actionButton.animate().scaleX(0f).scaleY(0f).setDuration(100)
            }

        fun changeTitle(title: String, textView: TextView) {
            textView.animate().alpha(0f).setDuration(150).withEndAction {
                textView.text = title
                textView.animate().alpha(1f).duration = 150
            }
        }

        fun showFabAddState(
            tabPosition: Int,
            floatingActionButton: FloatingActionButton,
            context: Context
        ) {
            floatingActionButton.animate().translationY(
                if (tabPosition == 2 || tabPosition == 3) {
                    PixelsToDpUtil.convertPixelsToDp(-58, context)
                } else {
                    PixelsToDpUtil.convertPixelsToDp(0, context)
                }
            ).duration = 150

        }

        fun showSearchView(searchView: androidx.appcompat.widget.SearchView) {
            searchView.visibility = View.VISIBLE
            val animator = ViewAnimationUtils.createCircularReveal(
                searchView,
                (searchView.right + searchView.left / 2),
                (searchView.top + searchView.bottom * 2), 0f, searchView.width.toFloat()
            )
            animator.duration = 350
            animator.start()
        }

        fun hideSearchView(
            searchView: androidx.appcompat.widget.SearchView,
            appBarLayout: AppBarLayout
        ) {
            val animator = ViewAnimationUtils.createCircularReveal(
                searchView,
                (searchView.right + searchView.left / 2),
                (searchView.top + searchView.bottom * 2), searchView.width.toFloat(), 0f
            )
            animator.duration = 300
            animator.start()
            animator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    searchView.visibility = View.INVISIBLE
                    appBarLayout.setExpanded(true, true)
                    animation?.removeAllListeners()
                }
            })
        }

        fun animateBtnTakePhoto(imgButton: ImageButton) {
            imgButton.animate().scaleX(0.8f).scaleY(0.8f).setDuration(50).withEndAction {
                imgButton.animate().scaleX(1.0f).scaleY(1.0f)
            }
        }
        fun rotateSwitchCameraButton(imgButton: ImageButton) {
            imgButton.animate().rotationY(180f).scaleX(1.3f).scaleY(1.3f)
                .setDuration(300)
                .withEndAction { imgButton.animate().rotationY(0f)
                    .scaleY(1.0f).scaleX(1.0f) }
        }
        fun changeSwitchFlashButton(imgButton: ImageButton, img: Int, context: Context) {
            imgButton.animate().scaleY(0.5f).scaleX(0.5f).alpha(0f)
                .setDuration(100).withEndAction {
                    imgButton.setImageDrawable(context.getDrawable(img))
                    imgButton.animate().scaleY(1.0f).scaleX(1.0f).alpha(1.0f)
                        .duration = 100
                }
        }
        fun scaleView(view:View, duration: Int, scale: Float) {
            view.animate().scaleX(scale).scaleY(scale).duration = duration.toLong()
        }

        fun showView(view: View, duration: Int, alpha: Float) {
            view.animate().setDuration(duration.toLong()).alpha(alpha)
        }
        fun changeIconButtonSendMessage(resIcon : Int, imageView: ImageView) {
            imageView.animate().scaleY(0.8f).scaleX(0.8f).alpha(0f).setDuration(150)
                .withEndAction {
                    imageView.setImageResource(resIcon)
                    imageView.animate().scaleY(1f).scaleX(1f).alpha(1f).duration= 150
                }
        }
        fun animateIconsChat
                    (imgButton1: ImageButton, imgButton2: ImageButton, value: Int, context: Context) {
            val animatorSet = AnimatorSet()
            animatorSet.playTogether(ObjectAnimator.ofFloat(imgButton1,"translationX",
                PixelsToDpUtil.convertPixelsToDp(value, context)))
            animatorSet.playTogether(ObjectAnimator.ofFloat(imgButton2,"translationX",
                PixelsToDpUtil.convertPixelsToDp(value, context)))
            animatorSet.duration = 150
            animatorSet.start()
        }
        fun showViewAttachFiles(view: View, isInvisible: Boolean, context: Context) {
            var x = view.right
            var y = view.bottom

            x -= (PixelsToDpUtil.convertPixelsToDp(112,context) + PixelsToDpUtil.convertPixelsToDp(6, context)).toInt()
            val hypot = hypot(view.width.toDouble(),view.height.toDouble())

            lateinit var anim : Animator
            if(isInvisible) {
                anim = ViewAnimationUtils.createCircularReveal(view, x ,y, 0f ,hypot.toFloat())
                anim.addListener(object: AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator?) {
                        super.onAnimationStart(animation)
                        view.visibility = View.VISIBLE
                    }
                })
            } else {
                anim = ViewAnimationUtils.createCircularReveal(view, x ,y, hypot.toFloat() , 0F)
                anim.addListener(object: AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        view.visibility = View.INVISIBLE
                    }
                })
            }
            anim.interpolator = AccelerateDecelerateInterpolator()
            anim.start()

        }
    }




}