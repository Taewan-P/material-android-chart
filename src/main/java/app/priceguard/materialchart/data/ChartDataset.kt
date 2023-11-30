package app.priceguard.materialchart.data

interface ChartDataset {
    val showXAxis: Boolean
    val showYAxis: Boolean
    val data: List<ChartData>
}