package com.tusur.teacherhelper.presentation.performance

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.widget.Scroller
import androidx.core.graphics.withTranslation
import com.google.android.material.R.attr.colorOnSurface
import com.google.android.material.R.attr.colorOutlineVariant
import com.google.android.material.R.attr.colorSurface
import com.google.android.material.R.attr.colorSurfaceContainerLowest
import com.google.android.material.color.MaterialColors
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.domain.model.TableContent
import kotlin.math.abs


class TableView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attributeSet, defStyleAttr) {

    private val dimens = object {
        val marginStart = resources.getDimension(R.dimen.default_content_margin).toInt()
        var columnsWidth = floatArrayOf()
        val rowHeight = resources.getDimension(R.dimen.table_row_height).toInt()
        val labelHeight = resources.getDimension(R.dimen.table_label_height)
        val totalRowHeight get() = rowHeight * rowCount.toFloat()
        val minCellTextPadding = resources.getDimension(R.dimen.table_cell_horizontal_padding)
        var screenWidth = context.resources.displayMetrics.widthPixels
        val columnCount get() = columnsWidth.count()
        val rowCount get() = content!!.rowCount
    }

    private val paints = object {
        val divider = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = resources.getDimension(R.dimen.table_divider_width)
            color = MaterialColors.getColor(this@TableView, colorOutlineVariant)
        }
        val shadow = Paint(divider).apply {
            val radius = resources.getDimension(R.dimen.table_shadow_radius)
            setShadowLayer(radius, radius / 2, 0f, Color.GRAY)
        }
        val cell = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = MaterialColors.getColor(this@TableView, colorOnSurface)
            textAlign = Paint.Align.CENTER
            textSize = resources.getDimension(R.dimen.table_cell_text_size)
        }
        val label = TextPaint(cell).apply {
            textSize = resources.getDimension(R.dimen.table_label_text_size)
        }
        val leadingColumn = TextPaint(cell).apply {
            textAlign = Paint.Align.LEFT
        }
        val accentBackground = Paint().apply {
            style = Paint.Style.FILL
            color = MaterialColors.getColor(this@TableView, colorSurfaceContainerLowest)
        }
        val defaultBackground = Paint(accentBackground).apply {
            color = MaterialColors.getColor(this@TableView, colorSurface)
        }
    }

    private var content: TableContent<String>? = null

    private var isScrollEnabled = true
    private val scroller = Scroller(context, null, true)
    private var velocityTracker: VelocityTracker? = null
    private val viewConfiguration = ViewConfiguration.get(context)
    private val maximumFlingVelocity = viewConfiguration.scaledMaximumFlingVelocity
    private val minimumFlingVelocity = viewConfiguration.scaledMinimumFlingVelocity
    private var previousScrollerX = 0
    private var scrollState = SCROLL_STATE_IDLE
    private var currentScrollOffset = 0
    private var lastDownOrMoveEventX = 0f
    private var lastDownEventTime = 0L

    private var needToCalculateWidth = true
    private val TextPaint.centerY get() = (descent() + ascent()) / 2

    var onClickListener: OnClickListener? = null

    fun set(tableContent: TableContent<String>) {
        val oldContent = content ?: TableContent.empty()
        if (oldContent == tableContent) return

        if (oldContent.rowCount != tableContent.rowCount ||
            oldContent.columnCount != tableContent.columnCount
        ) {
            content = tableContent
            needToCalculateWidth = true
            requestLayout()
        } else {
            val oldColumnsWidthExpanded = dimens.columnsWidth // expanded old width
            calculateColumnsWidth()
            val oldColumnsWidth = dimens.columnsWidth // not expanded old width
            content = tableContent
            calculateColumnsWidth()
            val newColumnsWidth = dimens.columnsWidth // new content width
            for (i in 0 until oldContent.columnCount) {
                val abs = abs(oldColumnsWidth[i] - newColumnsWidth[i])
                val sum = oldColumnsWidth[i] + newColumnsWidth[i]
                if (abs / (sum / 2) > 0.1f) {
                    requestLayout()
                    return
                }
            }
            // if columns width equals then restore old expanded values
            dimens.columnsWidth = oldColumnsWidthExpanded
            invalidate()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val content = content ?: return setMeasuredDimension(0, 0)
        calculateColumnsWidthIfNeeded()

        dimens.screenWidth = context.resources.displayMetrics.widthPixels

        val minColumnsWidth = dimens.columnsWidth.sum().toInt()
        val dividersWidth = (content.columnCount * paints.divider.strokeWidth).toInt()
        val width = (minColumnsWidth + dividersWidth).coerceAtLeast(dimens.screenWidth)

        if (width == dimens.screenWidth) {
            expandColumnsBounds(dimens.screenWidth - dividersWidth)
        }
        val height = dimens.rowHeight * dimens.rowCount + dimens.labelHeight.toInt()
        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (w < dimens.screenWidth) {
            isScrollEnabled = false
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain()
        }
        velocityTracker!!.addMovement(event)
        val action = event.actionMasked
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                lastDownEventTime = event.eventTime
                if (!scroller.isFinished) {
                    scroller.forceFinished(true)
                    scrollState = SCROLL_STATE_IDLE
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (!isScrollEnabled) {
                    return true
                }

                val currentMoveX = event.x
                if (scrollState != SCROLL_STATE_TOUCH_SCROLL) {
                    if (currentMoveX <= dimens.columnsWidth[0]) {
                        return true
                    }
                    scrollState = SCROLL_STATE_TOUCH_SCROLL
                } else {
                    val deltaMoveX = (currentMoveX - lastDownOrMoveEventX).toInt()
                    scrollBy(deltaMoveX, 0)
                    invalidate()
                }
                lastDownOrMoveEventX = currentMoveX
            }

            MotionEvent.ACTION_UP -> {
                val velocityTracker = velocityTracker!!
                velocityTracker.computeCurrentVelocity(1000, maximumFlingVelocity.toFloat())
                val initialVelocity = velocityTracker.xVelocity.toInt()
                if (abs(initialVelocity) > minimumFlingVelocity) {
                    fling(initialVelocity)
                    scrollState = SCROLL_STATE_FLING
                } else {
                    val deltaTime = event.eventTime - lastDownEventTime
                    if (deltaTime < ViewConfiguration.getTapTimeout()) {
                        performClick(event.x, event.y)
                    }
                    scrollState = SCROLL_STATE_IDLE
                }
                velocityTracker.recycle()
                this.velocityTracker = null
            }
        }
        return true
    }


    override fun computeScroll() {
        if (scroller.isFinished) {
            return
        }
        scroller.computeScrollOffset()
        val currentScrollerX = scroller.currX
        if (previousScrollerX == 0) {
            previousScrollerX = scroller.startX
        }
        scrollBy(currentScrollerX - previousScrollerX, 0)
        previousScrollerX = currentScrollerX
        if (!scroller.isFinished) {
            invalidate()
        }
    }

    override fun scrollBy(x: Int, y: Int) {
        val startScrollOffset = currentScrollOffset
        currentScrollOffset += x
        if (currentScrollOffset > 0) {
            currentScrollOffset = 0
        } else {
            val maxOffset = -(width - dimens.screenWidth)
            if (currentScrollOffset < maxOffset) {
                currentScrollOffset = maxOffset
            }
        }
        if (startScrollOffset != currentScrollOffset) {
            onScrollChanged(0, currentScrollOffset, 0, startScrollOffset)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (content == null) return
        drawScrollableLabels(canvas)
        drawLeadingLabel(canvas)
        canvas.withTranslation(y = dimens.labelHeight) {
            for (i in 0 until dimens.rowCount) {
                drawScrollableRowCells(canvas, rowOrdinal = i)
                drawLeadingCell(canvas, rowOrdinal = i)
                canvas.translate(0f, dimens.rowHeight.toFloat())
            }
        }
        drawDividers(canvas)
    }

    private fun drawLeadingCell(canvas: Canvas, rowOrdinal: Int) {
        canvas.drawRect(
            0f,
            0f,
            dimens.columnsWidth[0],
            dimens.rowHeight.toFloat(),
            getBackgroundPaintFromOrdinal(rowOrdinal)
        )
        canvas.drawText(
            content!![rowOrdinal][0],
            dimens.marginStart.toFloat(),
            dimens.rowHeight / 2f - paints.leadingColumn.centerY,
            paints.leadingColumn
        )
    }

    private fun drawLeadingLabel(canvas: Canvas) {
        canvas.drawRect(
            0f,
            0f,
            dimens.columnsWidth[0],
            dimens.rowHeight.toFloat(),
            paints.defaultBackground
        )
        canvas.drawText(
            content!!.columnLabels[0],
            dimens.columnsWidth[0] / 2f,
            dimens.rowHeight / 2f - paints.label.centerY,
            paints.label
        )
    }

    private fun drawScrollableRowCells(canvas: Canvas, rowOrdinal: Int) {
        canvas.withTranslation {
            if (isAccentColorRow(rowOrdinal)) {
                drawRowBackground(canvas)
            }
            val offset = dimens.columnsWidth[0] + currentScrollOffset.toFloat()
            canvas.translate(offset, 0f)
            for (i in 1 until dimens.columnCount) {
                canvas.drawText(
                    content!![rowOrdinal][i],
                    dimens.columnsWidth[i] / 2f,
                    dimens.rowHeight.toFloat() / 2f - paints.cell.centerY,
                    paints.cell
                )
                canvas.translate(dimens.columnsWidth[i], 0f)
            }
        }
    }

    private fun drawScrollableLabels(canvas: Canvas) {
        canvas.withTranslation {
            val offset = dimens.columnsWidth[0] + currentScrollOffset.toFloat()
            canvas.translate(offset, 0f)
            for (i in 1 until dimens.columnCount) {
                val columnWidth = dimens.columnsWidth[i]
                canvas.drawText(
                    content!!.columnLabels[i],
                    columnWidth / 2f,
                    dimens.rowHeight.toFloat() / 2f - paints.cell.centerY,
                    paints.label
                )
                canvas.translate(columnWidth, 0f)
            }
        }
    }

    private fun drawLeadingColumnDivider(canvas: Canvas, x: Float) {
        canvas.drawLine(
            x,
            dimens.labelHeight,
            x,
            dimens.totalRowHeight + dimens.labelHeight,
            if (currentScrollOffset == 0) {
                paints.divider
            } else {
                paints.shadow
            }
        )
    }

    private fun drawDividers(canvas: Canvas) {
        val leadingColumnWidth = dimens.columnsWidth[0]
        drawLeadingColumnDivider(canvas, leadingColumnWidth)
        canvas.translate(0f, dimens.labelHeight)
        var currentX = leadingColumnWidth + currentScrollOffset
        for (i in 1 until dimens.columnCount) {
            if (currentX > leadingColumnWidth) {
                canvas.drawLine(
                    currentX,
                    0f,
                    currentX,
                    dimens.totalRowHeight,
                    paints.divider
                )
            }
            currentX += dimens.columnsWidth[i]
        }
    }

    private fun drawRowBackground(canvas: Canvas) {
        canvas.drawRect(
            0f,
            0f,
            width.toFloat(),
            dimens.rowHeight.toFloat(),
            paints.accentBackground
        )
    }

    private fun expandColumnsBounds(toSize: Int) {
        val expansionCoefficient = toSize / dimens.columnsWidth.sum()
        for (i in 0 until dimens.columnsWidth.count()) {
            dimens.columnsWidth[i] *= expansionCoefficient
        }
    }

    private fun getBackgroundPaintFromOrdinal(ordinal: Int): Paint {
        return if (isAccentColorRow(ordinal)) {
            paints.accentBackground
        } else {
            paints.defaultBackground
        }
    }

    private fun isAccentColorRow(ordinal: Int): Boolean {
        return ordinal % 2 == 0
    }

    private fun performClick(x: Float, y: Float): Boolean {
        if (y <= dimens.labelHeight) {
            val columnIndex = resolveColumnIndex(x)
            onClickListener?.onLabelClick(columnIndex)
        } else {
            val columnIndex = resolveColumnIndex(x)
            val rowIndex = resolveRowIndex(y)
            onClickListener?.onCellClick(columnIndex, rowIndex)
        }
        return performClick()
    }

    private fun resolveRowIndex(y: Float): Int {
        return ((y - dimens.labelHeight) / dimens.rowHeight).toInt()
    }

    private fun resolveColumnIndex(x: Float): Int {
        var currentIndex = 0
        var currentSum = dimens.columnsWidth[0]
        for (i in 1 until dimens.columnCount) {
            val fixedX = if (x < dimens.columnsWidth[0]) {
                x
            } else {
                x - currentScrollOffset
            }
            if (fixedX < currentSum) {
                return currentIndex
            } else {
                currentSum += dimens.columnsWidth[i]
                ++currentIndex
            }
        }
        return currentIndex
    }

    private fun calculateColumnsWidthIfNeeded() {
        if (!needToCalculateWidth) return
        calculateColumnsWidth()
        needToCalculateWidth = false
    }

    private fun calculateColumnsWidth() {
        dimens.columnsWidth = FloatArray(content!!.columnCount) { i ->
            paints.label.measureText(content!!.columnLabels[i]) + dimens.minCellTextPadding
        }
        content!!.forEach { text, _, column ->
            val textWidth = paints.cell.measureText(text) + dimens.minCellTextPadding
            if (dimens.columnsWidth[column] < textWidth) {
                dimens.columnsWidth[column] = textWidth
            }
        }
    }

    private fun fling(velocityX: Int) {
        previousScrollerX = 0
        val decreasedVelocity = velocityX / 2
        scroller.fling(
            if (velocityX > 0) {
                0
            } else {
                MAX_FLING_X
            },
            0, decreasedVelocity, 0, 0, MAX_FLING_X, 0, 0
        )
    }

    interface OnClickListener {
        fun onCellClick(columnIndex: Int, rowIndex: Int)
        fun onLabelClick(ordinal: Int)
    }

    private companion object {

        /**
         * The view is not scrolling.
         */
        const val SCROLL_STATE_IDLE = 0

        /**
         * The user is scrolling using touch, and their finger is still on the screen.
         */
        const val SCROLL_STATE_TOUCH_SCROLL = 1

        /**
         * The user had previously been scrolling using touch and performed a fling.
         */
        const val SCROLL_STATE_FLING = 2

        const val MAX_FLING_X = Int.MAX_VALUE
    }
}