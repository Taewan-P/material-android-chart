package app.priceguard.chartsampleapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import app.priceguard.materialchart.Chart
import app.priceguard.materialchart.data.GraphMode
import java.lang.String

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        val chart1 = findViewById<Chart>(R.id.example_chart_1)
        chart1.dataset = ExampleDataset(
            showXAxis = false,
            showYAxis = false,\
            graphMode = GraphMode.DAY,
            data = listOf(
                ExampleData(1F, 10F),
                ExampleData(2F, 1F),
                ExampleData(3F, 3F),
                ExampleData(4F, 7F),
                ExampleData(5F, 8F),
                ExampleData(6F, 4F),
                ExampleData(7F, 2F),
                ExampleData(8F, 8F),
                ExampleData(9F, 8F)
            ),
            gridLines = listOf(ExampleGridLine("목표가", -1f), ExampleGridLine("역대최저가", 1f))
        )

        val chart2 = findViewById<Chart>(R.id.example_chart_2)
        chart2.dataset = ExampleDataset(
            showXAxis = true,
            showYAxis = true,
            graphMode = GraphMode.DAY,
            data = listOf(
                ExampleData( 1638265200.toFloat(), 10F),
                ExampleData( 1638272400.toFloat(), 1F),
                ExampleData( 1638279600.toFloat(), 3F),
                ExampleData( 1638286800.toFloat(), 7F),
                ExampleData( 1638294000.toFloat(), 8F),
                ExampleData( 1638301200.toFloat(), 4F),
                ExampleData( 1638308400.toFloat(), 2F),
                ExampleData( 1638315600.toFloat(), 8F)
            ),
            gridLines = listOf(ExampleGridLine("목표가", -1f), ExampleGridLine("역대최저가", 11f))
        )
    }
}