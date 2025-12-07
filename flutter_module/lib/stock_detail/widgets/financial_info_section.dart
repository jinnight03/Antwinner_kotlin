import 'package:flutter/material.dart';
import 'package:fl_chart/fl_chart.dart'; // Import fl_chart
import 'dart:math'; // For max function

class FinancialInfoSection extends StatelessWidget {
  const FinancialInfoSection({super.key});

  // TODO: Replace with actual data model and fetching
  // Annual Data (Ensuring values are numeric where possible for chart)
  final List<Map<String, dynamic>> _annualData = const [
    {'날짜': '2022-12-01', '매출액': 2455, '영업이익': 113, '당기순이익': 140, '유보율': 855, '부채비율': 117}, // Added 당기순이익 sample
    {'날짜': '2023-12-01', '매출액': 2484, '영업이익': 51, '당기순이익': -10, '유보율': 799, '부채비율': 87},
    {'날짜': '2024-12-01', '매출액': 2234, '영업이익': 60, '당기순이익': 5, '유보율': 791, '부채비율': 93},
    // {'날짜': '2025-12-01', '매출액': '-', '영업이익': '-', '유보율': '-', '부채비율': '-'}, // Exclude non-numeric for chart
  ];

  // Quarterly Data
  final List<Map<String, dynamic>> _quarterlyData = const [
    {'날짜': '2024-06-01', '매출액': 598, '영업이익': 21, '유보율': 806, '부채비율': 86},
    {'날짜': '2024-09-01', '매출액': 531, '영업이익': 13, '유보율': 803, '부채비율': 77},
    {'날짜': '2024-12-01', '매출액': 575, '영업이익': 8, '유보율': 791, '부채비율': 93},
    // {'날짜': '2025-03-01', '매출액': '-', '영업이익': '-', '유보율': '-', '부채비율': '-'}, // Exclude non-numeric
  ];

  @override
  Widget build(BuildContext context) {
    final headers = ['날짜', '매출액', '영업이익', '유보율', '부채비율']; // 당기순이익 is not in table header but used in chart

    // Prepare data for the annual chart
    final List<BarChartGroupData> annualChartData = _prepareAnnualChartData(context);
    // Calculate max Y value for scaling
    final double maxYValue = _calculateMaxYValue();

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
           mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
             Text(
              '재무정보(요약)',
              style: Theme.of(context).textTheme.titleLarge?.copyWith(fontWeight: FontWeight.bold),
            ),
            Text(
              '단위:억원, %', // Units
              style: Theme.of(context).textTheme.bodySmall?.copyWith(color: Colors.grey[600]),
            ),
          ],
        ),

        const SizedBox(height: 16),
        Text('연간', style: Theme.of(context).textTheme.titleMedium),
        const SizedBox(height: 8),
        _buildFinancialTable(_annualData, headers),
        const SizedBox(height: 16),
        // Annual Financial Chart (Grouped Bar Chart)
        SizedBox(
          height: 180, // Adjust height
          child: BarChart(
            BarChartData(
              maxY: maxYValue * 1.1, // Add padding to max Y
              minY: _calculateMinYValue(), // Handle potential negative values
              alignment: BarChartAlignment.spaceAround,
              barGroups: annualChartData,
              titlesData: FlTitlesData(
                 leftTitles: const AxisTitles(sideTitles: SideTitles(showTitles: true, reservedSize: 40)),
                 bottomTitles: AxisTitles(sideTitles: SideTitles(showTitles: true, getTitlesWidget: _bottomTitles, reservedSize: 30)),
                 topTitles: const AxisTitles(sideTitles: SideTitles(showTitles: false)),
                 rightTitles: const AxisTitles(sideTitles: SideTitles(showTitles: false)),
              ),
              gridData: FlGridData(
                 show: true,
                 drawVerticalLine: false,
                 horizontalInterval: maxYValue / 5, // Adjust interval based on data
              ),
              borderData: FlBorderData(show: false),
              barTouchData: BarTouchData(
                 touchTooltipData: BarTouchTooltipData(
                   tooltipBgColor: Colors.blueGrey,
                   getTooltipItem: (group, groupIndex, rod, rodIndex) {
                     String label = '';
                     switch (rodIndex) {
                       case 0:
                         label = '매출액';
                         break;
                       case 1:
                         label = '영업이익';
                         break;
                        case 2:
                         label = '당기순이익';
                         break;
                     }
                     return BarTooltipItem(
                       '$label\n',
                       const TextStyle(color: Colors.white, fontWeight: FontWeight.bold),
                       children: <TextSpan>[
                         TextSpan(
                           text: rod.toY.toString(),
                           style: const TextStyle(color: Colors.yellow, fontWeight: FontWeight.w500),
                         ),
                       ],
                     );
                   },
                 ),
               ),
                // Add legend here if needed
            ),
          ),
        ),
        _buildChartLegend(context), // Add Legend

        const SizedBox(height: 24),
        Text('분기', style: Theme.of(context).textTheme.titleMedium),
        const SizedBox(height: 8),
        // Note: Using annual headers for quarterly table for now
        _buildFinancialTable(_quarterlyData, headers.where((h) => _quarterlyData.first.containsKey(h)).toList()),
        // TODO: Add button/link to view more financial details
      ],
    );
  }

  // Helper to create BarChartGroupData for annual chart
  List<BarChartGroupData> _prepareAnnualChartData(BuildContext context) {
    final List<BarChartGroupData> barGroups = [];
    final double barWidth = 15;
    final Color salesColor = Colors.lightBlue[300]!;
    final Color profitColor = Colors.blue[700]!;
    final Color netIncomeColor = Colors.orange[300]!;

    for (int i = 0; i < _annualData.length; i++) {
       final yearData = _annualData[i];
       // Ensure data is numeric before adding
       final double sales = (yearData['매출액'] is num) ? (yearData['매출액'] as num).toDouble() : 0.0;
       final double profit = (yearData['영업이익'] is num) ? (yearData['영업이익'] as num).toDouble() : 0.0;
       final double netIncome = (yearData['당기순이익'] is num) ? (yearData['당기순이익'] as num).toDouble() : 0.0;

      barGroups.add(
        BarChartGroupData(
          x: i,
          barRods: [
            // 매출액
            BarChartRodData(toY: sales, color: salesColor, width: barWidth),
            // 영업이익
            BarChartRodData(toY: profit, color: profitColor, width: barWidth),
             // 당기순이익
            BarChartRodData(toY: netIncome, color: netIncomeColor, width: barWidth),
          ],
        ),
      );
    }
    return barGroups;
  }

  // Calculate max Y value for chart scaling
   double _calculateMaxYValue() {
    double maxVal = 0;
    for (var data in _annualData) {
      if (data['매출액'] is num) maxVal = max(maxVal, (data['매출액'] as num).toDouble());
      if (data['영업이익'] is num) maxVal = max(maxVal, (data['영업이익'] as num).toDouble());
      if (data['당기순이익'] is num) maxVal = max(maxVal, (data['당기순이익'] as num).toDouble());
    }
    // Return a slightly larger value than the max, or a default if maxVal is 0 or less
    return maxVal <= 0 ? 100 : maxVal;
  }
   // Calculate min Y value for chart scaling (to handle negative profits)
   double _calculateMinYValue() {
    double minVal = 0;
    for (var data in _annualData) {
      if (data['영업이익'] is num) minVal = min(minVal, (data['영업이익'] as num).toDouble());
      if (data['당기순이익'] is num) minVal = min(minVal, (data['당기순이익'] as num).toDouble());
    }
    // Add some padding below the minimum negative value, or return 0 if all are non-negative
    return minVal < 0 ? minVal * 1.1 : 0;
  }

  // Widget for bottom axis titles (Years)
  Widget _bottomTitles(double value, TitleMeta meta) {
    final titles = _annualData.map((d) => d['날짜'].toString().substring(0, 4)).toList(); // Extract year
    final index = value.toInt();
    Widget text = const Text('');
    if (index >= 0 && index < titles.length) {
      text = Text(titles[index], style: const TextStyle(color: Colors.black, fontSize: 10));
    }
    return SideTitleWidget(axisSide: meta.axisSide, space: 4, child: text);
  }

  // Widget for chart legend
  Widget _buildChartLegend(BuildContext context) {
     return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        _legendItem(Colors.lightBlue[300]!, '매출액'),
        const SizedBox(width: 16),
        _legendItem(Colors.blue[700]!, '영업이익'),
        const SizedBox(width: 16),
        _legendItem(Colors.orange[300]!, '당기순이익'),
      ],
    );
  }

  Widget _legendItem(Color color, String text) {
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        Container(width: 10, height: 10, color: color),
        const SizedBox(width: 4),
        Text(text, style: const TextStyle(fontSize: 12)),
      ],
    );
  }

  Widget _buildFinancialTable(List<Map<String, dynamic>> data, List<String> headers) {
    final headerStyle = TextStyle(color: Colors.grey[600], fontSize: 13);
    final cellStyle = const TextStyle(fontSize: 13);
    const cellPadding = EdgeInsets.symmetric(vertical: 8.0, horizontal: 4.0);

    return Table(
      border: TableBorder.all(color: Colors.grey[300]!, width: 0.5),
       columnWidths: const {
         0: FlexColumnWidth(2.5), // Date wider
         1: FlexColumnWidth(2),
         2: FlexColumnWidth(2),
         3: FlexColumnWidth(2),
         4: FlexColumnWidth(2),
       },
      children: [
        // Header Row
        TableRow(
           decoration: BoxDecoration(color: Colors.grey[100]),
          children: headers.map((header) {
            return Padding(
              padding: cellPadding,
              child: Text(header, style: headerStyle, textAlign: TextAlign.center),
            );
          }).toList(),
        ),
        // Data Rows
        ...data.map((row) {
          return TableRow(
            children: headers.map((header) {
               final value = row[header];
               // Check if value exists, otherwise display '-'
               final displayValue = value != null ? value.toString() : '-';
              return Padding(
                padding: cellPadding,
                child: Text(displayValue, style: cellStyle, textAlign: TextAlign.center),
              );
            }).toList(),
          );
        }),
      ],
    );
  }
} 