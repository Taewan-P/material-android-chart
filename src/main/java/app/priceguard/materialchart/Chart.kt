package app.priceguard.materialchart

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader.TileMode
import android.graphics.Rect
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.appcompat.widget.ThemeUtils
import androidx.core.content.ContextCompat
import app.priceguard.materialchart.data.ChartDataset
import app.priceguard.materialchart.util.Dp
import app.priceguard.materialchart.util.Px
import app.priceguard.materialchart.util.div
import app.priceguard.materialchart.util.minus
import app.priceguard.materialchart.util.plus
import app.priceguard.materialchart.util.times
import app.priceguard.materialchart.util.toDp
import app.priceguard.materialchart.util.toPx
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

    // Padding: Empty space from the view to the graph. This includes the other side as well (Like horizontal paddings & vertical paddings)
    var xAxisPadding: Dp = Dp(32F)
    var yAxisPadding: Dp = Dp(32F)

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

    // Use Android theme
    private var colorPrimary: Int
    private var colorSecondary: Int
    private var colorError: Int
    private var colorSurface: Int
    private var colorOnSurface: Int

    private val paint = Paint()
    private val xAxisPaint = Paint(paint)
    private val yAxisPaint = Paint(paint)
    private val linesPaint = Paint(paint)
    private val gradientPaint = Paint(paint)
    private val gradientCoverPaint = Paint(paint)
    private val gridLinePaint = Paint(paint)

    private val bounds = Rect()

    init {
        // TODO: 그래프 영역 선언, 캔버스 생성

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
            Color.BLACK
        )

        colorSurface = typedArray.getColor(
            R.styleable.Chart_colorSurface,
            Color.WHITE
        )

        colorOnSurface = typedArray.getColor(
            R.styleable.Chart_colorOnSurface,
            Color.BLACK
        )

        typedArray.recycle()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawLine(canvas)
        drawGridLine(canvas, paint)
        drawXAxis(canvas, xAxisPaint)
        drawYAxis(canvas, yAxisPaint)
    }

    private fun drawXAxis(canvas: Canvas, paint: Paint) {
        if(dataset == null) return

        // Get chart data & max/min value
        val chartData = dataset?.data ?: return

        val maxValue = chartData.maxOf { it.x }
        val minValue = chartData.minOf { it.x }

        // Calculate available axis space
        val availableSpace: Dp = Px(width.toFloat()).toDp(context) - xAxisPadding * Dp(2F)

        // Calculate how much ticks can be drawn (Based on spacing settings above)
        val availableLabels = (availableSpace / xAxisSpacing).value.toInt()

        // Calculate how much each ticks should represent
        val unit = roundToSecondSignificantDigit((maxValue - minValue) / (availableLabels - 1).toFloat())

        // Draw Axis
        val axisStartPointX: Px = xAxisPadding.toPx(context)
        val axisStartPointY: Px = Px(height.toFloat()) - yAxisPadding.toPx(context)
        val axisEndPointX: Px = (xAxisPadding + availableSpace).toPx(context)
        val axisEndPointY: Px = Px(height.toFloat()) - yAxisPadding.toPx(context)

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

        (0.. availableLabels).forEach { idx ->
            val startPointX: Px = (axisStartPointX.toDp(context) + xAxisSpacing * Dp(idx.toFloat())).toPx(context)
            val startPointY: Px = (axisStartPointY.toDp(context) - halfTickLength).toPx(context)
            val endPointX: Px = (axisStartPointX.toDp(context) + xAxisSpacing * Dp(idx.toFloat())).toPx(context)
            val endPointY: Px = (axisStartPointY.toDp(context) + halfTickLength).toPx(context)
            
            canvas.drawLine(
                startPointX.value,
                startPointY.value,
                endPointX.value,
                endPointY.value,
                paint
            )

            val label = minValue + (unit * idx.toFloat() * 100.0).roundToInt() / 100.0
            val labelString = if (label - label.toInt() > 1e-6) {
                label.toString()
            } else {
                label.toInt().toString()
            }
            Log.d("label", labelString)

            paint.getTextBounds(labelString, 0, labelString.length, bounds)
            val textWidth = Px(bounds.width().toFloat())
            val textHeight = Px(bounds.height().toFloat())

            val labelStartPointX: Px = (axisStartPointX.toDp(context) + xAxisSpacing * Dp(idx.toFloat()) - (textWidth / Px(2F)).toDp(context)).toPx(context)
            val labelStartPointY: Px = (axisStartPointY.toDp(context) + halfTickLength + Dp(8F) + textHeight.toDp(context)).toPx(context)

            canvas.drawText(labelString, labelStartPointX.value, labelStartPointY.value, paint)
        }
    }

    private fun drawYAxis(canvas: Canvas, paint: Paint) {
        if(dataset == null) return

        // Get chart data & max/min value
        val chartData = dataset?.data ?: return

        val maxValue = chartData.maxOf { it.y }
        val minValue = chartData.minOf { it.y }

        // Calculate available axis space
        val availableSpace: Dp = Px(height.toFloat()).toDp(context) - yAxisPadding * Dp(2F)

        // Calculate how much ticks can be drawn (Based on spacing settings above)
        val availableLabels = (availableSpace / yAxisSpacing).value.toInt()

        // Calculate how much each ticks should represent
        val unit = roundToSecondSignificantDigit((maxValue - minValue) / (availableLabels - 1).toFloat())

        // Draw Axis
        val axisStartPointX: Px = xAxisPadding.toPx(context)
        val axisStartPointY: Px = (yAxisPadding + availableSpace).toPx(context)
        val axisEndPointX: Px = xAxisPadding.toPx(context)
        val axisEndPointY: Px = yAxisPadding.toPx(context)

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
      
        (1..availableLabels).forEach { idx ->
            val startPointX: Px = (axisStartPointX.toDp(context) - halfTickLength).toPx(context)
            val startPointY: Px = (axisStartPointY.toDp(context) - yAxisSpacing * Dp(idx.toFloat())).toPx(context)
            val endPointX: Px = (axisStartPointX.toDp(context) + halfTickLength).toPx(context)
            val endPointY: Px = (axisStartPointY.toDp(context) - yAxisSpacing * Dp(idx.toFloat())).toPx(context)

            canvas.drawLine(
                startPointX.value,
                startPointY.value,
                endPointX.value,
                endPointY.value,
                paint
            )

            val label = minValue + (unit * idx.toFloat() * 100.0).roundToInt() / 100.0
            val labelString = if (label - label.toInt() > 1e-6) {
                label.toString()
            } else {
                label.toInt().toString()
            }
            Log.d("label", labelString)

            paint.getTextBounds(labelString, 0, labelString.length, bounds)
            val textWidth = Px(bounds.width().toFloat())
            val textHeight = Px(bounds.height().toFloat())

            val labelStartPointX: Px = (axisStartPointX.toDp(context) - halfTickLength - Dp(8F) - textWidth.toDp(context)).toPx(context)
            val labelStartPointY: Px = (axisStartPointY.toDp(context) - yAxisSpacing * Dp(idx.toFloat()) + textHeight.toDp(context) / Dp(2F)).toPx(context)

            canvas.drawText(labelString, labelStartPointX.value, labelStartPointY.value, paint)
        }
    }

    private fun drawLine(canvas: Canvas) {
        if (dataset == null) return

        val chartData = dataset?.data!!
        val size = chartData.size

        val maxX = chartData.maxOf { it.x }
        val minX = chartData.minOf { it.x }
        val maxY = chartData.maxOf { it.y }
        val minY = chartData.minOf { it.y }

        val spaceX = maxX - minX
        val spaceY = maxY - minY

        // 축과 그래프 사이 빈틈 제거 위해 spaceStart 1f 빼기
        val graphSpaceStartX = xAxisPadding.toPx(context) + Px(axisStrokeWidth) - Px(1f)
        val graphSpaceEndX = Px(width.toFloat()) - xAxisPadding.toPx(context) - xGraphPadding.toPx(context)
        val graphSpaceStartY = yAxisPadding.toPx(context) + yGraphPadding.toPx(context) - Px(1f)
        val graphSpaceEndY = Px(height.toFloat()) - yAxisPadding.toPx(context) - yGraphPadding.toPx(context)

        val graphWidth = graphSpaceEndX - graphSpaceStartX
        val graphHeight = graphSpaceEndY - graphSpaceStartY

        val chartSpaceEndY = Px(height.toFloat()) - yAxisPadding.toPx(context) - Px(axisStrokeWidth)

        // 그라데이션 위치, 색상, 모드 설정
        gradientPaint.shader =
            LinearGradient(
                graphSpaceStartX.value,
                graphSpaceStartY.value,
                graphSpaceStartX.value,
                chartSpaceEndY.value,
                Color.RED,
                Color.WHITE,
                TileMode.CLAMP
            )

        // 그래프 전체를 그라데이션으로 칠하기
        canvas.drawRect(
            graphSpaceStartX.value,
            graphSpaceStartY.value,
            graphSpaceEndX.value,
            chartSpaceEndY.value,
            gradientPaint
        )

        // 그라데이션이 필요 없는 부분을 덮어버리는 paint 설정
        gradientCoverPaint.style = Paint.Style.FILL
        gradientCoverPaint.color = colorPrimary

        // 선 그리는 paint 설정
        linesPaint.style = Paint.Style.FILL
        linesPaint.strokeWidth = 1F
        linesPaint.color = Color.RED

        chartData.forEachIndexed { index, data ->
            if (index < size - 1) {
                val next = chartData[index + 1]

                // 각 데이터를 그릴 시작과 끝 위치 계산
                val startX = Px((data.x - minX) / spaceX) * graphWidth + graphSpaceStartX
                val startY = Px(1 - (data.y - minY) / spaceY) * graphHeight + graphSpaceStartY
                val endX = Px((next.x - minX) / spaceX) * graphWidth + graphSpaceStartX
                val endY = Px(1 - (next.y - minY) / spaceY) * graphHeight + graphSpaceStartY

                // 그라데이션 필요 없는 부분 덮어버리기 ( Rect 사이로 그라데이션이 보이는 걸 방지하기 위해 endX 1f 값 더함)
                canvas.drawRect(
                    startX.value,
                    graphSpaceStartY.value,
                    endX.value + 1f,
                    startY.value,
                    gradientCoverPaint
                )

                // 그래프 선 그리기
                canvas.drawLine(startX.value, startY.value, endX.value, startY.value, linesPaint)
                canvas.drawLine(endX.value, startY.value, endX.value, endY.value, linesPaint)
            }
        }
    }
    
    private fun roundToSecondSignificantDigit(number: Float): Float {
        if (number == 0F) {
            return 0F
        }

        val digit = floor(log10(number))
        val power = 10F.pow(digit)

        return (number / power).roundToInt() * power
    }

    private fun drawGridLine(canvas: Canvas, paint: Paint) {
        if(dataset == null) return

        // Get chart data & max/min value
        val chartData = dataset?.data ?: return

        val maxY = chartData.maxOf { it.y }
        val minY = chartData.minOf { it.y }
        val spaceY = maxY - minY

        val graphSpaceStartY = yAxisPadding.toPx(context) + yGraphPadding.toPx(context) - Px(1f)
        val graphSpaceEndY = Px(height.toFloat()) - yAxisPadding.toPx(context) - yGraphPadding.toPx(context)

        val graphHeight = graphSpaceEndY - graphSpaceStartY

        // Calculate available axis space
        val availableSpace: Dp = Px(width.toFloat()).toDp(context) - xAxisPadding * Dp(2F)

        //Set paint
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = gridLineStrokeWidth
        paint.color = colorSecondary

        //Draw GridLines
        dataset?.gridLines?.forEach { data ->
            val axisStartPointX: Px = xAxisPadding.toPx(context)
            val axisStartPointY: Px = Px(1 - (data.value - minY) / spaceY) * graphHeight + graphSpaceStartY
            val axisEndPointX: Px = (xAxisPadding + availableSpace).toPx(context)
            val axisEndPointY: Px = Px(1 - (data.value - minY) / spaceY) * graphHeight + graphSpaceStartY

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

            val labelString = data.name

            paint.getTextBounds(labelString, 0, labelString.length, bounds)
            val textWidth = Px(bounds.width().toFloat())
            val textHeight = Px(bounds.height().toFloat())

            val labelStartPointX: Px = axisEndPointX - textWidth
            val labelStartPointY: Px = axisStartPointY - textHeight

            canvas.drawText(data.name, labelStartPointX.value, labelStartPointY.value, paint)
        }
    }
}