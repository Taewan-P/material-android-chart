package app.priceguard.chartsampleapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import app.priceguard.materialchart.Chart

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Chart>(R.id.example_chart_1).dataset = ExampleDataset(
            showXAxis = true, showYAxis = false, touchListener = { v, _ -> v.performClick() }, data = listOf(
                ExampleData(1F, 11000F),
                ExampleData(2F, 11000F),
                ExampleData(3F, 13000F),
                ExampleData(4F, 12000F),
                ExampleData(5F, 11000F),
                ExampleData(6F, 14000F),
                ExampleData(7F, 12000F),
                ExampleData(8F, 11000F),
                ExampleData(9F, 11000F)
            )
        )
        findViewById<Chart>(R.id.example_chart_2).dataset = ExampleDataset(
            showXAxis = false, showYAxis = false, touchListener = { v, _ -> v.performClick() }, data = listOf(
                ExampleData(2F, 5F),
                ExampleData(5F, 10F),
                ExampleData(8F, 1F),
                ExampleData(9F, 3F),
                ExampleData(14F, 7F),
                ExampleData(16F, 8F),
                ExampleData(18F, 4F),
                ExampleData(24F, 2F),
                ExampleData(25F, 8F),
                ExampleData(26F, 8F)
            )
        )
    }
}