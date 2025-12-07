import 'package:flutter/material.dart';

class NewsDisclosureSection extends StatelessWidget {
  const NewsDisclosureSection({super.key});

  @override
  Widget build(BuildContext context) {
    // TODO: Fetch actual news and disclosure data
    return Column(
      children: [
        ExpansionTile(
          title: Row(
            children: [
              Icon(Icons.newspaper, color: Colors.grey[700]),
              const SizedBox(width: 8),
              Text(
                '뉴스',
                style: Theme.of(context).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w600),
              ),
            ],
          ),
          children: const <Widget>[
            // Replace with a list of actual news items
            ListTile(title: Text('뉴스 항목 1')),
            ListTile(title: Text('뉴스 항목 2')),
            ListTile(title: Text('더 많은 뉴스 보기...')),
          ],
        ),
        const Divider(height: 1, indent: 16, endIndent: 16),
        ExpansionTile(
          title: Row(
             children: [
              Icon(Icons.feed, color: Colors.grey[700]),
              const SizedBox(width: 8),
              Text(
                '공시',
                style: Theme.of(context).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w600),
              ),
            ],
          ),
          children: const <Widget>[
             // Replace with a list of actual disclosure items
            ListTile(title: Text('공시 항목 1')),
            ListTile(title: Text('공시 항목 2')),
             ListTile(title: Text('더 많은 공시 보기...')),
          ],
        ),
      ],
    );
  }
} 