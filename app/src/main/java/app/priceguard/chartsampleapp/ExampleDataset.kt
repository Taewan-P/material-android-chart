package app.priceguard.chartsampleapp

import android.view.View
import app.priceguard.materialchart.data.ChartData
import app.priceguard.materialchart.data.ChartDataset
import app.priceguard.materialchart.data.GridLine

data class ExampleDataset(
    override val showXAxis: Boolean,
    override val showYAxis: Boolean,
    override val touchListener: View.OnTouchListener,
    override val data: List<ChartData>,
    override val gridLines: List<GridLine>
) : ChartDataset
