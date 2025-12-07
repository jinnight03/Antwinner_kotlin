import 'dart:math';
import 'package:flutter/material.dart';
// import 'package:fl_chart/fl_chart.dart'; // No longer primary chart, keep for BarChart
import 'package:fl_chart/fl_chart.dart' show BarChart, BarChartData, BarChartGroupData, BarChartRodData, FlGridData, FlTitlesData, AxisTitles, SideTitles, FlBorderData, BarTouchData, BarTouchTooltipData, BarTooltipItem;
import 'package:candlesticks/candlesticks.dart'; // Import candlesticks package
import 'package:intl/intl.dart'; // Import intl for number formatting
import 'package:syncfusion_flutter_core/theme.dart'; // Syncfusion core
import 'package:syncfusion_flutter_sliders/sliders.dart'; // Syncfusion sliders

// Enum for selectable chart periods
enum ChartPeriod { oneMonth, threeMonths, sixMonths, oneYear, all }

// Convert to StatefulWidget
class StockChartSection extends StatefulWidget {
  const StockChartSection({super.key});

  @override
  State<StockChartSection> createState() => _StockChartSectionState();
}

class _StockChartSectionState extends State<StockChartSection> {

  // State variables
  ChartPeriod _selectedPeriod = ChartPeriod.threeMonths; // Default period
  List<Candle> _displayedCandles = [];
  List<BarChartGroupData> _displayedVolumeBars = [];

  // Range Slider State
  late DateTime _rangeStartDate; // Use DateTime for range values
  late DateTime _rangeEndDate;

  // TODO: Replace with actual fetched chart data and date logic
  // Keep original full data accessible
  final List<Candle> _allOhlcData = [
    // Add more data points to simulate longer periods
    Candle(date: DateTime(2023, 10, 30), open: 4000, high: 4100, low: 3900, close: 4050, volume: 1200),
    Candle(date: DateTime(2023, 11, 1), open: 4050, high: 4200, low: 4000, close: 4150, volume: 1300),
    Candle(date: DateTime(2023, 11, 15), open: 4150, high: 4180, low: 3950, close: 4000, volume: 1100),
    Candle(date: DateTime(2023, 12, 1), open: 4000, high: 4050, low: 3800, close: 3850, volume: 1400),
    Candle(date: DateTime(2023, 12, 15), open: 3850, high: 3900, low: 3500, close: 3550, volume: 1600),
    // Previous sample data starts here (adjust dates if needed)
    Candle(date: DateTime(2024, 1, 1), open: 3000, high: 3250, low: 2950, close: 3200, volume: 1000),
    Candle(date: DateTime(2024, 1, 2), open: 3200, high: 3250, low: 3050, close: 3100, volume: 1200),
    Candle(date: DateTime(2024, 1, 3), open: 3100, high: 3550, low: 3080, close: 3500, volume: 800),
    Candle(date: DateTime(2024, 1, 4), open: 3500, high: 4050, low: 3450, close: 4000, volume: 1500),
    Candle(date: DateTime(2024, 1, 5), open: 4000, high: 4100, low: 3750, close: 3800, volume: 2000),
    Candle(date: DateTime(2024, 1, 8), open: 3800, high: 4300, low: 3780, close: 4200, volume: 1800),
    Candle(date: DateTime(2024, 1, 9), open: 4200, high: 4250, low: 4050, close: 4100, volume: 2200),
    Candle(date: DateTime(2024, 1, 10), open: 4100, high: 5100, low: 4080, close: 5000, volume: 2100),
    Candle(date: DateTime(2024, 1, 11), open: 5000, high: 5600, low: 4950, close: 5500, volume: 3000),
    Candle(date: DateTime(2024, 1, 12), open: 5500, high: 5550, low: 5100, close: 5200, volume: 4000),
    Candle(date: DateTime(2024, 1, 15), open: 5200, high: 5850, low: 5150, close: 5800, volume: 3500),
    Candle(date: DateTime(2024, 1, 16), open: 5800, high: 6100, low: 5750, close: 6000, volume: 5000),
    Candle(date: DateTime(2024, 1, 17), open: 6000, high: 6050, low: 5600, close: 5700, volume: 5315),
    Candle(date: DateTime(2024, 1, 18), open: 5700, high: 5750, low: 4400, close: 4500, volume: 4500),
    Candle(date: DateTime(2024, 1, 19), open: 4500, high: 4900, low: 4450, close: 4800, volume: 2500),
    Candle(date: DateTime(2024, 1, 22), open: 4800, high: 4850, low: 4550, close: 4600, volume: 2800),
    Candle(date: DateTime(2024, 1, 23), open: 4600, high: 4650, low: 3750, close: 3800, volume: 2600),
    Candle(date: DateTime(2024, 1, 24), open: 3800, high: 3900, low: 3550, close: 3600, volume: 1800),
    Candle(date: DateTime(2024, 1, 25), open: 3600, high: 3700, low: 3580, close: 3630, volume: 1500),
    Candle(date: DateTime(2024, 1, 26), open: 3630, high: 3800, low: 3600, close: 3750, volume: 2327),
    Candle(date: DateTime(2024, 2, 1), open: 3750, high: 3900, low: 3700, close: 3850, volume: 1900),
    Candle(date: DateTime(2024, 2, 15), open: 3850, high: 4000, low: 3800, close: 3950, volume: 2100),
    Candle(date: DateTime(2024, 3, 1), open: 3950, high: 4100, low: 3900, close: 4050, volume: 2000),
    Candle(date: DateTime(2024, 3, 15), open: 4050, high: 4200, low: 4000, close: 4150, volume: 2200),
    Candle(date: DateTime(2024, 4, 1), open: 4150, high: 4300, low: 4100, close: 4250, volume: 2400),
    Candle(date: DateTime(2024, 4, 15), open: 4250, high: 4400, low: 4200, close: 4350, volume: 2500),
    Candle(date: DateTime(2024, 4, 30), open: 4350, high: 4500, low: 4300, close: 3630, volume: 2800), // Match end price
  ];

  @override
  void initState() {
    super.initState();
    // Sort data just in case it's not
    _allOhlcData.sort((a, b) => a.date.compareTo(b.date));
    // Initialize range slider dates before updating chart
    if (_allOhlcData.isNotEmpty) {
      _rangeStartDate = _allOhlcData.first.date;
      _rangeEndDate = _allOhlcData.last.date;
    }
    _updateChartData(_selectedPeriod, initializeRange: true);
  }

  // Update displayed data based on selected period
  void _updateChartData(ChartPeriod period, {bool initializeRange = false}) {
    _selectedPeriod = period;
    final now = DateTime.now();
    DateTime filterStartDate;

    switch (period) {
      case ChartPeriod.oneMonth:
        filterStartDate = now.subtract(const Duration(days: 30));
        break;
      case ChartPeriod.threeMonths:
        filterStartDate = now.subtract(const Duration(days: 90));
        break;
      case ChartPeriod.sixMonths:
        filterStartDate = now.subtract(const Duration(days: 180));
        break;
      case ChartPeriod.oneYear:
        filterStartDate = now.subtract(const Duration(days: 365));
        break;
      case ChartPeriod.all:
      default:
        filterStartDate = DateTime(1970); // Effectively show all data
        break;
    }

    // Filter the full data based on the selected *period*
    List<Candle> periodCandles = _allOhlcData.where((c) => c.date.isAfter(filterStartDate)).toList();
    if (periodCandles.length < 5 && _allOhlcData.length >= 5) {
       periodCandles = _allOhlcData.sublist(max(0, _allOhlcData.length - 5));
    }
     if (periodCandles.isEmpty && _allOhlcData.isNotEmpty) {
         periodCandles = [_allOhlcData.last]; // Show at least the last one
      }

    setState(() {
      _displayedCandles = periodCandles; // Update main chart candles
      _displayedVolumeBars = _getVolumeBars(_displayedCandles);

      // Initialize or adjust range slider based on the *filtered* period data
      if (initializeRange && _displayedCandles.isNotEmpty) {
        _rangeStartDate = _displayedCandles.first.date;
        _rangeEndDate = _displayedCandles.last.date;
      }
      // Ensure range slider dates are within the bounds of displayed candles
      if (_displayedCandles.isNotEmpty) {
         if (_rangeStartDate.isBefore(_displayedCandles.first.date)) {
           _rangeStartDate = _displayedCandles.first.date;
         }
         if (_rangeEndDate.isAfter(_displayedCandles.last.date)) {
            _rangeEndDate = _displayedCandles.last.date;
         }
         // Ensure start is not after end
          if (_rangeStartDate.isAfter(_rangeEndDate)) {
             _rangeStartDate = _rangeEndDate;
          }
      }

       // TODO: Optionally, further filter _displayedCandles based on _rangeStartDate and _rangeEndDate here
       // if you want the main chart to zoom according to the slider immediately.
    });
  }

  // Generate volume bars for the *displayed* candles
   List<BarChartGroupData> _getVolumeBars(List<Candle> candles) {
    final List<BarChartGroupData> bars = [];
    for (int i = 0; i < candles.length; i++) {
      final candle = candles[i];
      final Color barColor = candle.close >= candle.open ? Colors.redAccent[100]! : Colors.blueGrey[300]!;
      bars.add(
        BarChartGroupData(
          x: i,
          barRods: [BarChartRodData(toY: candle.volume?.toDouble() ?? 0, color: barColor)],
        ),
      );
    }
    return bars;
  }

  // Helper to get display text for period
  String _getPeriodText(ChartPeriod period) {
    switch (period) {
      case ChartPeriod.oneMonth: return '1개월';
      case ChartPeriod.threeMonths: return '3개월';
      case ChartPeriod.sixMonths: return '6개월';
      case ChartPeriod.oneYear: return '1년';
      case ChartPeriod.all: return '전체';
    }
  }

  // Build the mini chart for the Range Slider child
  Widget _buildMiniChart(List<Candle> candles) {
     if (candles.isEmpty) return const SizedBox(height: 40); // Return empty if no data

    final List<FlSpot> spots = [];
    for (int i = 0; i < candles.length; i++) {
      spots.add(FlSpot(i.toDouble(), candles[i].close));
    }

     // Find min/max Y for the mini chart
    double minY = spots.map((s) => s.y).reduce((a, b) => a < b ? a : b);
    double maxY = spots.map((s) => s.y).reduce((a, b) => a > b ? a : b);
    // Add padding if min and max are the same
    if (maxY == minY) {
      maxY += 1;
      minY -= 1;
    }

     return LineChart(
      LineChartData(
        lineBarsData: [
          LineChartBarData(
            spots: spots,
            isCurved: false,
            color: Colors.grey[600],
            barWidth: 1,
            dotData: const FlDotData(show: false),
          ),
        ],
        minY: minY * 0.95, // Slightly adjusted padding
        maxY: maxY * 1.05,
        gridData: const FlGridData(show: false),
        borderData: FlBorderData(show: false),
        titlesData: const FlTitlesData(show: false), // Hide all titles
        lineTouchData: const LineTouchData(enabled: false), // Disable touch
      ),
      swapAnimationDuration: Duration.zero,
    );
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        // Title
        Padding(
          padding: const EdgeInsets.fromLTRB(20, 20, 20, 10),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              const Text(
                '차트',
                style: TextStyle(
                  fontSize: 18,
                  fontWeight: FontWeight.w600,
                ),
              ),
              IconButton(
                icon: Icon(
                  Icons.fullscreen_outlined,
                  color: Colors.grey[700],
                  size: 22,
                ),
                padding: EdgeInsets.zero,
                constraints: const BoxConstraints(),
                onPressed: () {
                  // Open fullscreen chart
                },
              ),
            ],
          ),
        ),

        // Period selection tabs
        Padding(
          padding: const EdgeInsets.symmetric(horizontal: 16),
          child: Row(
            children: ChartPeriod.values.map((period) {
              bool isSelected = period == _selectedPeriod;
              return Expanded(
                child: GestureDetector(
                  onTap: () => setState(() => _updateChartData(period)),
                  child: Container(
                    margin: const EdgeInsets.symmetric(horizontal: 4),
                    padding: const EdgeInsets.symmetric(vertical: 8),
                    decoration: BoxDecoration(
                      color: isSelected ? Colors.blue.shade50 : Colors.grey.shade100,
                      borderRadius: BorderRadius.circular(6),
                      border: isSelected
                          ? Border.all(color: Colors.blue.shade300, width: 1)
                          : null,
                    ),
                    alignment: Alignment.center,
                    child: Text(
                      _getPeriodText(period),
                      style: TextStyle(
                        color: isSelected ? Colors.blue.shade700 : Colors.grey.shade700,
                        fontWeight: isSelected ? FontWeight.w600 : FontWeight.w500,
                        fontSize: 13,
                      ),
                    ),
                  ),
                ),
              );
            }).toList(),
          ),
        ),

        const SizedBox(height: 8),

        // Main Chart
        if (_displayedCandles.isEmpty)
          const Padding(
            padding: EdgeInsets.all(16),
            child: Center(child: CircularProgressIndicator()),
          )
        else
          Container(
            height: 250,
            padding: const EdgeInsets.only(right: 16),
            child: Candlesticks(
              candles: _displayedCandles,
              onLoadMoreCandles: () {
                // Placeholder for loading more candles
              },
              actions: [
                ToolBarAction(
                  width: 46,
                  child: Icon(
                    Icons.show_chart, 
                    color: Colors.grey[700], 
                    size: 18,
                  ),
                  onPressed: () {
                    // Switch to line chart
                  },
                ),
              ],
              // Customized style for candlesticks
              candleSticksStyle: const CandleSticksStyle(
                bullColor: Color(0xFFE53935),   // Red for up
                bearColor: Color(0xFF1E88E5),   // Blue for down
                volumeBackgroundColor: Colors.black12,
                gridLineColor: Color(0xFFEEEEEE),
                borderColor: Colors.transparent,
                labelTextStyle: TextStyle(
                  color: Color(0xFF7B7B7B),
                  fontSize: 10,
                  fontWeight: FontWeight.w500,
                ),
              ),
            ),
          ),

        const SizedBox(height: 8),

        // Volume Chart
        Container(
          padding: const EdgeInsets.symmetric(horizontal: 16),
          height: 60,
          child: BarChart(
            BarChartData(
              gridData: FlGridData(show: false),
              titlesData: FlTitlesData(show: false),
              borderData: FlBorderData(show: false),
              barGroups: _displayedVolumeBars,
              barTouchData: BarTouchData(
                touchTooltipData: BarTouchTooltipData(
                  tooltipBgColor: Colors.white.withOpacity(0.8),
                  getTooltipItem: (group, groupIndex, rod, rodIndex) {
                    // Convert to human-readable volume text
                    final volumeText = _formatVolume(_displayedCandles[groupIndex].volume ?? 0);
                    return BarTooltipItem(
                      volumeText,
                      const TextStyle(
                        color: Colors.black87, 
                        fontWeight: FontWeight.w500,
                        fontSize: 10,
                      ),
                    );
                  },
                ),
              ),
            ),
          ),
        ),

        const SizedBox(height: 16),

        // Range Slider
        Container(
          margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
          height: 80,
          child: Column(
            children: [
              if (_allOhlcData.isNotEmpty) ...[
                // Mini chart above slider
                SizedBox(
                  height: 30,
                  child: _buildMiniChart(_allOhlcData),
                ),
              
                SfRangeSliderTheme(
                  data: SfRangeSliderThemeData(
                    activeTrackColor: Colors.blue.shade200,
                    inactiveTrackColor: Colors.grey.shade200,
                    thumbColor: Colors.white,
                    thumbStrokeColor: Colors.blue.shade500,
                    thumbStrokeWidth: 1.5,
                    overlayColor: Colors.transparent,
                    activeTickColor: Colors.transparent,
                    inactiveTickColor: Colors.transparent,
                    activeLabelStyle: const TextStyle(fontSize: 0),
                    inactiveLabelStyle: const TextStyle(fontSize: 0),
                  ),
                  child: SfRangeSlider(
                    min: _allOhlcData.first.date,
                    max: _allOhlcData.last.date,
                    values: SfRangeValues(_rangeStartDate, _rangeEndDate),
                    dateIntervalType: DateIntervalType.months,
                    dateFormat: DateFormat('yy.MM'),
                    showLabels: false,
                    showTicks: false,
                    minorTicksPerInterval: 0,
                    enableTooltip: true,
                    tooltipTextFormatterCallback: (actualValue, formattedText) {
                      // Format the date for the tooltip
                      final dateTime = actualValue as DateTime;
                      return DateFormat('yy.MM.dd').format(dateTime);
                    },
                    onChanged: (SfRangeValues values) {
                      setState(() {
                        _rangeStartDate = values.start as DateTime;
                        _rangeEndDate = values.end as DateTime;
                        // This doesn't actually filter the chart yet - could add real filtering here
                      });
                    },
                  ),
                ),
              ],
            ],
          ),
        ),
      ],
    );
  }

  // Format volume number to readable text (e.g. 1200 -> "1.2천")
  String _formatVolume(num volume) {
    if (volume >= 100000000) {
      return '${(volume / 100000000).toStringAsFixed(1)}억';
    } else if (volume >= 10000) {
      return '${(volume / 10000).toStringAsFixed(1)}만';
    } else if (volume >= 1000) {
      return '${(volume / 1000).toStringAsFixed(1)}천';
    } else {
      return volume.toString();
    }
  }
} 