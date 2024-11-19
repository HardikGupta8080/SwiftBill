package com.example.swiftbill.fragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.swiftbill.R
import com.example.swiftbill.databinding.FragmentDashboardBinding
import com.example.swiftbill.model.Billdata
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val db = FirebaseFirestore.getInstance()
        val billsCollection = db.collection("USER")
            .document(Firebase.auth.currentUser?.uid.toString())
            .collection("BILL") // Adjust this to your actual collection name

        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

// Start of the month
        calendar.set(currentYear, currentMonth, 1, 0, 0, 0)
        val startOfMonth = calendar.time

// End of the month
        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.SECOND, -1)
        val endOfMonth = calendar.time

// Date format for local filtering
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val startOfMonthStr = dateFormat.format(startOfMonth)
        val endOfMonthStr = dateFormat.format(endOfMonth)

        billsCollection
            .get()
            .addOnSuccessListener { querySnapshot ->
                val bills = querySnapshot.documents.mapNotNull { it.toObject(Billdata::class.java) }

                // Convert start and end of month to Date objects
                val startDate = dateFormat.parse(startOfMonthStr)
                val endDate = dateFormat.parse(endOfMonthStr)

                // Filter bills locally based on the date range
                val filteredBills = bills.filter { bill ->
                    val billDate = dateFormat.parse(bill.date) // Parse bill date to Date
                    billDate in startDate..endDate // Check if it falls in the range
                }

                // Process the filtered bills for weekly data
                calculateWeeklySales(filteredBills)
                calculateWeeklyProfit(filteredBills)
                val topProducts = calculateCurrentWeekTopProducts(filteredBills)
                showCurrentWeekTopProductsInPieChart(binding.pieChartTopSellingProducts,topProducts)

            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Error fetching bills: ", e)
            }

        // Initialize the charts
        //setupLineChart()
        //setupPieChart()
        //setupBarChart()



        return binding.root
    }

    private fun setupLineChart(entries:List<Entry>) {
       /* val entries = arrayListOf(
            Entry(2010f, 2000f),
            Entry(2011f, 3000f),
            Entry(2012f, 4000f)
        )*/

        val dataSet = LineDataSet(entries, "Sales Over Time")
        dataSet.color = ContextCompat.getColor(requireContext(), R.color.blue)
        dataSet.valueTextColor = ContextCompat.getColor(requireContext(), R.color.black)
        dataSet.lineWidth = 2f
        dataSet.circleRadius = 4f

        val lineData = LineData(dataSet)
        binding.lineChartSalesOverTime.data = lineData
        binding.lineChartSalesOverTime.invalidate()
        binding.lineChartSalesOverTime.description.text=""
    }
    private fun setupBarChart(revenueEntries: List<BarEntry>) {
        // Generate labels for all weeks in the current month
        val weekLabels = generateWeekLabels()

        // Ensure bar entries align with the week indices
        val allEntries = mutableListOf<BarEntry>()

        // Loop over the range of week labels to match them with the revenue entries
        for (i in 1..weekLabels.size) {
            // Find a matching revenue entry for the current week index or set value to 0 if not found
            val entry = revenueEntries.find { it.x.toInt() == i } ?: BarEntry(i.toFloat(), 0f)
            allEntries.add(entry)
        }

        // Create a dataset for the revenue bars with the matched entries
        val revenueDataSet = BarDataSet(allEntries, "Revenue").apply {
            color = ContextCompat.getColor(requireContext(), R.color.blue)
            xMax
        // Set the color of the bars
        }

        // Create the BarData object using the dataset
        val barData = BarData(revenueDataSet)

        // Set the data for the chart
        binding.barChartRevenueExpenses.data = barData
        binding.barChartRevenueExpenses.description.text=""
        binding.barChartRevenueExpenses.setFitBars(true) // Fit the bars into the chart

        // Customize the X-axis
        val xAxis = binding.barChartRevenueExpenses.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(weekLabels) // Set week labels as X-axis values
        xAxis.position = XAxis.XAxisPosition.BOTTOM // Position the X-axis labels at the bottom
        xAxis.granularity = 1f // Ensure each bar corresponds to a label
        xAxis.setDrawGridLines(false) // Optional: Remove grid lines for better clarity

        // Customize the chart to make sure all weeks are visible
        val maxX = weekLabels.size.toFloat()
        xAxis.axisMinimum = 0f // Start from the first week
        xAxis.axisMaximum = maxX // Set the maximum value based on the number of weeks

        // Refresh the chart
        binding.barChartRevenueExpenses.invalidate()
    }

    private fun generateWeekLabels(): List<String> {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        calendar.set(currentYear, currentMonth, 1) // Start of the month
        val totalWeeks = calendar.getActualMaximum(Calendar.WEEK_OF_MONTH)

        // Ensure the labels for each week in the month are generated, even if we start from Week 2
        return (1..totalWeeks).map { "Week $it" }
    }





    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun calculateWeeklySales(bills: List<Billdata>) {
        val weeklySales = mutableMapOf<Int, Double>()
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

        for (bill in bills) {
            try {
                // Parse the date
                val billDate = dateFormat.parse(bill.date) ?: throw IllegalArgumentException("Invalid date format: ${bill.date}")

                // Set calendar date
                val calendar = Calendar.getInstance()
                calendar.time = billDate

                // Set first day of the week to Monday
                calendar.firstDayOfWeek = Calendar.MONDAY

                // Calculate the week of the month
                val weekOfMonth = calendar.get(Calendar.WEEK_OF_MONTH)

                println("Processing bill: $bill")
                println("Date: ${bill.date} -> Week of Month: $weekOfMonth")

                // Add the total amount to the respective week
                weeklySales[weekOfMonth] = (weeklySales[weekOfMonth] ?: 0.0) + bill.totalAmount!!
            } catch (e: Exception) {
                println("Error processing bill ${bill.billId}: ${e.message}")
            }
        }

        println("Final Weekly Sales: $weeklySales")
        val weeklyEntries = weeklySales.entries.map { BarEntry(it.key.toFloat(), it.value.toFloat()) }
        setupBarChart(weeklyEntries)
    }
    fun calculateWeeklyProfit(bills :List<Billdata>) {
       val weeklyProfit = mutableMapOf<Int, Double>()
       val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

       for (bill in bills) {
           try {
               // Parse the date
               val billDate = dateFormat.parse(bill.date)
                   ?: throw IllegalArgumentException("Invalid date format: ${bill.date}")

               // Set calendar date
               val calendar = Calendar.getInstance()
               calendar.time = billDate

               // Set first day of the week to Monday
               calendar.firstDayOfWeek = Calendar.MONDAY

               // Calculate the week of the month
               val weekOfMonth = calendar.get(Calendar.WEEK_OF_MONTH)

               println("Processing bill: $bill")
               println("Date: ${bill.date} -> Week of Month: $weekOfMonth")

               // Add the total amount to the respective week
               weeklyProfit[weekOfMonth] = (weeklyProfit[weekOfMonth] ?: 0.0) + bill.profit!!?:0.0
           } catch (e: Exception) {
               println("Error processing bill ${bill.billId}: ${e.message}")
           }
       }
       val weeklyEntries = weeklyProfit.entries.map { Entry(it.key.toFloat(), it.value.toFloat()) }
       setupLineChart(weeklyEntries)
   }
    fun calculateCurrentWeekTopProducts(bills: List<Billdata>): List<Pair<String, Int>> {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val calendar = Calendar.getInstance()

        // Get current week number and year
        val currentWeek = calendar.get(Calendar.WEEK_OF_YEAR)
        val currentYear = calendar.get(Calendar.YEAR)

        // Map to hold product counts
        val productCounts = mutableMapOf<String, Int>()

        for (bill in bills) {
            val billDate = dateFormat.parse(bill.date)
            calendar.time = billDate

            // Check if the bill is from the current week and year
            val billWeek = calendar.get(Calendar.WEEK_OF_YEAR)
            val billYear = calendar.get(Calendar.YEAR)

            if (billWeek == currentWeek && billYear == currentYear) {
                for (item in bill.items!!) {
                    val productName = item.productname
                    val productCount = item.quantity

                    // Increment the product count
                    productCounts.merge(productName!!, productCount!!) { oldCount, newCount ->
                        oldCount + newCount
                    }
                }
            }
        }

        // Sort by sales count and take the top 5 products
        val topProducts = productCounts.entries.sortedByDescending { it.value }
            .take(5) // Get top 5
            .map { it.key to it.value } // Convert to Pair<ProductName, SalesCount>

        return topProducts
    }

    fun showCurrentWeekTopProductsInPieChart(pieChart: PieChart, topProducts: List<Pair<String, Int>>) {
        // Prepare entries for the pie chart
        val entries = topProducts.map { (productName, salesCount) ->
            PieEntry(salesCount.toFloat(), productName)
        }

        // Create a PieDataSet
        val dataSet = PieDataSet(entries, "Top Products This Week")
        dataSet.colors = listOf(
            ColorTemplate.COLORFUL_COLORS[0],
            ColorTemplate.COLORFUL_COLORS[1],
            ColorTemplate.COLORFUL_COLORS[2],
            ColorTemplate.COLORFUL_COLORS[3],
            ColorTemplate.COLORFUL_COLORS[4]
        )
        dataSet.valueTextSize = 12f
        dataSet.valueTextColor = Color.BLACK

        // Create PieData
        val pieData = PieData(dataSet)
        pieData.setValueFormatter(PercentFormatter())

        // Configure the pie chart
        pieChart.data = pieData
        pieChart.description.isEnabled = false
        pieChart.isDrawHoleEnabled = true
        pieChart.setHoleColor(Color.WHITE)
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.setUsePercentValues(true)
        pieChart.setCenterText("Top Products")
        pieChart.setCenterTextSize(16f)
        pieChart.invalidate() // Refresh the chart
    }









}
