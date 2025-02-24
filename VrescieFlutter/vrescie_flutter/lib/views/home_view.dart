import 'package:flutter/material.dart';

class HomeView extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text("Home View"),
      ),
      body: Center(
        child: ElevatedButton(
          onPressed: () {
            // Przechodzimy do widoku szczegółów
            Navigator.pushNamed(context, '/details');
          },
          child: Text("Go to Details"),
        ),
      ),
    );
  }
}
