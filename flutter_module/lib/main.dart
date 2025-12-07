import 'package:flutter/material.dart';
import 'stock_detail/stock_detail_page.dart'; // StockDetailPage 및 Stock 클래스 import

void main() => runApp(const MyApp());

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Stock Detail', // 앱 타이틀 변경
      theme: ThemeData(
        primarySwatch: Colors.blue,
        // 여기에 앱 전체 테마를 정의할 수 있습니다.
      ),
      // home: PlaceholderWidget(), // 초기 화면은 네이티브에서 라우트로 제어하므로 home 제거 또는 플레이스홀더 사용
      onGenerateRoute: (settings) {
        // 네이티브에서 호출할 라우트 처리
        if (settings.name != null && settings.name!.startsWith('/stockDetail')) {
          final Uri uri = Uri.parse(settings.name!);
          final String? name = uri.queryParameters['name'];
          final String? code = uri.queryParameters['code'];

          // 필수 파라미터 확인
          if (name != null && code != null) {
            final stock = Stock(name: name, code: code);
            return MaterialPageRoute(
              builder: (context) => StockDetailPage(stock: stock),
              settings: settings, // 원본 라우트 설정을 전달할 수 있음
            );
          } else {
            // 파라미터가 누락된 경우 에러 페이지 또는 기본 페이지 표시
            return MaterialPageRoute(
              builder: (context) => const Scaffold(
                body: Center(child: Text('Error: Missing stock details')),
              )
            );
          }
        }

        // '/stockDetail' 외 다른 라우트 또는 초기 라우트 처리 (옵션)
        // 예를 들어, Flutter 모듈의 기본 화면을 보여줄 수 있음
        return MaterialPageRoute(
          builder: (context) => const Scaffold(
            body: Center(child: Text('Flutter Module Root (No Route Specified)')),
          )
        );
      },
    );
  }
}

// // 기존 MyHomePage 및 _MyHomePageState 코드는 제거합니다.
// class MyHomePage extends StatefulWidget { ... }
// class _MyHomePageState extends State<MyHomePage> { ... }
