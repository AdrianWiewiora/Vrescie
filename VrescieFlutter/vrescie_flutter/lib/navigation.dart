import 'package:flutter/material.dart';
import 'views/home_view.dart';
import 'views/details_view.dart';

class Navigation {
  static Route<dynamic> generateRoute(RouteSettings settings) {
    switch (settings.name) {
      case '/':
        return MaterialPageRoute(builder: (_) => HomeView());
      case '/details':
        return MaterialPageRoute(builder: (_) => DetailsView());
    // Możesz dodać więcej przypadków tutaj
      default:
        return MaterialPageRoute(
          builder: (_) => Scaffold(
            body: Center(
              child: Text('No route defined for ${settings.name}'),
            ),
          ),
        );
    }
  }
}
