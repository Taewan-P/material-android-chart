package app.priceguard.materialchart

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
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
import com.google.android.material.resources.MaterialAttributes
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

    // Spacing: Empty space for each axis value. Depending on this value, the data may not fully show.
    // If it doesn't fit, you should change the width & height of the chart view.
    var xAxisSpacing: Dp = Dp(32F)
    var yAxisSpacing: Dp = Dp(32F)

    // Tick: lines that are shown in axis with data labels
    var halfTickLength: Dp = Dp(4F)
    
    // Use Android theme
    var colorPrimary: Int = Color.BLACK
    var colorSecondary: Int = Color.BLACK
    var colorError: Int = Color.BLACK
    var colorSurface: Int = Color.BLACK
    var colorOnSurface: Int = Color.BLACK
  
    private val paint = Paint()
    private val xAxisPaint = Paint(paint)
    private val yAxisPaint = Paint(paint)
    private val linesPaint = Paint(paint)

    private val bounds = Rect()

    init {
        // TODO: 그래프 영역 선언, 캔버스 생성
        // New canvas
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        drawXAxis(canvas, xAxisPaint)
        drawYAxis(canvas, yAxisPaint)
        drawLine(canvas, linesPaint)
    }

    fun setColor(primary: Int, secondary: Int, error: Int, surface: Int, onSurface: Int) {
        colorPrimary = primary
        colorSecondary = secondary
        colorError = error
        colorSurface = surface
        colorOnSurface = onSurface
        invalidate()
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
        val unit = roundToSecondSignificantDigit((maxValue - minValue) / availableLabels.toFloat())

        // Draw Axis
        val axisStartPointX: Px = xAxisPadding.toPx(context)
        val axisStartPointY: Px = Px(height.toFloat()) - yAxisPadding.toPx(context)
        val axisEndPointX: Px = (xAxisPadding + availableSpace).toPx(context)
        val axisEndPointY: Px = Px(height.toFloat()) - yAxisPadding.toPx(context)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3F
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
        val unit = roundToSecondSignificantDigit((maxValue - minValue) / availableLabels.toFloat())

        // Draw Axis
        val axisStartPointX: Px = xAxisPadding.toPx(context)
        val axisStartPointY: Px = (yAxisPadding + availableSpace).toPx(context)
        val axisEndPointX: Px = xAxisPadding.toPx(context)
        val axisEndPointY: Px = yAxisPadding.toPx(context)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3F
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

    private fun drawLine(canvas: Canvas, paint: Paint) {
        // TODO: 페인트 그래프 값 설정
        // TODO: 값을 받아, 선으로 잇기

        // x,y 간격 계산
        if (dataset == null) return

        val chartData = dataset?.data!!
        val size = chartData.size

        val maxX = chartData.maxOf { it.x }
        val minX = chartData.minOf { it.x }
        val maxY = chartData.maxOf { it.y }
        val minY = chartData.minOf { it.y }

        val spaceX = maxX - minX
        val spaceY = maxY - minY

        val chartSpaceStart = Dp(64F)

        paint.style = Paint.Style.FILL
        paint.strokeWidth = 5F
        paint.color = colorPrimary

        Log.d("asdf", dataset.toString())
        dataset?.data?.forEachIndexed { index, data ->
            if (index < size - 1) {
                val next = chartData[index + 1]

                val startX = Px((data.x - minX) / spaceX) * (Px(width.toFloat()) - chartSpaceStart.toPx(context)) + xAxisPadding.toPx(context)
                val startY = Px(1 - (data.y - minY) / spaceY) * (Px(height.toFloat()) - chartSpaceStart.toPx(context)) + yAxisPadding.toPx(context)
                val endX = Px((next.x - minX) / spaceX) * (Px(width.toFloat()) - chartSpaceStart.toPx(context)) + xAxisPadding.toPx(context)
                val endY = Px(1 - (next.y - minY) / spaceY) * (Px(height.toFloat()) - chartSpaceStart.toPx(context)) + yAxisPadding.toPx(context)
                Log.d("dataset", "$startX, $startY, $endX, $endY")

                canvas.drawLine(startX.value, startY.value, endX.value, startY.value, paint)
                canvas.drawLine(endX.value, startY.value, endX.value, endY.value, paint)
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

}