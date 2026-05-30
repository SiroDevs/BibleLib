import 'package:biblelib/core/constants/app_constants.dart';
import 'package:biblelib/core/di/service_locator.dart';
import 'package:biblelib/core/theme/app_theme.dart';
import 'package:biblelib/features/settings/presentation/bloc/settings_bloc.dart';
import 'package:biblelib/features/settings/presentation/bloc/settings_event.dart';
import 'package:biblelib/features/settings/presentation/bloc/settings_state.dart';
import 'package:biblelib/features/splash/presentation/pages/splash_screen.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

class BibleLibApp extends StatelessWidget {
  const BibleLibApp({super.key});

  @override
  Widget build(BuildContext context) {
    return BlocProvider(
      create: (_) => sl<SettingsBloc>()..add(const LoadSettingsEvent()),
      child: BlocBuilder<SettingsBloc, SettingsState>(
        builder: (context, settings) {
          return MaterialApp(
            title: kAppName,
            debugShowCheckedModeBanner: false,
            theme: AppTheme.lightTheme,
            darkTheme: AppTheme.darkTheme,
            themeMode: settings.themeMode,
            home: const SplashScreen(),
          );
        },
      ),
    );
  }
}
