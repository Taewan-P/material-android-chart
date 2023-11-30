package app.priceguard.chartsampleapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import app.priceguard.materialchart.Chart
import app.priceguard.materialchart.data.GraphMode

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val chart1 = findViewById<Chart>(R.id.example_chart_1)
        chart1.dataset = ExampleDataset(
            showXAxis = false,
            showYAxis = false,
            isInteractive = false,
            graphMode = GraphMode.DAY,
            data = listOf(
                ExampleData(1F, 10F, true),
                ExampleData(2F, 1F, true),
                ExampleData(3F, 3F, true),
                ExampleData(4F, 7F, true),
                ExampleData(5F, 8F, true),
                ExampleData(6F, 4F, true),
                ExampleData(7F, 2F, true),
                ExampleData(8F, 8F, true),
                ExampleData(9F, 8F, true)
            ),
            gridLines = listOf(ExampleGridLine("목표가", -1f), ExampleGridLine("역대최저가", 1f))
        )

        val chart2 = findViewById<Chart>(R.id.example_chart_2)
        chart2.dataset = ExampleDataset(
            showXAxis = true,
            showYAxis = true,
            isInteractive = true,
            graphMode = GraphMode.DAY,
            data = listOf(
                ExampleData(1638265200.toFloat(), 10F, true),
                ExampleData(1638272400.toFloat(), 1F, true),
                ExampleData(1638279600.toFloat(), 3F, false),
                ExampleData(1638286800.toFloat(), 7F, false),
                ExampleData(1638294000.toFloat(), 8F, true),
                ExampleData(1638301200.toFloat(), 4F, true),
                ExampleData(1638308400.toFloat(), 2F, false),
                ExampleData(1638315600.toFloat(), 8F, true)
            ),
            gridLines = listOf(ExampleGridLine("목표가", -1f), ExampleGridLine("역대최저가", 2f))
        )
    }
}