package app.priceguard.materialchart.data

import android.view.View.OnTouchListener

interface ChartDataset {
    val showXAxis: Boolean
    val showYAxis: Boolean
    val touchListener: OnTouchListener?
    val graphMode: GraphMode
    val data: List<ChartData>
    val gridLines: List<GridLine>
}