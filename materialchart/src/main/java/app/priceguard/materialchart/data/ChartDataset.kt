package app.priceguard.materialchart.data

interface ChartDataset {
    val showXAxis: Boolean
    val showYAxis: Boolean
    val graphMode: GraphMode
    val data: List<ChartData>
    val gridLines: List<GridLine>
}