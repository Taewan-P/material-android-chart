package app.priceguard.chartsampleapp

import android.view.View
import app.priceguard.materialchart.data.ChartData
import app.priceguard.materialchart.data.ChartDataset

data class ExampleDataset(
    override val showXAxis: Boolean,
    override val showYAxis: Boolean,
    override val data: List<ChartData>
) : ChartDataset
