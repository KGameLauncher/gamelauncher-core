package de.dasbabypixel.gamelauncher.android.compat

import android.content.Context
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
import androidx.core.view.WindowInsetsCompat.Type.InsetsType
import de.dasbabypixel.gamelauncher.android.apilevel.Api16
import de.dasbabypixel.gamelauncher.android.apilevel.Api30

fun Window.decorFitsSystemWindows(decorFitsSystemWindows: Boolean) {
    WindowCompat.setDecorFitsSystemWindows(this, decorFitsSystemWindows)
}

val Window.insetsControllerCompat: WindowInsetsControllerCompat
    get() = WindowInsetsControllerCompat(this, this.decorView)

class WindowCompat {
    companion object {
        fun setDecorFitsSystemWindows(window: Window, decorFitsSystemWindows: Boolean) {
            if (Build.VERSION.SDK_INT >= 30) {
                Api30.setDecorFitsSystemWindows(window, decorFitsSystemWindows)
            } else if (Build.VERSION.SDK_INT >= 16) {
                Api16.setDecorFitsSystemWindows(window, decorFitsSystemWindows)
            }
        }
    }
}

class WindowInsetsControllerCompat(private val window: Window, private val view: View) {

    private val `impl`: Impl

    init {
        `impl` = Impl()
    }

    companion object {
        /**
         * The default option for [.setSystemBarsBehavior]. System bars will be forcibly
         * shown on any user interaction on the corresponding display if navigation bars are hidden
         * by [.hide] or
         * [WindowInsetsAnimationControllerCompat.setInsetsAndAlpha].
         */
        const val BEHAVIOR_SHOW_BARS_BY_TOUCH: Int = 0

        /**
         * Option for [.setSystemBarsBehavior]: Window would like to remain interactive
         * when hiding navigation bars by calling [.hide] or
         * [WindowInsetsAnimationControllerCompat.setInsetsAndAlpha].
         *
         *
         * When system bars are hidden in this mode, they can be revealed with system
         * gestures, such as swiping from the edge of the screen where the bar is hidden from.
         */
        const val BEHAVIOR_SHOW_BARS_BY_SWIPE: Int = 1

        /**
         * Option for [.setSystemBarsBehavior]: Window would like to remain
         * interactive when hiding navigation bars by calling [.hide] or
         * [WindowInsetsAnimationControllerCompat.setInsetsAndAlpha].
         *
         *
         * When system bars are hidden in this mode, they can be revealed temporarily with system
         * gestures, such as swiping from the edge of the screen where the bar is hidden from. These
         * transient system bars will overlay app’s content, may have some degree of
         * transparency, and will automatically hide after a short timeout.
         */
        const val BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE: Int = 2
    }

    var systemBarsBehaviour: Int
        get() = impl.getSystemBarsBehaviour()
        set(value) = impl.setSystemBarsBehavior(value)

    fun show(types: Int) {
        impl.show(types)
    }

    fun hide(types: Int) {
        impl.hide(types)
    }

    class Type {
        companion object {
            private const val FIRST: Int = 1
            private const val LAST: Int = 1 shl 8

            private const val STATUS_BARS: Int = FIRST
            private const val NAVIGATION_BARS: Int = 1 shl 1
            private const val CAPTION_BAR: Int = 1 shl 2

            private const val IME: Int = 1 shl 3

            private const val SYSTEM_GESTURES: Int = 1 shl 4
            private const val MANDATORY_SYSTEM_GESTURES: Int = 1 shl 5
            private const val TAPPABLE_ELEMENT: Int = 1 shl 6

            private const val DISPLAY_CUTOUT: Int = 1 shl 7

            /**
             * @return An insets type representing any system bars for displaying status.
             */
            fun statusBars() = STATUS_BARS

            /**
             * @return An insets type representing any system bars for navigation.
             */
            fun navigationBars() = NAVIGATION_BARS

            /**
             * @return An insets type representing the window of a caption bar.
             */
            fun captionBar() = CAPTION_BAR

            /**
             * @return An insets type representing the window of an {@link InputMethod}.
             */
            fun ime() = IME

            /**
             * Returns an insets type representing the system gesture insets.
             *
             * <p>The system gesture insets represent the area of a window where system gestures have
             * priority and may consume some or all touch input, e.g. due to the a system bar
             * occupying it, or it being reserved for touch-only gestures.
             *
             * <p>Simple taps are guaranteed to reach the window even within the system gesture insets,
             * as long as they are outside the {@link #getSystemWindowInsets() system window insets}.
             *
             * <p>When {@link View#SYSTEM_UI_FLAG_LAYOUT_STABLE} is requested, an inset will be returned
             * even when the system gestures are inactive due to
             * {@link View#SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN} or
             * {@link View#SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION}.
             *
             * @see #getSystemGestureInsets()
             */
            fun systemGestures() = SYSTEM_GESTURES

            /**
             * @see #getMandatorySystemGestureInsets
             */
            fun mandatorySystemGestures() = MANDATORY_SYSTEM_GESTURES

            /**
             * @see #getTappableElementInsets
             */
            fun tappableElement() = TAPPABLE_ELEMENT

            /**
             * Returns an insets type representing the area that used by {@link DisplayCutoutCompat}.
             *
             * <p>This is equivalent to the safe insets on {@link #getDisplayCutout()}.</p>
             *
             * @see DisplayCutoutCompat#getSafeInsetLeft()
             * @see DisplayCutoutCompat#getSafeInsetTop()
             * @see DisplayCutoutCompat#getSafeInsetRight()
             * @see DisplayCutoutCompat#getSafeInsetBottom()
             */
            fun displayCutout() = DISPLAY_CUTOUT

            /**
             * @return All system bars. Includes {@link #statusBars()}, {@link #captionBar()} as well as
             * {@link #navigationBars()}, but not {@link #ime()}.
             */
            fun systemBars() = STATUS_BARS or NAVIGATION_BARS or CAPTION_BAR
        }
    }

    @RequiresApi(30)
    private open class Impl30(val window: Window) : Impl() {
        val insetsController = window.insetsController

        override fun show(@InsetsType types: Int) {
            if (types and Type.IME != 0 && Build.VERSION.SDK_INT < 32) {
                val imm =
                    window.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

                // This is a strange-looking workaround by making a call and ignoring the result.
                // We don't use the return value here, but isActive() has the side-effect of
                // calling a hidden method checkFocus(), which ensures that the IME state has the
                // correct view in some situations (especially when the focused view changes).
                // This is essentially a backport, since an equivalent checkFocus() call was
                // added in API 32 to improve behavior:
                // https://issuetracker.google.com/issues/189858204
                imm.isActive
            }
            insetsController?.show(types)
        }

        override fun hide(@InsetsType types: Int) {
            insetsController?.hide(types)
        }

        override fun setSystemBarsBehavior(behaviour: Int) {
            insetsController?.systemBarsBehavior = behaviour
        }

        override fun getSystemBarsBehaviour(): Int {
            return insetsController?.systemBarsBehavior ?: 0
        }
    }

    @RequiresApi(20)
    private open class Impl20(val window: Window, val view: View) : Impl() {
        override fun show(typeMask: Int) {
            var i = Type.FIRST
            while (i <= Type.LAST
            ) {
                if ((typeMask and i) == 0) {
                    i = i shl 1
                    continue
                }
                showForType(i)
                i = i shl 1
            }
        }

        protected fun setSystemUiFlag(systemUiFlag: Int) {
            val decorView: View = window.decorView
            decorView.systemUiVisibility = (decorView.systemUiVisibility or systemUiFlag)
        }

        protected fun unsetSystemUiFlag(systemUiFlag: Int) {
            val decorView: View = window.decorView
            decorView.systemUiVisibility = (decorView.systemUiVisibility and systemUiFlag.inv())
        }


        protected fun setWindowFlag(windowFlag: Int) {
            window.addFlags(windowFlag)
        }

        protected fun unsetWindowFlag(windowFlag: Int) {
            window.clearFlags(windowFlag)
        }

        private fun showForType(type: Int) {
            when (type) {
                Type.STATUS_BARS -> {
                    unsetSystemUiFlag(View.SYSTEM_UI_FLAG_FULLSCREEN)
                    unsetWindowFlag(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                    return
                }

                Type.NAVIGATION_BARS -> {
                    unsetSystemUiFlag(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
                    return
                }

                Type.IME -> {
                    // We'll try to find an available textView to focus to show the IME
                    var view: View? = this.view

                    if (view!!.isInEditMode || view.onCheckIsTextEditor()) {
                        // The IME needs a text view to be focused to be shown
                        // The view given to retrieve this controller is a textView so we can assume
                        // that we can focus it in order to show the IME
                        view.requestFocus()
                    } else {
                        view = window.currentFocus
                    }

                    // Fallback on the container view
                    if (view == null) {
                        view = window.findViewById(android.R.id.content)
                    }

                    if (view != null && view.hasWindowFocus()) {
                        val finalView = view
                        finalView.post {
                            val imm =
                                finalView.context
                                    .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.showSoftInput(finalView, 0)
                        }
                    }
                }
            }
        }

        override fun hide(typeMask: Int) {
            var i = Type.FIRST
            while (i <= Type.LAST
            ) {
                if ((typeMask and i) == 0) {
                    i = i shl 1
                    continue
                }
                hideForType(i)
                i = i shl 1
            }
        }

        private fun hideForType(type: Int) {
            when (type) {
                Type.STATUS_BARS -> {
                    setSystemUiFlag(View.SYSTEM_UI_FLAG_FULLSCREEN)
                    return
                }

                Type.NAVIGATION_BARS -> {
                    setSystemUiFlag(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
                    return
                }

                Type.IME -> (window.context
                    .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromWindow(window.decorView.windowToken, 0)
            }
        }

        override fun setSystemBarsBehavior(behavior: Int) {
            when (behavior) {
                BEHAVIOR_SHOW_BARS_BY_SWIPE -> {
                    unsetSystemUiFlag(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
                    setSystemUiFlag(View.SYSTEM_UI_FLAG_IMMERSIVE)
                }

                BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE -> {
                    unsetSystemUiFlag(View.SYSTEM_UI_FLAG_IMMERSIVE)
                    setSystemUiFlag(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
                }

                BEHAVIOR_SHOW_BARS_BY_TOUCH -> unsetSystemUiFlag(
                    View.SYSTEM_UI_FLAG_IMMERSIVE
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
            }
        }
    }

    private open class Impl {

        open fun setSystemBarsBehavior(behaviour: Int) {
        }

        open fun getSystemBarsBehaviour(): Int = 0

        open fun show(types: Int) {
        }

        open fun hide(types: Int) {
        }
    }
}