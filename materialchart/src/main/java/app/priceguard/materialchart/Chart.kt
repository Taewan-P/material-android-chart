package app.priceguard.materialchart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader.TileMode
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.ColorUtils
import app.priceguard.materialchart.data.ChartDataset
import app.priceguard.materialchart.data.GraphMode
import app.priceguard.materialchart.util.Dp
import app.priceguard.materialchart.util.Px
import app.priceguard.materialchart.util.div
import app.priceguard.materialchart.util.minus
import app.priceguard.materialchart.util.plus
import app.priceguard.materialchart.util.times
import app.priceguard.materialchart.util.toDp
import app.priceguard.materialchart.util.toPx
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt


class Chart @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    var dataset: ChartDataset? = null
        set(value) {
            field = value
            if(value?.showXAxis == false){
                yAxisMargin = zeroDp
                yGraphPadding = zeroDp
            }
            if(value?.showYAxis == false) {
                xAxisMargin = zeroDp
                xGraphPadding = zeroDp
            }
            invalidate()
        }

    // Margin: Empty space from the view to the graph on the outside. This includes the other side as well (Like horizontal & vertical margins)
    var xAxisMargin: Dp = Dp(32F)
        set(value) {
            field = value
            invalidate()
        }

    var yAxisMargin: Dp = Dp(32F)
        set(value) {
            field = value
            invalidate()
        }

    // Padding: Empty space from the graph on the inside.
    var xAxisPadding: Dp = xAxisMargin / Dp(2F)
    var yAxisPadding: Dp = yAxisMargin

    var xGraphPadding: Dp = Dp(32F)
    var yGraphPadding: Dp = Dp(32F)

    // Spacing: Empty space for each axis value. Depending on this value, the data may not fully show.
    // If it doesn't fit, you should change the width & height of the chart view.
    var xAxisSpacing: Dp = Dp(32F)
    var yAxisSpacing: Dp = Dp(32F)

    var axisStrokeWidth = 3f

    // Tick: lines that are shown in axis with data labels
    var halfTickLength: Dp = Dp(4F)
    var gridLineStrokeWidth = 6f

    // ZeroDp: Delete Padding. 1dp for show lines in corners and edges
    var zeroDp = Dp(1F)

    private var pointX = 0f
    private var isDragging = false

    // Use Android theme
    private var colorPrimary: Int
    private var colorSecondary: Int
    private var colorError: Int
    private var colorSurface: Int
    private var colorOnSurface: Int
    private var colorPrimaryContainer: Int
    private var colorOnPrimaryContainer: Int

    private val paint = Paint()
    private val xAxisPaint = Paint(paint)
    private val yAxisPaint = Paint(paint)
    private val linesPaint = Paint(paint)
    private val gradientPaint = Paint(paint)
    private val gradientCoverPaint = Paint(paint)
    private val circlePaint = Paint(paint)
    private val textLabelPaint = Paint(paint)
    private val textRectPaint = Paint(paint)
    private val gridLinePaint = Paint(paint)

    private val bounds = Rect()

    init {
        val typedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.Chart,
            defStyleAttr,
            0
        )

        colorPrimary = typedArray.getColor(
            R.styleable.Chart_colorPrimary,
            Color.BLACK
        )

        colorSecondary = typedArray.getColor(
            R.styleable.Chart_colorSecondary,
            Color.GRAY
        )

        colorError = typedArray.getColor(
            R.styleable.Chart_colorError,
            Color.RED
        )

        colorSurface = typedArray.getColor(
            R.styleable.Chart_colorSurface,
            Color.WHITE
        )

        colorOnSurface = typedArray.getColor(
            R.styleable.Chart_colorOnSurface,
            Color.BLACK
        )

        colorPrimaryContainer = typedArray.getColor(
            R.styleable.Chart_colorPrimaryContainer,
            Color.BLACK
        )

        colorOnPrimaryContainer = typedArray.getColor(
            R.styleable.Chart_colorOnPrimaryContainer,
            Color.WHITE
        )

        setBackgroundColor(colorSurface)

        typedArray.recycle()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //drawLine() and DrawGridLine() must be call after margin set
        //and call before drawXAxis and drawYAxis be called
        dataset ?: return

        drawLine(canvas)
        drawGridLine(canvas, gridLinePaint)

        if (dataset?.showXAxis == true) {
            drawXAxis(canvas, xAxisPaint)
        }
        if (dataset?.showYAxis == true) {
            drawYAxis(canvas, yAxisPaint)
        }
        if (isDragging) {
            drawPointAndLabel(canvas)
        }
    }

    private fun drawXAxis(canvas: Canvas, paint: Paint) {
        if (dataset == null) return

        // Get chart data & max/min value
        val chartData = dataset?.data ?: return

        val maxValue = chartData.maxOf { it.x }
        val minValue = chartData.minOf { it.x }

        val difference = if (maxValue == minValue) {
            maxValue
        } else {
            maxValue - minValue
        }

        // Calculate available axis space
        val availableSpace: Dp = Px(width.toFloat()).toDp(context) - xAxisMargin * Dp(2F)

        // Calculate axis space that labels are actually drawn
        val availableLabelSpace: Dp = availableSpace - xAxisPadding

        // Number of ticks
        // ~ 150dp : 3 (max, min, 50%)
        // ~ 250dp : 5 (max, min, 25%, 50%, 75%)
        // 250dp ~ : Auto
        val availableLabels = when {
            availableLabelSpace.value <= 150F -> 3
            availableLabelSpace.value <= 250F -> 5
            else -> {
                (availableLabelSpace / yAxisSpacing).value.toInt()
            }
        }

        // Calculate how much each ticks should represent
        val unit = roundToSecondSignificantDigit(difference / (availableLabels - 1).toFloat())

        val actualSpacing = availableLabelSpace * Dp(unit / difference)

        // Calculate how much labels are actually needed & override spacing
        val neededLabels = if (availableLabels <= 5) {
            availableLabels
        } else {
            (availableLabelSpace / actualSpacing).value.roundToInt()
        }

        // Draw Axis
        val axisStartPointX: Px = xAxisMargin.toPx(context)
        val axisStartPointY: Px = Px(height.toFloat()) - yAxisMargin.toPx(context)
        val axisEndPointX: Px = (xAxisMargin + availableSpace).toPx(context)
        val axisEndPointY: Px = Px(height.toFloat()) - yAxisMargin.toPx(context)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = axisStrokeWidth
        paint.color = colorOnSurface
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND

        canvas.drawLine(
            axisStartPointX.value,
            axisStartPointY.value,
            axisEndPointX.value,
            axisEndPointY.value,
            paint
        )

        // Draw ticks & labels
        paint.strokeWidth = 2F
        paint.typeface = Typeface.DEFAULT
        paint.textSize = 24F

        val tickStartPointY: Px = (axisStartPointY.toDp(context) - halfTickLength).toPx(context)
        val tickEndPointY: Px = (axisStartPointY.toDp(context) + halfTickLength).toPx(context)

        // Draw min & max first
        val minPointX: Px = axisStartPointX
        val maxPointX: Px = axisEndPointX - xAxisPadding.toPx(context)


        val minLabel: String = convertTimeStampToDate(minValue, dataset?.graphMode ?: GraphMode.DAY)
        val maxLabel: String = convertTimeStampToDate(maxValue, dataset?.graphMode ?: GraphMode.DAY)

        if (minValue == maxValue) {
            drawAxisTick(canvas, maxPointX, tickStartPointY, maxPointX, tickEndPointY, paint)
            drawXAxisLabelText(canvas, maxLabel, maxPointX, tickEndPointY, Dp(8F), paint)
            return
        }

        drawAxisTick(canvas, minPointX, tickStartPointY, minPointX, tickEndPointY, paint)
        drawAxisTick(canvas, maxPointX, tickStartPointY, maxPointX, tickEndPointY, paint)
        drawXAxisLabelText(canvas, minLabel, minPointX, tickEndPointY, Dp(8F), paint)
        drawXAxisLabelText(canvas, maxLabel, maxPointX, tickEndPointY, Dp(8F), paint)

        (neededLabels - 1 downTo 1).forEach { idx ->
            val tickPointX: Px = maxPointX - actualSpacing.toPx(context) * Px(idx.toFloat())

            val labelString =
                convertTimeStampToDate(maxValue - idx * unit, dataset?.graphMode ?: GraphMode.DAY)
            if (tickPointX.value >= axisStartPointX.value) {
                drawAxisTick(canvas, tickPointX, tickStartPointY, tickPointX, tickEndPointY, paint)
                drawXAxisLabelText(canvas, labelString, tickPointX, tickEndPointY, Dp(8F), paint)
            }
        }
    }

    private fun drawYAxis(canvas: Canvas, paint: Paint) {
        dataset ?: return

        // Get chart data & max/min value
        val chartData = dataset?.data ?: return

        val maxValue = chartData.maxOf { it.y }
        val minValue = chartData.minOf { it.y }

        val difference = if (maxValue == minValue) {
            maxValue
        } else {
            maxValue - minValue
        }

        // Calculate available axis space
        val availableSpace: Dp = Px(height.toFloat()).toDp(context) - yAxisMargin * Dp(2F)

        // Calculate axis space that labels are actually drawn
        val availableLabelSpace: Dp = availableSpace - yAxisPadding

        // Number of ticks
        // ~ 150dp : 3 (max, min, 50%)
        // ~ 250dp : 5 (max, min, 25%, 50%, 75%)
        // 250dp ~ : Auto
        val availableLabels = when {
            availableLabelSpace.value <= 150F -> 3
            availableLabelSpace.value <= 250F -> 5
            else -> {
                (availableLabelSpace / yAxisSpacing).value.toInt()
            }
        }

        val unit = roundToSecondSignificantDigit(difference / (availableLabels - 1).toFloat())

        val actualSpacing = availableLabelSpace * Dp(unit / difference)

        // Calculate how much labels are actually needed & override spacing
        val neededLabels = if (availableLabels <= 5) {
            availableLabels
        } else {
            (availableLabelSpace / actualSpacing).value.roundToInt()
        }

        // Draw Axis
        val axisStartPointX: Px = xAxisMargin.toPx(context)
        val axisStartPointY: Px = (yAxisMargin + availableSpace).toPx(context)
        val axisEndPointX: Px = xAxisMargin.toPx(context)
        val axisEndPointY: Px = yAxisMargin.toPx(context)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = axisStrokeWidth
        paint.color = colorOnSurface
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND

        canvas.drawLine(
            axisStartPointX.value,
            axisStartPointY.value,
            axisEndPointX.value,
            axisEndPointY.value,
            paint
        )

        // Draw ticks & labels
        paint.strokeWidth = 2F
        paint.typeface = Typeface.DEFAULT
        paint.textSize = 24F

        val tickStartPointX: Px = (axisStartPointX.toDp(context) - halfTickLength).toPx(context)
        val tickEndPointX: Px = (axisStartPointX.toDp(context) + halfTickLength).toPx(context)

        // Draw min & max first
        val minPointY: Px = (axisStartPointY.toDp(context) - yAxisPadding / Dp(2F)).toPx(context)
        val maxPointY: Px = (axisEndPointY.toDp(context) + yAxisPadding / Dp(2F)).toPx(context)

        if (minValue == maxValue) {
            val middlePointY = (minPointY + maxPointY) / Px(2F)
            drawAxisTick(canvas, tickStartPointX, middlePointY, tickEndPointX, middlePointY, paint)
            drawYAxisLabelText(canvas, maxValue, tickStartPointX, middlePointY, Dp(8F), paint)
            return
        }

        drawAxisTick(canvas, tickStartPointX, minPointY, tickEndPointX, minPointY, paint)
        drawAxisTick(canvas, tickStartPointX, maxPointY, tickEndPointX, maxPointY, paint)
        drawYAxisLabelText(canvas, minValue, tickStartPointX, minPointY, Dp(8F), paint)
        drawYAxisLabelText(canvas, maxValue, tickStartPointX, maxPointY, Dp(8F), paint)

        // Draw remaining ticks
        (1 until neededLabels).forEach { idx ->
            val tickPointY: Px = minPointY - actualSpacing.toPx(context) * Px(idx.toFloat())

            if (tickPointY.value >= maxPointY.value) {
                drawAxisTick(canvas, tickStartPointX, tickPointY, tickEndPointX, tickPointY, paint)
                drawYAxisLabelText(
                    canvas,
                    minValue + unit * idx,
                    tickStartPointX,
                    tickPointY,
                    Dp(8F),
                    paint
                )
            }
        }
    }

    private fun drawAxisTick(
        canvas: Canvas,
        tickStartX: Px,
        tickStartY: Px,
        tickEndX: Px,
        tickEndY: Px,
        paint: Paint
    ) {
        canvas.drawLine(
            tickStartX.value,
            tickStartY.value,
            tickEndX.value,
            tickEndY.value,
            paint
        )
    }

    private fun drawXAxisLabelText(
        canvas: Canvas,
        label: String,
        startPointX: Px,
        startPointY: Px,
        marginTop: Dp,
        paint: Paint
    ) {
        paint.getTextBounds(label, 0, label.length, bounds)
        val textWidth = Px(bounds.width().toFloat())
        val textHeight = Px(bounds.height().toFloat())

        val labelStartPointX: Px = startPointX - textWidth / Px(2F)
        val labelStartPointY: Px = startPointY + marginTop.toPx(context) + textHeight

        canvas.drawText(label, labelStartPointX.value, labelStartPointY.value, paint)
    }

    private fun drawYAxisLabelText(
        canvas: Canvas,
        label: Float,
        startPointX: Px,
        startPointY: Px,
        marginEnd: Dp,
        paint: Paint
    ) {
        val labelString = if (label - label.toInt() > 1e-6) {
            String.format("%.1f", label)
        } else {
            label.toInt().toString()
        }

        paint.getTextBounds(labelString, 0, labelString.length, bounds)
        val textWidth = Px(bounds.width().toFloat())
        val textHeight = Px(bounds.height().toFloat())

        val labelStartPointX: Px =
            (startPointX.toDp(context) - marginEnd - textWidth.toDp(context)).toPx(context)
        val labelStartPointY: Px =
            (startPointY.toDp(context) + textHeight.toDp(context) / Dp(2F)).toPx(context)

        canvas.drawText(labelString, labelStartPointX.value, labelStartPointY.value, paint)
    }

    private fun drawLine(canvas: Canvas) {
        dataset ?: return

        val chartData = dataset?.data!!
        val size = chartData.size

        val maxX = chartData.maxOf { it.x }
        val minX = chartData.minOf { it.x }
        val maxY = chartData.maxOf { it.y }
        val minY = chartData.minOf { it.y }

        val spaceX = if (maxX - minX > 0) maxX - minX else maxX
        val spaceY = if (maxY - minY > 0) maxY - minY else maxY

        val graphSpaceStartX = calculateXAxisFirstTick()
        val graphSpaceEndX = calculateXAxisLastTick()
        val graphSpaceStartY = calculateYAxisFirstAndLastTick().second
        val graphSpaceEndY = calculateYAxisFirstAndLastTick().first

        val gradationEndY = Px(height.toFloat()) - yAxisMargin.toPx(context)

        val graphWidth = graphSpaceEndX - graphSpaceStartX
        val graphHeight = graphSpaceEndY - graphSpaceStartY

        val chartSpaceEndY = Px(height.toFloat()) - yAxisMargin.toPx(context) - Px(axisStrokeWidth)

        // Set gradation position, color, mode
        gradientPaint.shader =
            LinearGradient(
                graphSpaceStartX.value,
                graphSpaceStartY.value,
                graphSpaceStartX.value,
                gradationEndY.value,
                ColorUtils.setAlphaComponent(colorPrimary, 180),
                Color.TRANSPARENT,
                TileMode.CLAMP
            )

        // Fill gradation
        canvas.drawRect(
            graphSpaceStartX.value,
            graphSpaceStartY.value + 1f,
            graphSpaceEndX.value,
            chartSpaceEndY.value,
            gradientPaint
        )

        // Set gradation cover paint
        gradientCoverPaint.style = Paint.Style.FILL
        gradientCoverPaint.color = colorSurface

        // Set lines paint
        linesPaint.style = Paint.Style.FILL
        linesPaint.strokeWidth = 6F

        chartData.forEachIndexed { index, data ->
            if (index < size - 1) {
                val next = chartData[index + 1]

                // Calculate position of each data
                val startX = Px((data.x - minX) / spaceX) * graphWidth + graphSpaceStartX
                val startY = Px(1 - (data.y - minY) / spaceY) * graphHeight + graphSpaceStartY
                val endX = Px((next.x - minX) / spaceX) * graphWidth + graphSpaceStartX

                // Hide the area that doesn't require a gradation
                canvas.drawRect(
                    startX.value - 1F,
                    0F,
                    endX.value + 1F,
                    startY.value,
                    gradientCoverPaint
                )
            }
        }

        linesPaint.strokeJoin = Paint.Join.ROUND
        linesPaint.strokeCap = Paint.Cap.ROUND

        chartData.forEachIndexed { index, data ->
            if (index < size - 1) {
                val next = chartData[index + 1]
                linesPaint.color = if (data.valid) {
                    colorPrimary
                } else {
                    colorError
                }

                // Calculate position of each data
                val startX = Px((data.x - minX) / spaceX) * graphWidth + graphSpaceStartX
                val startY = Px(1 - (data.y - minY) / spaceY) * graphHeight + graphSpaceStartY
                val endX = Px((next.x - minX) / spaceX) * graphWidth + graphSpaceStartX
                val endY = Px(1 - (next.y - minY) / spaceY) * graphHeight + graphSpaceStartY

                canvas.drawLine(startX.value, startY.value, endX.value, startY.value, linesPaint)
                canvas.drawLine(endX.value, startY.value, endX.value, endY.value, linesPaint)
            }
        }
    }

    private fun drawPointAndLabel(canvas: Canvas) {
        dataset ?: return

        val chartData = dataset?.data!!
        val size = chartData.size

        val maxX = chartData.maxOf { it.x }
        val minX = chartData.minOf { it.x }
        val maxY = chartData.maxOf { it.y }
        val minY = chartData.minOf { it.y }

        val spaceX = if (maxX - minX > 0) {
            maxX - minX
        } else {
            maxX
        }
        val spaceY = if (maxY - minY > 0) {
            maxY - minY
        } else {
            maxY
        }

        val graphSpaceStartX = calculateXAxisFirstTick()
        val graphSpaceEndX = calculateXAxisLastTick()
        val graphSpaceStartY = calculateYAxisFirstAndLastTick().second
        val graphSpaceEndY = calculateYAxisFirstAndLastTick().first

        val graphWidth = graphSpaceEndX - graphSpaceStartX
        val graphHeight = graphSpaceEndY - graphSpaceStartY

        chartData.forEachIndexed { index, data ->
            if (index < size - 1) {
                val next = chartData[index + 1]

                // Calculate position of each data
                val startX = Px((data.x - minX) / spaceX) * graphWidth + graphSpaceStartX
                val startY = Px(1 - (data.y - minY) / spaceY) * graphHeight + graphSpaceStartY
                val endX = Px((next.x - minX) / spaceX) * graphWidth + graphSpaceStartX

                val circleSize = Dp(8f).toPx(context)
                if (startX.value < pointX && endX.value > pointX) {

                    circlePaint.style = Paint.Style.FILL
                    circlePaint.color = colorPrimary
                    canvas.drawCircle(pointX, startY.value, circleSize.value / 2, circlePaint)

                    val text = convertToText(data.y)

                    textLabelPaint.typeface = Typeface.DEFAULT
                    textLabelPaint.textSize = 48F
                    textLabelPaint.color = colorOnPrimaryContainer
                    textLabelPaint.getTextBounds(text, 0, text.length, bounds)

                    val labelRectPaddingVertical = Dp(8F).toPx(context)
                    val labelRectPaddingHorizontal = Dp(8F).toPx(context)
                    val distanceTextAndPoint = circleSize + Dp(8F).toPx(context)

                    val rectWidth = bounds.width()
                    val rectHeight = bounds.height()
                    val rect = RectF(
                        pointX - rectWidth / 2 - labelRectPaddingHorizontal.value,
                        startY.value - rectHeight - distanceTextAndPoint.value - labelRectPaddingVertical.value,
                        pointX + rectWidth / 2 + labelRectPaddingHorizontal.value,
                        startY.value - distanceTextAndPoint.value + labelRectPaddingVertical.value
                    )

                    textRectPaint.color = colorPrimaryContainer

                    canvas.drawRoundRect(rect, 10f, 10f, textRectPaint)
                    canvas.drawText(
                        text,
                        pointX - bounds.width() / 2 - 4F,
                        startY.value - distanceTextAndPoint.value,
                        textLabelPaint
                    )
                }
            }
        }

    }

    private fun convertToText(num: Float): String {
        return if (num.toInt().toFloat() == num) {
            num.toInt().toString()
        } else {
            num.toString()
        }
    }

    private fun calculateXAxisFirstTick(): Px {
        return xAxisMargin.toPx(context)
    }

    private fun calculateXAxisLastTick(): Px {
        val availableSpace: Dp = Px(width.toFloat()).toDp(context) - xAxisMargin * Dp(2F)
        return (xAxisMargin + availableSpace - xAxisPadding).toPx(context)
    }

    private fun calculateYAxisFirstAndLastTick(): Pair<Px, Px> {
        val chartData = dataset?.data!!

        val maxY = chartData.maxOf { it.y }
        val minY = chartData.minOf { it.y }

        val availableSpace: Dp = Px(height.toFloat()).toDp(context) - yAxisMargin * Dp(2F)
        val axisStartPointY: Px = (yAxisMargin + availableSpace).toPx(context)
        val axisEndPointY: Px = yAxisMargin.toPx(context)

        val minPointY: Px = (axisStartPointY.toDp(context) - yAxisPadding / Dp(2F)).toPx(context)
        val maxPointY: Px = (axisEndPointY.toDp(context) + yAxisPadding / Dp(2F)).toPx(context)

        if (maxY == minY) {
            val middlePointY = (minPointY + maxPointY) / Px(2F)
            return Pair(middlePointY, middlePointY)
        }

        return Pair(minPointY, maxPointY)
    }

    private fun convertTimeStampToDate(timestamp: Float, mode: GraphMode): String {
        val date = Date((timestamp * 1000).toLong())

        val format = when (mode) {
            GraphMode.DAY -> SimpleDateFormat("HH:mm", Locale.getDefault())
            GraphMode.WEEK -> SimpleDateFormat("MM/dd", Locale.getDefault())
            GraphMode.MONTH -> SimpleDateFormat("MM/dd", Locale.getDefault())
            GraphMode.QUARTER -> SimpleDateFormat("yyyy/MM", Locale.getDefault())
        }

        return format.format(date)
    }

    private fun roundToSecondSignificantDigit(number: Float): Float {
        if (number == 0F) {
            return 0F
        }

        val digit = floor(log10(number))
        val power = 10F.pow(digit)

        val result = (number / power).roundToInt() * power

        if (result < number) {
            return result + power
        }

        return result
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (dataset?.isInteractive != true) {
            return false
        }
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                parent.requestDisallowInterceptTouchEvent(true)
                isDragging = true
            }

            MotionEvent.ACTION_MOVE -> {
                pointX = event.x
                invalidate()
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                parent.requestDisallowInterceptTouchEvent(false)
                isDragging = false
                invalidate()
            }
        }
        return true
    }

    private fun drawGridLine(canvas: Canvas, paint: Paint) {

        // Get chart data & max/min value
        val chartData = dataset?.data ?: return

        val maxY = chartData.maxOf { it.y }
        val minY = chartData.minOf { it.y }
        val spaceY = maxY - minY

        val graphSpaceStartY = calculateYAxisFirstAndLastTick().second
        val graphSpaceEndY = calculateYAxisFirstAndLastTick().first

        val graphHeight = graphSpaceEndY - graphSpaceStartY

        // Calculate available axis space
        val availableSpace: Dp = Px(width.toFloat()).toDp(context) - xAxisMargin * Dp(2F)

        //Draw GridLines
        dataset?.gridLines?.sortedBy { it.value * -1 }?.forEach { data ->
            val lineHeight: Px =
                Px(1 - (data.value - minY) / spaceY) * graphHeight + graphSpaceStartY
            if (minY > data.value || data.value > maxY) {
                return@forEach
            }
            val axisStartPointX: Px = (xAxisMargin).toPx(context)
            val axisEndPointX: Px = (xAxisMargin + availableSpace).toPx(context)

            //Set paint
            val dashPath = DashPathEffect(floatArrayOf(25f, 5f), 2f)
            paint.style = Paint.Style.STROKE
            paint.pathEffect = dashPath
            paint.strokeWidth = gridLineStrokeWidth
            paint.color = colorSecondary

            canvas.drawLine(
                axisStartPointX.value,
                lineHeight.value,
                axisEndPointX.value,
                lineHeight.value,
                paint
            )

            // Draw ticks & labels
            paint.strokeWidth = 2F
            paint.pathEffect = null
            paint.typeface = Typeface.DEFAULT
            paint.textSize = 24F
            paint.color = colorOnSurface

            val labelString = data.name

            paint.getTextBounds(labelString, 0, labelString.length, bounds)
            val textWidth = Px(bounds.width().toFloat())
            val textHeight = Px(bounds.height().toFloat())

            val labelStartPointX: Px = axisEndPointX - textWidth
            val labelStartPointY: Px = lineHeight - textHeight

            canvas.drawText(data.name, labelStartPointX.value, labelStartPointY.value, paint)
        }
    }

    fun setXAxisMargin(value: Float) {
        xAxisMargin = Dp(value)
    }

    fun setYAxisMargin(value: Float) {
        yAxisMargin = Dp(value)
    }
}