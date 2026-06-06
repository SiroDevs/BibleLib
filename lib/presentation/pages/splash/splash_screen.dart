import 'package:animated_text_kit/animated_text_kit.dart';
import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:styled_widget/styled_widget.dart';

import '../../../core/constants/app_constants.dart';
import '../../../core/theme/app_colors.dart';
import '../reader/reader_screen.dart';
import '../selection/selection_screen.dart';

class SplashScreen extends StatefulWidget {
  const SplashScreen({super.key});

  @override
  SplashScreenState createState() => SplashScreenState();
}

class SplashScreenState extends State<SplashScreen> {
  @override
  void initState() {
    super.initState();
    _goToNextScreen();
  }

  Future<void> _goToNextScreen() async {
    await Future<void>.delayed(const Duration(seconds: 3));
    if (!mounted) return;
    final prefs = await SharedPreferences.getInstance();
    final isFirst = prefs.getBool(kIsFirstLaunchKey) ?? true;
    if (!mounted) return;
    Navigator.pushAndRemoveUntil(
      context,
      MaterialPageRoute(
        builder: (_) =>
            isFirst ? const SelectionScreen() : const ReaderScreen(),
      ),
      (route) => false,
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            const Spacer(),
            const SizedBox(height: 10),
            AnimatedTextKit(
              animatedTexts: [
                TyperAnimatedText(
                  kAppName,
                  textStyle:
                      Theme.of(context).textTheme.displayLarge?.copyWith(
                            fontSize: 36,
                            fontWeight: FontWeight.bold,
                            color: AppColors.primary,
                          ),
                  speed: const Duration(milliseconds: 110),
                ),
              ],
              totalRepeatCount: 1,
              pause: Duration.zero,
              displayFullTextOnTap: true,
              stopPauseOnTap: true,
            ),
            const Spacer(),
            const Divider().padding(horizontal: 30),
            const Text(kAppCredits, style: TextStyle(fontSize: 14)),
            const SizedBox(height: 50),
          ],
        ),
      ),
    );
  }
}
