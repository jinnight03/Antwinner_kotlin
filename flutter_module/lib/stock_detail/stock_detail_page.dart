import 'package:flutter/material.dart';
import 'widgets/basic_info_section.dart';
import 'widgets/financial_info_section.dart';
import 'widgets/investor_trading_section.dart';
import 'widgets/news_disclosure_section.dart';
import 'widgets/significant_rise_days_section.dart';
import 'widgets/stock_chart_section.dart';
import 'widgets/stock_info_section.dart';

// TODO: Replace with actual stock data model
class Stock {
  final String name;
  final String code; // Assuming a stock code is needed

  Stock({required this.name, required this.code});
}

class StockDetailPage extends StatelessWidget {
  final Stock stock; // Pass stock data to the page

  const StockDetailPage({super.key, required this.stock});

  @override
  Widget build(BuildContext context) {
    // Placeholder data - replace with actual data fetching later
    final String currentPrice = "3,630원";
    final String changePercent = "-7.52%";
    final String market = "코스닥";
    final List<String> themes = ["홍준표"]; // Example theme

    return Scaffold(
      backgroundColor: Colors.white,
      appBar: AppBar(
        elevation: 0,
        backgroundColor: Colors.white,
        foregroundColor: Colors.black87,
        centerTitle: true,
        title: Text(
          stock.name,
          style: const TextStyle(
            fontWeight: FontWeight.w600,
            fontSize: 18,
          ),
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.bookmark_border),
            onPressed: () {
              // Add to watchlist functionality
            },
          ),
          IconButton(
            icon: const Icon(Icons.share_outlined),
            onPressed: () {
              // Share functionality
            },
          ),
        ],
      ),
      body: Container(
        color: Colors.grey[50],
        child: ListView(
          padding: EdgeInsets.zero,
          children: <Widget>[
            // Stock info with white background
            Container(
              color: Colors.white,
              padding: const EdgeInsets.all(20.0),
              child: StockInfoSection(
                name: stock.name,
                currentPrice: currentPrice,
                changePercent: changePercent,
                market: market,
                themes: themes,
              ),
            ),
            
            const SizedBox(height: 8),
            
            // Significant rise days
            _buildSectionContainer(
              const SignificantRiseDaysSection(),
            ),
            
            const SizedBox(height: 8),
            
            // Chart section
            _buildSectionContainer(
              const StockChartSection(),
              padding: EdgeInsets.zero, // No padding for chart to maximize space
            ),
            
            const SizedBox(height: 8),
            
            // Basic info
            _buildSectionContainer(
              const BasicInfoSection(),
            ),
            
            const SizedBox(height: 8),
            
            // Investor trading
            _buildSectionContainer(
              const InvestorTradingSection(),
            ),
            
            const SizedBox(height: 8),
            
            // News disclosure
            _buildSectionContainer(
              const NewsDisclosureSection(),
            ),
            
            const SizedBox(height: 8),
            
            // Financial info
            _buildSectionContainer(
              const FinancialInfoSection(),
            ),
            
            const SizedBox(height: 20),
          ],
        ),
      ),
    );
  }
  
  Widget _buildSectionContainer(Widget child, {EdgeInsets? padding}) {
    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(0),
      ),
      padding: padding ?? const EdgeInsets.all(20.0),
      child: child,
    );
  }
} 