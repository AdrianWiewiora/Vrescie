import 'package:flutter/material.dart';

class DetailsView extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text("Details View"),
      ),
      body: Center(
        child: ElevatedButton(
          onPressed: () {
            // Wracamy do widoku głównego
            Navigator.pop(context);
          },
          child: Text("Back to Home"),
        ),
      ),
    );
  }
}
