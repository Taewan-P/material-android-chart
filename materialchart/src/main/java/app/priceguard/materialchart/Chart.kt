package app.priceguard.materialchart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader.TileMode
import android.util.AttributeSet
import android.util.Log
import android.view.View
import app.priceguard.materialchart.data.ChartDataset
import app.priceguard.materialchart.util.Dp
import app.priceguard.materialchart.util.Px
import app.priceguard.materialchart.util.div
import app.priceguard.materialchart.util.minus
import app.priceguard.materialchart.util.plus
import app.priceguard.materialchart.util.times
import app.priceguard.materialchart.util.toDp
import app.priceguard.materialchart.util.toPx

class Chart @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var dataset: ChartDataset? = null

    // Padding: Empty space from the view to the graph. This includes the other side as well (Like horizontal paddings & vertical paddings)
    var xAxisPadding: Dp = Dp(16F)
    var yAxisPadding: Dp = Dp(16F)

    var xGraphPadding: Dp = Dp(32F)
    var yGraphPadding: Dp = Dp(32F)

    // Spacing: Empty space for each axis value. Depending on this value, the data may not fully show.
    // If it doesn't fit, you should change the width & height of the chart view.
    var xAxisSpacing: Dp = Dp(32F)
    var yAxisSpacing: Dp = Dp(32F)

    // Gridline: lines that are shown in axis with data labels
    var halfGridLineLength: Dp = Dp(4F)

    var axisStrokeWidth = 3f

    private var viewWidth = Dp(width.toFloat())
    private var viewHeight = Dp(height.toFloat())

    private val paint = Paint()
    private val xAxisPaint = Paint(paint)
    private val yAxisPaint = Paint(paint)
    private val linesPaint = Paint(paint)
    private val gradientPaint = Paint(paint)
    private val gradientCoverPaint = Paint(paint)

    init {
        // TODO: 그래프 영역 선언, 캔버스 생성
        // New canvas

    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        viewWidth = Dp(width.toFloat())
        viewHeight = Dp(height.toFloat())

        drawXAxis(canvas, xAxisPaint)
        drawYAxis(canvas, yAxisPaint)
        drawLine(canvas)
    }

    private fun drawXAxis(canvas: Canvas, paint: Paint) {
        // Calculate available axis space
        val availableSpace: Dp = Px(width.toFloat()).toDp(context) - xAxisPadding * Dp(2F)

        // Calculate how much gridlines can be drawn (Based on spacing settings above)
        val availableLabels = (availableSpace / xAxisSpacing).value.toInt()

        // Draw Axis
        val axisStartPointX: Px = xAxisPadding.toPx(context)
        val axisStartPointY: Px = Px(height.toFloat()) - yAxisPadding.toPx(context)
        val axisEndPointX: Px = (xAxisPadding + availableSpace).toPx(context)
        val axisEndPointY: Px = Px(height.toFloat()) - yAxisPadding.toPx(context)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = axisStrokeWidth
        paint.color = Color.BLACK
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND

        canvas.drawLine(
            axisStartPointX.value,
            axisStartPointY.value,
            axisEndPointX.value,
            axisEndPointY.value,
            paint
        )

        // Draw gridlines
        paint.strokeWidth = 2F

        (1 until availableLabels).forEach { idx ->
            val startPointX: Px =
                (axisStartPointX.toDp(context) + xAxisSpacing * Dp(idx.toFloat())).toPx(context)
            val startPointY: Px = (axisStartPointY.toDp(context) - halfGridLineLength).toPx(context)
            val endPointX: Px =
                (axisStartPointX.toDp(context) + xAxisSpacing * Dp(idx.toFloat())).toPx(context)
            val endPointY: Px = (axisStartPointY.toDp(context) + halfGridLineLength).toPx(context)

            canvas.drawLine(
                startPointX.value,
                startPointY.value,
                endPointX.value,
                endPointY.value,
                paint
            )
        }

        // TODO: Draw X data labels
    }

    private fun drawYAxis(canvas: Canvas, paint: Paint) {
        // Calculate available axis space
        val availableSpace: Dp = Px(height.toFloat()).toDp(context) - yAxisPadding * Dp(2F)

        // Calculate how much gridlines can be drawn (Based on spacing settings above)
        // TODO: Normalize the Y Axis
        val availableLabels = (availableSpace / yAxisSpacing).value.toInt()

        // Draw Axis
        val axisStartPointX: Px = xAxisPadding.toPx(context)
        val axisStartPointY: Px = (yAxisPadding + availableSpace).toPx(context)
        val axisEndPointX: Px = xAxisPadding.toPx(context)
        val axisEndPointY: Px = yAxisPadding.toPx(context)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = axisStrokeWidth
        paint.color = Color.BLACK
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND

        canvas.drawLine(
            axisStartPointX.value,
            axisStartPointY.value,
            axisEndPointX.value,
            axisEndPointY.value,
            paint
        )

        // Draw gridlines
        // TODO: Change this part after Y Axis Normalization
        paint.strokeWidth = 2F

        (1 until availableLabels).forEach { idx ->
            val startPointX: Px = (axisStartPointX.toDp(context) - halfGridLineLength).toPx(context)
            val startPointY: Px =
                (axisStartPointY.toDp(context) - yAxisSpacing * Dp(idx.toFloat())).toPx(context)
            val endPointX: Px = (axisStartPointX.toDp(context) + halfGridLineLength).toPx(context)
            val endPointY: Px =
                (axisStartPointY.toDp(context) - yAxisSpacing * Dp(idx.toFloat())).toPx(context)

            canvas.drawLine(
                startPointX.value,
                startPointY.value,
                endPointX.value,
                endPointY.value,
                paint
            )
        }


        // TODO: Draw Y data labels

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
        gradientPaint.shader = LinearGradient(
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
        gradientCoverPaint.color = Color.WHITE

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

}