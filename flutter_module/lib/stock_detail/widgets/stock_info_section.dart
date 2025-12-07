import 'package:flutter/material.dart';

class StockInfoSection extends StatelessWidget {
  final String name;
  final String currentPrice;
  final String changePercent;
  final String market;
  final List<String> themes;

  const StockInfoSection({
    super.key,
    required this.name,
    required this.currentPrice,
    required this.changePercent,
    required this.market,
    required this.themes,
  });

  @override
  Widget build(BuildContext context) {
    // Determine color based on price change
    final bool isNegative = changePercent.startsWith('-');
    final Color priceColor = isNegative ? const Color(0xFF1E88E5) : const Color(0xFFE53935);

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  name,
                  style: const TextStyle(
                    fontSize: 22, 
                    fontWeight: FontWeight.w700,
                    letterSpacing: -0.5,
                  ),
                ),
                const SizedBox(height: 4),
                Row(
                  children: [
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                      decoration: BoxDecoration(
                        color: Colors.grey[100],
                        borderRadius: BorderRadius.circular(4),
                      ),
                      child: Text(
                        market,
                        style: TextStyle(
                          fontSize: 13,
                          color: Colors.grey[800],
                          fontWeight: FontWeight.w500,
                        ),
                      ),
                    ),
                    const SizedBox(width: 8),
                    // Display themes/tags (like 홍준표)
                    ...themes.map((theme) => Container(
                      margin: const EdgeInsets.only(right: 8.0),
                      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                      decoration: BoxDecoration(
                        color: Colors.blue[50],
                        borderRadius: BorderRadius.circular(4),
                      ),
                      child: Text(
                        theme,
                        style: TextStyle(
                          fontSize: 13,
                          color: Colors.blue[800],
                          fontWeight: FontWeight.w500,
                        ),
                      ),
                    )),
                  ],
                ),
              ],
            ),
            Row(
              children: [
                // Add a favorite button
                IconButton(
                  icon: Icon(
                    Icons.notifications_none_outlined,
                    color: Colors.grey[700],
                  ),
                  onPressed: () {
                    // Set price alert functionality
                  },
                ),
              ],
            ),
          ],
        ),
        const SizedBox(height: 16),
        Row(
          crossAxisAlignment: CrossAxisAlignment.end,
          children: [
            Text(
              currentPrice,
              style: const TextStyle(
                fontSize: 28,
                fontWeight: FontWeight.w700,
                height: 1.1,
              ),
            ),
            const SizedBox(width: 8),
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
              margin: const EdgeInsets.only(bottom: 4),
              decoration: BoxDecoration(
                color: isNegative ? Colors.blue[50] : Colors.red[50],
                borderRadius: BorderRadius.circular(4),
              ),
              child: Text(
                changePercent,
                style: TextStyle(
                  fontSize: 16,
                  fontWeight: FontWeight.w600,
                  color: priceColor,
                ),
              ),
            ),
          ],
        ),
        const SizedBox(height: 24),
        // Additional Info with more modern layout
        Container(
          padding: const EdgeInsets.all(16),
          decoration: BoxDecoration(
            color: Colors.grey[50],
            borderRadius: BorderRadius.circular(8),
          ),
          child: Column(
            children: [
              _buildInfoRow('업종', '1차 철강 제조업'),
              const Divider(height: 16, thickness: 0.5),
              _buildInfoRow('주요제품', '철강선재류'),
              const Divider(height: 16, thickness: 0.5),
              _buildInfoRow('투자자 수', '1,477명'),
              const Divider(height: 16, thickness: 0.5),
              _buildInfoRow(
                '평균 수익률', 
                '-39.00%', 
                valueColor: Colors.blue,
                trailing: const Icon(
                  Icons.arrow_forward_ios,
                  size: 14,
                  color: Colors.grey,
                ),
              ),
            ],
          ),
        ),
      ],
    );
  }

  Widget _buildInfoRow(String label, String value, {Color? valueColor, Widget? trailing}) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        children: [
          Text(
            label,
            style: TextStyle(
              fontSize: 14,
              color: Colors.grey[700],
              fontWeight: FontWeight.w500,
            ),
          ),
          const Spacer(),
          if (valueColor != null)
            Text(
              value,
              style: TextStyle(
                fontSize: 14,
                fontWeight: FontWeight.w600,
                color: valueColor,
              ),
            )
          else
            Text(
              value,
              style: const TextStyle(
                fontSize: 14,
                fontWeight: FontWeight.w600,
              ),
            ),
          if (trailing != null) ...[
            const SizedBox(width: 4),
            trailing,
          ],
        ],
      ),
    );
  }
} 