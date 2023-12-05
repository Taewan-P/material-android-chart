package app.priceguard.chartsampleapp

import app.priceguard.materialchart.data.ChartData
import app.priceguard.materialchart.data.ChartDataset
import app.priceguard.materialchart.data.GraphMode
import app.priceguard.materialchart.data.GridLine

data class ExampleDataset(
    override val showXAxis: Boolean,
    override val showYAxis: Boolean,
    override val isInteractive: Boolean,
    override val graphMode: GraphMode,
    override val xLabel: String,
    override val yLabel: String,
    override val data: List<ChartData>,
    override val gridLines: List<GridLine>
) : ChartDataset
