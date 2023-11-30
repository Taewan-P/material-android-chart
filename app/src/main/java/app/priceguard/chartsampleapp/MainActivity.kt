package app.priceguard.chartsampleapp

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import app.priceguard.materialchart.Chart
import java.lang.String

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        val chart1 = findViewById<Chart>(R.id.example_chart_1)
        chart1.dataset = ExampleDataset(
            showXAxis = false,
            showYAxis = false,
            touchListener = { v, _ -> v.performClick() },
            data = listOf(
                ExampleData(1F, 10F),
                ExampleData(2F, 1F),
                ExampleData(3F, 3F),
                ExampleData(4F, 7F),
                ExampleData(5F, 8F),
                ExampleData(6F, 4F),
                ExampleData(7F, 2F),
                ExampleData(8F, 8F)
            ),
            gridLines = listOf(ExampleGridLine("목표가", -1f), ExampleGridLine("역대최저가", 1f))
        )

        val chart2 = findViewById<Chart>(R.id.example_chart_2)
        chart2.dataset = ExampleDataset(
            showXAxis = true,
            showYAxis = true,
            touchListener = { v, _ -> v.performClick() },
            data = listOf(
                ExampleData(1F, 10F),
                ExampleData(2F, 1F),
                ExampleData(3F, 3F),
                ExampleData(4F, 7F),
                ExampleData(5F, 8F),
                ExampleData(6F, 4F),
                ExampleData(7F, 2F),
                ExampleData(8F, 8F)
            )
        )
    }
}