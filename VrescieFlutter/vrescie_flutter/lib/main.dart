import 'package:flutter/material.dart';
import 'navigation.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Vrescie',
      theme: ThemeData(
        primarySwatch: Colors.blue,
        visualDensity: VisualDensity.adaptivePlatformDensity,
      ),
      // Ustawienie początkowego widoku i nawigacji
      initialRoute: '/',
      onGenerateRoute: Navigation.generateRoute,
    );
  }
}
