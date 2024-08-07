package com.zxy.view

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager


/**
 * description:可自动监听可见性FrameLayout
 */
@Suppress("unused")
open class VisibleSensorLayout : FrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    )

    private var containerLeft = 0
    private var containerRight = 0
    private var containerTop = 0
    private var containerBottom = 0
    private var visibleState: State = State.GONE
    private var windowIsVisible: Boolean = false
    private var parentFragment: Fragment? = null
    var tagName = ""

    //用于计算可见性判断的区域，默认为decorView
    var basicRectView: View? = (context as? Activity)?.window?.decorView
        set(value) {
            field = value
            refreshRectInfo()
            handleListener()
        }

    private val onGlobalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
        handleListener()
    }
    private val onScrollChangedListener = ViewTreeObserver.OnScrollChangedListener {
        handleListener()
    }
    var visibilityListener: VisibilityListener? = null

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        basicRectView?.post {
            refreshRectInfo()
            handleListener()
        }
    }

    private fun refreshRectInfo() {
        val rect = Rect()
        basicRectView?.getGlobalVisibleRect(rect)
        containerTop = rect.top
        containerBottom = rect.bottom
        containerLeft = rect.left
        containerRight = rect.right
    }

    private fun getCurrentVisibleState(): State {
        if (!isAttachedToWindow) {
            return State.GONE
        }
        if (visibility != VISIBLE) {
            return State.GONE
        }
        if (!windowIsVisible) {
            return State.GONE
        }
        if (parentFragment?.isHidden == true) {
            return State.GONE
        }

        val globalRect = Rect()
        getGlobalVisibleRect(globalRect)
        // 检查控件是否全部在容器的可视范围内
        val globalRectAllVisible = (globalRect.top in containerTop until containerBottom)
                && (globalRect.bottom in (containerTop + 1)..containerBottom)
                && (globalRect.left in containerLeft until containerRight)
                && (globalRect.right in (containerLeft + 1)..containerRight)
        val globalRectPartiallyVisible =
            (globalRect.left < containerRight || globalRect.right > containerLeft)
                    && (globalRect.top < containerBottom && globalRect.bottom > containerTop)

        val drawingRect = Rect()
        getDrawingRect(drawingRect)
        val drawingArea =
            (drawingRect.bottom - drawingRect.top) * (drawingRect.right - drawingRect.left)
        val globalArea = (globalRect.bottom - globalRect.top) * (globalRect.right - globalRect.left)
        val areaShowAll = drawingArea == globalArea
        val showAll = globalRectAllVisible && areaShowAll
        val showRect = (!areaShowAll && globalRectAllVisible && globalRectPartiallyVisible)
        if (tagName.isNotEmpty()) {
            Log.d(
                tagName,
                "container - $containerTop   $containerBottom   $containerLeft   $containerRight   "
            )
            Log.d(
                tagName,
                "globalRect - ${globalRect.top}   ${globalRect.bottom}   ${globalRect.left}   ${globalRect.right}   "
            )
            Log.d(
                tagName,
                "drawingRect - ${drawingRect.top}   ${drawingRect.bottom}   ${drawingRect.left}   ${drawingRect.right}   "
            )
            Log.d(tagName, "drawingArea $drawingArea    globalArea $globalArea")
            Log.d(tagName, "globalRectAllVisible $globalRectAllVisible")
            Log.d(tagName, "globalRectPartiallyVisible $globalRectPartiallyVisible")
            Log.d(tagName, "showAll - $showAll")
            Log.d(tagName, "showRect - $showRect")
        }
        return if (showAll) {
            State.COMPLETELY_VISIBLE
        } else if (showRect) {
            State.PARTIALLY_VISIBLE
        } else {
            State.GONE
        }
    }

    private fun getParentFragment(): Fragment? {
        val fragmentManager = findFragmentManager(context)
        if (fragmentManager != null) {
            val fragments = fragmentManager.fragments
            for (fragment in fragments) {
                val view = fragment.view?.findViewById<View>(id)
                if (view != null) {
                    return fragment
                }
            }
        }
        return null
    }

    private fun findFragmentManager(context: Context): FragmentManager? {
        return (context as? FragmentActivity)?.supportFragmentManager
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        parentFragment = getParentFragment()
        refreshRectInfo()
        handleListener()
        viewTreeObserver.addOnGlobalLayoutListener(onGlobalLayoutListener)
        viewTreeObserver.addOnScrollChangedListener(onScrollChangedListener)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        handleListener()
        viewTreeObserver.removeOnGlobalLayoutListener(onGlobalLayoutListener)
        viewTreeObserver.removeOnScrollChangedListener(onScrollChangedListener)
    }

    private fun handleListener() {
        post {
            val currentState = getCurrentVisibleState()
            if (visibleState == currentState) {
                return@post
            }
            visibleState = currentState
            visibilityListener?.onVisibleStateChange(currentState)
        }
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        windowIsVisible = visibility == VISIBLE
        handleListener()
    }

    fun visibleToUser(allExposure: Boolean = false): Boolean {
        return if (allExposure) {
            getCurrentVisibleState() == State.COMPLETELY_VISIBLE
        } else {
            getCurrentVisibleState() == State.COMPLETELY_VISIBLE || getCurrentVisibleState() == State.PARTIALLY_VISIBLE
        }
    }

    enum class State {
        /**
         * 全部可见
         */
        COMPLETELY_VISIBLE,

        /**
         * 部分可见
         */
        PARTIALLY_VISIBLE,

        /**
         * 不可见
         */
        GONE
    }

    interface VisibilityListener {
        /** 可见性改变回调 */
        fun onVisibleStateChange(state: State)
    }
}