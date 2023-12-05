package app.priceguard.materialchart.data

interface ChartDataset {
    val showXAxis: Boolean
    val showYAxis: Boolean
    val isInteractive: Boolean
    val graphMode: GraphMode
    val xLabel: String
    val yLabel: String
    val data: List<ChartData>
    val gridLines: List<GridLine>
}
