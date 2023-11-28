package app.priceguard.materialchart.data

import android.view.View.OnTouchListener

interface ChartDataset {
    val showXAxis: Boolean
    val showYAxis: Boolean
    val touchListener: OnTouchListener
    val data: List<ChartData>
}